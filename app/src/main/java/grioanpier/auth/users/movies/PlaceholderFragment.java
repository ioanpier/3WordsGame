package grioanpier.auth.users.movies;

import android.content.Intent;
import android.net.Uri;
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
public class PlaceholderFragment extends Fragment {
    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.loading_screen, container, false);

        ImageView image = (ImageView) rootView.findViewById(R.id.monkImageView);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO the 2nd parameter should be the StartingScreen.class
                //This is for testing purposes only
                Intent intent = new Intent(getActivity(), StartingScreen.class);
                startActivity(intent);
            }
        });


        Uri storySpecificUri = StoriesContract.StoriesEntry.buildStoriesSpecific("Weird Story");
        Log.v("TestStoriesContract",storySpecificUri.toString());


        return rootView;
    }



}
