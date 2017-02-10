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
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// AnagramActivity
public class AnagramActivity extends Activity
    implements AdapterView.OnItemClickListener,
               TextView.OnEditorActionListener,
               Data.OnPostExecuteListener,
               View.OnClickListener
{
    public static final int ANAGRAMS = 1024;

    private Data data;
    private Button search;
    private ListView listView;
    private TextView textView;
    private ArrayAdapter adapter;
    private List<String> wordList;
    private List<String> anagramList;

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anagram);

        // Enable back navigation on action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        View layout = findViewById(R.id.layout);
        textView = (TextView)findViewById(R.id.phrase);
        search = (Button)findViewById(R.id.search);
        listView = (ListView)findViewById(R.id.list);

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
            anagramList = new ArrayList<String>();

        // Create adapter
        adapter =
            new ArrayAdapter<String>(this,
                                     android.R.layout.simple_list_item_1,
                                     anagramList);
        if (listView != null)
            listView.setAdapter(adapter);

        // Restore word list
        if (data != null)
            wordList = data.getWordList();

        // Check word list
        if (wordList != null)
            return;

        // Create word list
        wordList = new ArrayList<String>();

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
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id)
    {
        String phrase = (String)parent.getItemAtPosition(position);
        if (textView != null)
            textView.setText(phrase);

        // Start the web search
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(Main.WORD, phrase);
        startActivity(intent);
    }

    // onEditorAction
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

        default:
            if (textView != null)
                textView.clearFocus();
            return;
        }
    }

    private void doSearch()
    {
        if (data != null && !data.getSearching() && textView != null)
        {
            String phrase = textView.getText()
                .toString().toLowerCase(Locale.getDefault());
            if (phrase.length() > 0)
            {
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
        anagramList.clear();

        for (String anagram: resultList)
            anagramList.add(anagram);

        adapter.notifyDataSetChanged();
        search.setEnabled(true);
    }
}
