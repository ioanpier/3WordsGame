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

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by Ioannis
 */
public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
        This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
           This helper function deletes all records from both database tables using the database
           functions only.  This is designed to be used to reset the state of the database until the
           delete functionality is available in the ContentProvider.
         */
    public void deleteAllRecordsFromDB() {
        StoriesDbHelper dbHelper = new StoriesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(StoriesContract.StoriesEntry.TABLE_NAME, null, null);
        db.close();
    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                StoriesProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: StoriesProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + StoriesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, StoriesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: StoriesProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        //content://grioanpier.auth.users.movies
        String type = mContext.getContentResolver().getType(StoriesContract.StoriesEntry.CONTENT_URI);
        assertEquals("Error: the StoriesEntry CONTENT_URI should return StoriesEntry.CONTENT_TYPE",
                StoriesContract.StoriesEntry.CONTENT_TYPE, type);

        String test = "StoryName";
        type = mContext.getContentResolver().getType(StoriesContract.StoriesEntry.buildStoriesSpecific(test));
        assertEquals("Error: the StoriesEntry CONTENT_URI with story should return StoriesEntry.CONTENT_ITEM_TYPE",
                StoriesContract.StoriesEntry.CONTENT_ITEM_TYPE, type);

    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicStoriesQuery() {
        // insert our test records into the database
        StoriesDbHelper dbHelper = new StoriesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues storyValues = TestUtilities.createStoryValues();

        long storyRowId = db.insert(StoriesContract.StoriesEntry.TABLE_NAME, null, storyValues);
        assertTrue("Unable to Insert StoriesEntry into the Database", storyRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor storyCursor = mContext.getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicStoriesQuery", storyCursor, storyValues);
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {
        ContentValues storiesValues = TestUtilities.createStoryValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(StoriesContract.StoriesEntry.CONTENT_URI, true, tco);
        Uri storyUri = mContext.getContentResolver().insert(StoriesContract.StoriesEntry.CONTENT_URI, storiesValues);

        // Did our content observer get called?
        // If this fails, the insert location isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long storyRowId = ContentUris.parseId(storyUri);

        // Verify we got a row back.
        assertTrue(storyRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor storyCursor = mContext.getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating StoriesEntry.",
                storyCursor, storiesValues);

        //Get the story with a specific name (head)
        storyCursor = mContext.getContentResolver().query(
                StoriesContract.StoriesEntry.buildStoriesSpecific(TestUtilities.TEST_HEAD),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider.  Error validating story with specific name.",
                storyCursor, storiesValues);

    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update location is functioning correctly.
     */
    public void testUpdateStory() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createStoryValues();

        Uri storyUri = mContext.getContentResolver().
                insert(StoriesContract.StoriesEntry.CONTENT_URI, values);
        long storyRowId = ContentUris.parseId(storyUri);

        // Verify we got a row back.
        assertTrue(storyRowId != -1);
        Log.d(LOG_TAG, "New row id: " + storyRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(StoriesContract.StoriesEntry._ID, storyRowId);
        updatedValues.put(StoriesContract.StoriesEntry.COLUMN_HEAD, "A different name cause I didn't like the previous one.");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(StoriesContract.StoriesEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                StoriesContract.StoriesEntry.CONTENT_URI, updatedValues, StoriesContract.StoriesEntry._ID + "= ?",
                new String[] { Long.toString(storyRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // If this fails it means that the content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,   // projection
                StoriesContract.StoriesEntry._ID + " = " + storyRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateStory.  Error validating story entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our story delete.
        TestUtilities.TestContentObserver storyObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(StoriesContract.StoriesEntry.CONTENT_URI, true, storyObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, most-likely the
        // getContext().getContentResolver().notifyChange(uri, null); isn't called
        // in the ContentProvider delete.  (only if the insertReadProvider is succeeding)
        storyObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(storyObserver);
    }

}
