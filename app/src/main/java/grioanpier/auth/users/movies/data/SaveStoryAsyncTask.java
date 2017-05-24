package grioanpier.auth.users.movies.data;
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
