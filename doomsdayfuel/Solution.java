package doomsdayfuel;

import java.util.Arrays;

public class Solution {

    private static class Fraction {
        private int numer;
        private int denom;

        public Fraction(int numer, int denom) {
            int gcd = gcd(numer, denom);

            if (gcd == 0) {
                System.err.println("here");
                ;
            }

            this.numer = numer / gcd;
            this.denom = denom / gcd;
        }

        public int getNumer() {
            return numer;
        }

        public int getDenom() {
            return denom;
        }

        public Fraction multiply(Fraction that) {
            int numer = this.numer * that.numer;
            int denom = this.denom * that.denom;

            return new Fraction(numer, denom);
        }

        public Fraction add(Fraction that) {
            int numer = (this.numer * that.denom) + (that.numer * this.denom);
            int denom = that.denom * this.denom;

            return new Fraction(numer, denom);
        }

        @Override
        public String toString() {
            return numer + "/" + denom;
        }

        @Override
        public boolean equals(Object that) {
            if (!(that instanceof Fraction)) {
                return false;
            }

            Fraction thatFrac = (Fraction) that;

            return (this.numer == thatFrac.numer) && (this.denom == thatFrac.denom);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + this.numer;
            hash = 31 * hash + this.denom;
            return hash;
        }

        // Greatest common divisor
        private int gcd(int n1, int n2) {
            if (n2 == 0) {
                return n1;
            }
            return gcd(n2, n1 % n2);
        }

    }

    public static int[] solution(int[][] m) {
        Fraction[][] transMatrix = new Fraction[m.length][m[0].length];

        for (int i = 0; i < m.length; i++) {
            int stateSum = 0;

            for (int j = 0; j < m[i].length; j++) {
                stateSum += m[i][j];
            }

            if (stateSum > 0) {
                for (int j = 0; j < m[i].length; j++) {
                    transMatrix[i][j] = new Fraction(m[i][j], stateSum);
                }
            } else {
                for (int j = 0; j < m[i].length; j++) {
                    int numer = i == j ? 1 : 0;
                    transMatrix[i][j] = new Fraction(numer, 1);
                }
            }
        }

        System.out.println(Arrays.deepToString(transMatrix));

        Fraction[][] foo = transMatrix;
        Fraction[][] prev = foo;

        do {
            prev = foo;
            foo = multiply(foo, transMatrix);
        } while (!Arrays.deepEquals(prev, foo));

        System.out.println(Arrays.deepToString(foo));

        return null;
    }

    private static Fraction[][] multiply(Fraction[][] a, Fraction[][] b) {
        int aRows = a.length;
        int aColumns = a[0].length;
        int bRows = b.length;
        int bColumns = b[0].length;

        if (aColumns != bRows) {
            // TODO change this
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        Fraction[][] c = new Fraction[aRows][bColumns];

        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bColumns; j++) {
                c[i][j] = new Fraction(0, 1);
            }
        }

        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bColumns; j++) {
                for (int k = 0; k < aColumns; k++) {
                    c[i][j] = c[i][j].add(a[i][k].multiply(b[k][j]));
                }
            }
        }

        System.out.println(Arrays.deepToString(c));

        return c;
    }

    public static void main(String[] args) {
        int[][] testCase = {
                { 0, 2, 1, 0, 0 },
                { 0, 0, 0, 3, 4 },
                { 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0 }
        };

        solution(testCase);
    }
}
