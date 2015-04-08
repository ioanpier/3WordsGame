package grioanpier.auth.users.movies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Ioannis on 3/4/2015.
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

    private static final String sStorySelection =
            StoriesContract.StoriesEntry.TABLE_NAME +
                    "." + StoriesContract.StoriesEntry.COLUMN_STORY + " = ? ";

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

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
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
                return getWeatherByLocationSetting(uri, projection, sortOrder);
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
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
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
