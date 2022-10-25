package findtheaccesscodes;

import java.util.Arrays;

public class Solution {
    public static int solution(int[] l) {
        int[] lCopy = Arrays.copyOf(l, l.length);
        reverse(lCopy);

        int count = 0;

        for (int i = 0; i < lCopy.length; i++) {
            if (lCopy[i] == 0)
                continue;

            for (int j = i + 1; j < lCopy.length; j++) {
                if (lCopy[j] == 0)
                    continue;

                if (lCopy[i] % lCopy[j] == 0) {
                    for (int k = j + 1; k < lCopy.length; k++) {
                        if (lCopy[k] == 0)
                            continue;

                        if (lCopy[j] % lCopy[k] == 0) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    public static void reverse(int[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            int tmp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = tmp;
        }
    }

    public static void main(String[] args) {
        int[] test1 = { 1, 1, 1 };
        System.out.println(Solution.solution(test1)); // 1

        int[] test2 = { 1, 2, 3, 4, 5, 6 };
        System.out.println(Solution.solution(test2)); // 3

        int[] test3 = { 6, 3, 2, 5, 4, 1 };
        System.out.println(Solution.solution(test3)); // 0

        int[] test4 = { 1, 2, 3 };
        System.out.println(Solution.solution(test4)); // 0

        int[] test5 = {};
        System.out.println(Solution.solution(test5)); // 0

        int[] test6 = { 0, 1, 1 };
        System.out.println(Solution.solution(test6)); // 0
    }
}
