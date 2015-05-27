package grioanpier.auth.users.movies;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import grioanpier.auth.users.movies.data.StoriesContract;


public class Story_Preview extends ActionBarActivity {

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_story__preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final int STORY_LOADER = 0;
        private final static String LOG_TAG = Story_Preview.class.getSimpleName() + " " + PlaceholderFragment.class.getSimpleName();

        ArrayAdapter<String> adapter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_story__preview, container, false);
            ListView listView = (ListView) rootView.findViewById(R.id.story_listview);
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
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
                    Log.v(LOG_TAG, "onLoadFinished");
                    adapter.clear();
                    if (cursor.moveToFirst()){
                        Log.v(LOG_TAG, "moveToFirst");
                        String story = cursor.getString(0);

                        for (String string : every3words(story))
                            adapter.add(string);

                    }else{
                        Log.v(LOG_TAG, "story not found!");
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
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
