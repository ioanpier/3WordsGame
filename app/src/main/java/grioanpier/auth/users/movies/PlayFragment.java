package grioanpier.auth.users.movies;

//**

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

    private static final int STORY_LOADER = 0;

    public PlayFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);

        listView = (ListView) rootView.findViewById(R.id.story_listview);
        //listItems = ApplicationHelper.getInstance().story;
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,
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
            Log.v(LOG_TAG, "I am the host and it's my turn!");
            editText.setEnabled(true);
            editText.setFocusableInTouchMode(true);
        } else if (ApplicationHelper.myTurn) {
            Log.v(LOG_TAG, "I am a player and it's my turn!");
            editText.setEnabled(true);
            editText.setFocusableInTouchMode(true);
        } else {
            Log.v(LOG_TAG, "Nope, not my turn!");
            //editText is disabled by default inside the xml
            editText.setFocusableInTouchMode(false);
        }





        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        deviceType = ApplicationHelper.getInstance().DEVICE_TYPE;
        mHandler = new StoryHandler(getActivity());
        ApplicationHelper.getInstance().setStoryHandler(mHandler);

    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationHelper.getInstance().unregisterStoryHandler();
        //new StoryInitialAsyncTask(getActivity()).execute(builder.toString(), ApplicationHelper.STORY_HEAD);*/
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
    public static void play() {
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
     * This method is called when the game starts. It is needed in the case of two pane layouts, because the story arraylist in applicationhelper
     * is recreated and the adapter needs to be set again.
     */
    public void gameHasStarted(){
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,
                ApplicationHelper.getInstance().story);

        listView.setAdapter(adapter);
    }

    //Queries the database for the story and adds it to the arrayadapter.
    //@Override
    //public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    //    Log.v(LOG_TAG, "onCreateLoader");
    //    getActivity();
    //    String storyHead = ApplicationHelper.STORY_HEAD;
    //    Log.v(LOG_TAG, "validate storyHead " + storyHead);
    //    Log.v(LOG_TAG, "play fragment create loader");
    //    //We want the story with COLUMN_HEAD==ApplicationHelper.STORY_HEAD
    //    return new CursorLoader(getActivity(),
    //            StoriesContract.StoriesEntry.CONTENT_URI,
    //            new String[]{StoriesContract.StoriesEntry.COLUMN_STORY + " = ?"},
    //            StoriesContract.StoriesEntry.COLUMN_HEAD,
    //            new String[]{storyHead},
    //            null);
    //}
//
    //@Override
    //public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    //    Log.v(LOG_TAG, "onLoadFinished");
    //    if (!cursor.moveToFirst()){
    //        Log.v(LOG_TAG, "Load finished but escaped!");
    //        return;
    //    }
//
    //    Log.v(LOG_TAG, "play fragment Load finished");
//
    //    int columnIndex = cursor.getColumnIndex(StoriesContract.StoriesEntry.COLUMN_STORY);
    //    String story = cursor.getString(columnIndex);
    //    Log.v(LOG_TAG, "story");
    //    for (String s : every3words(story))
    //        Log.v(LOG_TAG, s);
//
    //    adapter.clear();
    //    adapter = new ArrayAdapter<>(getActivity(),
    //            android.R.layout.simple_list_item_1,
    //            every3words(story));
//
    //    listView.setAdapter(adapter);
    //}
//
    //@Override
    //public void onLoaderReset(Loader<Cursor> loader) {
    //}

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
            switch (msg.what) {
                case ApplicationHelper.STORY:
                    //ApplicationHelper.getInstance().story.add((String) msg.obj);

                    adapter.add((String) msg.obj);
                    System.out.println("in app: " + ApplicationHelper.getInstance().story);
                    System.out.println("in listitems: " + listItems);


                    Log.v(LOG_TAG, "Story received Listener");
                    Log.v(LOG_TAG, "deviceType: " + deviceType);
                    Log.v(LOG_TAG, "myTurn: " + Boolean.toString(ApplicationHelper.myTurn));
                    if (deviceType == Constants.DEVICE_HOST && !ApplicationHelper.myTurn){
                        Log.v(LOG_TAG, "I am the host and it's not my Turn, better notify the next player");
                        ApplicationHelper.getInstance().notifyNextPlayer();
                    }

                    break;
                case ApplicationHelper.STORY_CODE:
                    String message = (String) msg.obj;
                    Log.v(LOG_TAG, "ACTIVITY_CODE case: " + message);
                    //These messages always contain a single Integer code.
                    int swithz = ((String) msg.obj).charAt(0) - 48;
                    Log.v(LOG_TAG, "handler switch " + swithz);
                    switch (swithz) {
                        case ApplicationHelper.YOUR_TURN:
                            Log.v(LOG_TAG, "STORY TURN");
                            if (deviceType != Constants.DEVICE_SPECTATOR) {
                                //Inform the playFragment to allow story input.
                                play();
                                ApplicationHelper.myTurn = true;
                                Toast.makeText(mContext, "Your turn!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.v(LOG_TAG, "Device is Spectator");
                                ApplicationHelper.getInstance().write(String.valueOf(ApplicationHelper.PASS), ApplicationHelper.STORY_CODE);
                            }

                            break;
                        case ApplicationHelper.PASS:
                            if (deviceType == Constants.DEVICE_HOST) {
                                Log.v(LOG_TAG, "Someone passed his turn (he was a spectator). Notify the next player.");
                                ApplicationHelper.getInstance().notifyNextPlayer();
                            }

                            break;
                        default:
                            Log.v(LOG_TAG, "other");
                            break;

                    }

                    break;

            }//outer switch
        }
    }// handler class

    public interface StoryReceivedListener {
        void onStoryReceived();
    }

    private static StoryReceivedListener storyReceivedListener;

    public void setStoryReceivedListener(StoryReceivedListener listener) {
        storyReceivedListener = listener;
    }

    public void unregisterStoryReceivedListener() {
        storyReceivedListener = null;
    }


}