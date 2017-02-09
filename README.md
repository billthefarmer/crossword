# ![Logo](src/main/res/drawable-mdpi/ic_launcher.png) Crossword [![Build Status](https://travis-ci.org/billthefarmer/crossword.svg?branch=master)](https://travis-ci.org/billthefarmer/crossword)

Android crossword solver. The app is available on [F-Droid](https://f-droid.org/repository/browse/?fdid=org.billthefarmer.crossword) and [here](https://github.com/billthefarmer/crossword/releases).

![Crossword](https://github.com/billthefarmer/billthefarmer.github.io/raw/master/images/crossword/Crossword.png) ![Definition](https://github.com/billthefarmer/billthefarmer.github.io/raw/master/images/crossword/Definition.png)

![Help](https://github.com/billthefarmer/billthefarmer.github.io/raw/master/images/crossword/Help.png) ![Anagram](https://github.com/billthefarmer/billthefarmer.github.io/raw/master/images/crossword/Anagram.png)

* Words up to 28 letters
* 110,000 word dictionary
* Web search word definitions

## Solve crossword entries
Set the number of letters in the word you are looking for using the
dropdown. If the number of slots won't fit on the screen, rotate the
phone so they will. The letter slots will scroll sideways. The longest
word in the dictionary is antidisestablishmentarianism, which is 28
letters.  Even if 28 slots won't fit on the screen, the app should
still find this word.

Fill in the letters you have in the slots. As each letter is entered
the next slot will be selected and the app will search for words. You
can also use the the keyboard **Next** button or the **Search** button
to search. A stop character may be used as a wildcard.

A list of matching words will appear below. If you select a word in the
list, the slots will be filled in with that word, and a web search
will be made for the definition on
[DuckDuckGo](https://duckduckgo.com).

There are three icons in the toolbar, **Anagram**, wich will show the
anagram screen, **Help**, which will show the help screen, and
**About**, which will show the copyright, licence and version.

## Solve anagrams
Type in the word or phrase you want anagrams for, and use the keyboard
**Done** button or the **Search** button to search. The search may
take a long time, depending on the length of the phrase. During the
search the **Search** button is disabled. The anagram search uses a
smaller 58,000 word dictionary to reduce the search time, and because
the larger dictionary contains a large number of not very useful three
and four letter words and scronyms.
