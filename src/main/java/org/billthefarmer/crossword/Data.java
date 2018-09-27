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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

// Data class
public class Data
{
    private static final String TAG = "Anagram";
    private static final int LENGTH = 2;

    private static Data instance;

    private List<String> anagramList;
    private List<String> resultList;
    private List<String> wordList;
    private boolean searching;

    private OnPostExecuteListener listener;

    // Letter values
    private static final Integer values[] =
    {
        1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3,
        1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10
    };
    // Letters
    private static final Character letters[] =
    {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    // Lists
    private static final List<Integer> valuesList =
        Arrays.asList(values);
    private static final List<Character> lettersList =
        Arrays.asList(letters);

    // Constructor
    private Data()
    {
    }

    // Get instance
    public static Data getInstance(OnPostExecuteListener listener)
    {
        if (instance == null)
            instance = new Data();

        instance.listener = listener;
        return instance;
    }

    // Get list
    public List<String> getWordList()
    {
        return wordList;
    }

    // Set list
    public void setWordList(List<String> list)
    {
        wordList = list;
    }

    // Get list
    public List<String> getResultList()
    {
        return resultList;
    }

    // Set list
    public void setResultList(List<String> list)
    {
        resultList = list;
    }

    // Get list
    public List<String> getAnagramList()
    {
        return anagramList;
    }

    // Set list
    public void setAnagramList(List<String> list)
    {
        anagramList = list;
    }

    // Get searching
    public boolean getSearching()
    {
        return searching;
    }

    // Scrabble letter values:
    // A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
    // 1 3 3 2 1 4 2 4 1 8 5 1 3 1 1 3 1 1 1 1 1 4 4 8 4 1
    //                                 0                 0

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

    // startAnagramTask
    protected void startAnagramTask(String phrase, List<String> wordList)
    {
        // Start the task
        AnagramTask anagramTask = new AnagramTask();
        anagramTask.wordList = wordList;
        anagramTask.execute(phrase);
        searching = true;
    }

    // Start search task
    protected void startSearchTask(String match, List<String> wordList)
    {
        SearchTask searchTask = new SearchTask();
        searchTask.wordList = wordList;
        searchTask.execute(match);
        searching = true;
    }

    // OnPostExecuteListener interface
    interface OnPostExecuteListener
    {
        void onPostExecute(List<String> resultList);
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
            catch (Exception e)
            {
            }

            return null;
        }
    }

    // AnagramTask
    protected class AnagramTask
        extends AsyncTask<String, Void, List<String>>
    {
        protected List<String> wordList;
        private List<String> anagramList;
        private List<Float> valueList;
        private Element elements;

        // The system calls this to perform work in a worker thread
        // and delivers it the parameters given to AsyncTask.execute()
        @Override
        protected List<String> doInBackground(String... phrases)
        {
            anagramList = new ArrayList<>();
            valueList = new ArrayList<>();

            // Find words that will fit in phrase
            elements = findWords(phrases[0], wordList);
            // Find anagrams from words
            findAnagrams(elements);
            return anagramList;
        }

        // The system calls this to perform work in the UI thread and
        // delivers the result from doInBackground()
        @Override
        protected void onPostExecute(List<String> anagramList)
        {
            List<String> resultList = new ArrayList<>();
            List<Float> list = new ArrayList<>(valueList);

            // Sort in reverse value order, high value first
            Collections.sort(list);
            Collections.reverse(list);
            for (float value : list)
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
        private Element findWords(String phrase, List<String> wordList)
        {
            Element elements = null;
            Element element = null;

            // Build a forward linked list of elements containing each
            // word and what is left of the phrase after the word is
            // removed

            // Check each word in list
            for (String word : wordList)
            {
                char p[];
                if (word.length() <= 2)
                    continue;

                if ((p = findString(word, phrase)) != null)
                {
                    // First element
                    if (element == null)
                    {
                        element = new Element(word, new String(p));
                        elements = element;
                    }

                    // Build a list
                    else
                    {
                        element.next = new Element(word, new String(p));
                        element = element.next;
                    }
                }
            }

            return elements;
        }

        // findAnagrams
        private void findAnagrams(Element element)
        {
            // Find anagrams for each word in the list
            while (element != null)
            {
                anagram(element.next, element);
                element = element.next;
            }
        }

        // anagram
        private boolean anagram(Element elements, Element element)
        {
            // Stop when limit reached
            if (anagramList.size() >= AnagramActivity.ANAGRAMS)
                return true;

            // Found an anagram, don't reuse this word
            if (element.phrase.trim().length() == 0)
            {
                addAnagram(element);
                return true;
            }

            // Build a reverse linked list of elements containing each
            // word and what is left of the phrase after the word is
            // removed. The last successful element will contain an
            // all blanks phrase, so trim() will leave an empty string

            // Search forward from this point in the list
            while (elements != null)
            {
                char p[];

                // If this word fits, try forward in the list, don't
                // reuse a word if successful
                if ((p = findString(elements.word, element.phrase)) != null)
                {
                    if (anagram(elements.next,
                                new Element(elements.word,
                                            new String(p), element)))
                        return true;
                }

                elements = elements.next;
            }

            return false;
        }

        // findString
        private char findString(String w, String p)[]
        {
            char word[] = w.toCharArray();
            char phrase[] = p.toCharArray();

            // Check each char in the word
            for (char cw : word)
            {
                boolean found = false;
                int index = 0;
                for (char cp : phrase)
                {
                    // If the char fits
                    if (cp == cw)
                    {
                        // Replace char with blank
                        found = true;
                        phrase[index] = ' ';
                        break;
                    }
                    index++;
                }

                if (!found)
                    return null;
            }

            // Return phrase less letters in this word
            return phrase;
        }

        // addAnagram
        private void addAnagram(Element element)
        {
            // Reverse order of words
            Deque<String> stack = new ArrayDeque<>();
            while (element != null)
            {
                stack.push(element.word);
                element = element.last;
            }

            // Add words to anagram
            float value = 0;
            StringBuilder buffer = new StringBuilder();
            while (stack.peek() != null)
            {
                String word = stack.pop();
                buffer.append(word).append(" ");
                value += getValue(word);
            }
            anagramList.add(buffer.toString().trim());
            valueList.add(value);
        }

        // getValue
        protected float getValue(String word)
        {
            float value = 1;
            char chars[] = word.toCharArray();

            // Multiply the scrabble value of the letters in the word
            for (char c : chars)
            {
                int index = lettersList.indexOf(c);
                value *= valuesList.get(index);
            }

            return value;
        }
    }

    // Element
    public class Element
    {
        protected String word;
        protected String phrase;
        protected Element last;
        protected Element next;

        // Element
        public Element(String word, String phrase)
        {
            this(word, phrase, null, null);
        }

        // Element
        public Element(String word, String phrase, Element last)
        {
            this(word, phrase, last, null);
        }

        // Element
        public Element(String word, String phrase, Element last, Element next)
        {
            this.word = word;
            this.phrase = phrase;
            this.last = last;
            this.next = next;
        }
    }

    // SearchTask
    protected class SearchTask extends AsyncTask<String, Void, List<String>>
    {
        protected List<String> wordList;
        private List<String> resultList;

        // The system calls this to perform work in a worker thread
        // and delivers it the parameters given to AsyncTask.execute()
        @Override
        protected List<String> doInBackground(String... matches)
        {
            resultList = new ArrayList<>();

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
}
