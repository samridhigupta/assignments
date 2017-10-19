package com.uc;// IR13A.java CS6054 2015 Cheng
// kNN classification without feature selection
// Usage:  java IR13A adInvertedTf.txt isrInvertedTf.txt 

import java.io.*;
import java.util.*;

public class IR13A{

 static final double trainingProportion = 0.5;
 static final int K = 13;  // KNN's K
 int numberOfTerms1 = 0, numberOfTerms2 = 0, numberOfTerms = 0;
 int numberOfDocs1 = 0, numberOfDocs2 = 0;
 int numberOfIncidences1 = 0, numberOfIncidences2 = 0;
 String[] dictionary1 = null, dictionary2 = null, dictionary = null; 
 int[] postingsLists1 = null, postingsLists2 = null;
 int[] postings1 = null, postings2 = null;
 double[] tfidf1 = null, tfidf2 = null;
 boolean[] training1 = null, training2 = null;
 double[][] cosSim1 = null, cosSim2 = null, cosSim12 = null;
 int trainingSetSize1 = 0, trainingSetSize2 = 0;

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

    for (int i = 0; i < numberOfTerms1; i++){
       postingsLists1[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary1[i] = tokens[0];
       int df = tokens.length / 2;
       for (int j = 0; j < df; j++){
         postings1[n] = Integer.parseInt(tokens[2 * j + 1]);
         int tf = Integer.parseInt(tokens[2 * j + 2]);
         tfidf1[n] = 1.0 + Math.log10((double)tf);
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
    n = 0;
    for (int i = 0; i < numberOfTerms2; i++){
       postingsLists2[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary2[i] = tokens[0];
       int df = tokens.length / 2;
       for (int j = 0; j < df; j++){
         postings2[n] = Integer.parseInt(tokens[2 * j + 1]);
         int tf = Integer.parseInt(tokens[2 * j + 2]);
         tfidf2[n] = 1.0 + Math.log10((double)tf);
         n++;
       }
    }
    postingsLists2[numberOfTerms2] = n;
    in.close(); 
 }

 void normalizeVectors(){
   double[] docLengths1 = new double[numberOfDocs1];
   for (int i = 0; i < numberOfDocs1; i++) docLengths1[i] = 0;
   double[] docLengths2 = new double[numberOfDocs2];
   for (int i = 0; i < numberOfDocs2; i++) docLengths2[i] = 0;
   double logN = Math.log10((double)(numberOfDocs1 + numberOfDocs2));

   for (int termID = 0; termID < numberOfTerms; termID++){
     int termID1 = find(dictionary[termID], dictionary1);
     int termID2 = find(dictionary[termID], dictionary2);
     int df = 0;
     if (termID1 >= 0) df += 
        postingsLists1[termID1 + 1] - postingsLists1[termID1];
     if (termID2 >= 0) df += 
        postingsLists2[termID2 + 1] - postingsLists2[termID2];
     double idf = logN - Math.log10((double)df);

     if (termID1 >= 0) for (int k = postingsLists1[termID1]; 
       k < postingsLists1[termID1 + 1]; k++){
          tfidf1[k] *= idf;
          docLengths1[postings1[k]] += tfidf1[k] * tfidf1[k];
     }
     if (termID2 >= 0) for (int k = postingsLists2[termID2]; 
       k < postingsLists2[termID2 + 1]; k++){
          tfidf2[k] *= idf;
          docLengths2[postings2[k]] += tfidf2[k] * tfidf2[k];
     }
   }
   for (int i = 0; i < numberOfDocs1; i++) 
     docLengths1[i] = Math.sqrt(docLengths1[i]);
   for (int i = 0; i < numberOfDocs2; i++) 
       docLengths2[i] = Math.sqrt(docLengths2[i]);

   for (int termID = 0; termID < numberOfTerms; termID++){
     int termID1 = find(dictionary[termID], dictionary1);
     int termID2 = find(dictionary[termID], dictionary2);

     if (termID1 >= 0) for (int k = postingsLists1[termID1]; 
       k < postingsLists1[termID1 + 1]; k++)
          tfidf1[k] /= docLengths1[postings1[k]];
     if (termID2 >= 0) for (int k = postingsLists2[termID2]; 
       k < postingsLists2[termID2 + 1]; k++)
          tfidf2[k] /= docLengths2[postings2[k]];
   }

 }

 void pairwise(){ // tfidf similarity between pairs of docs
   cosSim1 = new double[numberOfDocs1][numberOfDocs1];
   for (int i = 0; i < numberOfDocs1; i++)
     for (int j = 0; j < numberOfDocs1; j++) cosSim1[i][j] = 0;
   cosSim2 = new double[numberOfDocs2][numberOfDocs2];
   for (int i = 0; i < numberOfDocs2; i++)
     for (int j = 0; j < numberOfDocs2; j++) cosSim2[i][j] = 0;
   cosSim12 = new double[numberOfDocs1][numberOfDocs2];
   for (int i = 0; i < numberOfDocs1; i++)
     for (int j = 0; j < numberOfDocs2; j++) cosSim12[i][j] = 0;

   for (int termID = 0; termID < numberOfTerms; termID++){
     int termID1 = find(dictionary[termID], dictionary1);
     int termID2 = find(dictionary[termID], dictionary2);
     if (termID1 >= 0 && termID2 >= 0)
       for (int j = postingsLists1[termID1]; 
           j < postingsLists1[termID1 + 1]; j++)
         for (int k = postingsLists2[termID2]; 
           k < postingsLists2[termID2 + 1]; k++)
          cosSim12[postings1[j]][postings2[k]] += tfidf1[j] * tfidf2[k];
     if (termID1 >= 0)
       for (int j = postingsLists1[termID1]; 
           j < postingsLists1[termID1 + 1]; j++)
         for (int k = j + 1; k < postingsLists1[termID1 + 1]; k++)
          cosSim1[postings1[j]][postings1[k]] += tfidf1[j] * tfidf1[k];
     if (termID2 >= 0)
       for (int j = postingsLists2[termID2]; 
           j < postingsLists2[termID2 + 1]; j++)
         for (int k = j + 1; k < postingsLists2[termID2 + 1]; k++)
          cosSim2[postings2[j]][postings2[k]] += tfidf2[j] * tfidf2[k];
   }

   for (int i = 0; i < numberOfDocs1; i++){
     cosSim1[i][i] = 1.0;
     for (int j = i + 1; j < numberOfDocs1; j++)
        cosSim1[j][i] = cosSim1[i][j];
   }
   for (int i = 0; i < numberOfDocs2; i++){
     cosSim2[i][i] = 1.0;
     for (int j = i + 1; j < numberOfDocs2; j++)
        cosSim2[j][i] = cosSim2[i][j];
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
    for (int i = 0; i < numberOfDocs1; i++){
        training1[i] = random.nextDouble() < trainingProportion;
        if (training1[i]) trainingSetSize1++;
    }
    training2 = new boolean[numberOfDocs2];
    for (int i = 0; i < numberOfDocs2; i++){
        training2[i] = random.nextDouble() < trainingProportion;
        if (training2[i]) trainingSetSize2++;
    }
  }


 void applyKNN(){  
   int[] nearest = new int[K + 1];
   boolean[] class1 = new boolean[K + 1];
   int oddKs = 7;
   int[] truePositives = new int[oddKs], trueNegatives = new int[oddKs], 
        falsePositives = new int[oddKs], falseNegatives = new int[oddKs];
   for (int i = 0; i < 7; i++)
      truePositives[i] = trueNegatives[i] = 
      falsePositives[i] = falseNegatives[i] = 0;
   for (int i = 0; i < numberOfDocs1; i++) if (!training1[i]){
     int numberOfResults = 0;
     for (int j = 0; j < numberOfDocs1; j++) if (j != i){
      int k = numberOfResults - 1; for (; k >= 0; k--)
         if (cosSim1[i][j] > (class1[k] ? cosSim1[i][nearest[k]]
             : cosSim12[i][nearest[k]])){  // insertion sort
            nearest[k + 1] = nearest[k];
            class1[k + 1] = class1[k];
         }else break;
      if (k < K - 1){ nearest[k + 1] = j; class1[k + 1] = true; }
      if (numberOfResults < K) numberOfResults++;
     }
     for (int j = 0; j < numberOfDocs2; j++){
      int k = numberOfResults - 1; for (; k >= 0; k--)
         if (cosSim12[i][j] > (class1[k] ? cosSim1[i][nearest[k]]
             : cosSim12[i][nearest[k]])){  // insertion sort
            nearest[k + 1] = nearest[k];
            class1[k + 1] = class1[k];
         }else break;
      if (k < K - 1){ nearest[k + 1] = j; class1[k + 1] = false; }
      if (numberOfResults < K) numberOfResults++;
     }
     int callYes = 0;
     for (int k = 0; k < oddKs; k++){
        if (k > 0 && class1[k * 2 - 1]) callYes++;
        if (class1[k * 2]) callYes++;
        if (callYes > k) truePositives[k]++;
        else falseNegatives[k]++;
     }
   }

   for (int i = 0; i < numberOfDocs2; i++) if (!training2[i]){
     int numberOfResults = 0;
     for (int j = 0; j < numberOfDocs1; j++){
      int k = numberOfResults - 1; for (; k >= 0; k--)
         if (cosSim12[j][i] > (class1[k] ? cosSim12[nearest[k]][i]
             : cosSim2[i][nearest[k]])){  // insertion sort
            nearest[k + 1] = nearest[k];
            class1[k + 1] = class1[k];
         }else break;
      if (k < K - 1){ nearest[k + 1] = j; class1[k + 1] = true; }
      if (numberOfResults < K) numberOfResults++;
     }
     for (int j = 0; j < numberOfDocs2; j++) if (j != i){
      int k = numberOfResults - 1; for (; k >= 0; k--)
         if (cosSim2[i][j] > (class1[k] ? cosSim12[nearest[k]][i]
             : cosSim2[i][nearest[k]])){  // insertion sort
            nearest[k + 1] = nearest[k];
            class1[k + 1] = class1[k];
         }else break;
      if (k < K - 1){ nearest[k + 1] = j; class1[k + 1] = false; }
      if (numberOfResults < K) numberOfResults++;
     }
     int callYes = 0;
     for (int k = 0; k < oddKs; k++){
        if (k > 0 && class1[k * 2 - 1]) callYes++;
        if (class1[k * 2]) callYes++;
        if (callYes > k) falsePositives[k]++;
        else trueNegatives[k]++;
     }
   }
   for (int k = 0; k < oddKs; k++){
    System.out.println("\nk = " + (k * 2 + 1));
    System.out.println(truePositives[k] + " " + falsePositives[k]);
    System.out.println(falseNegatives[k] + " " + trueNegatives[k]);
    double accuracy = (double)(truePositives[k]+trueNegatives[k])/(truePositives[k]+trueNegatives[k]+falseNegatives[k]+falsePositives[k]);
    System.out.println("Accuracy = " + accuracy);
   }
 }

 public static void main(String[] args){
   IR13A ir13 = new IR13A();
   ir13.readInvertedIndexes("adInvertedTf.txt isrInvertedTf.txt".split(" "));
   ir13.combineTerms();
   ir13.selectTrainingSet();
   ir13.normalizeVectors();
   ir13.pairwise();
   ir13.applyKNN();
 }
}