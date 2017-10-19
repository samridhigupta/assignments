package com.uc;// IR17.java CS6054 2015 Cheng
// simple hiearchical clustering
// Usage:  java IR17 adInvertedTf.txt ad14.txt

import java.io.*;
import java.util.*;

public class IR17{

 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null; 
 int[] postingsLists = null;
 int[] postings = null;
 double[] tfidfs = null;
 String[] titles = null;
 double[][] C = null;
 boolean[] I = null;
 int[][] mergers = null;
 int[] depths = null;

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
      String line = in.nextLine();
      int pos = line.indexOf('\t');
      titles[i] = line.substring(0, pos) +
                  " " + line.substring(pos + 1);
      in.nextLine(); in.nextLine();
    }
    in.close();
  }

 void normalizeVectors(){
   double[] docLengths = new double[numberOfDocs];
   for (int i = 0; i < numberOfDocs; i++) docLengths[i] = 0;
   int lo = 0, hi = 0;
   for (int termID = 0; termID < numberOfTerms; termID++){
     lo = hi; hi = postingsLists[termID + 1];
     for (int j = lo; j < hi; j++) 
        docLengths[postings[j]] += tfidfs[j] * tfidfs[j];
   }
   for (int i = 0; i < numberOfDocs; i++) 
       docLengths[i] = Math.sqrt(docLengths[i]);
   lo = 0; hi = 0;
   for (int termID = 0; termID < numberOfTerms; termID++){
     lo = hi; hi = postingsLists[termID + 1];
     for (int j = lo; j < hi; j++) tfidfs[j] /= docLengths[postings[j]];
   }
 }

  void pairwise(){ // tfidf similarity between pairs of docs for C
   C = new double[numberOfDocs][numberOfDocs];
   for (int i = 0; i < numberOfDocs; i++)
     for (int j = i + 1; j < numberOfDocs; j++) C[i][j] = 0;
   int lo = 0, hi = 0;
   for (int i = 0; i < numberOfTerms; i++){
     lo = hi; hi = postingsLists[i + 1];
     for (int j = lo; j < hi; j++) 
       for (int k = j + 1; k < hi; k++)
         C[postings[j]][postings[k]] += tfidfs[j] * tfidfs[k];
   }
   for (int i = 0; i < numberOfDocs; i++)
     for (int j = i + 1; j < numberOfDocs; j++)
        C[j][i] = C[i][j];
   
 }

 void simpleHAC(){  // Figure 17.2
   mergers = new int[numberOfDocs - 1][2];
   depths = new int[numberOfDocs - 1];
   I = new boolean[numberOfDocs];
   for (int i = 0; i < numberOfDocs; i++) I[i] = true;
   for (int iter = 0; iter < numberOfDocs - 1; iter++){
     double maxSim = -1.0; int topi = -1, topm = -1;
     for (int i = 0; i < numberOfDocs; i++) if (I[i])
       for (int m = i + 1; m < numberOfDocs; m++) if (I[m])
         if (C[i][m] > maxSim){ maxSim = C[i][m]; topi = i; topm = m; }
     mergers[iter][0] = topi; mergers[iter][1] = topm;
     depths[iter] = (int)(maxSim * 100);
     for (int j = 0; j < numberOfDocs; j++) if (I[j] && j != topi)
       C[j][topi] = C[topi][j] = sim(j, topi, topm);
     I[topm] = false;
   }
  }     

// The following sim is for complete-link
// you need to change this for single-link in the second run
 double sim(int i, int k1, int k2){ // Figure 17.8 complete-link
   return C[i][k1] >= C[i][k2] ? C[i][k1] : C[i][k2];
 }       

 void showHierarchy(){
   subtree(numberOfDocs - 2);
 }

 void subtree(int index){
   int k = index - 1; for (; k >= 0; k--)
     if (mergers[k][0] == mergers[index][0]) break;
   if (k < 0){
       int j = depths[index];
       for (; j >= 0; j--)
           System.out.print("\t");

       System.out.println(titles[mergers[index][0]]);
   }else subtree(k);
   k = index - 1; for (; k >= 0; k--)
     if (mergers[k][0] == mergers[index][1]) break;
   if (k < 0){
       int j = depths[index];
       for (; j >= 0; j--)
           System.out.print("\t");

       System.out.println(titles[mergers[index][1]]);
   }else subtree(k);
 }


 public static void main(String[] args){
  IR17 ir17 = new IR17();
  ir17.readInvertedIndex("isrInvertedTf.txt");
  ir17.readTitles("isr4.txt");
  ir17.normalizeVectors();
  ir17.pairwise();
  ir17.simpleHAC();
  ir17.showHierarchy();
 }
}
