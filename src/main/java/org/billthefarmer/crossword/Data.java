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

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

// Data class
public class Data
{
    private static Data instance;

    private List<String> resultList;
    private List<String> wordList;

    private OnPostExecuteListener listener;

    // Constructor
    private Data() {}

    // Get instance
    public static Data getInstance(OnPostExecuteListener listener)
    {
        if (instance == null)
            instance = new Data();

        instance.listener = listener;
        return instance;
    }

    // Set list
    public void setWordList(List<String> list)
    {
        wordList = list;
    }

    // Get list
    public List<String> getWordList()
    {
        return wordList;
    }

    // Set list
    public void setResultList(List<String> list)
    {
        resultList = list;
    }

    // Get list
    public List<String> getResultList()
    {
        return resultList;
    }

    // Start search task
    protected void startSearchTask(String match, List<String> wordList)
    {
        SearchTask searchTask = new SearchTask();
        searchTask.wordList = wordList;

        searchTask.execute(match);
    }

    // SearchTask class
    protected class SearchTask
        extends AsyncTask<String, Void, List<String>>
    {
        protected List<String> wordList;
        private List<String> resultList;

        // The system calls this to perform work in a worker thread
        // and delivers it the parameters given to AsyncTask.execute()
        @Override
        protected List<String> doInBackground(String... matches)
        {
            resultList = new ArrayList<String>();

            String match = matches[0];
            int length = match.length();
            for (String word : wordList)
            {
                if (word.length() != length)
                    continue;

                if (word.matches(match))
                    resultList.add(word);

                if (resultList.size() > Main.RESULTS)
                    break;
            }

            // Return the result
            return resultList;
        }

        // The system calls this to perform work in the UI thread and
        // delivers the result from doInBackground()
        @Override
        protected void onPostExecute(List<String> result)
        {
            if (listener != null)
                listener.onPostExecute(result);
        }
    }

    // OnPostExecuteListener interface
    interface OnPostExecuteListener
    {
        void onPostExecute(List<String> resultList);
    }
}
