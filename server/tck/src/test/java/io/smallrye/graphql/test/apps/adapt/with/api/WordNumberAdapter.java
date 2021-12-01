package io.smallrye.graphql.test.apps.adapt.with.api;

import java.util.HashMap;
import java.util.Map;

import io.smallrye.graphql.api.Adapter;

/**
 * Using an adapter to integer
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class WordNumberAdapter implements Adapter<WordNumber, Integer> {

    @Override
    public WordNumber from(Integer number) {
        if (numberMap.containsKey(number)) {
            return numberMap.get(number);
        }
        return unknown;
    }

    @Override
    public Integer to(WordNumber word) {
        if (wordMap.containsKey(word.number)) {
            return wordMap.get(word.number);
        }
        return minusOne;
    }

    private static WordNumber unknown = new WordNumber("unknown");
    private static Integer minusOne = -1;

    private static Map<Integer, WordNumber> numberMap = new HashMap<>();
    static {
        numberMap.put(0, new WordNumber("zero"));
        numberMap.put(1, new WordNumber("one"));
        numberMap.put(2, new WordNumber("two"));
        numberMap.put(3, new WordNumber("three"));
        numberMap.put(4, new WordNumber("four"));
        numberMap.put(5, new WordNumber("five"));
        numberMap.put(6, new WordNumber("six"));
        numberMap.put(7, new WordNumber("seven"));
        numberMap.put(8, new WordNumber("eight"));
        numberMap.put(9, new WordNumber("nine"));
    }

    private static Map<String, Integer> wordMap = new HashMap<>();
    static {
        wordMap.put("zero", 0);
        wordMap.put("one", 1);
        wordMap.put("two", 2);
        wordMap.put("three", 3);
        wordMap.put("four", 4);
        wordMap.put("five", 5);
        wordMap.put("six", 6);
        wordMap.put("seven", 7);
        wordMap.put("eight", 8);
        wordMap.put("nine", 9);
    }
}
