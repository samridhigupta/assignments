package com.uc;// IR25C.java CS6054 2015 Cheng
// personalized PageRank
// Usage:  java IR25C latent hidden

import java.io.*;
import java.util.*;

public class IR25C {

    static final int numberOfNeighbors = 5;
    static final int numberOfIterations = 1000;  // stochastic matrix self multiply 1000 times
    static final int TOPS = 20;
    static final double teleportRate = 0.1;  // alpha in book
    int numberOfTerms = 0;
    String[] dictionary = null;
    int[][] neighbors = null;
    double[] PageRank = null;
    int[] indegrees = null;

    void readNet(String filename) {
        Scanner in = null;
        try {
            in = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            System.err.println("not found");
            System.exit(1);
        }
        String[] tokens = in.nextLine().split(" ");
        numberOfTerms = Integer.parseInt(tokens[0]);
        dictionary = new String[numberOfTerms];
        indegrees = new int[numberOfTerms];
        for (int i = 0; i < numberOfNeighbors; i++) indegrees[i] = 0;
        neighbors = new int[numberOfTerms][numberOfNeighbors];
        for (int i = 0; i < numberOfTerms; i++) {
            tokens = in.nextLine().split(" ");
            dictionary[i] = tokens[0];
            for (int j = 0; j < numberOfNeighbors; j++) {
                neighbors[i][j] = Integer.parseInt(tokens[j + 1]);
                indegrees[neighbors[i][j]]++;
            }
        }
        in.close();
    }

    // binary search
    int find(String key) {
        int lo = 0;
        int hi = numberOfTerms - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int diff = key.compareTo(dictionary[mid]);
            if (diff == 0) return mid;
            if (diff < 0) hi = mid - 1;
            else lo = mid + 1;
        }
        return -1;
    }

    void powerMethod(String[] query) {
        int queryLen = query.length;
        int[] queryTerms = new int[queryLen];
        for (int i = 0; i < queryLen; i++) {
            queryTerms[i] = find(query[i]);
            if (queryTerms[i] < 0) {
                System.err.println(query[i] + " is not in dictionary.");
                System.exit(1);
            }
        }
        double initialPR = 1.0 / numberOfTerms * (1 - teleportRate);
        double weight = 1.0 / numberOfNeighbors * (1 - teleportRate);
        double queryWeight = teleportRate / queryLen;
        PageRank = new double[numberOfTerms];
        double[] newRank = new double[numberOfTerms];
        for (int i = 0; i < numberOfTerms; i++) PageRank[i] = initialPR;
        for (int iter = 0; iter < numberOfIterations; iter++) {
            // Your code to add queryWeight to PageRank of each query term
            for (int i = 0; i < queryLen; i++)
                PageRank[queryTerms[i]] += queryWeight;
            for (int i = 0; i < numberOfTerms; i++) newRank[i] = 0;
            for (int i = 0; i < numberOfTerms; i++)
                for (int j = 0; j < numberOfNeighbors; j++)
                    newRank[neighbors[i][j]] += PageRank[i] * weight;
            for (int i = 0; i < numberOfTerms; i++) PageRank[i] = newRank[i];
        }
    }

    void showTopPageRank() {
        int[] tops = new int[TOPS + 1];
        int numberOfRanked = 0;
        for (int i = 0; i < numberOfTerms; i++) {
            int k = numberOfRanked - 1;
            for (; k >= 0; k--)
                if (PageRank[i] > PageRank[tops[k]]) tops[k + 1] = tops[k];
                else break;
            if (k < TOPS - 1) tops[k + 1] = i;
            if (numberOfRanked < TOPS) numberOfRanked++;
        }
        for (int k = 0; k < TOPS; k++)
            System.out.println(dictionary[tops[k]] + " " + PageRank[tops[k]] + " " +
                    indegrees[tops[k]]);  // shows both PageRank and indegree
    }


    public static void main(String[] args) {
        IR25C ir25 = new IR25C();
        ir25.readNet("/Users/samridhi/workspace/IR/src/com/uc/isrInverted.txt"); // "isrNet.txt is the output from java IR25A isrInvertedTf.txt
        ir25.powerMethod("latent hidden".split(" ")); // args is the query
        ir25.showTopPageRank();
    }
}

