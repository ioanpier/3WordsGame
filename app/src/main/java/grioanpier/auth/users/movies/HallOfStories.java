package grioanpier.auth.users.movies;
/*
Copyright {2016} {Ioannis Pierros (ioanpier@gmail.com)}

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
                    adapter.clear();
                    if (cursor.moveToFirst()){
                        do {
                            adapter.add(cursor.getString(0));
                        }while (cursor.moveToNext());
                    }else{
                        Toast.makeText(getActivity(), "There are no stories", Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String head = adapter.getItem(position);
                    Intent intent = new Intent(getActivity(), StoryPreview.class);
                    intent.putExtra("HEAD", head);
                    startActivity(intent);
                }
            });

            return rootView;
        }
    }
}
