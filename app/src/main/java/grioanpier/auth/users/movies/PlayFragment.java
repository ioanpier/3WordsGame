package grioanpier.auth.users.movies;

//**

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import grioanpier.auth.users.movies.data.StoriesContract;
import grioanpier.auth.users.movies.data.StoryInitialAsyncTask;
import grioanpier.auth.users.movies.data.StoryUpdateAsyncTask;
import grioanpier.auth.users.movies.utility.ApplicationHelper;

public class PlayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = PlayFragment.class.getSimpleName();
    private ArrayList<String> listItems = new ArrayList<>();
    private static ArrayAdapter<String> adapter;
    private EditText editText;
    private ListView listView;
    private Button debug;

    private static final int STORY_LOADER = 0;

    public PlayFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);

        listView = (ListView) rootView.findViewById(R.id.story_listview);

        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                listItems);

        Log.v(LOG_TAG, "initializing story loader");
        //getLoaderManager().initLoader(STORY_LOADER, null, this);

        editText = (EditText) rootView.findViewById(R.id.main_edittext);

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


        debug = (Button) rootView.findViewById(R.id.debug_button);

        debug.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("debug button on click!");
                ApplicationHelper app = ApplicationHelper.getInstance();
                Log.v("DEBUG BUTTON", Boolean.toString(app.isHost));
                Log.v("DEBUG BUTTON", Boolean.toString(app.myTurn));
                Log.v("DEBUG BUTTON", Boolean.toString(app.GAME_HAS_STARTED));
                Log.v("DEBUG BUTTON", Integer.toString(app.getWhoIsPlaying()));
                //Log.v("DEBUG BUTTON", );

                new AlertDialog.Builder(getActivity())
                        .setMessage("Enable Edit Text?")
                        .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                editText.setFocusableInTouchMode(true);
                                editText.setEnabled(true);
                            }
                        }).show();


            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ApplicationHelper.getInstance().setStoryHandler(mHandler);
    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationHelper.getInstance().unregisterStoryHandler();
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
    public void play() {
        editText.setEnabled(true);
        editText.setFocusableInTouchMode(true);
        Toast.makeText(getActivity(), "Your turn!", Toast.LENGTH_SHORT).show();
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

            if (ApplicationHelper.getInstance().isHost && ApplicationHelper.firstTurn){
                Log.v(LOG_TAG, "I am the host and this is the first turn.");
                Log.v(LOG_TAG, "write message with STORY_INITIAL");

                ApplicationHelper.getInstance().write(message, ApplicationHelper.STORY_INITIAL);
            }

            else {
                Log.v(LOG_TAG, "I am not host or this isn't my first turn");
                Log.v(LOG_TAG, "either way, write message with STORY");
                ApplicationHelper.getInstance().write(message, ApplicationHelper.STORY);
            }
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
     * The Handler that gets the messages. The messages are first handled in {@link ApplicationHelper}
     */
    private StoryHandler mHandler = new StoryHandler(getActivity());


    //Queries the database for the story and adds it to the arrayadapter.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader");
        String storyHead = ApplicationHelper.STORY_HEAD;

        //We want the story with COLUMN_HEAD==ApplicationHelper.STORY_HEAD
        return new CursorLoader(getActivity(),
                StoriesContract.StoriesEntry.CONTENT_URI,
                new String[]{StoriesContract.StoriesEntry.COLUMN_STORY},
                StoriesContract.StoriesEntry.COLUMN_HEAD,
                new String[]{storyHead},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v(LOG_TAG, "onLoadFinished");
        if (!cursor.moveToFirst())
            return;

        int columnIndex = cursor.getColumnIndex(StoriesContract.StoriesEntry.COLUMN_STORY);
        String story = cursor.getString(columnIndex);
        Log.v(LOG_TAG, "story");
        for (String s : every3words(story))
            Log.v(LOG_TAG, s);


        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                every3words(story));

        listView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
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
                    Log.v(LOG_TAG, "STORY CASE");
                    //Received a new part of the Story
                    adapter.add((String) msg.obj);

                    new StoryUpdateAsyncTask(mContext).execute((String) msg.obj, ApplicationHelper.STORY_HEAD);

                    if (storyReceivedListener != null) {
                        storyReceivedListener.onStoryReceived();
                    }
                    break;
                case ApplicationHelper.STORY_INITIAL:
                    Log.v(LOG_TAG, "STORY INITIAL CASE");
                    new StoryInitialAsyncTask(mContext).execute((String) msg.obj, ApplicationHelper.STORY_HEAD);
                    ApplicationHelper.firstTurn = false;

                    //If the user is the host, he has already added that to the database
                    break;

            }
        }
    }

    public interface StoryReceivedListener {
        public void onStoryReceived();
    }

    private static StoryReceivedListener storyReceivedListener;

    public void setStoryReceivedListener(StoryReceivedListener listener) {
        storyReceivedListener = listener;
    }

    public void unregisterStoryReceivedListener() {
        storyReceivedListener = null;
    }


}