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
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ioannis
 */
public class StoriesDbHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "stories.db";

    public StoriesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        final String SQL_CREATE_STORIES_TABLE = "CREATE TABLE " +
                StoriesContract.StoriesEntry.TABLE_NAME + " (" +
                StoriesContract.StoriesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StoriesContract.StoriesEntry.COLUMN_STORY + " TEXT NOT NULL ON CONFLICT REPLACE," +
                StoriesContract.StoriesEntry.COLUMN_HEAD + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE" +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_STORIES_TABLE);
    }

    @Override
    public void onUpgrade ( SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        //every time the database changes, this method will be filled in to cover for that.
        //no changes have been made yet, so this method isn't needed.
        //it throws an error as a reminder in case the database is changed and the onUpgrade isn't.
        throw new SQLException("onUpgrade for the database hasn't been implemented yet");
    }
}
