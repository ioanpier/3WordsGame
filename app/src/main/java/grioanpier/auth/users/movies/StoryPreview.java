package grioanpier.auth.users.movies;
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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import grioanpier.auth.users.movies.data.StoriesContract;
import grioanpier.auth.users.movies.utility.ApplicationHelper;


public class StoryPreview extends ActionBarActivity {

    private ShareActionProvider mShareActionProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story__preview);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_story__preview, menu);

        MenuItem item = menu.findItem(R.id.share_action_provider);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(getShareIntent());

        return true;
    }

    private Intent getShareIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, ApplicationHelper.getInstance().getStory());

        return intent;
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final int STORY_LOADER = 0;
        private final static String LOG_TAG = StoryPreview.class.getSimpleName() + " " + PlaceholderFragment.class.getSimpleName();

        ArrayAdapter<String> adapter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_story__preview, container, false);
            ListView listView = (ListView) rootView.findViewById(R.id.story_listview);
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            listView.setAdapter(adapter);
            final String head = getActivity().getIntent().getStringExtra("HEAD");

            getLoaderManager().initLoader(STORY_LOADER, null, new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    return new CursorLoader(getActivity(),
                            StoriesContract.StoriesEntry.CONTENT_URI,
                            new String[]{StoriesContract.StoriesEntry.COLUMN_STORY},
                            StoriesContract.StoriesEntry.COLUMN_HEAD + " = ?",
                            new String[]{head},
                            null
                    );

                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    adapter.clear();
                    if (cursor.moveToFirst()) {
                        String story = cursor.getString(0);
                        for (String string : every3words(story))
                            adapter.add(string);
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {}
            });

            return rootView;
        }

        /**
         * Seperates the string every 3 words
         *
         * @param string The string to seperate
         * @return a table. Every cell has 3 words.
         */
        private String[] every3words(String string) {
            String[] newString = string.split("\\s+");

            String[] finalMessage = new String[newString.length / 3];


            for (int i = 0; i < finalMessage.length; i++) {
                finalMessage[i] = newString[i * 3] + " " + newString[i * 3 + 1] + " " + newString[i * 3 + 2];
            }
            return finalMessage;
        }
    }
}
