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
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.View;
import android.util.Log;
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
               View.OnClickListener, Data.OnPostExecuteListener
{
    public static final String TAG = "Crossword";
    public static final String RESULT_LIST = "result_list";

    public static final int LETTERS = 7;
    public static final int RESULTS = 100;

    private Data data;

    private Spinner spinner;
    private Button button;
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

        data = Data.getInstance(this);

        spinner = (Spinner)findViewById(R.id.spinner);
        letters = (LinearLayout)findViewById(R.id.letters);
        results = (ListView)findViewById(R.id.list);
        button = (Button)findViewById(R.id.search);

        if (spinner != null)
        {
            spinner.setSelection(LETTERS - 1);
            spinner.setOnItemSelectedListener(this);
        }

        if (button != null)
            button.setOnClickListener(this);

        if (letters != null)
        {
            for (int i = 0; i < letters.getChildCount(); i++)
            {
                TextView v = (TextView)letters.getChildAt(i);
                if (i < LETTERS)
                    v.setVisibility(View.VISIBLE);

                else
                {
                    v.setVisibility(View.GONE);
                    v.setText("");
                }
            }
        }

        if (savedInstanceState != null)
            resultList = savedInstanceState.getStringArrayList(RESULT_LIST);

        if (resultList == null)
            resultList = new ArrayList<String>();

        adapter =
            new ArrayAdapter<String>(this,
                                     android.R.layout.simple_list_item_1,
                                     resultList);
        if (results != null)
            results.setAdapter(adapter);

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

        data = Data.getInstance(this);
    }

    // onPause
    @Override
    protected void onPause()
    {
        super.onPause();

        data = Data.getInstance(null);
    }

    // onSaveInstanceState
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(RESULT_LIST,
                                    (ArrayList<String>)resultList);
    }

    // onItemSelected
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id)
    {
        // An item was selected. You can retrieve the selected item
        // using parent.getItemAtPosition(pos)
        String item = (String)parent.getItemAtPosition(pos);
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

    // On click
    @Override
    public void onClick(View view)
    {
        // Get id
        int id = view.getId();

        // Check id
        switch (id)
        {
            // Button
        case R.id.search:
            break;

        default:
            return;
        }

        // Build a match string
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            TextView v = (TextView)letters.getChildAt(i);
            String letter = v.getText().toString();
            if (letter.equals(""))
                buffer.append(".");

            else
                buffer.append(letter.toLowerCase());
        }

        String match = buffer.toString();

        // Start search task
        if (data != null)
        {
            data.startSearchTask(match, wordList, resultList);

            // Disable button
            view.setEnabled(false);
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

        // Enable button
        button.setEnabled(true);
    }
}
