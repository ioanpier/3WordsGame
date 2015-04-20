package grioanpier.auth.users.movies.data;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Makes the first insert for the story. The following ones are carried by the {@link grioanpier.auth.users.movies.data.StoryUpdateAsyncTask}
 */
public class StoryInitialAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String LOG_TAG = StoryInitialAsyncTask.class.getSimpleName();
    private static Context mContext;
    public StoryInitialAsyncTask(Context context){
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.v(LOG_TAG, "doInBackground");
        String first3words = params[0];
        String storyHead = params[1];
        ContentValues values = new ContentValues();
        values.put(StoriesContract.StoriesEntry.COLUMN_STORY, first3words);
        values.put(StoriesContract.StoriesEntry.COLUMN_HEAD, storyHead);

        mContext.getContentResolver().insert(
                StoriesContract.StoriesEntry.CONTENT_URI,
                values
        );
        return null;
    }


}
