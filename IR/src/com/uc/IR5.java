package com.uc;

/**
 * Created by samridhi on 10/09/15.
 */
import java.io.*;
import java.util.*;
import java.text.*;

public class IR5{

    static final int numberOfPrecomputedLogTfs = 100;
    static final int tops1 = 5;  /* relevant based on tf-idf*/ static final int tops2 = 20; // top ranked based on w
    int numberOfTerms = 0;
    int numberOfDocs = 0;
    int numberOfIncidences = 0;
    String[] dictionary = null;  // read in
    int[] postingsLists = null; // read in
    int[] postings = null; // read in
    int[] tfs = null;  // read in
    double[] precomputedLogTfs = new double[numberOfPrecomputedLogTfs];
    double[] idfSquares = null; // precomputed
    double[] docLengths = null; // precomputed
    double[] scores1 = null; // tf-idf scores
    double[] scores2 = null;  // only w is used.
    int[] topDocs1 = new int[tops1 + 1];
    int[] topDocs2 = new int[tops2 + 1];
    double[] topScores1 = new double[tops1 + 1];
    double[] topScores2 = new double[tops2 + 1];
    int numberOfResults1 = 0;
    int numberOfResults2 = 0;
    DecimalFormat decimalf = new DecimalFormat("#0.000");

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

    // binary search
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


    void answerQueries(){  // precondition: readInvertedIndex() done
        scores1 = new double[numberOfDocs];
        scores2 = new double[numberOfDocs];
        Scanner in = new Scanner(System.in);
        System.out.println("Enter a query (a number of words).");
        while (in.hasNextLine()){
            String query = in.nextLine();
            if (query.length() == 0) break;
            cosineScore(query);
            retrieveByRanking();
            reportPrecisionRecallF1();
            System.out.println("Enter a query or empty line for end.");
        }
    }

    void cosineScore(String query){
        // precondition: readInvertIndex() done, uses binary search function find()
        for (int i = 0; i < numberOfDocs; i++) scores1[i] = scores2[i] = 0;
        String[] terms = query.split(" ");
        int len = terms.length;
        double queryLength = 0;
        for (int j = 0; j < len; j++){
            int termID = find(terms[j], dictionary);
            if (termID >= 0){
                queryLength += idfSquares[termID];
                for (int k = postingsLists[termID]; k < postingsLists[termID + 1]; k++){
                    double w = tfs[k] < numberOfPrecomputedLogTfs ?
                            precomputedLogTfs[tfs[k]] : 1.0 + Math.log10((double)(tfs[k]));
                    scores1[postings[k]] += w * idfSquares[termID];
                    scores2[postings[k]] += w;
                }
            }
        }
        queryLength = Math.sqrt(queryLength);
        for (int i = 0; i < numberOfDocs; i++)
            scores1[i] /= (queryLength * docLengths[i]);

    }

    void retrieveByRanking(){  // precondition: cosineScore(query) done
        numberOfResults1 = numberOfResults2 = 0;
        for (int i = 0; i < numberOfDocs; i++){
            if (scores1[i] > 0){
                int k = numberOfResults1 - 1; for (; k >= 0; k--)
                    if (scores1[i] > topScores1[k]){  // insertion sort
                        topDocs1[k + 1] = topDocs1[k];
                        topScores1[k + 1] = topScores1[k];
                    }else break;
                if (k < tops1 - 1){ topDocs1[k + 1] = i; topScores1[k + 1] = scores1[i]; }
                if (numberOfResults1 < tops1) numberOfResults1++;
            }
            if (scores2[i] > 0){
                int k = numberOfResults2 - 1; for (; k >= 0; k--)
                    if (scores2[i] > topScores2[k]){  // insertion sort
                        topDocs2[k + 1] = topDocs2[k];
                        topScores2[k + 1] = topScores2[k];
                    }else break;
                if (k < tops2 - 1){ topDocs2[k + 1] = i; topScores2[k + 1] = scores2[i]; }
                if (numberOfResults2 < tops2) numberOfResults2++;
            }
        }
    }

    void reportPrecisionRecallF1(){
// false positive rate = 1 - specificity, for ROC plot with recall
        int tp = 0;
        for (int i = 0; i < numberOfResults2; i++){
//   retrieved = i + 1, relevant = numberOfResult1
            int k = 0; for (; k < numberOfResults1; k++)
                if (topDocs2[i] == topDocs1[k]) break;
            if (k < numberOfResults1) tp++;
            double precision = (double)tp/(i+1);
            double recall = (double)tp/numberOfResults1;
            double f1 = 2.0 / ((1.0 / precision) + (1.0 / recall));
            double fpr = (double)(i + 1 - tp) / (numberOfResults2 - numberOfResults1);
            System.out.println(decimalf.format(precision) + "\t" +
                    decimalf.format(recall) + "\t" + decimalf.format(f1)
                    + "\t" + decimalf.format(fpr));
        }
    }

    public static void main(String[] args){
        IR5 ir5 = new IR5();
        ir5.readInvertedIndex("/Users/samridhi/workspace/IR/src/com/uc/isrInvertedTf.txt");
        ir5.precompute();
        ir5.answerQueries();
    }
}