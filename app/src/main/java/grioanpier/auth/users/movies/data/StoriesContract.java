package grioanpier.auth.users.movies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ioannis
 */
public class StoriesContract {
    public static final String CONTENT_AUTHORITY = "grioanpier.auth.users.movies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_STORIES = "stories";

    public static final class StoriesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STORIES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STORIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STORIES;

        public static final String TABLE_NAME = "stories";
        public static final String COLUMN_STORY = "story";
        public static final String COLUMN_HEAD = "head";

        public static Uri buildStoriesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildStoriesSpecific (String storyHead){
            return CONTENT_URI.buildUpon().appendPath(storyHead).build();
        }

        public static String getStoryIdFromUri (Uri uri){
            return uri.getPathSegments().get(0);
        }

        public static String getStorySpecificFromUri (Uri uri){
            return uri.getPathSegments().get(1);
        }


    }
}
