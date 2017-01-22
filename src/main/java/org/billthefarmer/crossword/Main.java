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
import android.os.Bundle;
import android.content.res.Resources;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// Main
public class Main extends Activity
    implements AdapterView.OnItemSelectedListener,
               AdapterView.OnItemClickListener,
               TextView.OnEditorActionListener,
               Data.OnPostExecuteListener,
               View.OnClickListener
{
    public static final String TAG = "Crossword";

    public static final int LETTERS = 7;
    public static final int RESULTS = 100;

    private Data data;

    private Spinner spinner;
    private Button clear;
    private Button search;
    private LinearLayout letters;
    private ListView results;
    private ArrayAdapter<String> adapter;

    private List<String> wordList;
    private List<String> resultList;

    private int length = LETTERS;

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Get data instance
        data = Data.getInstance(this);

        // Find views
        spinner = (Spinner)findViewById(R.id.spinner);
        letters = (LinearLayout)findViewById(R.id.letters);
        results = (ListView)findViewById(R.id.list);
        clear = (Button)findViewById(R.id.clear);
        search = (Button)findViewById(R.id.search);

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
                TextView letter = (TextView)letters.getChildAt(i);
                if (i < LETTERS)
                    letter.setVisibility(View.VISIBLE);

                else
                {
                    letter.setVisibility(View.GONE);
                    letter.setText("");
                }

                letter.setOnEditorActionListener(this);
            }
        }

        if (results != null)
            results.setOnItemClickListener(this);

        // Restore result list
        if (data != null)
            resultList = data.getResultList();

        if (resultList == null)
            resultList = new ArrayList<String>();

        adapter =
            new ArrayAdapter<String>(this,
                                     android.R.layout.simple_list_item_1,
                                     resultList);
        if (results != null)
        {
            results.setAdapter(adapter);
            results.setOnItemSelectedListener(this);
        }

        // Restore word list
        if (data != null)
            wordList = data.getWordList();

        if (wordList != null)
            return;

        Resources resources = getResources();
        InputStream stream = resources.openRawResource(R.raw.corncob_lowercase);
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader buffer = new BufferedReader(reader);

        wordList = new ArrayList<String>();
        String word;

        try
        {
            while ((word = buffer.readLine()) != null)
                wordList.add(word);
        }

        catch (Exception e) {}
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
        if (data != null)
        {
            data.setResultList(resultList);
            data.setWordList(wordList);
        }
    }

    // onItemSelected
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id)
    {
        // An item was selected. You can retrieve the selected item
        // using parent.getItemAtPosition(pos)
        String item = (String)parent.getItemAtPosition(pos);

        // Get length
        length = Integer.parseInt(item);

        // Remove the unused slots
        if (letters != null)
        {
            for (int i = 0; i < letters.getChildCount(); i++)
            {
                TextView v = (TextView)letters.getChildAt(i);
                if (i < length)
                    v.setVisibility(View.VISIBLE);

                else
                {
                    v.setVisibility(View.GONE);
                    v.setText("");
                }
            }
        }
    }

    // onNothingSelected
    public void onNothingSelected(AdapterView<?> parent) {}

    // onItemClick
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id)
    {
        String item = (String)parent.getItemAtPosition(position);

        for (int i = 0; i < length; i++)
        {
            TextView text = (TextView)letters.getChildAt(i);
            text.setText(item.substring(i, i+1).toUpperCase());
        }
    }

    // onEditorAction
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
    {
        switch (actionId)
        {
        case EditorInfo.IME_ACTION_NEXT:
            String letter = view.getText().toString();
            if (!letter.equals(""))
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
            // Clear
        case R.id.clear:
            doClear();
            break;

            // Search
        case R.id.search:
            doSearch();
            break;

        default:
            return;
        }
    }

    // doSearch
    private void doSearch()
    {
        // Build a match string
        StringBuilder buffer = new StringBuilder();
        boolean empty = true;
        for (int i = 0; i < length; i++)
        {
            TextView text = (TextView)letters.getChildAt(i);
            String letter = text.getText().toString();
            if (letter.equals(""))
                buffer.append(".");

            else
            {
                buffer.append(letter.toLowerCase());
                empty = false;
            }
        }

        if (empty)
            return;

        String match = buffer.toString();

        // Start search task
        if (data != null)
        {
            data.startSearchTask(match, wordList, resultList);
            search.setEnabled(false);
        }
    }

    // doClear
    private void doClear()
    {
        for (int i = 0; i < length; i++)
        {
            TextView text = (TextView)letters.getChildAt(i);
            text.setText("");
        }
    }

    // The system calls this to perform work in the UI thread and
    // delivers the result from doInBackground()
    @Override
    public void onPostExecute(List<String> resultList)
    {
        // Show results
        if (resultList != null)
            adapter.notifyDataSetChanged();

        search.setEnabled(true);
    }
}
