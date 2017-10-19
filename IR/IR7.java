package com.uc;// com.uc.IR7.java CS6054 2015 Cheng
// BIM ranking with pseudo relevance feedback
// Usage:  java com.uc.IR7 isrInvertedTf.txt isr4.txt
// Usage:  java com.uc.IR7 adInvertedTf.txt ad14.txt

import java.io.*;
import java.util.*;
import java.text.*;

public class IR7{

 static final int tops = 5;
 Scanner in = new Scanner(System.in);
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null;  // read in
 String[] titles = null; // read in
 int[] postingsLists = null; // read in
 int[] postings = null; // read in
 int[] dfs = null;  // read in
 double[] pt = null;
 double[] ut = null;
 double[] scores = null;
 int[] topDocs = new int[tops + 1];
 double[] topScores = new double[tops + 1];
 int numberOfTopDocs = 0;
 HashSet<Integer> topDocSet = new HashSet<Integer>();
 boolean changes = true;
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
    dfs = new int[numberOfTerms];
    postingsLists = new int[numberOfTerms + 1];
    postings = new int[numberOfIncidences];
    int n = 0;
    for (int i = 0; i < numberOfTerms; i++){
       postingsLists[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary[i] = tokens[0];
       dfs[i] = tokens.length / 2;
       for (int j = 0; j < dfs[i]; j++)
         postings[n++] = Integer.parseInt(tokens[2 * j + 1]);
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

  void initializePU(){
    pt = new double[numberOfTerms];
    ut = new double[numberOfTerms];
    for (int i = 0; i < numberOfTerms; i++){
       pt[i] = 0.5;
       ut[i] = (double)(dfs[i]) / numberOfDocs;
    }
    topDocSet.clear();
    changes = true;
  }

  void updatePU(){
    changes = false;
    for (int k = 0; k < numberOfTopDocs; k++)
     if (!topDocSet.contains(topDocs[k])){
        changes = true; break;
     }
    if (!changes) return;
    topDocSet.clear();
    for (int k = 0; k < numberOfTopDocs; k++)
      topDocSet.add(topDocs[k]);
    int lo = 0; int hi = 0;
    for (int termID = 0; termID < numberOfTerms; termID++){
      lo = hi; hi = postingsLists[termID + 1];
      int vt = 0;
      for (int j = lo; j < hi; j++) 
         if (topDocSet.contains(postings[j])) vt++;
        pt[termID] = (Math.abs(vt)+0.5)/(numberOfTopDocs+1);
        ut[termID] = (dfs[termID]-vt+0.5)/(numberOfDocs-numberOfTopDocs+1);
    }
  }

 void BIMScores(String query){
   for (int i = 0; i < numberOfDocs; i++) scores[i] = 0;
   String[] terms = query.split(" ");
   int len = terms.length;
   for (int j = 0; j < len; j++){
     int termID = find(terms[j], dictionary);
     if (termID >= 0){
         System.out.println(terms[j] + " " +
            decimalf.format(pt[termID]) + " " +
            decimalf.format(ut[termID]));
         double ct = Math.log(pt[termID]/(1-pt[termID]))+Math.log((1-ut[termID])/ut[termID]);
         for (int k = postingsLists[termID]; k < postingsLists[termID + 1]; k++)
          scores[postings[k]] += ct;
     }
    }
  }

 void answerQueries(){  // precondition: readInvertedIndex() done
   scores = new double[numberOfDocs];
   System.out.println("Enter a query (a number of words).");
   while (in.hasNextLine()){
     String query = in.nextLine();
     if (query.length() == 0) break;
     initializePU();
     int n = 0;
    while (changes){
     System.out.println("\nIteration " + ++n);
     BIMScores(query);
     retrieveByRanking();
     updatePU();
    }
    displayList();
     System.out.println("\nEnter a query or empty line for end.");
   }
 }



 void retrieveByRanking(){  // precondition: BIMScores(query) done
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
 }

 void displayList(){
     for (int i = 0; i < numberOfTopDocs; i++)
      System.out.println(titles[topDocs[i]] + " " +
              decimalf.format(topScores[i]));
 }


 public static void main(String[] args){
   IR7 ir7 = new IR7();
   ir7.readInvertedIndex(args[0]);
   ir7.readTitles(args[1]);
   ir7.answerQueries();
 }
}