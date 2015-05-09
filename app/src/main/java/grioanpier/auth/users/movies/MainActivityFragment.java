package grioanpier.auth.users.movies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import grioanpier.auth.users.movies.data.StoriesContract;

/**
 * Created by Ioannis
 */
public class MainActivityFragment extends Fragment {
    private final static String LOG_TAG = MainActivityFragment.class.getSimpleName();
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_loading_screen, container, false);

        ImageView image = (ImageView) rootView.findViewById(R.id.monkImageView);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StartingScreen.class);
                startActivity(intent);
            }
        });

        Cursor cursor = getActivity().getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()){
            Log.v(LOG_TAG, cursor.getString(0) + " and " + cursor.getString(1));
            while (cursor.moveToNext()){
                Log.v(LOG_TAG, cursor.getString(0) + " and " + cursor.getString(1));
            }
        }else{
            Log.v(LOG_TAG, "database was empty!");
        }

        cursor.close();

        /*
        if (cursor.moveToFirst()){
            Log.v(LOG_TAG, "Database isn't empty");
        }else{
            Log.v(LOG_TAG, "Database is empty");
        }

        cursor.close();

        //Delete everything
        getActivity().getContentResolver().delete(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,
                null
        );

        ////////////////////////////////////////////////

        Log.v(LOG_TAG, "Add values to database");
        ContentValues values = new ContentValues();

        String head = "Story Name HERE";
        values.put(StoriesContract.StoriesEntry.COLUMN_STORY, "first 3 words");
        values.put(StoriesContract.StoriesEntry.COLUMN_HEAD, head);

        Uri uri = getActivity().getContentResolver().insert(
                StoriesContract.StoriesEntry.CONTENT_URI,
                values
        );


        Log.v(LOG_TAG, "insert more to the story");
        values = new ContentValues();

        values.put(StoriesContract.StoriesEntry.COLUMN_STORY, "second story row");

        String[] selArgs = {head};
        int rowsAffected = getActivity().getContentResolver().update(
                StoriesContract.StoriesEntry.CONTENT_URI,
                values,
                StoriesContract.StoriesEntry.COLUMN_HEAD + " = ?",
                selArgs
        );


        Log.v(LOG_TAG, "update returned " + rowsAffected);

        Log.v(LOG_TAG, "Query values");
        cursor = getActivity().getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()){
            do{
                DatabaseUtils.dumpCurrentRow(cursor);

            }while(cursor.moveToNext());
        }

        cursor.close();

        Log.v(LOG_TAG, "delete databse");
        //Delete everything
        getActivity().getContentResolver().delete(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,
                null
        );

        Log.v(LOG_TAG, "query database");
        cursor = getActivity().getContentResolver().query(
                StoriesContract.StoriesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()){
            do{
                DatabaseUtils.dumpCurrentRow(cursor);

            }while(cursor.moveToNext());
        }

        cursor.close();
        */


        return rootView;
    }


}
