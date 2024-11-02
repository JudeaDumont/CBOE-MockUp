package com.example.Model;

import java.util.ArrayList;
import java.util.List;

public class GeneratePermutations {

    public static List<List<Integer>> generatePermutations(int x) {
        List<List<Integer>> result = new ArrayList<>();

        // Initialize the array with numbers from 0 to x - 1
        int[] array = new int[x];
        int[] directions = new int[x];

        for (int i = 0; i < x; i++) {
            array[i] = i;
            directions[i] = -1; // -1 means left, 1 means right
        }

        // Add the first permutation
        result.add(toList(array));

        // Generate and store all permutations iteratively
        while (true) {
            int mobileIndex = -1;
            int mobileValue = -1;

            // Find the largest mobile integer
            for (int i = 0; i < x; i++) {
                int dir = directions[i];
                int neighborIndex = i + dir;

                if (neighborIndex >= 0 && neighborIndex < x && array[i] > array[neighborIndex] && array[i] > mobileValue) {
                    mobileValue = array[i];
                    mobileIndex = i;
                }
            }

            // Stop if there is no mobile integer
            if (mobileIndex == -1) break;

            // Swap the mobile integer in its direction
            int swapIndex = mobileIndex + directions[mobileIndex];
            swap(array, mobileIndex, swapIndex);
            swap(directions, mobileIndex, swapIndex);

            // Reverse the direction of all elements larger than the mobile integer
            for (int i = 0; i < x; i++) {
                if (array[i] > mobileValue) {
                    directions[i] = -directions[i];
                }
            }

            // Store the current permutation in the result list
            result.add(toList(array));
        }

        return result;
    }

    private static List<Integer> toList(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int num : array) {
            list.add(num);
        }
        return list;
    }

    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
