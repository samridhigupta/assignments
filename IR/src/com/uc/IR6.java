package com.uc;

/**
 * Created by samridhi on 15/09/15.
 */
// IR6.java CS6054 2015 Cheng
// Tf-idf ranking of documents for queries with query expansion
// Usage:  java IR6 isrInvertedTf.txt isr4.txt
// Usage:  java IR6 adInvertedTf.txt ad14.txt

import java.io.*;
        import java.util.*;

public class IR6{

    static final int numberOfPrecomputedLogTfs = 100;
    static final int tops = 5;
    Scanner in = new Scanner(System.in);
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
    int[] topDocs = new int[tops + 1];
    double[] topScores = new double[tops + 1];
    int numberOfTopDocs = 0;
    TreeMap<Integer, Integer> query = new TreeMap<Integer, Integer>();
    int[] relevantDocs = null;

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
        scores = new double[numberOfDocs];
        System.out.println("Enter a query (a number of words).");
        while (in.hasNextLine()){
            String line = in.nextLine();
            if (line.length() == 0) break;
            query.clear();  // query is a map of (termID, tf(in query))
            String[] terms = line.split(" ");
            int len = terms.length;
            for (int j = 0; j < len; j++){
                int termID = find(terms[j], dictionary);
                if (termID >= 0) if (query.containsKey(termID))
                    query.put(termID, query.get(termID) + 1);
                else query.put(termID, 1);
            }
            cosineScore();
            retrieveByRanking();
            System.out.println("\nWhich articles are relevent? type numbers 1 to "
                    + tops);
            IdeDecHi();
            cosineScore();
            retrieveByRanking();
            System.out.println("\nEnter a query or empty line for end.");
        }
    }

    void IdeDecHi(){
        relevantDocs = new int[numberOfDocs];
        // relevant = 1, nonrelevant = -1, others = 0
        markRelevance();
        RocchioAlgorithm();
    }

    void markRelevance(){  // get relevantDocs filled
        boolean hasNonrelevant = false;  // get the top-ranked nonrelevant doc
        boolean[] relevant = new boolean[tops];
        for (int i = 0; i < tops; i++) relevant[i] = false;
        for (int i = 0; i < numberOfDocs; i++) relevantDocs[i] = 0;
        String line = in.nextLine();  // read User's selection
        for (int i = 0; i < line.length(); i++)
            if (Character.isDigit(line.charAt(i)))
                relevant[line.charAt(i) - '1'] = true;
        for (int i = 0; i < tops; i++)
            if (relevant[i]) relevantDocs[topDocs[i]] = 1;
            else if (!hasNonrelevant){
                hasNonrelevant = true;
                relevantDocs[topDocs[i]] = -1;
            }
    }

    void RocchioAlgorithm(){
        // simply add/subtract term frequencies in each relevant/nonrelevant doc
        for (int termID = 0; termID < numberOfTerms; termID++)
            for (int k = postingsLists[termID]; k < postingsLists[termID + 1]; k++){
                int docID = postings[k];
                if(relevantDocs[docID]==1){
                    if (termID >= 0) if (query.containsKey(termID))
                        query.put(termID, query.get(termID) + tfs[k]);
                    else query.put(termID, tfs[k]);
                }else if(relevantDocs[docID]==-1){
                    if (termID >= 0) if (query.containsKey(termID))
                        query.put(termID, query.get(termID) -tfs[k]);
                    else query.put(termID, -tfs[k]);
                }
            }
    }


    void cosineScore(){
        // precondition: readInvertIndex() done, uses binary search function find()
        // scores may be negative now because qw may be negative
        for (int i = 0; i < numberOfDocs; i++) scores[i] = 0;
        double queryLength = 0;
        System.out.println("Number of terms in query: " + query.size());
        for (Map.Entry<Integer, Integer> m: query.entrySet()){
            int termID = m.getKey();
            int tf = m.getValue();
            double qw = tf < 0 ? -1.0 - Math.log10((double)(-tf)) :
                    tf < numberOfPrecomputedLogTfs ?
                            precomputedLogTfs[tf] : 1.0 + Math.log10((double)(tf));
            queryLength += qw * qw * idfSquares[termID];
            for (int k = postingsLists[termID]; k < postingsLists[termID + 1]; k++){
                double w = tfs[k] < numberOfPrecomputedLogTfs ?
                        precomputedLogTfs[tfs[k]] : 1.0 + Math.log10((double)(tfs[k]));
                scores[postings[k]] += w * qw * idfSquares[termID];
            }
        }
        queryLength = Math.sqrt(queryLength);
        for (int i = 0; i < numberOfDocs; i++)
            scores[i] /= (queryLength * docLengths[i]);

    }

    void retrieveByRanking(){  // precondition: cosineScore(query) done
        numberOfTopDocs = 0;
        for (int i = 0; i < numberOfDocs; i++) if (scores[i] > 0){
            int k = numberOfTopDocs - 1; for (; k >= 0; k--)
                if (scores[i] > topScores[k]){  // insertion sort
                    topDocs[k + 1] = topDocs[k];
                    topScores[k + 1] = topScores[k];
                }else break;
            if (k < tops - 1){ topDocs[k + 1] = i; topScores[k + 1] = scores[i]; }
            if (numberOfTopDocs < tops) numberOfTopDocs++;
        }
        for (int i = 0; i < numberOfTopDocs; i++)
            System.out.println((i + 1) + " " + titles[topDocs[i]] + " " + topScores[i]);
    }


    public static void main(String[] args){
        IR6 ir6 = new IR6();
        ir6.readInvertedIndex("/Users/samridhi/workspace/IR/src/com/uc/isrInvertedTf.txt");
        ir6.readTitles("/Users/samridhi/workspace/IR/src/com/uc/isr4.txt");
        ir6.precompute();
        ir6.answerQueries();
    }
}