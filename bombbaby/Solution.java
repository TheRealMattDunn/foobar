package bombbaby;

import java.math.BigInteger;

public class Solution {
    public static final String IMPOSSIBLE = "impossible";

    public static String solution(String x, String y) {
        BigInteger xBig = new BigInteger(x);
        BigInteger yBig = new BigInteger(y);

        if(xBig.compareTo(BigInteger.ZERO) < 1 || yBig.compareTo(BigInteger.ZERO) < 1) {
            return IMPOSSIBLE;
        }

        BigInteger largest = xBig.compareTo(yBig) > 0 ? xBig : yBig;
        BigInteger smallest = xBig.compareTo(yBig) < 0 ? xBig : yBig;

        BigInteger n = BigInteger.ZERO;

        while (true) {
            if (largest.compareTo(BigInteger.ONE) == 0 && smallest.compareTo(BigInteger.ONE) == 0) {
                break;
            }

            if (largest.compareTo(smallest) == 0) {
                return IMPOSSIBLE;
            }

            BigInteger step = largest.divide(smallest.add(BigInteger.ONE));
            n = n.add(step);
            largest = largest.subtract(smallest.multiply(step));

            if (largest.compareTo(smallest) < 0) {
                BigInteger tmp = largest;
                largest = smallest;
                smallest = tmp;
            }
        }

        return n.toString();
    }

    public static void main(String[] args) {
        System.out.println(solution("2", "1"));
        System.out.println(solution("4", "7"));
        System.out.println(solution("1", "1"));
        System.out.println(solution("0", "0"));
        System.out.println(solution("0", "1"));
        System.out.println(solution("50", "50"));
        System.out.println(solution("123235345346747", "5241236354766"));
    }
}
