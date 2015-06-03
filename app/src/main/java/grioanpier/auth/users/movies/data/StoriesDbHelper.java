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
