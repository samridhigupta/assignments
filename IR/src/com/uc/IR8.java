package com.uc;// IR8.java CS6054 2015 Cheng
// Language models with four scoring systems
// Usage:  java IR8 isrInvertedTf.txt isr4.txt
// Usage:  java IR8 adInvertedTf.txt ad14.txt

import java.io.*;
import java.util.*;
import java.text.*;

public class IR8{

 static final int tops = 5;
 static final int numberOfMethods = 4;
 static final String[] methods = new String[]{ 
     "tf-idf variant ltn.bnn",
     "basic Okapi BM25", 
     "Jelinek-Mercer smoothing", 
     "Dirichlet smoothing"};
 static final double k1 = 0.5;  // k1 and b for basic BM25
 static final double b = 0.4;
 static final double lambda = 0.8; // for Jelinek-Mercer
 static final double alpha = 0.5;  // for Dirichlet
 Scanner in = new Scanner(System.in);
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null;  // read in
 String[] titles = null; // read in
 int[] postingsLists = null; // read in
 int[] postings = null; // read in
 int[] tfs = null;  // read in
 double[] idfs = null;
 short[] lds = null;
 double T = 0;
 double lave = 0;
 short[] cfs = null;
 double[][] scores = null;
 int[][] topDocs = new int[numberOfMethods][tops + 1];
 double[][] topScores = new double[numberOfMethods][tops + 1];
 int[] numberOfTopDocs = new int[numberOfMethods];
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
    tfs = new int[numberOfIncidences];
    postingsLists = new int[numberOfTerms + 1];
    postings = new int[numberOfIncidences];
    int n = 0;
    for (int i = 0; i < numberOfTerms; i++){
       postingsLists[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary[i] = tokens[0];
       int df = tokens.length / 2;
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

  void precompute(){
    double log10N = Math.log(numberOfDocs * 1.0);
    idfs = new double[numberOfTerms];
    lds = new short[numberOfDocs];
    cfs = new short[numberOfTerms];
    for (int i = 0; i < numberOfDocs; i++) lds[i] = 0;
    int lo = 0; int hi = 0;
    for (int termID = 0; termID < numberOfTerms; termID++){
      lo = hi; hi = postingsLists[termID + 1];
      cfs[termID] = 0;
      idfs[termID] = log10N - Math.log10((double)(hi - lo));
      int df = hi - lo;
      for (int j = lo; j < hi; j++){
         cfs[termID] += tfs[j];
         lds[postings[j]] += tfs[j];
      }
      T += cfs[termID];
    }
    lave = T / numberOfDocs;
  }

 void answerQueries(){  // precondition: readInvertedIndex() done
   scores = new double[numberOfMethods][numberOfDocs];
   System.out.println("Enter a query (a number of words).");
   while (in.hasNextLine()){
     String query = in.nextLine();
     if (query.length() == 0) break;
     LMScores(query);
     retrieveByRanking();
     displayList();
     System.out.println("\nEnter a query or empty line for end.");
   }
 }

 void LMScores(String query){
   for (int i = 0; i < numberOfDocs; i++) {
     scores[0][i] = scores[1][i] = 0;
     scores[2][i] = scores[3][i] = 1.0;
   }
   String[] terms = query.split(" ");
   int len = terms.length;
   for (int j = 0; j < len; j++){
     int termID = find(terms[j], dictionary);
     if (termID >= 0){
       // J-M and Dirichlet for docs with no query terms
       double jmC = (1 - lambda) * cfs[termID] / T;
       double dirichletC = (alpha * cfs[termID] / T);
       for (int i = 0; i < numberOfDocs; i++){
         scores[2][i] *= jmC; 
         scores[3][i] *= dirichletC / (lds[i] + alpha);
       }
       for (int k = postingsLists[termID];
                k < postingsLists[termID + 1]; k++){
           int docID = postings[k];  int ld = lds[docID];
           // tf-idf with no length normalization (ltn.bnn variant)
           scores[0][docID] +=
                   (1.0 + Math.log10((double)tfs[k])) * idfs[termID];
           // BM25, (11.32)
           scores[1][docID] += idfs[termID]*((k1+1)*tfs[k])/(k1*((1-b)+b*(ld/lave))+tfs[k]);
           // Jelinek-Mercer, (12.10)
           scores[2][docID] *= (lambda*(tfs[k]/ld)+jmC)/jmC;
           // Dirichlet, (12.11)
           scores[3][docID] *= (tfs[k]+dirichletC)/dirichletC;
       }      
     }
   }
 }           

 void retrieveByRanking(){  // precondition: LMScores(query) done
   for (int j = 0; j < numberOfMethods; j++){
     numberOfTopDocs[j] = 0;
     for (int i = 0; i < numberOfDocs; i++){
      int k = numberOfTopDocs[j] - 1; for (; k >= 0; k--)
         if (scores[j][i] > topScores[j][k]){  // insertion sort
            topDocs[j][k + 1] = topDocs[j][k];
            topScores[j][k + 1] = topScores[j][k];
         }else break;
      if (k < tops - 1){ topDocs[j][k + 1] = i; 
          topScores[j][k + 1] = scores[j][i]; 
      }
      if (numberOfTopDocs[j] < tops) numberOfTopDocs[j]++;
     }
   }
 }

 void displayList(){
    for (int j = 0; j < numberOfMethods; j++){
     System.out.println("\n" + methods[j] + ":");
     for (int i = 0; i < numberOfTopDocs[j]; i++)
      System.out.println(titles[topDocs[j][i]] + " " + 
         topScores[j][i]);
   }
 }


 public static void main(String[] args){
   IR8 ir8 = new IR8();
   ir8.readInvertedIndex("isrInvertedTf.txt");
   ir8.readTitles("isr4.txt");
   ir8.precompute();
   ir8.answerQueries();
 }
}