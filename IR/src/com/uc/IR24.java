package com.uc;// IR24.java CS6054 2015 Cheng
// HITS with query
// Usage example:  java IR24 representation learning  

import java.io.*;
import java.util.*;
import java.text.*;

public class IR24{

 static final int numberOfRounds = 20;
 static final int TOPS = 20;
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null; 
 int[] postingsLists = null;
 int[] postings = null;
 double[] tfidfs = null;
 String[] titles = null;
 double[] h = null;  // hub scores
 double[] a = null; // authority scores
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
    postingsLists = new int[numberOfTerms + 1];
    postings = new int[numberOfIncidences];
    tfidfs = new double[numberOfIncidences];
    int n = 0;
    double logN = Math.log10((double)numberOfDocs);

    for (int i = 0; i < numberOfTerms; i++){
       postingsLists[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary[i] = tokens[0];
       int df = tokens.length / 2;
       double idf = logN - Math.log10((double)df);
       for (int j = 0; j < df; j++){
         postings[n] = Integer.parseInt(tokens[2 * j + 1]);
         int tf = Integer.parseInt(tokens[2 * j + 2]);
         tfidfs[n] = (1.0 + Math.log10(tf)) * idf;
         n++;
       }
    }
    postingsLists[numberOfTerms] = n;
    in.close();
  }

   void readTitles(String filename){  // read doc titles
    Scanner in = null; 
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println(filename + "not found");
      System.exit(1);
    }
    titles = new String[numberOfDocs];
    for (int i = 0; i < numberOfDocs; i++){
      String line = in.nextLine();
      int pos = line.indexOf('\t') + 1;
      titles[i] = line.substring(pos);
      in.nextLine(); in.nextLine();
    }
    in.close();
  }

// binary search
 int find(String key){
   int lo = 0; int hi = numberOfTerms - 1;
   while (lo <= hi){
     int mid = (lo + hi) / 2;
     int diff = key.compareTo(dictionary[mid]);
     if (diff == 0) return mid;
     if (diff < 0) hi = mid - 1; else lo = mid + 1;
   }
   return -1;
 }


  void initialize(String[] query){
    a = new double[numberOfDocs];
    h = new double[numberOfTerms];
    for (int i = 0; i < numberOfDocs; i++) a[i] = -1.0;
    for (int j = 0; j < numberOfTerms; j++) h[j] = -1.0;
    for (int q = 0; q < query.length; q++){
       int t = find(query[q]);
       if (t >= 0)       
        for (int k = postingsLists[t]; k < postingsLists[t + 1]; k++)
         a[postings[k]] = 1.0;
    }
    for (int j = 0; j < numberOfTerms; j++)
      for (int k = postingsLists[j]; k < postingsLists[j + 1]; k++)
        if (a[postings[k]] > 0){ h[j] = 1.0;  break; }
  }

  void h2a(){ // a = Ah, A is tfidfs
    for (int i = 0; i < numberOfDocs; i++) if (a[i] >= 0) a[i] = 0;
    for (int j = 0; j < numberOfTerms; j++) if (h[j] > 0)
      for (int k = postingsLists[j]; k < postingsLists[j + 1]; k++)
         if (a[postings[k]] >= 0) a[postings[k]] += h[j] * tfidfs[k];
    double maxA = 0;
    for (int i = 0; i < numberOfDocs; i++) if (a[i] > maxA) maxA = a[i];
    for (int i = 0; i < numberOfDocs; i++) if (a[i] > 0) a[i] /= maxA;
    System.err.println(maxA);
  }

  void a2h(){ // h = A'a, A' is the transpose of A
    double maxH = 0;
    for (int j = 0; j < numberOfTerms; j++) if (h[j] >= 0){
      h[j] = 0;
      for (int k = postingsLists[j]; k < postingsLists[j + 1]; k++)
          if (a[postings[k]] >= 0)
              h[j] += tfidfs[k] * a[postings[k]]; // Your code, h[j] += ?
      if (h[j] > maxH) maxH = h[j];
    }
    for (int j = 0; j < numberOfTerms; j++) if (h[j] > 0) h[j] /= maxH;
    System.err.println(maxH);
  }

  void HITS(){
    for (int i = 0; i < numberOfRounds; i++){
      h2a(); a2h();
    }
  }

  void topDocs(){
    int[] tops = new int[TOPS + 1];
    int numberOfRanked = 0;
    for (int i = 0; i < numberOfDocs; i++) if (a[i] > 0){
      int k = numberOfRanked - 1; for (; k >= 0; k--)
         if (a[i] > a[tops[k]]) tops[k + 1] = tops[k];
         else break;
      if (k < TOPS - 1) tops[k + 1] = i;
      if (numberOfRanked < TOPS) numberOfRanked++;
    }
    for (int k = 0; k < numberOfRanked; k++)
     System.out.println(titles[tops[k]] + " " + decimalf.format(a[tops[k]]));
  }

  void topTerms(){
    int[] tops = new int[TOPS + 1];
    int numberOfRanked = 0;
    for (int i = 0; i < numberOfTerms; i++) if (h[i] > 0){
        int k = numberOfRanked - 1; for (; k >= 0; k--)
            if (h[i] > h[tops[k]]) tops[k + 1] = tops[k];
            else break;
        if (k < TOPS - 1) tops[k + 1] = i;
        if (numberOfRanked < TOPS) numberOfRanked++;
    }
    for (int k = 0; k < numberOfRanked; k++)
     System.out.println(dictionary[tops[k]] + " " + decimalf.format(h[tops[k]]));
  }



 public static void main(String[] args){
   IR24 ir24 = new IR24();
   ir24.readInvertedIndex("isrInvertedTf.txt");
   ir24.readTitles("isr4.txt");
   ir24.initialize("representation learning".split(" "));
   ir24.HITS();
   ir24.topDocs();
   ir24.topTerms();
 }
}