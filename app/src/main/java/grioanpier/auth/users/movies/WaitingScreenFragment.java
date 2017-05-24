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