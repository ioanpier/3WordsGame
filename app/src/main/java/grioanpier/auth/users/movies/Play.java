package grioanpier.auth.users.movies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import grioanpier.auth.users.movies.data.StoriesContract;
import grioanpier.auth.users.movies.utility.ApplicationHelper;


public class Play extends ActionBarActivity {

    private static final String LOG_TAG = Play.class.getSimpleName();
    private Button debug;
    private Button toTheChat_button;

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

        debug = (Button) findViewById(R.id.debug_button);

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

                //new AlertDialog.Builder(getApplicationContext())
                //        .setMessage("Steal a turn?")
                //        .setPositiveButton("Steal!", new DialogInterface.OnClickListener() {
                //            public void onClick(DialogInterface dialog, int id) {
                //                editText.setFocusableInTouchMode(true);
                //                editText.setEnabled(true);
                //            }
                //        }).show();


            }
        });

        toTheChat_button = (Button) findViewById(R.id.goToChat);

        toTheChat_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WaitingScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ApplicationHelper.getInstance().setActivityHandler(mHandler);
    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationHelper.getInstance().unregisterActivityHandler();
    }

    //Used for saving the story
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
                default:
                    Log.v(LOG_TAG, "switch: " + msg.what);
            }
        }
    }


}
