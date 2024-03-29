package doomsdayfuel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Solution {

    private static class Fraction implements Cloneable {
        private long numer;
        private long denom;

        public Fraction(long numer, long denom) {
            long gcd = gcd(numer, denom);

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
            long numer = this.numer * that.numer;
            long denom = this.denom * that.denom;

            return new Fraction(numer, denom);
        }

        public Fraction divide(Fraction divisor) {
            Fraction inverseDivisor = new Fraction(divisor.denom, divisor.numer);
            return multiply(inverseDivisor);
        }

        public Fraction add(Fraction that) {
            long numer = (this.numer * that.denom) + (that.numer * this.denom);
            long denom = that.denom * this.denom;

            return new Fraction(numer, denom);
        }

        public Fraction subtract(Fraction that) {
            long numer = (this.numer * that.denom) - (that.numer * this.denom);
            long denom = that.denom * this.denom;

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
            long hash = 7;
            hash = 31 * hash + this.numer;
            hash = 31 * hash + this.denom;
            return Long.hashCode(hash);
        }

        @Override
        protected Fraction clone() throws CloneNotSupportedException {
            return new Fraction(numer, denom);
        }
    }

    private static Fraction[][] multiply(Fraction[][] a, Fraction[][] b) {
        if (a[0].length != b.length) {
            throw new IllegalArgumentException(
                    "Number of rows in a (" + a[0].length + ") must equal columns in b (" + b.length + ")");
        }

        Fraction[][] c = new Fraction[a.length][b[0].length];

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                c[i][j] = new Fraction(0, 1);
            }
        }

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < a[0].length; k++) {
                    c[i][j] = c[i][j].add(a[i][k].multiply(b[k][j]));
                }
            }
        }

        return c;
    }

    private static Fraction[][] subtract(Fraction[][] a, Fraction[][] b) {
        if (a.length != b.length || a[0].length != b[0].length) {
            throw new IllegalArgumentException("a and b different sizes");
        }

        Fraction[][] ans = new Fraction[a.length][a[0].length];

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                ans[i][j] = a[i][j].subtract(b[i][j]);
            }
        }

        return ans;
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

    private static Fraction[][] getBMatrix(Fraction[][] transitionMatrix) {
        int[] absorbStates = getAbsorbStates(transitionMatrix);

        List<List<Fraction>> bList = new ArrayList<>();

        for (int i = 0; i < transitionMatrix.length; i++) {
            int rowNum = i;

            if (!(IntStream.of(absorbStates).anyMatch(x -> x == rowNum))) {
                List<Fraction> row = new ArrayList<>();

                for (int j = 0; j < transitionMatrix[0].length; j++) {
                    int colNum = j;

                    if (!(IntStream.of(absorbStates).anyMatch(x -> x == colNum))) {
                        try {
                            row.add(transitionMatrix[i][j].clone());
                        } catch (CloneNotSupportedException exception) {
                            throw new AssertionError(exception);
                        }
                    }
                }

                bList.add(row);
            }
        }

        Fraction[][] b = new Fraction[bList.size()][];
        for (int i = 0; i < bList.size(); i++) {
            List<Fraction> row = bList.get(i);
            b[i] = row.toArray(new Fraction[row.size()]);
        }

        return b;
    }

    private static Fraction[][] getFundamentalMatrix(Fraction[][] bMatrix) {
        Fraction[][] identity = new Fraction[bMatrix.length][bMatrix[0].length];

        for (int i = 0; i < bMatrix.length; i++) {
            for (int j = 0; j < bMatrix[0].length; j++) {
                identity[i][j] = i == j ? new Fraction(1, 1) : new Fraction(0, 1);
            }
        }

        Fraction[][] inverseFundamental = subtract(identity, bMatrix);
        Fraction[][] fundamental = invert(inverseFundamental);

        return fundamental;
    }

    private static Fraction[][] getAMatrix(Fraction[][] transitionMatrix) {
        int[] absorbStates = getAbsorbStates(transitionMatrix);

        List<List<Fraction>> aList = new ArrayList<>();

        for (int i = 0; i < transitionMatrix.length; i++) {
            int rowNum = i;

            if (!(IntStream.of(absorbStates).anyMatch(x -> x == rowNum))) {
                List<Fraction> row = new ArrayList<>();

                for (int j = 0; j < transitionMatrix[0].length; j++) {
                    int colNum = j;

                    if ((IntStream.of(absorbStates).anyMatch(x -> x == colNum))) {
                        try {
                            row.add(transitionMatrix[i][j].clone());
                        } catch (CloneNotSupportedException exception) {
                            throw new AssertionError(exception);
                        }
                    }
                }

                aList.add(row);
            }
        }

        Fraction[][] a = new Fraction[aList.size()][];
        for (int i = 0; i < aList.size(); i++) {
            List<Fraction> row = aList.get(i);
            a[i] = row.toArray(new Fraction[row.size()]);
        }

        return a;
    }

    private static Fraction[][] getSolutionMatrix(Fraction[][] fundamentalMatrix, Fraction[][] aMatrix) {
        Fraction[][] solution = multiply(fundamentalMatrix, aMatrix);

        return solution;
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
    private static long gcd(long n1, long n2) {
        if (n2 == 0) {
            return n1;
        }
        return gcd(n2, n1 % n2);
    }

    private static long lcm(long[] nums) {
        long ans = nums[0];

        for (int i = 1; i < nums.length; i++)
            ans = (nums[i] * ans) / gcd(nums[i], ans);

        return ans;
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

        // Check for the case where all states are capturing - by comparing with the
        // identity matrix
        Fraction[][] identityMatrix = new Fraction[transMatrix.length][transMatrix[0].length];

        for (int i = 0; i < transMatrix.length; i++) {
            for (int j = 0; j < transMatrix[0].length; j++) {
                identityMatrix[i][j] = i == j ? new Fraction(1, 1) : new Fraction(0, 1);
            }
        }

        if (Arrays.deepEquals(transMatrix, identityMatrix)) {
            int[] ans = new int[m[0].length + 1];
            ans[0] = 1;
            ans[ans.length - 1] = 1;
            return ans;
        }

        Fraction[][] b = getBMatrix(transMatrix);
        Fraction[][] fundamental = getFundamentalMatrix(b);
        Fraction[][] a = getAMatrix(transMatrix);
        Fraction[][] solution = getSolutionMatrix(fundamental, a);

        Fraction[] absorbProbs = solution[0];
        long[] denoms = new long[absorbProbs.length];

        for (int i = 0; i < absorbProbs.length; i++) {
            denoms[i] = absorbProbs[i].denom;
        }

        long lcm = lcm(denoms);
        int[] ans = new int[absorbProbs.length + 1];

        for (int i = 0; i < absorbProbs.length; i++) {
            Fraction prob = absorbProbs[i];
            ans[i] = (int) (prob.numer * lcm / prob.denom);
        }

        ans[ans.length - 1] = (int) lcm;
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
        // testGetFundamentalMatrix();

        int[][] testCase1 = {
                { 0, 2, 1, 0, 0 },
                { 0, 0, 0, 3, 4 },
                { 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0 }
        };

        System.out.println(Arrays.toString(solution(testCase1)));

        int[][] testCase2 = {
                { 0, 1, 0, 0, 0, 1 },
                { 4, 0, 0, 3, 2, 0 },
                { 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0 }
        };

        System.out.println(Arrays.toString(solution(testCase2)));
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

    private static void testGetFundamentalMatrix() {
        // WARNING - this test currently fails

        Fraction[][] matrix = {
                { new Fraction(0, 1), new Fraction(2, 3), new Fraction(1, 3), new Fraction(0, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(1, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1), new Fraction(2, 3), new Fraction(1, 3) }
        };

        Fraction[][] ans = {
                { new Fraction(0, 1), new Fraction(2, 3), new Fraction(1, 1) },
                { new Fraction(0, 1), new Fraction(0, 1), new Fraction(0, 1) },
                { new Fraction(0, 1), new Fraction(2, 3), new Fraction(1, 3) },
        };

        System.out.println(Arrays.deepToString(getFundamentalMatrix(matrix)));

        if (!Arrays.deepEquals(getFundamentalMatrix(matrix), ans)) {
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
        long int1 = 63;
        long int2 = 117;

        if (gcd(int1, int2) != 9) {
            throw new AssertionError();
        }
    }

    private static void testLcm() {
        long[] nums = { 12, 15, 75 };

        if (lcm(nums) != 300) {
            throw new AssertionError();
        }
    }
}
