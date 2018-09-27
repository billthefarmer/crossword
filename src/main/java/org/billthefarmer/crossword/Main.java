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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Main
public class Main extends Activity
        implements AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener,
        TextView.OnEditorActionListener,
        Data.OnPostExecuteListener,
        View.OnClickListener,
        TextWatcher {
    public static final String TAG = "Crossword";
    public static final String WORD = "word";

    public static final String PREF_DARK = "pref_dark";

    public static final int LETTERS = 7;
    public static final int RESULTS = 256;
    public static final int VERSION_M = 23;

    private Data data;

    private Button search;
    private ViewGroup letters;
    private ArrayAdapter<String> adapter;

    private List<String> wordList;
    private List<String> resultList;

    private int length = LETTERS;
    private boolean dark = true;

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get preferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        dark = preferences.getBoolean(PREF_DARK, false);

        if (dark)
            setTheme(R.style.AppDarkTheme);

        setContentView(R.layout.main);

        // Find views
        Spinner spinner = findViewById(R.id.spinner);
        letters = findViewById(R.id.letters);
        ListView results = findViewById(R.id.list);
        Button clear = findViewById(R.id.clear);
        search = findViewById(R.id.search);

        // Set up listeners
        if (spinner != null) {
            spinner.setSelection(LETTERS - 1);
            spinner.setOnItemSelectedListener(this);
        }

        if (clear != null)
            clear.setOnClickListener(this);

        if (search != null)
            search.setOnClickListener(this);

        // Set up letter slots
        if (letters != null) {
            for (int i = 0; i < letters.getChildCount(); i++) {
                TextView letter = (TextView) letters.getChildAt(i);
                if (i < LETTERS)
                    letter.setVisibility(View.VISIBLE);

                else {
                    letter.setVisibility(View.GONE);
                    letter.setText("");
                }

                // Add listeners
                letter.setOnEditorActionListener(this);
                letter.addTextChangedListener(this);
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
        adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        resultList);
        if (results != null)
            results.setAdapter(adapter);
    }

    // onResume
    @Override
    protected void onResume() {
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
    protected void onPause() {
        super.onPause();

        // Get preferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(PREF_DARK, dark);
        editor.apply();

        // Disconnect listener
        data = Data.getInstance(null);

        // Save result and word list
        if (data != null) {
            data.setResultList(resultList);
            data.setWordList(wordList);
        }
    }

    // On create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it
        // is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    // onPrepareOptionsMenu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_dark).setChecked(dark);

        return true;
    }

    // On options item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get id
        int id = item.getItemId();
        switch (id) {
            // Anagram
            case R.id.action_anagram:
                return onAnagramClick(item);

            // Help
            case R.id.action_help:
                return onHelpClick(item);

            // About
            case R.id.action_about:
                return onAboutClick(item);

            // Dark
            case R.id.action_dark:
                return onDarkClick(item);

            default:
                return false;
        }
    }

    // On anagram click
    private boolean onAnagramClick(MenuItem item) {
        // Discard crossword word list
        wordList = null;

        // Start anagram activity
        Intent intent = new Intent(this, AnagramActivity.class);
        startActivity(intent);

        return true;
    }

    // On help click
    private boolean onHelpClick(MenuItem item) {
        // Start help activity
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);

        return true;
    }

    // On about click
    private boolean onAboutClick(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about);

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String format = getString(R.string.version);

        String message =
                String.format(Locale.getDefault(),
                        format, BuildConfig.VERSION_NAME,
                        dateFormat.format(BuildConfig.BUILT));
        builder.setMessage(message);

        // Add the button
        builder.setPositiveButton(android.R.string.ok, null);

        // Create the AlertDialog
        Dialog dialog = builder.show();

        // Set movement method
        TextView text = dialog.findViewById(android.R.id.message);
        if (text != null)
            text.setMovementMethod(LinkMovementMethod.getInstance());

        return true;
    }

    // On dark click
    private boolean onDarkClick(MenuItem item) {
        dark = !dark;
        item.setChecked(dark);
        if (Build.VERSION.SDK_INT != VERSION_M)
            recreate();

        return true;
    }

    // onItemSelected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item
        // using parent.getItemAtPosition(pos)
        // Check id
        switch (parent.getId()) {
            case R.id.spinner:
                String item = (String) parent.getItemAtPosition(pos);

                // Get length
                length = Integer.parseInt(item);

                // Remove the unused slots
                if (letters != null) {
                    for (int i = 0; i < letters.getChildCount(); i++) {
                        TextView text = (TextView) letters.getChildAt(i);
                        if (i < length)
                            text.setVisibility(View.VISIBLE);

                            // Temporarily remove the text change listener to
                            // stop unexpected consequences
                        else {
                            text.setVisibility(View.GONE);
                            text.removeTextChangedListener(this);
                            text.setText("");
                            text.addTextChangedListener(this);
                        }
                    }
                }
        }
    }

    // onNothingSelected
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // onItemClick
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        // An item was selected. You can retrieve the selected item
        // using parent.getItemAtPosition(pos)
        // Check id
        switch (parent.getId()) {
            case R.id.list:
                String word = (String) parent.getItemAtPosition(position);
                String s = word.toUpperCase(Locale.getDefault());

                // Fill the letters in the slots and temporarily remove
                // the text change listener to stop unexpected
                // consequences
                for (int i = 0; i < Math.min(length, s.length()); i++) {
                    TextView text = (TextView) letters.getChildAt(i);
                    text.removeTextChangedListener(this);
                    text.setText(s.substring(i, i + 1));
                    text.addTextChangedListener(this);
                }

                // Start the web search
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(WORD, word);
                startActivity(intent);
        }
    }

    // onEditorAction
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        // Check id
        switch (actionId) {
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
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        TextView text = (TextView) getCurrentFocus();

        // Can't be sure if we got the right slot, but move focus to
        // the next one if there is a letter in the slot
        if (text != null && text.length() > 0) {
            View next = text.focusSearch(View.FOCUS_RIGHT);
            if (next != null)
                next.requestFocus();

            doSearch();
        }
    }

    // afterTextChanged
    @Override
    public void afterTextChanged(Editable s) {
    }

    // beforeTextChanged
    @Override
    public void beforeTextChanged(CharSequence s, int start,
                                  int count, int after) {
    }

    // On click
    @Override
    public void onClick(View view) {
        // Get id
        int id = view.getId();

        // Check id
        switch (id) {
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
    private void doSearch() {
        // Build a match string
        StringBuilder buffer = new StringBuilder();
        boolean empty = true;
        for (int i = 0; i < length; i++) {
            TextView text = (TextView) letters.getChildAt(i);

            // If there is a letter in the slot
            if (text.length() > 0) {
                String letter = text.getText().toString();
                buffer.append(letter.toLowerCase(Locale.getDefault()));
                empty = false;
            }

            // Wildcard
            else
                buffer.append(".");
        }

        // Don't search if no letters
        if (empty)
            return;

        // Match string
        String match = buffer.toString();

        // Start search task
        if (data != null) {
            data.startSearchTask(match, wordList);
            search.setEnabled(false);
        }
    }

    // doClear
    private void doClear() {
        // Temporarily remove the text change listener to stop
        // unexpected consequences
        for (int i = 0; i < length; i++) {
            TextView text = (TextView) letters.getChildAt(i);
            text.removeTextChangedListener(this);
            text.setText("");
            text.addTextChangedListener(this);
        }
    }

    // The system calls this to perform work in the UI thread and
    // delivers the result from doInBackground()
    @Override
    public void onPostExecute(List<String> resultList) {
        if (resultList != null) {
            this.resultList.clear();

            // Add results to list
            this.resultList.addAll(resultList);

            // Show results
            adapter.notifyDataSetChanged();
        }

        search.setEnabled(true);
    }
}
