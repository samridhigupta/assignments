package com.uc;

/**
 * Created by samridhi on 08/09/15.
 */
import java.io.*;
import java.util.*;
public class IR4B {
    static final int numberOfPrecomputedLogTfs = 100;
    static final int tops = 5;
    int numberOfTerms = 0;
    int numberOfDocs = 0;
    int numberOfIncidences = 0;
    String[] dictionary = null;  // read in
    String[] titles = null; // read in
    int[] postingsLists = null; // read in
    int[] postings = null; // read in
    int[] tfs = null;  // read in
    double[] precomputedLogTfs = new double[numberOfPrecomputedLogTfs];
    double[] idfSquares = null; // precomputed
    double[] docLengths = null; // precomputed
    double[] scores = null;

    void readInvertedIndex(String filename){
        Scanner in = null;
        try {
            in = new Scanner(new File(filename));
        } catch (FileNotFoundException e){
            System.err.println("not found");
            System.exit(1);
        }
        String[] tokens = in.nextLine().split(" ");
        numberOfTerms = Integer.parseInt(tokens[0]);
        numberOfDocs = Integer.parseInt(tokens[1]);
        numberOfIncidences = Integer.parseInt(tokens[2]);
        dictionary = new String[numberOfTerms];
        idfSquares = new double[numberOfTerms];
        postingsLists = new int[numberOfTerms + 1];
        postings = new int[numberOfIncidences];
        tfs = new int[numberOfIncidences];
        double logN = Math.log10((double)numberOfDocs);
        int n = 0;
        for (int i = 0; i < numberOfTerms; i++){
            postingsLists[i] = n;
            tokens = in.nextLine().split(" ");
            dictionary[i] = tokens[0];
            int df = tokens.length / 2;
            double idf = logN - Math.log10((double)df);
            idfSquares[i] = idf * idf;
            for (int j = 0; j < df; j++){
                postings[n] = Integer.parseInt(tokens[2 * j + 1]);
                tfs[n] = Integer.parseInt(tokens[2 * j + 2]);
                n++;
            }
        }
        postingsLists[numberOfTerms] = n;
        in.close();
    }

    void readTitles(String filename){
        Scanner in = null;
        try {
            in = new Scanner(new File(filename));
        } catch (FileNotFoundException e){
            System.err.println("not found");
            System.exit(1);
        }
        titles = new String[numberOfDocs];
        for (int i = 0; i < numberOfDocs; i++){
            titles[i] = in.nextLine();
            in.nextLine(); in.nextLine();
        }
        in.close();
    }

    void precompute(){
        for (int i = 1; i < numberOfPrecomputedLogTfs; i++)
            precomputedLogTfs[i] = 1.0 + Math.log10((double)i);
        docLengths = new double[numberOfDocs];
        for (int i = 0; i < numberOfDocs; i++) docLengths[i] = 0;
        for (int termID = 0; termID < numberOfTerms; termID++)
            for (int k = postingsLists[termID]; k < postingsLists[termID + 1]; k++){
                double w = tfs[k] < numberOfPrecomputedLogTfs ?
                        precomputedLogTfs[tfs[k]] : 1.0 + Math.log10((double)(tfs[k]));
                docLengths[postings[k]] += w * w * idfSquares[termID];
            }
        for (int i = 0; i < numberOfDocs; i++)
            docLengths[i] = Math.sqrt(docLengths[i]);
    }

    int find(String key, String[] array){
        int lo = 0; int hi = array.length - 1;
        while (lo <= hi){
            int mid = (lo + hi) / 2;
            int diff = key.compareTo(array[mid]);
            if (diff == 0) return mid;
            if (diff < 0) hi = mid - 1; else lo = mid + 1;
        }
        return -1;
    }

    void cosineScore(String query){
        // precondition: readInvertIndex() done, uses binary search function find()
        for (int i = 0; i < numberOfDocs; i++) scores[i] = 0;
        String[] terms = query.split(" ");
        int len = terms.length;
        double queryLength = 0;
        for (int j = 0; j < len; j++){
            int termID = find(terms[j], dictionary);
            if (termID >= 0){
                queryLength += idfSquares[termID];
                for (int k = postingsLists[termID]; k < postingsLists[termID + 1]; k++){
                    double w = tfs[k] < numberOfPrecomputedLogTfs ?
                            precomputedLogTfs[tfs[k]] : 1.0 + Math.log10((double)(tfs[k]));// wtd
                    scores[postings[k]] += w * idfSquares[termID];
                     // Your code to accumulate tf-idf scores to
                    //docId = postings[k]
                    //w *idfSquares
                    //w = wtd;
                    //idfSquare = wt;


                    // scores[docID] (what is docID?)
                    // Fig 6.14 line 6 ( w is part of wf(t,d) or w(t,d) )
                    // the similar part for the query term is 1 and the other part
                    // is idf of the term
                    /*
                    float Scores[N] = 0
Initialize Length[N]
for each query term t
do calculate wt,q and fetch postings list for t
for each pair(d,tft,d)inpostingslist
do Scores[d] += wft,d × wt,q Read the array Length[d]
foreachd
do Scores[d] = Scores[d]/Length[d] return Top K components of Scores[]
10

                     */
                }
            }
        }
        queryLength = Math.sqrt(queryLength);
        for (int i = 0; i < numberOfDocs; i++)
            scores[i] /= (queryLength * docLengths[i]);
    }

    void retrieveByRanking(){  // precondition: cosineScore(query) done
        int[] results = new int[tops + 1];
        double[] topScores = new double[tops + 1];
        int numberOfResults = 0;
        for (int i = 0; i < numberOfDocs; i++) if (scores[i] > 0){
            int k = numberOfResults - 1; for (; k >= 0; k--)
                if (scores[i] > topScores[k]){  // insertion sort
                    results[k + 1] = results[k];
                    topScores[k + 1] = topScores[k];
                }else break;
            if (k < tops - 1){ results[k + 1] = i; topScores[k + 1] = scores[i]; }
            if (numberOfResults < tops) numberOfResults++;
        }
        for (int i = 0; i < numberOfResults; i++)
            System.out.println(titles[results[i]] + " " + topScores[i]);
    }

    void answerQueries(){  // precondition: readInvertedIndex() done
        scores = new double[numberOfDocs];
        Scanner in = new Scanner(System.in);
        System.out.println("Enter a query (a number of words).");
        while (in.hasNextLine()){
            String query = in.nextLine();
            if (query.length() == 0) break;
            cosineScore(query);
            retrieveByRanking();
            System.out.println("Enter a query or empty line for end.");
        }
    }

    public static void main(String[] args){
        IR4B ir4 = new IR4B();
        ir4.readInvertedIndex("/Users/samridhi/workspace/IR/src/com/uc/isrInvertedTf.txt");
        ir4.readTitles("/Users/samridhi/workspace/IR/src/com/uc/isr4.txt");
        ir4.precompute();

        ir4.answerQueries();
    }
}