package skippingwork;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Solution {
    public static int solution(int[] x, int[] y) {
        Arrays.sort(x);
        Arrays.sort(y);

        int[] shortest = x.length > y.length ? y : x;
        int[] longest = x.length > y.length ? x : y;

        return compare(shortest, longest);
    }

    public static int compare(int[] shortest, int[] longest) {
        Map<Integer, Integer> shortestLookup = new HashMap<>();

        for(int i : shortest) {
            if(shortestLookup.containsKey(i)) {
                shortestLookup.put(i, shortestLookup.get(i) + 1);
            } else {
                shortestLookup.put(i, 1);
            }
        }

        for(int i: longest) {
            if(!shortestLookup.containsKey(i)) {
                return i;
            } else {
                if(shortestLookup.get(i) == 0) {
                    shortestLookup.remove(i);
                } else {
                    shortestLookup.put(i, shortestLookup.get(i) - 1);
                }
            }
        }

        throw new AssertionError();
    }

    public static void main(String[] args) {
        System.out.println("hello");
        int[] x = {13, 5, 6, 2, 5};
        int[] y = {5, 2, 5, 13};
        System.out.println(solution(x, y));

        int[] a = {14, 27, 1, 4, 2, 50, 3, 1};
        int[] b = {2, 4, -4, 3, 1, 1, 14, 27, 50};
        System.out.println(solution(a, b));
    }
}
