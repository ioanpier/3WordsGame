package grioanpier.auth.users.movies.utility;
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
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * An extension of {@link EditText} that places a limit on the maximum words allowed. The current limit is 3.
 */
public class EditTextThreeWords extends EditText {
    private String LOG_TAG = EditTextThreeWords.class.getSimpleName();
    private EditTextThreeWords editText;
    private InputFilter filter;
    private final int MAX_WORDS = 3;
    private static final int WAITING_INPUT = 0;
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
                    setCharLimit(editText, editText.getText().length());
                } else {
                    removeFilter(editText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
