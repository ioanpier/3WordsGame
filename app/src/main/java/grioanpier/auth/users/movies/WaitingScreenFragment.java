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
import android.bluetooth.BluetoothAdapter;
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
    private TextView mMacTextView;
    private final String LOG_TAG = WaitingScreenFragment.class.getSimpleName();

    private int deviceType;
    private int mPlayersJoined = 1;
    private static final String PLAYERS_IN_ROOM = "players in room";
    private static final int PLAYERS_JOINED_STRING_ID = R.string.playersJoined;
    private static final int MAC_DISPLAYED = R.string.your_mac_is;

    public WaitingScreenFragment() {}

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
        mMacTextView = ((TextView) rootView.findViewById(R.id.mac));

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
            mMacTextView.setVisibility(View.GONE);
        } else {
            switch (deviceType) {
                case Constants.DEVICE_HOST:
                    mPlayersJoinedTextView.setText(getActivity().getString(PLAYERS_JOINED_STRING_ID, mPlayersJoined));
                    mMacTextView.setText(getActivity().getString(MAC_DISPLAYED, BluetoothAdapter.getDefaultAdapter().getAddress()));
                    break;
                default:
                    mPlayersJoinedTextView.setVisibility(View.GONE);
                    mButton.setVisibility(View.GONE);
                    mMacTextView.setVisibility(View.GONE);
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