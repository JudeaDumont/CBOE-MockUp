package com.example.Model;

import java.util.*;

public class GeneratePermutationsChecks {

    public static List<Integer> generatePermutations(String check, String[] subs) {
        List<Integer> result = new ArrayList<>();

        if (check == null || check.isEmpty() || subs == null || subs.length == 0)
            return result;

        int wordLength = subs[0].length();
        int wordsCount = subs.length;
        int totalLength = wordLength * wordsCount;

        if (check.length() < totalLength)
            return result;

        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : subs) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }

        for (int i = 0; i < wordLength; i++) {
            int left = i;
            int right = i;
            int count = 0;
            Map<String, Integer> currentCount = new HashMap<>();

            while (right + wordLength <= check.length()) {
                String word = check.substring(right, right + wordLength);
                right += wordLength;

                if (wordCount.containsKey(word)) {
                    currentCount.put(word, currentCount.getOrDefault(word, 0) + 1);
                    count++;

                    // If word frequency exceeds, shift the window
                    while (currentCount.get(word) > wordCount.get(word)) {
                        String leftWord = check.substring(left, left + wordLength);
                        currentCount.put(leftWord, currentCount.get(leftWord) - 1);
                        count--;
                        left += wordLength;
                    }

                    // If all words match, add the starting index to result
                    if (count == wordsCount) {
                        result.add(left);
                    }
                } else {
                    // Reset counts if word not in subs
                    currentCount.clear();
                    count = 0;
                    left = right;
                }
            }
        }

        return result;
    }
}
