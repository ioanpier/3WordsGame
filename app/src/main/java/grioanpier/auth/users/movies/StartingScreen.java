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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import grioanpier.auth.users.movies.utility.ApplicationHelper;


public class StartingScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_screen);
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

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_starting_screen, container, false);


            Button button_multiplayer = (Button) rootView.findViewById(R.id.multiplayer_button);

            button_multiplayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Multiplayer is currently unavailable", Toast.LENGTH_LONG).show();
                }
            });

            Button button_localgame = (Button) rootView.findViewById(R.id.localgame_button);
            button_localgame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ApplicationHelper.getInstance().GAME_HAS_STARTED) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage(R.string.dialog_gamehasstarted_desc)
                                .setPositiveButton(R.string.dialog_gamehasstarted_resume, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent;
                                        if (ApplicationHelper.twoPane)
                                            intent = new Intent(getActivity(), WaitingScreen.class);
                                        else
                                            intent = new Intent(getActivity(), Play.class);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.dialog_gamehasstarted_newgame, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ApplicationHelper.getInstance().prepareNewGame();
                                        Intent intent = new Intent(getActivity(), LocalGame.class);
                                        startActivity(intent);
                                    }
                                }).show();
                    } else {
                        ApplicationHelper.getInstance().prepareNewGame();
                        Intent intent = new Intent(getActivity(), LocalGame.class);
                        startActivity(intent);
                    }


                }
            });

            Button button_hallofstories = (Button) rootView.findViewById(R.id.hallofstories_button);
            button_hallofstories.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), HallOfStories.class);
                    startActivity(intent);
                }
            });


            Button button_about = (Button) rootView.findViewById(R.id.about_button);
            button_about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), About.class);
                    startActivity(intent);
                }
            });

            return rootView;
        }

    }
}
