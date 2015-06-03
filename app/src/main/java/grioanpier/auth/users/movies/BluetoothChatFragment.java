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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
        ApplicationHelper.getInstance().setChatHandler(mHandler);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putStringArrayList(CHAT_ITEMS, mConversationArrayList);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ApplicationHelper.getInstance().unregisterChatHandler();
    }

    /**
     * Sends a message.
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that there's actually something to send
        if (message.length() > 0) {
            ApplicationHelper.getInstance().write(message, ApplicationHelper.CHAT);
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
            ApplicationHelper.getInstance().chat.add((String) msg.obj);
            mConversationArrayAdapter.notifyDataSetChanged();

            //This is commented so that if I ever need to use this, I will remember.
            //Maybe for displaying the messages in a different way, depending on who sent it.
            //switch (msg.what) {
            //    case ApplicationHelper.MESSAGE_ME:
            //        ;
            //        break;
            //    case ApplicationHelper.MESSAGE_OTHER:
            //
            //        break;
            //}
        }
    }

}
