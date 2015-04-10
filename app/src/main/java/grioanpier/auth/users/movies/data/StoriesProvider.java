package grioanpier.auth.users.movies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Ioannis
 */
public class StoriesProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private StoriesDbHelper mOpenHelper;

    static final int STORIES = 100;
    static final int STORIES_SPECIFIC = 101;

    private static final SQLiteQueryBuilder sStoryQueryBuilder;
    static {
        sStoryQueryBuilder = new SQLiteQueryBuilder();
    }

    //stories.story = ?
    private static final String sStorySelection =
            StoriesContract.StoriesEntry.TABLE_NAME +
                    "." + StoriesContract.StoriesEntry.COLUMN_STORY + " = ? ";

    private Cursor getStorySpecific(Uri uri, String[] projection, String sortOrder) {
        String storySpecific = StoriesContract.StoriesEntry.getStorySpecificFromUri(uri);
        String[] selectionArgs = new String[]{storySpecific};
        String selection = sStorySelection;

        return sStoryQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StoriesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, StoriesContract.PATH_STORIES, STORIES);
        matcher.addURI(authority, StoriesContract.PATH_STORIES + "/*", STORIES_SPECIFIC);

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
            case STORIES_SPECIFIC:
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
            }
            break;

            case STORIES_SPECIFIC:
            {
                return getStorySpecific(uri, projection, sortOrder);
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
                    throw new android.database.SQLException("Failed to insert row into " + uri);
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
    public int bulkInsert (Uri uri, ContentValues[] values){
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
