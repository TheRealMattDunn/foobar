package ionfluxrelabeling;

import java.util.Arrays;

public class Solution {
    public static int[] solution(int h, int[] q) {
        // Post-order traversal - root node is labeled last

        int[] parents = new int[q.length];

        for(int i = 0; i < q.length; i++) {
            int minIndex = 1;
            int maxIndex = getNumNodes(h);
            int nodeToFind = q[i];

            int parentNode = -1;
            
            // If the node we're looking for is greater than the max, then we just return -1
            if(nodeToFind < maxIndex) {
                int currentNode = maxIndex;

                // Loop until we find the node we're looking for
                while(currentNode != nodeToFind) {
                    parentNode = currentNode;

                    // Work out whether the node we're looking for is on the LHS or RHS of the tree - bisect the tree each pass
                    if(nodeToFind < ((parentNode - minIndex) / 2) + minIndex) { // LHS
                        currentNode = ((parentNode - minIndex) / 2) + minIndex - 1;
                        maxIndex = currentNode;
                    } else { // RHS
                        currentNode = currentNode - 1;
                        maxIndex = currentNode;
                        minIndex = ((parentNode - minIndex) / 2) + minIndex;
                    }
                }
            }

            parents[i] = parentNode;
        }

        return parents;
    }

    public static int getNumNodes(int h) {
        return (int)Math.pow(2, h) - 1;
    }

    public static void main(String[] args) {
        int[] testCase1 = {7, 3, 5, 1};
        System.out.println(Arrays.toString(solution(3, testCase1)));

        int[] testCase2 = {19, 14, 28};
        System.out.println(Arrays.toString(solution(5, testCase2)));
    }
}
