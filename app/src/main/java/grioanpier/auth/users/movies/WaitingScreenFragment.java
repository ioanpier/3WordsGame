
package grioanpier.auth.users.movies;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    private int playersJoined = 1;
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
            playersJoined = savedInstanceState.getInt(PLAYERS_IN_ROOM);
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
        mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, playersJoined));

        return rootView;
    }

    private static CustomHandler handler;



    @Override
    public void onStart(){
        super.onStart();
        handler = new CustomHandler(this);
        ApplicationHelper.getInstance().setChatHandler(handler);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ApplicationHelper.getInstance().unregisterChatHandler();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PLAYERS_IN_ROOM, playersJoined);
    }

    public void playersJoinedIncrement() {
        playersJoined++;
        mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, playersJoined));
    }

    public void playersJoinedDecrement() {
        playersJoined--;
        mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, playersJoined));
    }

    private void startGame() {
        //TODO implement me
    }

    public interface StartGameButtonClicked {
        public void onStartGameButtonClicked();
    }


    private class CustomHandler extends Handler {

        private WaitingScreenFragment fragment;

        public CustomHandler(WaitingScreenFragment activity) {
            super();
            this.fragment = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ApplicationHelper.HANDLER_IS_PLAYER:
                    break;
                default:
                    System.out.println("what: "+msg.what);
                    break;
            }
        }

    }


}//Fragment