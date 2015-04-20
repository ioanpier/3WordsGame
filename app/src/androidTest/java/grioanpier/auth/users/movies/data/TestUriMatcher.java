package grioanpier.auth.users.movies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Ioannis
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String STORY_QUERRY = "WEIRD_STORY";

    // content://grioanpier.auth.users.movies/stories"
    private static final Uri TEST_STORIES_DIR = StoriesContract.StoriesEntry.CONTENT_URI;
    private static final Uri TEST_STORIES_SPECIFIC_DIR = StoriesContract.StoriesEntry.buildStoriesSpecific(STORY_QUERRY);

    public void testUriMatcher() {
        UriMatcher testMatcher = StoriesProvider.buildUriMatcher();

        assertEquals("Error: The STORIES URI was matched incorrectly.",
                testMatcher.match(TEST_STORIES_DIR), StoriesProvider.STORIES);
        assertEquals("Error: The WEATHER WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_STORIES_SPECIFIC_DIR), StoriesProvider.STORY_SPECIFIC);
    }
}
