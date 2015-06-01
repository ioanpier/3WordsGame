package grioanpier.auth.users.movies.data;

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
                StoriesContract.StoriesEntry.COLUMN_HEAD + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE," +
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
