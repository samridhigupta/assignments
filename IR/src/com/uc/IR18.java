package com.uc;// IR18.java CS6054 2015 Cheng
// quick shift and partitional clustering
// Usage:  java IR18 isrInvertedTf.txt isr4.txt

import java.io.*;
import java.util.*;

public class IR18{

 static final int desirableClusterSize = 100;
 static final int maxNumberOfClusters = 100;
 static final int tops = 10;
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null; 
 int[] postingsLists = null;
 int[] postings = null;
 double[] tfidfs = null;
 String[] titles = null;
 double[][] cosSim = null;
 double[] density = null;
 int[] parents = null;
 int[] subtreeSizes = null;
 int root = -1;

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

  void pairwise(){ // tfidf similarity between pairs of docs 
   cosSim = new double[numberOfDocs][numberOfDocs];
   for (int i = 0; i < numberOfDocs; i++)
     for (int j = i + 1; j < numberOfDocs; j++) cosSim[i][j] = 0;
   int lo = 0, hi = 0;
   for (int i = 0; i < numberOfTerms; i++){
     lo = hi; hi = postingsLists[i + 1];
     for (int j = lo; j < hi; j++) 
       for (int k = j + 1; k < hi; k++)
         cosSim[postings[j]][postings[k]] += tfidfs[j] * tfidfs[k];
   }
   for (int i = 0; i < numberOfDocs; i++){
     cosSim[i][i] = 1.0;
     for (int j = i + 1; j < numberOfDocs; j++)
        cosSim[j][i] = cosSim[i][j];
   }
 }

 void computeDensity(){  // P(i) = sum_j exp(-(1-cos(i,j)))
   density = new double[numberOfDocs];
   for (int i = 0; i < numberOfDocs; i++){
     density[i] = 0;
     for (int j = 0; j < numberOfDocs; j++)
       density[i] += Math.exp(cosSim[i][j] - 1.0);
   }
 }

// void quickShift(){ // parent of i is the closest neighbor with higher P
//  parents = new int[numberOfDocs];
//  for (int i = 0; i < numberOfDocs; i++){
//    parents[i] = -1; // only the doc with highest density will remain this
//    for (int j = 0; j < numberOfDocs; j++) if (density[j] > density[i])
//     // Your code to name the j with largest cosSim[j][i] as parents[i]
//    if (parents[i] < 0) root = i; // The only doc without a parent is root
//  }
// }
 void quickShift(){ // parent of i is the closest neighbor with higher P
    parents = new int[numberOfDocs];

    for (int i = 0; i < numberOfDocs; i++){
        parents[i] = -1; // only the doc with highest density will remain this
        double MaxCosSim = -1;
        for (int j = 0; j < numberOfDocs; j++) if (density[j] > density[i])
            // Your code to name the j with largest cosSim[j][i] as parents[i]
            if(cosSim[j][i] > MaxCosSim)
            {
                MaxCosSim = cosSim[j][i];
                parents[i] = j;
            }
        if (parents[i] < 0) root = i; // The only doc without a parent is root
    }
 }

 int subtree(int root){ // recursively traverses a subtree, return size
   int size = 1;
   for (int i = 0; i < numberOfDocs; i++) if (parents[i] == root)
     size += subtree(i);
   if (size >= desirableClusterSize){ // if subtree too big, cut it out
     parents[root] = -1; size = 0;
   }
   return size;
 }
 void showSubtree(int index, int depth){  // print titles with indentation
    int k = depth; for (; k >= 0; k--)
        System.out.print(" ");
    System.out.println(titles[index]);
    for (int j = 0; j < numberOfDocs; j++) if (parents[j] == index)
        showSubtree(j, depth + 1); // recursive call
 }
 void partition(){  // traverse all and each tree cut is a cluster
   subtree(root);
 }

 void showClusters(){  // when parents[i] is -1, its the root of a cluster
   int cluster = 1;
   for (int i = 0; i < numberOfDocs; i++) if (parents[i] < 0){
      System.out.println("Cluster #" + cluster++);
      showSubtree(i, 0);
      System.out.println();
   }
 }

 public static void main(String[] args){
  IR18 ir18 = new IR18();
  ir18.readInvertedIndex("isrInvertedTf.txt");
  ir18.readTitles("isr4.txt");
  ir18.normalizeVectors();
  ir18.pairwise();
  ir18.computeDensity();
  ir18.quickShift();
  ir18.partition();
  ir18.showClusters();
 }
}
