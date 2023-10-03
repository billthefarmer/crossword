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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Anagram
@SuppressWarnings("deprecation")
public class Anagram extends Activity
    implements AdapterView.OnItemClickListener,
    PopupMenu.OnMenuItemClickListener,
    TextView.OnEditorActionListener,
    Data.OnPostExecuteListener,
    View.OnClickListener
{
    public static final int ANAGRAMS = 1024;

    private Data data;
    private Toolbar toolbar;
    private ImageButton search;
    private TextView textView;
    private ArrayAdapter adapter;
    private List<String> wordList;
    private List<String> anagramList;

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get preferences
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        int theme = preferences.getInt(Main.PREF_THEME, 0);

        switch (theme)
        {
        case Main.PREF_LIGHT:
            setTheme(R.style.AppTheme);
            break;

        case Main.PREF_DARK:
            setTheme(R.style.AppDarkTheme);
            break;

        case Main.PREF_CYAN:
            setTheme(R.style.AppCyanTheme);
            break;

        case Main.PREF_BLUE:
            setTheme(R.style.AppBlueTheme);
            break;

        case Main.PREF_ORANGE:
            setTheme(R.style.AppOrangeTheme);
            break;

        case Main.PREF_PURPLE:
            setTheme(R.style.AppPurpleTheme);
            break;

        case Main.PREF_RED:
            setTheme(R.style.AppRedTheme);
            break;
        }

        setContentView(R.layout.anagram);

        // Find toolbar
        toolbar = findViewById(getResources().getIdentifier("action_bar",
                                                            "id", "android"));
        // Set up navigation
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_36dp);
        toolbar.setNavigationOnClickListener((v) ->
        {
            PopupMenu popup = new PopupMenu(this, v);
            popup.inflate(R.menu.navigation);
            popup.setOnMenuItemClickListener(this);
            popup.show();
        });

        // Find views
        View layout = findViewById(R.id.layout);
        textView = findViewById(R.id.phrase);
        search = findViewById(R.id.search);
        ListView listView = findViewById(R.id.list);

        // Set listeners
        if (layout != null)
            layout.setOnClickListener(this);

        if (textView != null)
            textView.setOnEditorActionListener(this);

        if (search != null)
            search.setOnClickListener(this);

        if (listView != null)
            listView.setOnItemClickListener(this);

        // Get data instance
        data = Data.getInstance(this);

        // Restore anagram list
        if (data != null)
            anagramList = data.getAnagramList();

        if (anagramList == null)
            anagramList = new ArrayList<>();

        // Create adapter
        adapter = new ArrayAdapter<>
            (this, android.R.layout.simple_list_item_1, anagramList);
        if (listView != null)
            listView.setAdapter(adapter);

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
            data.startLoadTask(this, R.raw.corncob_lowercase, wordList);
    }

    // onResume
    @Override
    protected void onResume()
    {
        super.onResume();

        // Reconnect listener
        data = Data.getInstance(this);
    }

    // onPause
    @Override
    protected void onPause()
    {
        super.onPause();

        // Disconnect listener
        data = Data.getInstance(null);

        // Save anagram and word list
        if (data != null)
        {
            data.setAnagramList(anagramList);
            data.setWordList(wordList);
        }
    }

    // On options item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Get id
        int id = item.getItemId();
        switch (id)
        {
        // Home
        case android.R.id.home:
            onBackPressed();
            break;

        default:
            return false;
        }

        return true;
    }

    // onBackPressed
    @Override
    public void onBackPressed()
    {
        // Discard anagram list and anagram word list
        anagramList = null;
        wordList = null;

        // Done
        finish();
    }

    // onItemClick
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id)
    {
        // Copy anagram to input field
        String phrase = parent.getItemAtPosition(position).toString();
        if (textView != null)
            textView.setText(phrase);

        // Start the web search
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(Main.WORD, phrase);
        startActivity(intent);
    }

    // onEditorAction
    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
    {
        // Get id
        switch (actionId)
        {
        // Find anagrams
        case EditorInfo.IME_ACTION_SEARCH:
            doSearch();
            break;
        }

        return false;
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
        // Search
        case R.id.search:
            doSearch();
            break;

        // Layout
        default:
            if (textView != null)
                textView.clearFocus();
        }
    }

    // onMenuItemClick
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        // Get id
        int id = item.getItemId();
        switch (id)
        {
        // Crossword
        case R.id.action_crossword:
            onBackPressed();
            return true;

        // Help
        case R.id.action_help:
            return onHelpClick(item);

        default:
            return false;
        }
    }

    // On help click
    private boolean onHelpClick(MenuItem item)
    {
        // Start help activity
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);

        return true;
    }

    // doSearch
    private void doSearch()
    {
        if (data != null && !data.getSearching() && textView != null)
        {
            // Get the phrase
            String phrase = textView.getText()
                            .toString().toLowerCase(Locale.getDefault());
            if (phrase.length() > 0)
            {
                // Find anagrams
                data.startAnagramTask(phrase, wordList);
                search.setEnabled(false);
            }
        }
    }

    // The system calls this to perform work in the UI thread and
    // delivers the result from doInBackground()
    @Override
    public void onPostExecute(List<String> resultList)
    {
        // Empty the current list
        anagramList.clear();

        // Add the new one
        anagramList.addAll(resultList);

        // Notify the adapter
        adapter.notifyDataSetChanged();
        search.setEnabled(true);
    }
}
