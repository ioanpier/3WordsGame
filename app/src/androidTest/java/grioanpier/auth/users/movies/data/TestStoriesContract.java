/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grioanpier.auth.users.movies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Ioannis
 */
public class TestStoriesContract extends AndroidTestCase {

    private static final String TEST_STORY = "Weird Story";
    private static final String LOG_TAG = TestStoriesContract.class.getSimpleName();

    public void testBuildStoriesSpecific() {
        Uri storySpecificUri = StoriesContract.StoriesEntry.buildStoriesSpecific(TEST_STORY);

        assertNotNull("Error: Null Uri returned.  You must fill-in buildWeatherLocation in " +
                        "WeatherContract.",
                storySpecificUri);
        assertEquals("Error: Story Specific not properly appended to the end of the Uri",
                TEST_STORY, storySpecificUri.getLastPathSegment());
        assertEquals("Error: Story Specific Uri doesn't match our expected result",
                storySpecificUri.toString(),
                "content://grioanpier.auth.users.movies/stories/Weird%20Story");



    }
}
