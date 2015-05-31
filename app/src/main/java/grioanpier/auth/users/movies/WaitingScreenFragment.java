
package grioanpier.auth.users.movies;

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
    private TextView mWaitingForHost;

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
        deviceType = ApplicationHelper.getInstance().DEVICE_TYPE;

        if (savedInstanceState != null) {
            mPlayersJoined = savedInstanceState.getInt(PLAYERS_IN_ROOM);
        }


        View rootView = inflater.inflate(R.layout.fragment_waiting_screen, container, false);
        mButton = (Button) rootView.findViewById(R.id.button_start_game);
        mPlayersJoinedTextView = (TextView) rootView.findViewById(R.id.players_joined);
        mWaitingForHost = ((TextView) rootView.findViewById(R.id.waitingForHost));

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((StartGameButtonClicked) getActivity()).onStartGameButtonClicked();
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (ApplicationHelper.getInstance().GAME_HAS_STARTED) {
            mPlayersJoinedTextView.setVisibility(View.GONE);
            mWaitingForHost.setVisibility(View.GONE);
            mButton.setText(R.string.resume_story);
            mButton.setVisibility(View.VISIBLE);
        } else {
            switch (deviceType) {
                case Constants.DEVICE_HOST:
                    mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, mPlayersJoined));
                    break;
                default:
                    mPlayersJoinedTextView.setVisibility(View.GONE);
                    mButton.setVisibility(View.GONE);
                    break;
            }
        }
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

    public void playersJoinedDecrement() {
        mPlayersJoined--;
        mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, mPlayersJoined));
    }

    public interface StartGameButtonClicked {
        void onStartGameButtonClicked();
    }


}//Fragment