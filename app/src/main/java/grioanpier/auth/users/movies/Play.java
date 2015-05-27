package grioanpier.auth.users.movies;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import grioanpier.auth.users.movies.data.StoriesContract;
import grioanpier.auth.users.movies.utility.ApplicationHelper;
import grioanpier.auth.users.movies.utility.Constants;


public class Play extends ActionBarActivity {

    private static final String LOG_TAG = Play.class.getSimpleName();
    private static int deviceType;
    private static PlayFragment playFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.v(LOG_TAG, "option item selected");

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id==R.id.save_story){
            Log.v(LOG_TAG, "save story menu item");
            ContentValues values = new ContentValues();
            values.put(StoriesContract.StoriesEntry.COLUMN_STORY, getStory());
            values.put(StoriesContract.StoriesEntry.COLUMN_HEAD, ApplicationHelper.STORY_HEAD);

            getContentResolver().insert(
                    StoriesContract.StoriesEntry.CONTENT_URI,
                    values
            );

            Toast.makeText(this, "Story saved!", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        deviceType = ApplicationHelper.getInstance().DEVICE_TYPE;
        switch (deviceType){
            case Constants.DEVICE_SPECTATOR:
                Log.v(LOG_TAG, "DEVICE_SPECTATOR");
                break;
            case Constants.DEVICE_PLAYER:
                Log.v(LOG_TAG, "DEVICE_PLAYER");
                break;
            case Constants.DEVICE_HOST:
                Log.v(LOG_TAG, "DEVICE_HOST");
                break;
        }
        playFragment = (PlayFragment) getSupportFragmentManager().findFragmentById(R.id.play_fragment);

    }

    @Override
    public void onStart() {
        super.onStart();
        ApplicationHelper.getInstance().setActivityHandler(mHandler);
        playFragment.setStoryReceivedListener(storyReceivedListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationHelper.getInstance().unregisterActivityHandler();
        playFragment.unregisterStoryReceivedListener();
    }


    PlayFragment.StoryReceivedListener storyReceivedListener = new PlayFragment.StoryReceivedListener() {
        @Override
        public void onStoryReceived() {
            Log.v(LOG_TAG, "Story received Listener");
            Log.v(LOG_TAG, "deviceType: " + deviceType);
            Log.v(LOG_TAG, "myTurn: " + Boolean.toString(ApplicationHelper.myTurn));
            if (deviceType == Constants.DEVICE_HOST && !ApplicationHelper.myTurn){
                Log.v(LOG_TAG, "I am the host and it's not my Turn, better notify the next player");
                ApplicationHelper.getInstance().notifyNextPlayer();
            }
        }
    };

    public String getStory(){
        StringBuilder builder = new StringBuilder();
        for (String line : ApplicationHelper.getInstance().story){
            builder.append(line).append(" ");
        }
        return builder.toString();
    }

    private ActivityHandler mHandler = new ActivityHandler(this);

    public static class ActivityHandler extends Handler {

        Context mContext;

        protected ActivityHandler(Context context) {
            super();
            mContext = context;
        }

        @Override
        public synchronized void handleMessage(Message msg) {
            Log.v(LOG_TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case ApplicationHelper.PLAYER_DISCONNECTED:
                    Toast.makeText(mContext, msg.obj + " disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case ApplicationHelper.ACTIVITY_CODE:
                    String message = (String) msg.obj;
                    Log.v(LOG_TAG, "ACTIVITY_CODE case: " + message);
                    //These messages always contain a single Integer code.
                    int swithz = ((String)msg.obj).charAt(0) - 48;
                    Log.v(LOG_TAG, "handler switch "+swithz);
                    switch (swithz) {
                        case ApplicationHelper.YOUR_TURN:
                            Log.v(LOG_TAG, "STORY TURN");
                            if (deviceType != Constants.DEVICE_SPECTATOR) {
                                //Inform the playFragment to allow story input.
                                playFragment.play();
                                ApplicationHelper.myTurn = true;
                            } else{
                                Log.v(LOG_TAG, "Device is Spectator");
                                ApplicationHelper.getInstance().write(String.valueOf(ApplicationHelper.PASS), ApplicationHelper.ACTIVITY_CODE);
                            }

                            break;
                        case ApplicationHelper.PASS:
                            if (deviceType == Constants.DEVICE_HOST){
                                Log.v(LOG_TAG, "Someone passed his turn (he was a spectator). Notify the next player.");
                                ApplicationHelper.getInstance().notifyNextPlayer();
                            }

                                break;
                        default:
                            Log.v(LOG_TAG, "other");
                            break;

                    }
                    break;
                default:
                    Log.v(LOG_TAG, "switch: " + msg.what);
            }
        }
    }


}
