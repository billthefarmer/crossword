////////////////////////////////////////////////////////////////////////////////
//
//  Crossword - An android crossword solver.
//
//  Copyright (C) 2017	Bill Farmer
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Bill Farmer	 william j farmer [at] yahoo [dot] co [dot] uk.
//
///////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.crossword;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Main
@SuppressWarnings("deprecation")
public class Main extends Activity
    implements AdapterView.OnItemSelectedListener,
    AdapterView.OnItemClickListener,
    TextView.OnEditorActionListener,
    Data.OnPostExecuteListener,
    View.OnClickListener,
    TextWatcher
{
    public static final String TAG = "Crossword";
    public static final String WORD = "word";

    public static final String TEXT_PLAIN = "text/plain";
    public static final String PREF_THEME = "pref_theme";

    public static final int LETTERS = 7;
    public static final int RESULTS = 256;


    public static final int PREF_LIGHT  = 1;
    public static final int PREF_DARK   = 2;
    public static final int PREF_CYAN   = 3;
    public static final int PREF_BLUE   = 4;
    public static final int PREF_ORANGE = 5;
    public static final int PREF_PURPLE = 6;
    public static final int PREF_RED    = 7;

    private Data data;

    private ImageButton search;
    private ViewGroup letters;
    private ViewGroup contains;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;

    private List<String> wordList;
    private List<String> resultList;

    private int length = LETTERS;
    private int theme = 0;

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get preferences
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        theme = preferences.getInt(PREF_THEME, 0);

        switch (theme)
        {
        case PREF_LIGHT:
            setTheme(R.style.AppTheme);
            break;

        case PREF_DARK:
            setTheme(R.style.AppDarkTheme);
            break;

        case PREF_CYAN:
            setTheme(R.style.AppCyanTheme);
            break;

        case PREF_BLUE:
            setTheme(R.style.AppBlueTheme);
            break;

        case PREF_ORANGE:
            setTheme(R.style.AppOrangeTheme);
            break;

        case PREF_PURPLE:
            setTheme(R.style.AppPurpleTheme);
            break;

        case PREF_RED:
            setTheme(R.style.AppRedTheme);
            break;
        }

        setContentView(R.layout.main);

        // Find views
        spinner = findViewById(R.id.spinner);
        letters = findViewById(R.id.letters);
        contains = findViewById(R.id.contains);
        ListView results = findViewById(R.id.list);
        ImageButton clear = findViewById(R.id.clear);
        search = findViewById(R.id.search);
        // Create an ArrayAdapter using the string array and a default
        // spinner layout
        ArrayAdapter<CharSequence> adapt = ArrayAdapter
            .createFromResource(this, R.array.numbers,
                                R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapt.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapt);

        // Set up listeners
        if (spinner != null)
        {
            spinner.setSelection(LETTERS - 1);
            spinner.setOnItemSelectedListener(this);
        }

        if (clear != null)
            clear.setOnClickListener(this);

        if (search != null)
            search.setOnClickListener(this);

        // Set up letter slots
        if (letters != null)
        {
            for (int i = 0; i < letters.getChildCount(); i++)
            {
                TextView letter = (TextView) letters.getChildAt(i);
                TextView content = (TextView) contains.getChildAt(i);
                if (i < LETTERS)
                {
                    letter.setVisibility(View.VISIBLE);
                    content.setVisibility(View.VISIBLE);
                }

                else
                {
                    letter.setVisibility(View.GONE);
                    content.setVisibility(View.GONE);
                    letter.setText("");
                    content.setText("");
                }

                // Add listeners
                letter.setOnEditorActionListener(this);
                content.setOnEditorActionListener(this);
                letter.addTextChangedListener(this);
                content.addTextChangedListener(this);
            }
        }

        if (results != null)
            results.setOnItemClickListener(this);

        // Get data instance
        data = Data.getInstance(this);

        // Restore result list
        if (data != null)
            resultList = data.getResultList();

        if (resultList == null)
            resultList = new ArrayList<>();

        // Create adapter
        adapter = new ArrayAdapter<String>
            (this, R.layout.simple_list_item_1, resultList);
        if (results != null)
            results.setAdapter(adapter);
    }

    // onResume
    @Override
    protected void onResume()
    {
        super.onResume();

        // Reconnect listener
        data = Data.getInstance(this);

        // Restore word list
        if (data != null)
            wordList = data.getWordList();

        // Check word list
        if (wordList != null)
            return;

        // Create word list
        wordList = new ArrayList<>();

        // Load words from resources
        if (data != null)
            data.startLoadTask(this, R.raw.words_en, wordList);
    }

    // onPause
    @Override
    protected void onPause()
    {
        super.onPause();

        // Get preferences
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(PREF_THEME, theme);
        editor.apply();

        // Disconnect listener
        data = Data.getInstance(null);

        // Save result and word list
        if (data != null)
        {
            data.setResultList(resultList);
            data.setWordList(wordList);
        }
    }

    // On create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it
        // is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    // On options item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Get id
        int id = item.getItemId();
        switch (id)
        {
        // Anagram
        case R.id.action_anagram:
            return onAnagramClick(item);

        // Share
        case R.id.action_share:
            return onShareClick(item);

        // Help
        case R.id.action_help:
            return onHelpClick(item);

        // Light
        case R.id.action_light:
            return onLightClick(item);

        // Dark
        case R.id.action_dark:
            return onDarkClick(item);

        // Cyan
        case R.id.action_cyan:
            return onCyanClick(item);

        // Blue
        case R.id.action_blue:
            return onBlueClick(item);

        // Orange
        case R.id.action_orange:
            return onOrangeClick(item);

        // Purple
        case R.id.action_purple:
            return onPurpleClick(item);

        // Red
        case R.id.action_red:
            return onRedClick(item);

        // About
        case R.id.action_about:
            return onAboutClick(item);

        default:
            return false;
        }
    }

    // On anagram click
    private boolean onAnagramClick(MenuItem item)
    {
        // Discard crossword word list
        wordList = null;

        // Start anagram activity
        Intent intent = new Intent(this, Anagram.class);
        startActivity(intent);

        return true;
    }

    // On share click
    private boolean onShareClick(MenuItem item)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.getDefault(), "%s %d, '",
                                     getString(R.string.letters), length));

        for (int i = 0; i < length; i++)
        {
            TextView text = (TextView) letters.getChildAt(i);

            // If there is a letter in the slot
            if (text.length() > 0)
            {
                String letter = text.getText().toString();
                builder.append(letter.toLowerCase(Locale.getDefault()));
            }

            // Wildcard
            else
                builder.append(".");
        }

        builder.append("', '");

        for (int i = 0; i < length; i++)
        {
            TextView text = (TextView) contains.getChildAt(i);

            // If there is a letter in the slot
            if (text.length() > 0)
            {
                String letter = text.getText().toString();
                builder.append(letter.toLowerCase(Locale.getDefault()));
            }
        }

        builder.append("'\n\n");

        for (String result: resultList.toArray(new String[0]))
            builder.append(String.format("%s\n", result));
        
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(TEXT_PLAIN);
        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        startActivity(Intent.createChooser(intent, null));
        return true;
    }

    // On help click
    private boolean onHelpClick(MenuItem item)
    {
        // Start help activity
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);

        return true;
    }

    // On light click
    private boolean onLightClick(MenuItem item)
    {
        theme = PREF_LIGHT;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();

        return true;
    }

    // On dark click
    private boolean onDarkClick(MenuItem item)
    {
        theme = PREF_DARK;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();

        return true;
    }

    // On cyan click
    private boolean onCyanClick(MenuItem item)
    {
        theme = PREF_CYAN;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();

        return true;
    }

    // On blue click
    private boolean onBlueClick(MenuItem item)
    {
        theme = PREF_BLUE;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();

        return true;
    }

    // On orange click
    private boolean onOrangeClick(MenuItem item)
    {
        theme = PREF_ORANGE;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();

        return true;
    }

    // On purple click
    private boolean onPurpleClick(MenuItem item)
    {
        theme = PREF_PURPLE;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();

        return true;
    }

    // On red click
    private boolean onRedClick(MenuItem item)
    {
        theme = PREF_RED;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();

        return true;
    }

    // On about click
    @SuppressWarnings("deprecation")
    private boolean onAboutClick(MenuItem item)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        SpannableStringBuilder spannable =
            new SpannableStringBuilder(getText(R.string.version));
        Pattern pattern = Pattern.compile("%s");
        Matcher matcher = pattern.matcher(spannable);
        if (matcher.find())
            spannable.replace(matcher.start(), matcher.end(),
                              BuildConfig.VERSION_NAME);
        matcher.reset(spannable);
        if (matcher.find())
            spannable.replace(matcher.start(), matcher.end(),
                              dateFormat.format(BuildConfig.BUILT));
        builder.setMessage(spannable);

        // Add the button
        builder.setPositiveButton(android.R.string.ok, null);

        // Create the AlertDialog
        Dialog dialog = builder.show();

        // Set movement method
        TextView text = dialog.findViewById(android.R.id.message);
        if (text != null)
        {
            text.setTextAppearance(builder.getContext(),
                                   android.R.style.TextAppearance_Small);
            text.setMovementMethod(LinkMovementMethod.getInstance());
        }

        return true;
    }

    // onItemSelected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id)
    {
        // An item was selected. You can retrieve the selected item
        // using parent.getItemAtPosition(pos)
        // Check id
        switch (parent.getId())
        {
        case R.id.spinner:
            String item = parent.getItemAtPosition(pos).toString();

            // Get length
            length = Integer.parseInt(item);

            // Remove the unused slots
            if (letters != null)
            {
                for (int i = 0; i < letters.getChildCount(); i++)
                {
                    TextView letter = (TextView) letters.getChildAt(i);
                    TextView content = (TextView) contains.getChildAt(i);
                    if (i < length)
                    {
                        letter.setVisibility(View.VISIBLE);
                        content.setVisibility(View.VISIBLE);
                    }

                    // Temporarily remove the text change listener to
                    // stop unexpected consequences
                    else
                    {
                        letter.setVisibility(View.GONE);
                        letter.removeTextChangedListener(this);
                        letter.setText("");
                        letter.addTextChangedListener(this);
                        content.setVisibility(View.GONE);
                        content.removeTextChangedListener(this);
                        content.setText("");
                        content.addTextChangedListener(this);
                    }
                }
            }
        }
    }

    // onNothingSelected
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    // onItemClick
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id)
    {
        // An item was selected. You can retrieve the selected item
        // using parent.getItemAtPosition(pos)
        // Check id
        switch (parent.getId())
        {
        case R.id.list:
            String word = parent.getItemAtPosition(position).toString();
            String s = word.toUpperCase(Locale.getDefault());

            // Fill the letters in the slots and temporarily remove
            // the text change listener to stop unexpected
            // consequences
            for (int i = 0; i < Math.min(length, s.length()); i++)
            {
                TextView letter = (TextView) letters.getChildAt(i);
                letter.removeTextChangedListener(this);
                letter.setText(s.substring(i, i + 1));
                letter.addTextChangedListener(this);
            }

            // Start the web search
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(WORD, word);
            startActivity(intent);
        }
    }

    // onEditorAction
    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
    {
        // Check id
        switch (actionId)
        {
        // Do a dictionary search if there is a letter in the slot
        case EditorInfo.IME_ACTION_NEXT:
            if (view.length() > 0 && !data.getSearching())
                doSearch();
            break;
        }

        return false;
    }

    // onTextChanged
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        TextView text = (TextView) getCurrentFocus();

        // Can't be sure if we got the right slot, but move focus to
        // the next one if there is a letter in the slot
        if (text != null && text.length() > 0)
        {
            View next = text.focusSearch(View.FOCUS_RIGHT);
            if (next != null)
                next.requestFocus();

            doSearch();
        }
    }

    // afterTextChanged
    @Override
    public void afterTextChanged(Editable s)
    {
    }

    // beforeTextChanged
    @Override
    public void beforeTextChanged(CharSequence s, int start,
                                  int count, int after)
    {
    }

    // On click
    @Override
    public void onClick(View view)
    {
        // Get id
        int id = view.getId();

        // Check id
        switch (id)
        {
        // Clear
        case R.id.clear:
            doClear();
            break;

        // Search
        case R.id.search:
            doSearch();
            break;

        default:
        }
    }

    // doSearch
    private void doSearch()
    {
        // Build a match string
        StringBuilder builder = new StringBuilder();
        boolean empty = true;
        for (int i = 0; i < length; i++)
        {
            TextView text = (TextView) letters.getChildAt(i);

            // If there is a letter in the slot
            if (text.length() > 0)
            {
                String letter = text.getText().toString();
                builder.append(letter.toLowerCase(Locale.getDefault()));
                empty = false;
            }

            // Wildcard
            else
                builder.append(".");
        }

        // Build a contains string
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            TextView text = (TextView) contains.getChildAt(i);

            // If there is a letter in the slot
            if (text.length() > 0)
            {
                String letter = text.getText().toString();
                buffer.append(letter.toLowerCase(Locale.getDefault()));
                empty = false;
            }
        }

        // Don't search if no letters
        if (empty)
            return;

        // Match string
        String match = builder.toString();
        String content = buffer.toString();

        // Start search task
        if (data != null)
        {
            data.startSearchTask(match, content, wordList);
            search.setEnabled(false);
        }
    }

    // doClear
    private void doClear()
    {
        // Temporarily remove the text change listener to stop
        // unexpected consequences
        for (int i = 0; i < length; i++)
        {
            TextView text = (TextView) letters.getChildAt(i);
            text.removeTextChangedListener(this);
            text.setText("");
            text.addTextChangedListener(this);
            text = (TextView) contains.getChildAt(i);
            text.removeTextChangedListener(this);
            text.setText("");
            text.addTextChangedListener(this);
        }

        letters.getChildAt(0).requestFocus();
    }

    // The system calls this to perform work in the UI thread and
    // delivers the result from doInBackground()
    @Override
    public void onPostExecute(List<String> resultList)
    {
        if (resultList != null)
        {
            this.resultList.clear();

            // Add results to list
            this.resultList.addAll(resultList);

            // Show results
            adapter.notifyDataSetChanged();
        }

        search.setEnabled(true);
    }
}
