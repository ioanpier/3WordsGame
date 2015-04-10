package grioanpier.auth.users.movies.data;

import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by Ioannis
 */
public class TestStoriesContract extends AndroidTestCase {

    private static final String TEST_STORY = "Weird Story";
    private static final String LOG_TAG = TestStoriesContract.class.getSimpleName();

    public void testBuildStoriesSpecific() {
        Uri storySpecificUri = StoriesContract.StoriesEntry.buildStoriesSpecific(TEST_STORY);
        Log.v(LOG_TAG, storySpecificUri.toString());
        System.out.println(storySpecificUri.toString());

        assertNotNull("Error: Null Uri returned.  You must fill-in buildWeatherLocation in " +
                        "WeatherContract.",
                storySpecificUri);
        assertEquals("Error: Story Specific not properly appended to the end of the Uri",
                TEST_STORY, storySpecificUri.getLastPathSegment());
        Log.v(LOG_TAG, storySpecificUri.toString());
        assertEquals("Error: Story Specific Uri doesn't match our expected result",
                storySpecificUri.toString(),
                "content://grioanpier.auth.users.movies/stories/Weird%20Story");



    }
}
