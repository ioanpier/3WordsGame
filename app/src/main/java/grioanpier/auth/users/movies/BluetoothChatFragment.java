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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import grioanpier.auth.users.movies.utility.ApplicationHelper;
import grioanpier.auth.users.movies.utility.Constants;


public class BluetoothChatFragment extends Fragment {

    private static final String LOG_TAG = BluetoothChatFragment.class.getSimpleName();

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;

    /**
     * Array adapter for the conversation thread
     */
    private static ArrayAdapter<String> mConversationArrayAdapter;
    private static ArrayList<String> mConversationArrayList;

    private static final String CHAT_ITEMS = "CHAT_ITEMS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.chat_listview);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);

        mConversationArrayList = ApplicationHelper.getInstance().chat;


        // Initialize the array adapter for the conversation thread using (possibly) the previous chat (restore it)
        mConversationArrayAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.message_chat,
                mConversationArrayList);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND){
                    sendMessage(view.getText().toString());
                    return true;
                }
                return false;
            }
        });
        //Sets the soft keyboard to be hidden when the app starts.
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onStart(){
        super.onStart();
        ApplicationHelper.addHanlder(mHandler);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putStringArrayList(CHAT_ITEMS, mConversationArrayList);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ApplicationHelper.removeHandler(mHandler);
    }

    /**
     * Sends a message.
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that there's actually something to send
        if (message.length() > 0) {
            String deviceName = ApplicationHelper.DEVICE_NAME;
            deviceName = ApplicationHelper.format(deviceName);

            message = ApplicationHelper.format(message);
            ApplicationHelper.getInstance().write(deviceName + message, Constants.BLUETOOTH_CHAT.hashCode());
            mOutEditText.setText("");
            mConversationView.setSelection(mConversationArrayAdapter.getCount() - 1);
        }
    }




    /**
     * The Handler that gets the messages. The messages are first handled in {@link ApplicationHelper}
     */
    private ChatHandler mHandler = new ChatHandler();
    //This is extended because simply making a new Handler was giving me "a leak might occur" warning.
    private static class ChatHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Log.i(LOG_TAG, "Chat Handler start!");

            if (msg.what!=Constants.BLUETOOTH_CHAT.hashCode()){
                Log.i(LOG_TAG, "This isn't for me");
                return;
            }

            String message = (String) msg.obj;
            Log.i(LOG_TAG, message);

            //Extract the device name
            int length = ApplicationHelper.deformat(message);
            String deviceName = message.substring(3,length+3);
            message = message.substring(length + 3, message.length());

            if (deviceName.equals(ApplicationHelper.DEVICE_NAME))
                deviceName = "You";

            //Extract the message
            length = ApplicationHelper.deformat(message);
            message = message.substring(3, length + 3);


            ApplicationHelper.getInstance().chat.add(deviceName + ": " + message);
            mConversationArrayAdapter.notifyDataSetChanged();

            Log.i(LOG_TAG, "Chat Handler end!");
        }
    }

}
