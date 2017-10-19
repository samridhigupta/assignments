package com.uc;// IR25A.java CS6054 2015 Cheng
// Making a term cooccurrence network 
// Using the Jaccard coefficient between terms considered as subsets of docs
// Each term takes at most five neighbors with the highest Jaccard coefficients
// Usage:  java IR25A adInvertedTf.txt > adNet.txt

import java.io.*;
import java.util.*;

public class IR25A{

 static final int maxDf = 100;
 static final int minDf = 10;
 static final int TOPS = 5;
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null; 
 int[] postingsLists = null;
 int[] postings = null;
 int numberOfUsefulTerms = 0;
 boolean[] usefulTerms = null;
 int[] usefulTermIndexes = null;
 int[][] termConfusion = null;

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
    postingsLists = new int[numberOfTerms + 1];
    postings = new int[numberOfIncidences];
    usefulTerms = new boolean[numberOfTerms];
    int n = 0;

    for (int i = 0; i < numberOfTerms; i++){
       postingsLists[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary[i] = tokens[0];
       int df = tokens.length / 2;
       if (df >= minDf && df <= maxDf){
         numberOfUsefulTerms++; usefulTerms[i] = true;
       }else usefulTerms[i] = false;
       for (int j = 0; j < df; j++)
         postings[n++] = Integer.parseInt(tokens[2 * j + 1]);
    }
    postingsLists[numberOfTerms] = n;
    in.close();
    n = 0;
    usefulTermIndexes = new int[numberOfUsefulTerms];
    for (int i = 0; i < numberOfTerms; i++) if (usefulTerms[i])
      usefulTermIndexes[n++] = i;
  }

 void computeTermConfusion(){
   int numberOfEdges = 0;
   termConfusion = new int[numberOfUsefulTerms][numberOfUsefulTerms];
   for (int i = 0; i < numberOfUsefulTerms; i++){
     int term1 = usefulTermIndexes[i];
     int lo = postingsLists[term1]; int hi = postingsLists[term1 + 1];
     termConfusion[i][i] = hi - lo;
     for (int j = 0; j < i; j++){
        termConfusion[i][j] = 0;  
        int term2 = usefulTermIndexes[j];
        for (int k = postingsLists[term2]; k < postingsLists[term2 + 1]; k++){
           int l = lo; for (; l < hi; l++) if (postings[l] == postings[k]) break;
           if (l < hi) termConfusion[i][j]++;
        }
        termConfusion[j][i] = termConfusion[i][j];
        if (termConfusion[i][j] > 0) numberOfEdges++;
     }
   }
 }

  void jaccard(){
   System.out.println(numberOfUsefulTerms);
   int[] tops = new int[TOPS + 1];   double[] topScores = new double[TOPS + 1];
   for (int i = 0; i < numberOfUsefulTerms; i++){
     System.out.print(dictionary[usefulTermIndexes[i]]);
     int numberOfRanked = 0;
     for (int j = 0; j < numberOfUsefulTerms; j++) if (j != i && termConfusion[i][j] > 0){
       double score = termConfusion[i][j] * 1.0 / 
        (termConfusion[i][i] + termConfusion[j][j] - termConfusion[i][j]);
       int k = numberOfRanked - 1; for (; k >= 0; k--)
          if (score > topScores[k]){ 
            topScores[k + 1] = topScores[k]; tops[k + 1] = tops[k];
          }else break;
       if (k < TOPS - 1){ topScores[k + 1] = score; tops[k + 1] = j; }
       if (numberOfRanked < TOPS) numberOfRanked++;
     }
     for (int k = 0; k < numberOfRanked; k++) System.out.print(" " + tops[k]);
     System.out.println();
   }
 }
    

 public static void main(String[] args){
   IR25A ir25 = new IR25A();
   ir25.readInvertedIndex("isrInvertedTf.txt");
   ir25.computeTermConfusion();
   ir25.jaccard();
 }
}