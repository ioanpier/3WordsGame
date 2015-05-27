package grioanpier.auth.users.movies.utility;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Ioannis on 16/4/2015.
 */
public class EditTextThreeWords extends EditText {
    private String LOG_TAG = EditTextThreeWords.class.getSimpleName();
    EditTextThreeWords editText;
    private InputFilter filter;
    private final int MAX_WORDS = 3;

    private static final int WAITING_INPUT = 0;
    private static final int CHANGED = 1;
    private static final int RESTARTED = 2;
    private int status = WAITING_INPUT;

    public EditTextThreeWords(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        editText =this;

        addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                int wordsLength = countWords(s.toString());// words.length;
                // count == 0 means a new word is going to start
                if (count == 0 && wordsLength >= MAX_WORDS) {
//
                    setCharLimit(editText, editText.getText().length());
//
                } else {
                    removeFilter(editText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //switch (status){
                //    case WAITING_INPUT:
                //        Log.v(LOG_TAG, "WAITING_INPUT");
                //        String text = s.toString();
                //        Log.v(LOG_TAG, "string is " + text);
                //        if (!text.isEmpty()){
                //            String[] t = text.split("\\s+");
                //            StringBuilder builder = new StringBuilder();
//
                //            for (int i=0; i<t.length && i<MAX_WORDS; i++){
                //                builder.append(t[i]).append(" ");
                //            }
                //            builder.deleteCharAt(builder.length()-1);
                //            s.clear();
                //            s.append(builder.toString());
//
                //        }
                //        status = CHANGED;
                //        break;
//
                //    case CHANGED:
                //        Log.v(LOG_TAG, "CHANGED");
                //        status = RESTARTED;
                //        break;
                //    case RESTARTED:
                //        Log.v(LOG_TAG, "RESTARTED");
                //        status = WAITING_INPUT;
                //        break;
                //}


            }//afterTextChanged
        });
    }


    private int countWords(String s) {
        //Remove the whitespaces at the beginning and the end
        String trim = s.trim();
        if (trim.isEmpty())
            return 0;

        //Split the string around the whitespaces.
        return trim.split("\\s+").length;
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
