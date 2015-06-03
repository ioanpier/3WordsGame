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
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by Ioannis
 */
public class StoriesProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private StoriesDbHelper mOpenHelper;

    static final int STORIES = 100;
    static final int STORY_SPECIFIC = 101;

    private static final SQLiteQueryBuilder sStoryQueryBuilder;

    static{
        sStoryQueryBuilder = new SQLiteQueryBuilder();
        sStoryQueryBuilder.setTables(StoriesContract.StoriesEntry.TABLE_NAME);
    }

    /*
        From Udacity's Developing Android Apps course. If I had a join, I would declare it here like so

        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);

        //weather INNER JOIN location ON weather.location_id = location._id
        This means to create an inner join between the weather and location tables
        The weather.location_id (a column in the weather table)
        Should point to the location._id (the _id column in the location table)


     */

    //stories.head = ?   This asks for a specific story
    private static final String sStorySelection =
            StoriesContract.StoriesEntry.TABLE_NAME +
                    "." + StoriesContract.StoriesEntry.COLUMN_HEAD + " = ? ";

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StoriesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, StoriesContract.PATH_STORIES, STORIES);
        matcher.addURI(authority, StoriesContract.PATH_STORIES + "/*", STORY_SPECIFIC);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new StoriesDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch(match){
            case STORIES:
                return StoriesContract.StoriesEntry.CONTENT_TYPE;
            case STORY_SPECIFIC:
                return StoriesContract.StoriesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            case STORIES:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StoriesContract.StoriesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case STORY_SPECIFIC:
            {
                String storySpecific = StoriesContract.StoriesEntry.getStorySpecificFromUri(uri);
                retCursor = sStoryQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sStorySelection,
                        new String[]{storySpecific},
                        null,
                        null,
                        null
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch(match){
            case STORIES: {
                long id = db.insert(StoriesContract.StoriesEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = StoriesContract.StoriesEntry.buildStoriesUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri + " with returned id=" + id);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        //This makes delete all rows return the number of rows deleted
        if (selection==null) selection="1";

        switch (match){
            case STORIES: {
                rowsDeleted = db.delete(StoriesContract.StoriesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted !=0 ){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        //This makes delete all rows return the number of rows deleted
        if (selection==null) selection="1";

        switch (match){
            case STORIES: {
                rowsUpdated = db.update(StoriesContract.StoriesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated !=0 ){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert (Uri uri, @NonNull ContentValues[]  values){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STORIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(StoriesContract.StoriesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
