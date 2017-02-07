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

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Data class
public class Data
{
    private static final String TAG = "Anagram";
    private static final int LENGTH = 2;

    private static Data instance;

    private List<String> anagramList;
    private List<String> resultList;
    private List<Float> valueList;
    private List<String> wordList;
    private boolean searching;

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

    // Set list
    public void setAnagramList(List<String> list)
    {
        anagramList = list;
    }

    // Get list
    public List<String> getAnagramList()
    {
        return anagramList;
    }

    // Get searching
    public boolean getSearching()
    {
        return searching;
    }

    // Start load task
    protected void startLoadTask(Context context, int id,
                                 List<String> wordList)
    {
        // Read words from resources
        Resources resources = context.getResources();
        InputStream stream = resources.openRawResource(id);
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader buffer = new BufferedReader(reader);

        // Start the task
        LoadTask loadTask = new LoadTask();
        loadTask.wordList = wordList;
        loadTask.execute(buffer);
    }

    // LoadTask
    protected class LoadTask
        extends AsyncTask<BufferedReader, Void, Void>
    {
        protected List<String> wordList;

        // The system calls this to perform work in a worker thread
        // and delivers it the parameters given to AsyncTask.execute()
        @Override
        protected Void doInBackground(BufferedReader... buffer)
        {
            String word;

            // Read words
            try
            {
                while ((word = buffer[0].readLine()) != null)
                    wordList.add(word);
            }

            catch (Exception e) {}

            return null;
        }
    }

    // startAnagramTask
    protected void startAnagramTask(String phrase, List<String> wordList)
    {
        // Start the task
        AnagramTask anagramTask = new AnagramTask();
        anagramTask.wordList = wordList;
        anagramTask.execute(phrase);
        searching = true;
    }

    // AnagramTask
    protected class AnagramTask
        extends AsyncTask<String, Void, List<String>>
    {
        protected List<String> wordList;
        protected List<String> anagramList;
        private List<Element> elementList;
        private int anagrams;

        // The system calls this to perform work in a worker thread
        // and delivers it the parameters given to AsyncTask.execute()
        @Override
        protected List<String> doInBackground(String... phrases)
        {
            anagramList = new ArrayList<String>();
            valueList = new ArrayList<Float>();

            elementList = findWords(phrases[0], wordList);
            findAnagrams(elementList);
            return anagramList;
        }

        // The system calls this to perform work in the UI thread and
        // delivers the result from doInBackground()
        @Override
        protected void onPostExecute(List<String> anagramList)
        {
            List<String> resultList = new ArrayList<String>();
            List<Float> list = new ArrayList<Float>(valueList);
            Collections.sort(list);
            Collections.reverse(list);
            for (float value: list)
            {
                int index = valueList.indexOf(value);
                resultList.add(anagramList.get(index));
                anagramList.remove(index);
                valueList.remove(index);
            }

            if (listener != null)
                listener.onPostExecute(resultList);
            searching = false;
        }

        // findWords
        private List<Element> findWords(String phrase, List<String> wordList)
        {
            ArrayList elementList = new ArrayList<Element>();

            for (String word: wordList)
            {
                char p[];
                if (word.length() <= 2)
                    continue;

                if ((p = findString(word, phrase)) != null)
                    elementList.add(new Element(word, new String(p)));
            }

            return elementList;
        }

        // findAnagrams
        private void findAnagrams(List<Element> elementList)
        {
            int length = elementList.size();
            int index = 0;
            List<String> wordList = new ArrayList<String>();
            for (Element element: elementList)
                anagram(wordList, elementList.subList(++index, length),
                        element);
        }

        // anagram
        private void anagram(List<String> wordList, List<Element> elementList,
                             Element element)
        {
            if (element.phrase.trim().length() == 0)
            {
                wordList.add(element.word);
                addAnagram(wordList);
                wordList.remove(element.word);
                return;
            }

            int index = 0;
            wordList.add(element.word);
            int length = elementList.size();
            for (Element e: elementList)
            {
                char p[];
                index++;

                if ((p = findString(e.word, element.phrase)) != null)
                    anagram(wordList, elementList.subList(index, length),
                            new Element(e.word, new String(p)));
            }
            wordList.remove(element.word);
        }

        private char findString(String w, String p)[]
        {
            char word[] = w.toCharArray();
            char phrase[] = p.toCharArray();

            for (char cw: word)
            {
                boolean found = false;
                int index = 0;
                for (char cp: phrase)
                {
                    if (cp == cw)
                    {
                        found = true;
                        phrase[index] = ' ';
                        break;
                    }
                    index++;
                }

                if (!found)
                    return null;
            }

            return phrase;
        }

        // showAnagram
        private void addAnagram(List<String> list)
        {
            float value = 0;
            StringBuilder buffer = new StringBuilder();
            for (String word: list)
            {
                buffer.append(word + " ");
                value += getValue(word);
            }
            anagramList.add(buffer.toString().trim());
            valueList.add(value);
            anagrams++;
        }

        // getValue
        protected float getValue(String word)
        {
            float value = 1;
            char chars[] = word.toCharArray();

            for (char c: chars)
            {
                int i = lettersList.indexOf(c);
                value *= valuesList.get(i);
            }

            return value;
        }
    }

    // Scrabble letter values:
    // A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
    // 1 3 3 2 1 4 2 4 1 8 5 1 3 1 1 3 1 1 1 1 1 4 4 8 4 1
    //                                 0                 0

    // Letter values
    private static final Integer values[] =
    {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3,
     1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};

    // Letters
    private static final Character letters[] =
    {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
     'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    // Lists
    private static final List<Integer> valuesList =
        Arrays.asList(values);
    private static final List<Character> lettersList =
        Arrays.asList(letters);

    // Element
    public class Element
    {
        protected String word;
        protected String phrase;

        // Element
        public Element(String word, String phrase)
        {
            this.word = word;
            this.phrase = phrase;
        }
    }

    // Start search task
    protected void startSearchTask(String match, List<String> wordList)
    {
        SearchTask searchTask = new SearchTask();
        searchTask.wordList = wordList;
        searchTask.execute(match);
        searching = true;
    }

    // SearchTask
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
            searching = false;
        }
    }

    // OnPostExecuteListener interface
    interface OnPostExecuteListener
    {
        void onPostExecute(List<String> resultList);
    }
}
