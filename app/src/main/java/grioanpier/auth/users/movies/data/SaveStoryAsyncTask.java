package grioanpier.auth.users.movies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;

import grioanpier.auth.users.movies.utility.ApplicationHelper;

/**
 * Created by Ioannis on 2/6/2015.
 */
public class SaveStoryAsyncTask extends AsyncTask<Void, Void, Void> {

    Context mContext;
    public SaveStoryAsyncTask(Context context){
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Looper.prepare();
        ContentValues values = new ContentValues();
        values.put(StoriesContract.StoriesEntry.COLUMN_STORY, getStory());
        values.put(StoriesContract.StoriesEntry.COLUMN_HEAD, ApplicationHelper.STORY_HEAD);

        String[] projection = {StoriesContract.StoriesEntry.COLUMN_STORY};
        String selection = StoriesContract.StoriesEntry.COLUMN_HEAD + " = ?";
        String[] selectionArgs = {ApplicationHelper.STORY_HEAD };

        Cursor cursor = mContext.getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        int rowsAffected;
        if (cursor.moveToFirst()){
            System.out.println("moved to first!");

            rowsAffected = mContext.getContentResolver().update(
                    StoriesContract.StoriesEntry.CONTENT_URI,
                    values,
                    StoriesContract.StoriesEntry.COLUMN_HEAD + " = ?",
                    selectionArgs
            );

            System.out.println("rows affected: " + rowsAffected);
        }else{
            System.out.println("gonna insert");
            Uri uri = mContext.getContentResolver().insert(
                    StoriesContract.StoriesEntry.CONTENT_URI,
                    values
            );

            System.out.println("uri was " + uri.toString());

        }
        cursor.close();

        return null;
    }


    //Used for saving the story
    public String getStory(){
        StringBuilder builder = new StringBuilder();
        for (String line : ApplicationHelper.getInstance().story){
            builder.append(line).append(" ");
        }
        return builder.toString();
    }


}
