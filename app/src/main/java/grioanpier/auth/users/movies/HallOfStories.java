package grioanpier.auth.users.movies;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import grioanpier.auth.users.movies.data.StoriesContract;


public class HallOfStories extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_of_stories);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hall_of_stories, menu);
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
        private final static String LOG_TAG = HallOfStories.class.getSimpleName() + " " + PlaceholderFragment.class.getSimpleName();
        public PlaceholderFragment() {
        }

        private static final int STORIES_LOADER = 0;

        ArrayAdapter<String> adapter;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_hall_of_stories, container, false);
            ListView listView = (ListView) rootView.findViewById(R.id.hallofstories_listview);
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            listView.setAdapter(adapter);

            getLoaderManager().initLoader(STORIES_LOADER, null, new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    return new CursorLoader(getActivity(),
                            StoriesContract.StoriesEntry.CONTENT_URI,
                            new String[]{StoriesContract.StoriesEntry.COLUMN_HEAD},
                            null,
                            null,
                            null
                    );
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    Log.v(LOG_TAG, "onLoadFinished");
                    adapter.clear();
                    if (cursor.moveToFirst()){
                        Log.v(LOG_TAG, "moveToFirst");
                        do {
                            adapter.add(cursor.getString(0));
                        }while (cursor.moveToNext());
                    }else{
                        Log.v(LOG_TAG, "database was empty!");
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String head = (String)adapter.getItem(position);
                    Intent intent = new Intent(getActivity(), Story_Preview.class);
                    intent.putExtra("HEAD", head);
                    startActivity(intent);
                }
            });

            return rootView;
        }
    }
}
