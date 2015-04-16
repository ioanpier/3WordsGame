package grioanpier.auth.users.movies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class Play extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private ArrayList<String> listItems=new ArrayList();
        private ArrayAdapter<String> adapter;
        private InputFilter filter;
        private final int MAX_WORDS = 3;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            String[] names = {
                    "Movie 1",
                    "Movie 2",
                    "Movie 3"
            };

            listItems= new ArrayList<>(Arrays.asList(names));
            adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1,
                    listItems);


            final ListView listView = (ListView) rootView.findViewById(R.id.main_listview);
            listView.setAdapter(adapter);

            final EditText editText = (EditText) rootView.findViewById(R.id.main_edittext);

            //Sets the soft keyboard to be hidden when the app starts.
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            //if IME_ACTION_SEND is clicked, adds the string to the list and updates the listView. Also clears the editText text.
            editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        if (countWords(editText.getText().toString())==3){
                            listItems.add(editText.getText().toString());
                            editText.setText("");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(adapter.getCount() - 1);
                        }else {
                            System.out.println("Need 3 words!");
                            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                            Toast.makeText(getActivity(), "test", Toast.LENGTH_LONG).show();

                        }

                        handled = true;
                    }
                    return handled;
                }
            });

            //https://stackoverflow.com/questions/28823898/android-how-to-set-maximum-word-limit-on-edittext
            //Allows only for 3 words in the editText
            editText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                    int wordsLength = countWords(s.toString());// words.length;
                    System.out.println(wordsLength);
                    // count == 0 means a new word is going to start
                    if (count == 0 && wordsLength >= MAX_WORDS) {
                        System.out.println(true);
                        setCharLimit(editText, editText.getText().length());
                    } else {
                        System.out.println(false);
                        removeFilter(editText);
                    }
                    System.out.println(String.valueOf(wordsLength) + "/" + MAX_WORDS);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            return rootView;
        }
        private int countWords(String s) {
            String trim = s.trim();
            if (trim.isEmpty())
                return 0;
            return trim.split("\\s+").length; // separate string around spaces
        }



        private void setCharLimit(EditText et, int max) {
            filter = new InputFilter.LengthFilter(max);
            et.setFilters(new InputFilter[] { filter });
        }

        private void removeFilter(EditText et) {
            if (filter != null) {
                et.setFilters(new InputFilter[0]);
                filter = null;
            }
        }
    }
}
