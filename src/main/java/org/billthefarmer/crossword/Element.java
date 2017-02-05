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

import java.util.Arrays;
import java.util.List;

// Element
public class Element
{
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
    private static final List<Integer> valueList =
        Arrays.asList(values);
    private static final List<Character> letterList =
        Arrays.asList(letters);

    protected String word;
    protected String phrase;

    // Element
    public Element(String word, String phrase)
    {
        this.word = word;
        this.phrase = phrase;
    }

    // getValue
    protected float getValue()
    {
        float value = 0;
        char chars[] = word.toCharArray();

        for (char c: chars)
        {
            int i = letterList.indexOf(c);
            value *= valueList.get(i);
        }

        return value;
    }
}
