package grioanpier.auth.users.movies.data;
/*
Copyright (c) <2015> Ioannis Pierros (ioanpier@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Looper;

import grioanpier.auth.users.movies.utility.ApplicationHelper;

/**
 * Saves the story from the ApplicationHelper to the Story table.
 * If a story with the same HEAD already exists, it updates it.
 */
public class SaveStoryAsyncTask extends AsyncTask<Void, Void, Void> {

    final Context mContext;
    public SaveStoryAsyncTask(Context context){
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Looper.prepare();
        ContentValues values = new ContentValues();
        values.put(StoriesContract.StoriesEntry.COLUMN_STORY, ApplicationHelper.getInstance().getStory());
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

        if (cursor.moveToFirst()){
            mContext.getContentResolver().update(
                    StoriesContract.StoriesEntry.CONTENT_URI,
                    values,
                    StoriesContract.StoriesEntry.COLUMN_HEAD + " = ?",
                    selectionArgs
            );
        }else{
            mContext.getContentResolver().insert(
                    StoriesContract.StoriesEntry.CONTENT_URI,
                    values
            );
        }
        cursor.close();

        return null;
    }


}
