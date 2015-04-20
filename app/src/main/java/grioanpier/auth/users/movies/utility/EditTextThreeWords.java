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

    EditTextThreeWords editText;
    private InputFilter filter;
    private final int MAX_WORDS = 3;

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
                    setCharLimit(editText, editText.getText().length());

                } else {
                    removeFilter(editText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
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
