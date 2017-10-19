package com.uc;// IR14.java CS6054 2015 Cheng
// Rocchio classification with data reduction
// Usage:  java IR14 adInvertedTf.txt isrInvertedTf.txt

import java.io.*;
import java.util.*;

public class IR14{

 static final double trainingProportion = 0.5;
 static final double supportThreshold = 0;
 int numberOfTerms1 = 0, numberOfTerms2 = 0, numberOfTerms = 0;
 int numberOfDocs1 = 0, numberOfDocs2 = 0;
 int numberOfIncidences1 = 0, numberOfIncidences2 = 0;
 String[] dictionary1 = null, dictionary2 = null, dictionary = null; 
 int[] postingsLists1 = null, postingsLists2 = null;
 int[] postings1 = null, postings2 = null;
 double[] tfidf1 = null, tfidf2 = null;
 boolean[] training1 = null, training2 = null;
 int trainingSetSize1 = 0, trainingSetSize2 = 0;
 double[] centroid1 = null, centroid2 = null;
 double[][] scores1 = null, scores2 = null;     
 double[] w = null; double b = 0;
 double[] margins1 = null, margins2 = null;
 boolean[] support1 = null, support2 = null;
 double centroid1Squared = 0, centroid2Squared = 0;

 void readInvertedIndexes(String[] filenames){
    Scanner in = null;  
    try {
      in = new Scanner(new File(filenames[0]));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    String[] tokens = in.nextLine().split(" ");
    numberOfTerms1 = Integer.parseInt(tokens[0]);
    numberOfDocs1 = Integer.parseInt(tokens[1]);
    numberOfIncidences1 = Integer.parseInt(tokens[2]);
    dictionary1 = new String[numberOfTerms1];
    postingsLists1 = new int[numberOfTerms1 + 1];
    postings1 = new int[numberOfIncidences1];
    tfidf1 = new double[numberOfIncidences1];
    int n = 0;
    double logN = Math.log10((double)numberOfDocs1);

    for (int i = 0; i < numberOfTerms1; i++){
       postingsLists1[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary1[i] = tokens[0];
       int df = tokens.length / 2;
       double idf = logN - Math.log10((double)df);
       for (int j = 0; j < df; j++){
         postings1[n] = Integer.parseInt(tokens[2 * j + 1]);
         int tf = Integer.parseInt(tokens[2 * j + 2]);
         tfidf1[n] = (1.0 + Math.log10(tf)) * idf;
         n++;
       }
    }
    postingsLists1[numberOfTerms1] = n;
    in.close();
    try {
      in = new Scanner(new File(filenames[1]));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    tokens = in.nextLine().split(" ");
    numberOfTerms2 = Integer.parseInt(tokens[0]);
    numberOfDocs2 = Integer.parseInt(tokens[1]);
    numberOfIncidences2 = Integer.parseInt(tokens[2]);
    dictionary2 = new String[numberOfTerms2];
    postingsLists2 = new int[numberOfTerms2 + 1];
    postings2 = new int[numberOfIncidences2];
    tfidf2 = new double[numberOfIncidences2];
    logN = Math.log10((double)numberOfDocs2);
    n = 0;
    for (int i = 0; i < numberOfTerms2; i++){
       postingsLists2[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary2[i] = tokens[0];
       int df = tokens.length / 2;
       double idf = logN - Math.log10((double)df);
       for (int j = 0; j < df; j++){
         postings2[n] = Integer.parseInt(tokens[2 * j + 1]);
         int tf = Integer.parseInt(tokens[2 * j + 2]);
         tfidf2[n] = (1.0 + Math.log10(tf)) * idf;
         n++;
       }
    }
    postingsLists2[numberOfTerms2] = n;
    in.close(); 
 }

 void normalizeVectors(){
   double[] docLengths1 = new double[numberOfDocs1];
   for (int i = 0; i < numberOfDocs1; i++) docLengths1[i] = 0;
   for (int termID = 0; termID < numberOfTerms1; termID++)
    for (int k = postingsLists1[termID]; k < postingsLists1[termID + 1]; k++)
        docLengths1[postings1[k]] += tfidf1[k] * tfidf1[k];
   for (int i = 0; i < numberOfDocs1; i++) 
       docLengths1[i] = Math.sqrt(docLengths1[i]);
   int lo = 0; int hi = 0;
   for (int termID = 0; termID < numberOfTerms1; termID++){
     lo = hi; hi = postingsLists1[termID + 1];
     for (int k = lo; k < hi; k++) tfidf1[k] /= docLengths1[postings1[k]];
   }

   double[] docLengths2 = new double[numberOfDocs2];
   for (int i = 0; i < numberOfDocs2; i++) docLengths2[i] = 0;
   for (int termID = 0; termID < numberOfTerms2; termID++)
    for (int k = postingsLists2[termID]; k < postingsLists2[termID + 1]; k++)
        docLengths2[postings2[k]] += tfidf2[k] * tfidf2[k];
   for (int i = 0; i < numberOfDocs2; i++) 
       docLengths2[i] = Math.sqrt(docLengths2[i]);
   lo = 0; hi = 0;
   for (int termID = 0; termID < numberOfTerms2; termID++){
     lo = hi; hi = postingsLists2[termID + 1];
     for (int k = lo; k < hi; k++) tfidf2[k] /= docLengths2[postings2[k]];
   }
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

 void combineTerms(){
   TreeSet<String> terms = new TreeSet<String>();
   for (int i = 0; i < numberOfTerms1; i++) terms.add(dictionary1[i]);
   for (int i = 0; i < numberOfTerms2; i++) terms.add(dictionary2[i]);
   numberOfTerms = terms.size();
   dictionary = new String[numberOfTerms];
   int n = 0;
   for (String s : terms) dictionary[n++] = s;
 }

  void selectTrainingSet(){
    Random random = new Random();
    training1 = new boolean[numberOfDocs1];
    support1 = new boolean[numberOfDocs1];
    for (int i = 0; i < numberOfDocs1; i++){
        training1[i] = random.nextDouble() < trainingProportion;
        if (training1[i]) trainingSetSize1++;
        support1[i] = training1[i];
    }
    training2 = new boolean[numberOfDocs2];
    support2 = new boolean[numberOfDocs2];
    for (int i = 0; i < numberOfDocs2; i++){
        training2[i] = random.nextDouble() < trainingProportion;
        if (training2[i]) trainingSetSize2++;
        support2[i] = training2[i];
    }
  }

 void trainRocchio(){
   centroid1 = new double[numberOfTerms1];
   int lo = 0; int hi = 0;
   for (int termID = 0; termID < numberOfTerms1; termID++){
      lo = hi; hi = postingsLists1[termID + 1];
      centroid1[termID] = 0;
      for (int k = lo; k < hi; k++) if (support1[postings1[k]])
         centroid1[termID] += tfidf1[k];
      centroid1[termID] /= trainingSetSize1;
   }
   centroid2 = new double[numberOfTerms2];
   lo = 0; hi = 0;
   for (int termID = 0; termID < numberOfTerms2; termID++){
      lo = hi; hi = postingsLists2[termID + 1];
      centroid2[termID] = 0;
      for (int k = lo; k < hi; k++) if (support2[postings2[k]])
         centroid2[termID] += tfidf2[k];
      centroid2[termID] /= trainingSetSize2;
   }
 }  

  void computeWB(){ // w = c1 - c2
   w = new double[numberOfTerms];
   for (int termID = 0; termID < numberOfTerms; termID++){
      int termID1 = find(dictionary[termID], dictionary1);
      int termID2 = find(dictionary[termID], dictionary2);
      w[termID] = termID1 >= 0 ? (termID2 >= 0 ? 
          centroid1[termID1] - centroid2[termID2] : centroid1[termID1]) 
          : -centroid2[termID2];
   }
   centroid1Squared = 0; 
   for (int termID = 0; termID < numberOfTerms1; termID++)
     centroid1Squared += centroid1[termID] * centroid1[termID];
   centroid2Squared = 0; 
   for (int termID = 0; termID < numberOfTerms2; termID++)
     centroid2Squared += centroid2[termID] * centroid2[termID];

    b = 0.5 * (centroid1Squared - centroid2Squared);
  }  

  void computeMargins(){
    margins1 = new double[numberOfDocs1];
    for (int i = 0; i < numberOfDocs1; i++) margins1[i] = -b;
    for (int termID = 0; termID < numberOfTerms; termID++){
      int termID1 = find(dictionary[termID], dictionary1);
      if (termID1 >= 0) for (int k = postingsLists1[termID1]; 
        k < postingsLists1[termID1 + 1]; k++)
        margins1[postings1[k]] += tfidf1[termID1] * w[termID];
    }
    margins2 = new double[numberOfDocs2];
    for (int i = 0; i < numberOfDocs2; i++) margins2[i] = -b;
    for (int termID = 0; termID < numberOfTerms; termID++){
      int termID2 = find(dictionary[termID], dictionary2);
      if (termID2 >= 0) for (int k = postingsLists2[termID2]; 
        k < postingsLists2[termID2 + 1]; k++)
        margins2[postings2[k]] += tfidf2[termID2] * w[termID];
    }
  }

  void shrinkSupport(double threshold){
    int numberOfSupport1 = 0;  int numberOfSupport2 = 0;
    for (int i = 0; i < numberOfDocs1; i++){
      support1[i] = training1[i] && (margins1[i] < threshold);
      if (support1[i]) numberOfSupport1++;
    }
    for (int i = 0; i < numberOfDocs2; i++){
      support2[i] = training2[i] && (margins2[i] < threshold);
        // Your code
      if (support2[i]) numberOfSupport2++;
    }
    System.out.println(numberOfSupport1 + " " + trainingSetSize1);
    System.out.println(numberOfSupport2 + " " + trainingSetSize2);
  }

  void initializeScores(){
    scores1 = new double[numberOfDocs1][2];     
    scores2 = new double[numberOfDocs2][2]; 
    for (int i = 0; i < numberOfDocs1; i++)
       scores1[i][0] = scores1[i][1] = 0;
    for (int i = 0; i < numberOfDocs2; i++)
       scores2[i][0] = scores2[i][1] = 0;
  }

  void accumulateScores(){
    for (int termID = 0; termID < numberOfTerms; termID++){
      int termID1 = find(dictionary[termID], dictionary1);
      int termID2 = find(dictionary[termID], dictionary2);
      if (termID1 >= 0)
        for (int k = postingsLists1[termID1]; 
           k < postingsLists1[termID1 + 1]; k++){
         double diff = tfidf1[k] - centroid1[termID1];
         scores1[postings1[k]][0] += diff * diff;
         diff = termID2 >= 0 ? tfidf1[k] - centroid2[termID2] : tfidf1[k];
         scores1[postings1[k]][1] += diff * diff;
        }
      if (termID2 >= 0)
        for (int k = postingsLists2[termID2]; 
           k < postingsLists2[termID2 + 1]; k++){
         double diff = tfidf2[k] - centroid2[termID2];
         scores2[postings2[k]][1] += diff * diff;
         diff = termID1 >= 0 ? tfidf2[k] - centroid1[termID1] : tfidf2[k];
         scores2[postings2[k]][0] += diff * diff;
        }
    }
  }


  void classifyTestSamples(){
    int truePositives = 0, trueNegatives = 0, 
        falsePositives = 0, falseNegatives = 0;
    for (int i = 0; i < numberOfDocs1; i++) if (!training1[i])
       if (scores1[i][0] <= scores1[i][1]) truePositives++;
       else falseNegatives++;
    for (int i = 0; i < numberOfDocs2; i++) if (!training2[i])
       if (scores2[i][0] <= scores2[i][1]) falsePositives++;
       else trueNegatives++;
    System.out.println(truePositives + " " + falsePositives);
    System.out.println(falseNegatives + " " + trueNegatives);
  }
    
  void applyRocchio(){
    initializeScores();
    accumulateScores();
    classifyTestSamples();
  }

 public static void main(String[] args){
   IR14 ir14 = new IR14();
   ir14.readInvertedIndexes("adInvertedTf.txt isrInvertedTf.txt".split(" "));
   ir14.normalizeVectors();
   ir14.combineTerms();
   ir14.selectTrainingSet();
   ir14.trainRocchio();
   ir14.applyRocchio();
   ir14.computeWB();
   ir14.computeMargins();
   ir14.shrinkSupport(0.01);
   ir14.trainRocchio();
   ir14.applyRocchio();
 }
}
