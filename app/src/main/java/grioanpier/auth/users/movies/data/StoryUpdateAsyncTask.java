package grioanpier.auth.users.movies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Updates the current story with the next 3 words.
 */
public class StoryUpdateAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String LOG_TAG = StoryInitialAsyncTask.class.getSimpleName();

    private static Context mContext;
    public StoryUpdateAsyncTask(Context context){
        mContext = context;
    }

    private String[] projection = {StoriesContract.StoriesEntry.COLUMN_STORY};
    private String selection = StoriesContract.StoriesEntry.COLUMN_HEAD;
    private String[] selectionArgs = new String[1];

    @Override
    protected Void doInBackground(String... params) {
        Log.v(LOG_TAG, "doInBackground");
        String next3words = params[0];
        //This should be the ApplicationHelper.STORY_HEAD
        selectionArgs[0] = params[1];
        //Query the database for the story so far.
        Cursor cursor = mContext.getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                projection, //the column I want to get
                selection,
                selectionArgs,
                null
        );

        ContentValues values = new ContentValues();
        if (cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(StoriesContract.StoriesEntry.COLUMN_STORY);
            String story = cursor.getString(columnIndex);
            values.put(StoriesContract.StoriesEntry.COLUMN_STORY, story + " " + next3words);
        }else{
            throw new IllegalStateException("The cursor should have returned a row!");
        }








        //Add the next3words and update the database.
        int rowsAffected = mContext.getContentResolver().update(
                StoriesContract.StoriesEntry.CONTENT_URI,
                values,
                StoriesContract.StoriesEntry.COLUMN_HEAD + " LIKE ?",
                selectionArgs
        );

        if (rowsAffected==0)
            throw new IllegalStateException("The database should have been updated!");

        cursor.close();
        return null;

    }
}
