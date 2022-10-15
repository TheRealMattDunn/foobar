package gearing;

import java.util.Arrays;

public class Solution {
    public static int[] solution(int[] pegs) {
        int r0 = 0;

        // Calculate sums to work out r0 using forumla derived offline
        for (int i = 0; i < pegs.length; i++) {
            if (i == 0 || i == pegs.length - 1) {
                int num = (int) Math.pow(-1, i) * pegs[i];
                r0 += num;
            } else {
                r0 += 2 * (int) Math.pow(-1, i) * pegs[i];
            }
        }

        // Formula needs to multiply by 2 in all cases...
        r0 = r0 * -2;

        // But when there is an even number of pegs, we divide by 3
        int denom = ((pegs.length % 2) == 0) ? 3 : 1;

        // Check that our radius isn't negative, and that all the gears fit in the gaps between the pegs
        if (r0 <= 0 || !checkGears(r0 / (double) denom, pegs)) {
            int[] ret = { -1, -1 };
            return ret;
        }

        // Work out the greatest common divisor, to get the ratio in its simplest form
        int gcd = gcd(r0, denom);

        // Return the result in an array
        int[] ret = { r0 / gcd, denom / gcd };
        return ret;
    }

    // Greatest common divisor
    public static int gcd(int n1, int n2) {
        if (n2 == 0) {
            return n1;
        }
        return gcd(n2, n1 % n2);
    }

    // Check whether all the gears fit in the gaps between the pegs
    public static boolean checkGears(double firstRadius, int[] pegs) {
        double currentRadius = firstRadius;

        for (int i = 0; i < pegs.length - 1; i++) {
            int pegGap = pegs[i + 1] - pegs[i];
            // Ensure that the gear fits in the current gap, and also that the radius is > 1
            if (currentRadius >= pegGap || currentRadius < 1) {
                return false;
            }

            currentRadius = pegGap - currentRadius;
        }

        return true;
    }

    public static void main(String[] args) {
        int[][] pegs = {
                { 10 }, // [-1, -1]
                { 4, 30, 50 }, // [12, 1]
                { 4, 17, 50 }, // [-1, -1]
                { 8, 20 }, // [8, 1]
                { 1, 51 }, // [100, 3]
                { 1, 31 }, // [20, 1]
                { 1, 31, 51, 71 }, // [20, 1]
                { 1, 31, 51, 71, 91 }, // [20, 1]
                { 4, 9, 17, 31, 40 } // [4, 1]
        };

        for (int i = 0; i < pegs.length; i++) {
            System.out.println(Arrays.toString(solution(pegs[i])));
        }
    }
}
