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
