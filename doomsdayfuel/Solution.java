package doomsdayfuel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Solution {

    private static class Fraction implements Cloneable {
        private int numer;
        private int denom;

        public Fraction(int numer, int denom) {
            int gcd = gcd(numer, denom);

            if (gcd == 0) {
                System.err.println("here");
            }

            this.numer = numer / gcd;
            this.denom = denom / gcd;

            if (this.denom < 0) {
                this.numer = -1 * this.numer;
                this.denom = -1 * this.denom;
            }
        }

        public Fraction multiply(Fraction that) {
            int numer = this.numer * that.numer;
            int denom = this.denom * that.denom;

            return new Fraction(numer, denom);
        }

        public Fraction divide(Fraction divisor) {
            Fraction inverseDivisor = new Fraction(divisor.denom, divisor.numer);
            return multiply(inverseDivisor);
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

        public float toFloat() {
            return numer / (float) denom;
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

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return new Fraction(numer, denom);
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

        Fraction[][] solutionMatrix = transMatrix;
        Fraction[][] prev = solutionMatrix;

        do {
            prev = solutionMatrix;
            solutionMatrix = multiply(solutionMatrix, transMatrix);
        } while (!Arrays.deepEquals(prev, solutionMatrix));

        System.out.println(Arrays.deepToString(solutionMatrix));

        Fraction[] absorbProbs = getAbsorbProbs(0, getAbsorbStates(transMatrix), solutionMatrix);
        System.out.println(Arrays.deepToString(absorbProbs));

        int[] denoms = new int[absorbProbs.length];

        for (int i = 0; i < absorbProbs.length; i++) {
            denoms[i] = absorbProbs[i].denom;
        }

        int lcm = lcm(denoms);

        System.out.println(lcm);

        int[] ans = new int[absorbProbs.length + 1];

        for (int i = 0; i < absorbProbs.length; i++) {
            Fraction prob = absorbProbs[i];

            ans[i] = prob.numer * lcm / prob.denom;
        }

        ans[ans.length - 1] = lcm;

        System.out.println(Arrays.toString(ans));

        return ans;
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

    private static Fraction[][] invert(Fraction[][] matrix) {
        // Adapted from Ahmad, Farooq & Khan, Hamid. (2010). An Efficient and Simple
        // Algorithm for Matrix Inversion. IJTD. 1. 20-27. 10.4018/jtd.2010010102.

        Fraction[][] inverted = Arrays.stream(matrix).map(el -> el.clone()).toArray($ -> matrix.clone());

        for (int p = 0; p < inverted.length; p++) {
            Fraction pivot = inverted[p][p];

            if (Math.abs(pivot.toFloat()) < 1e-5) {
                throw new IllegalArgumentException("Invalid matrix: no inverse possible");
            }

            for (int i = 0; i < inverted.length; i++) {
                inverted[i][p] = inverted[i][p].divide(pivot).multiply(new Fraction(-1, 1));
            }

            for (int i = 0; i < inverted.length; i++) {
                if (i != p) {
                    for (int j = 0; j < inverted.length; j++) {
                        if (j != p) {
                            inverted[i][j] = inverted[i][j].add(inverted[p][j].multiply(inverted[i][p]));
                        }
                    }
                }
            }

            for (int j = 0; j < inverted.length; j++) {
                inverted[p][j] = inverted[p][j].divide(pivot);
            }

            inverted[p][p] = new Fraction(1, 1).divide(pivot);
        }

        return inverted;
    }

    private static Fraction[][] getCanonicalMatrix(Fraction[][] matrix) {
        Fraction[][] clone = Arrays.stream(matrix).map(el -> el.clone()).toArray($ -> matrix.clone());

        int[] absorbStates = getAbsorbStates(clone);

        Fraction[][] canonical = new Fraction[clone.length][clone[0].length];

        // Put the abosorbing states at the top of the matrix
        for(int i = 0; i < absorbStates.length; i++) {
            for(int j = 0; j < clone[0].length; j++) {
                canonical[i][j] = clone[absorbStates[i]][j];
            }
        }

        // Now list the remaining rows
        for(int i = 0, nextRow = absorbStates.length; i < clone.length; i++) {
            int match = i;
            
            if(! IntStream.of(absorbStates).anyMatch(x -> x == match)) {
                for(int j = 0; j < clone[0].length; j++) {
                    canonical[nextRow][j] = clone[i][j];
                }
                nextRow++;
            }
        }

        return canonical;
    }

    private static int[] getAbsorbStates(Fraction[][] transitionMatrix) {
        List<Integer> absorbStates = new ArrayList<>();

        for (int i = 0; i < transitionMatrix.length; i++) {
            if (transitionMatrix[i][i].numer == transitionMatrix[i][i].denom) {
                absorbStates.add(i);
            }
        }

        return absorbStates.stream().mapToInt(i -> i).toArray();
    }

    private static Fraction[] getAbsorbProbs(int initialState, int[] absorbStates,
            Fraction[][] solutionMatrix) {
        if (initialState >= solutionMatrix.length) {
            throw new IllegalArgumentException(
                    "Invalid initialState " + initialState + ". Valid values are 0.." + (solutionMatrix.length - 1));
        }

        Fraction[] stateProbabilities = solutionMatrix[initialState];
        List<Fraction> probabilities = new ArrayList<>();

        for (int state : absorbStates) {
            probabilities.add(stateProbabilities[state]);
        }

        return probabilities.stream().toArray(Fraction[]::new);
    }

    // Greatest common divisor
    private static int gcd(int n1, int n2) {
        if (n2 == 0) {
            return n1;
        }
        return gcd(n2, n1 % n2);
    }

    private static int lcm(int[] nums) {
        int ans = nums[0];

        for (int i = 1; i < nums.length; i++)
            ans = (nums[i] * ans) / gcd(nums[i], ans);

        return ans;
    }

    public static void main(String[] args) {
        testFractionMultiply();
        testFractionAdd();
        testMatrixMultiply();
        testMatrixInvert();
        testGetAbsorbStates();
        testGetAbsorbProbs();
        testGcd();
        testLcm();
        testGetCanonicalMatrix();

        // int[][] testCase1 = {
        // { 0, 2, 1, 0, 0 },
        // { 0, 0, 0, 3, 4 },
        // { 0, 0, 0, 0, 0 },
        // { 0, 0, 0, 0, 0 },
        // { 0, 0, 0, 0, 0 }
        // };

        // System.out.println(Arrays.toString(solution(testCase1)));

        int[][] testCase2 = {
                { 0, 1, 0, 0, 0, 1 },
                { 4, 0, 0, 3, 2, 0 },
                { 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0 }
        };

        // System.out.println(Arrays.toString(solution(testCase2)));
    }

    // Rought and ready unit tests

    public static void testFractionMultiply() {
        Fraction frac1 = new Fraction(4, 5);
        Fraction frac2 = new Fraction(9, 21);

        Fraction frac3 = frac1.multiply(frac2);

        // 26/105 simplifies to 12/35, so this tests the automatic simplification too
        if (!frac3.equals(new Fraction(36, 105))) {
            throw new AssertionError();
        }
    }

    public static void testFractionAdd() {
        Fraction frac1 = new Fraction(4, 5);
        Fraction frac2 = new Fraction(9, 21);

        Fraction frac3 = frac1.add(frac2);

        if (!frac3.equals(new Fraction(43, 35))) {
            throw new AssertionError();
        }
    }

    public static void testMatrixMultiply() {
        Fraction[][] matrix1 = {
                { new Fraction(5, 1), new Fraction(0, 1), new Fraction(7, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1) }
        };

        Fraction[][] matrix2 = {
                { new Fraction(1, 1), new Fraction(8, 1), new Fraction(3, 1) },
                { new Fraction(0, 1), new Fraction(1, 1), new Fraction(1, 1) },
                { new Fraction(3, 1), new Fraction(9, 1), new Fraction(1, 1) }
        };

        Fraction[][] ans = {
                { new Fraction(26, 1), new Fraction(103, 1), new Fraction(22, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1) },
                { new Fraction(3, 1), new Fraction(9, 1), new Fraction(1, 1) }
        };

        if (!Arrays.deepEquals(multiply(matrix1, matrix2), ans)) {
            throw new AssertionError();
        }
    }

    public static void testMatrixInvert() {
        Fraction[][] matrix = {
                { new Fraction(2, 1), new Fraction(1, 1), new Fraction(3, 1) },
                { new Fraction(1, 1), new Fraction(3, 1), new Fraction(-3, 1) },
                { new Fraction(-2, 1), new Fraction(4, 1), new Fraction(4, 1) }
        };

        Fraction[][] ans = {
                { new Fraction(3, 10), new Fraction(1, 10), new Fraction(-3, 20) },
                { new Fraction(1, 40), new Fraction(7, 40), new Fraction(9, 80) },
                { new Fraction(1, 8), new Fraction(-1, 8), new Fraction(1, 16) }
        };

        Fraction[][] inverted = invert(matrix);

        if (!Arrays.deepEquals(inverted, ans)) {
            throw new AssertionError();
        }
    }

    private static void testGetAbsorbStates() {
        Fraction[][] matrix = {
                { new Fraction(5, 1), new Fraction(0, 1), new Fraction(7, 1), new Fraction(7, 1), new Fraction(7, 1) },
                { new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(1, 1), new Fraction(7, 1) }
        };

        int[] ans = { 1, 3 };

        if (!Arrays.equals(getAbsorbStates(matrix), ans)) {
            throw new AssertionError();
        }
    }

    private static void testGetCanonicalMatrix() {
        // { 0, 2, 1, 0, 0 },
        // { 0, 0, 0, 3, 4 },
        // { 0, 0, 0, 0, 0 },
        // { 0, 0, 0, 0, 0 },
        // { 0, 0, 0, 0, 0 }

        Fraction[][] matrix = {
            { new Fraction(0, 1), new Fraction(2, 3), new Fraction(1, 3), new Fraction(0, 1), new Fraction(0, 1) },
            { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(4, 7) },
            { new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1), new Fraction(0, 1) },
            { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1) },
            { new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(1, 1), new Fraction(7, 1) }
        };

        Fraction[][] ans = {
            { new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1), new Fraction(0, 1) },
            { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1) },
            { new Fraction(0, 1), new Fraction(2, 3), new Fraction(1, 3), new Fraction(0, 1), new Fraction(0, 1) },
            { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(4, 7) },
            { new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(1, 1), new Fraction(7, 1) }
        };

        if(! Arrays.deepEquals(getCanonicalMatrix(matrix), ans)) {
            throw new AssertionError();
        }
    }

    private static void testGetAbsorbProbs() {
        Fraction[][] matrix1 = {
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(3, 4), new Fraction(1, 4) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(5, 7), new Fraction(2, 7) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1) }
        };

        int[] absorbStates = { 1, 2, 3 };

        Fraction[] absorbProbs = getAbsorbProbs(0, absorbStates, matrix1);
        Fraction[] ans = { new Fraction(0, 1), new Fraction(3, 4), new Fraction(1, 4) };

        if (!Arrays.deepEquals(absorbProbs, ans)) {
            throw new AssertionError();
        }
        ;
    }

    private static void testGcd() {
        int int1 = 63;
        int int2 = 117;

        if (gcd(int1, int2) != 9) {
            throw new AssertionError();
        }
    }

    private static void testLcm() {
        int[] nums = { 12, 15, 75 };

        if (lcm(nums) != 300) {
            throw new AssertionError();
        }
    }
}
