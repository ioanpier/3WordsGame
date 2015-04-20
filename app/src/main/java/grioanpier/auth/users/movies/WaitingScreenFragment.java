
package grioanpier.auth.users.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import grioanpier.auth.users.movies.utility.ApplicationHelper;
import grioanpier.auth.users.movies.utility.Constants;

public class WaitingScreenFragment extends Fragment {

    private Button mButton;
    private TextView mPlayersJoinedTextView;

    private final String LOG_TAG = WaitingScreenFragment.class.getSimpleName();

    private int deviceType;
    private int mPlayersJoined = 1;
    private static final String PLAYERS_IN_ROOM = "players in room";
    private static int PLAYERS_JOINED_STRING_ID = R.string.playersJoined;

    public WaitingScreenFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        deviceType = intent.getIntExtra(Constants.DEVICE_TYPE, Constants.DEVICE_SPECTATOR);


        if (savedInstanceState != null) {
            mPlayersJoined = savedInstanceState.getInt(PLAYERS_IN_ROOM);
        }


        View rootView = inflater.inflate(R.layout.fragment_waiting_screen, container, false);
        mButton = (Button) rootView.findViewById(R.id.button_start_game);

        switch (deviceType) {

            case Constants.DEVICE_HOST:
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((StartGameButtonClicked) getActivity()).onStartGameButtonClicked();
                    }
                });
                break;
            default:
                //Remove the "Start the Game" button, that's only for the host.
                mButton.setVisibility(View.GONE);
                break;
        }

        mPlayersJoinedTextView = (TextView) rootView.findViewById(R.id.players_joined);
        mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, mPlayersJoined));

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ApplicationHelper.getInstance().unregisterChatHandler();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PLAYERS_IN_ROOM, mPlayersJoined);
    }

    public void playersJoinedIncrement() {
        mPlayersJoined++;
        mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, mPlayersJoined));
    }

    public void setPlayersJoined(int playersJoined){
        mPlayersJoined = playersJoined;
    }

    public void playersJoinedDecrement() {
        mPlayersJoined--;
        mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, mPlayersJoined));
    }

    private void startGame() {
        //TODO implement me
    }

    public interface StartGameButtonClicked {
        public void onStartGameButtonClicked();
    }


}//Fragment