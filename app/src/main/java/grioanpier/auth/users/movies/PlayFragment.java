package grioanpier.auth.users.movies;
/*
Copyright {2016} {Ioannis Pierros (ioanpier@gmail.com)}

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import grioanpier.auth.users.movies.utility.ApplicationHelper;
import grioanpier.auth.users.movies.utility.Constants;

public class PlayFragment extends Fragment {

    private static String LOG_TAG = PlayFragment.class.getSimpleName();
    private static ArrayList<String> listItems = new ArrayList<>();
    private final static String STORY = "story so far";
    private static ArrayAdapter<String> adapter;
    private static EditText editText;
    private ListView listView;
    private static int deviceType;

    public PlayFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);

        listView = (ListView) rootView.findViewById(R.id.story_listview);
        //listItems = ApplicationHelper.getInstance().story;
        adapter = new ArrayAdapter<>(getActivity(),
                R.layout.message_story,
                ApplicationHelper.getInstance().story);

        listView.setAdapter(adapter);
        editText = (EditText) rootView.findViewById(R.id.story_edittext);

        //Sets the soft keyboard to be hidden when the app starts.
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        hideKeyboard();

        //if IME_ACTION_SEND is clicked, adds the string to the list and updates the listView. Also clears the editText text.
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if (countWords(editText.getText().toString()) == 3) {
                        sendMessage(editText.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), "Put 3 words.", Toast.LENGTH_LONG).show();
                    }

                    handled = true;
                }
                return handled;
            }
        });


        if (ApplicationHelper.getInstance().isHost && ApplicationHelper.getInstance().getWhoIsPlaying() == -1) {
            editText.setEnabled(true);
            editText.setFocusableInTouchMode(true);
        } else if (ApplicationHelper.myTurn) {
            editText.setEnabled(true);
            editText.setFocusableInTouchMode(true);
        } else {
            editText.setFocusableInTouchMode(false);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        deviceType = ApplicationHelper.getInstance().DEVICE_TYPE;
        mHandler = new StoryHandler(getActivity());
        ApplicationHelper.addHanlder(mHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ApplicationHelper.removeHandler(mHandler);
    }

    private int countWords(String s) {
        //Remove the whitespaces at the beginning and the end
        String trim = s.trim();
        if (trim.isEmpty())
            return 0;

        //Split the string around the whitespaces.
        return trim.split("\\s+").length;
    }

    //Your turn to play! Unlock the EditText
    private static void play() {
        editText.setEnabled(true);
        editText.setFocusableInTouchMode(true);
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private synchronized void sendMessage(final String message) {
        // Check that there's actually something to send
        if (message.length() > 0) {
            editText.setText("");
            listView.setSelection(adapter.getCount() - 1);
            editText.setEnabled(false);
            editText.setFocusableInTouchMode(false);
            editText.clearFocus();
            hideKeyboard();
            ApplicationHelper.myTurn = false;
            ApplicationHelper.getInstance().write(message, ApplicationHelper.STORY);
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * This method is called when the game starts. It is needed in the case of two pane layouts, because the story arrayList in ApplicationHelper
     * is recreated and the adapter needs to be set again.
     */
    public void gameHasStarted() {
        adapter = new ArrayAdapter<>(getActivity(),
                R.layout.message_story,
                ApplicationHelper.getInstance().story);

        listView.setAdapter(adapter);
    }

    /**
     * Seperates the string every 3 words
     *
     * @param string The string to seperate
     * @return a table. Every cell has 3 words.
     */
    private String[] every3words(String string) {
        String[] newString = string.split("\\s+");

        String[] finalMessage = new String[newString.length / 3];


        for (int i = 0; i < finalMessage.length; i++) {
            finalMessage[i] = newString[i * 3] + " " + newString[i * 3 + 1] + " " + newString[i * 3 + 2];
        }
        return finalMessage;
    }

    /**
     * The Handler that gets the messages. The messages are first handled in {@link ApplicationHelper}
     */
    private StoryHandler mHandler;

    //This is extended simply because making a new Handler was giving me "a leak might occur" warning.
    public static class StoryHandler extends Handler {
        Context mContext;

        StoryHandler(Context context) {
            mContext = context;
        }


        @Override
        public void handleMessage(Message msg) {
            Log.i(LOG_TAG, "msg.obj: " + msg.obj);
            Log.i(LOG_TAG, "msg.what: "+ msg.what);
            switch (msg.what) {
                case ApplicationHelper.STORY:
                    adapter.add((String) msg.obj);
                    if (deviceType == Constants.DEVICE_HOST && !ApplicationHelper.myTurn)
                        ApplicationHelper.getInstance().notifyNextPlayer();

                    break;
                case ApplicationHelper.SINGLE_RECEIVER:
                    int code = Integer.parseInt((String)msg.obj);
                    switch (code) {
                        case ApplicationHelper.YOUR_TURN:
                            if (deviceType == Constants.DEVICE_SPECTATOR)
                                ApplicationHelper.getInstance().write(String.valueOf(ApplicationHelper.PASS), ApplicationHelper.SINGLE_RECEIVER);
                            else {
                                //Inform the playFragment to allow story input.
                                play();
                                ApplicationHelper.myTurn = true;
                                Toast.makeText(mContext, "Your turn!", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case ApplicationHelper.PASS:
                            if (deviceType == Constants.DEVICE_HOST)
                                ApplicationHelper.getInstance().notifyNextPlayer();

                            break;
                        default:
                            break;
                    }
                    Log.i(LOG_TAG, "SINGLE_RECEIVER");
                    break;


            }//outer switch
        }
    }// handler class

}