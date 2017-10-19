package com.uc;// IR9.java CS6054 2015 Cheng
// MultinomialNB classifier learning
// If you enter the two inverted indexes as following, 
// Usage:  java IR9 adInvertedTf.txt isrInvertedTf.txt
// then C is the class of ad14.txt docs and -C that of isr4.txt
// all variables with trailing 1 are for C and those with 2 are for -C
// scores1[d][0] is the C score for a C doc d.
// scores1[d][1] is the -C score for a C doc d.
// scores2[d][0] is the C score for a -C doc d.
// scores2[d][1] is the -C score for a -C doc d.
// misclassification of d happens when scores1[d][0] < scores1[d][1]
// or scores2[d][0] > scores2[d][1].

import java.io.*;
import java.util.*;
import java.text.*;

public class IR9{

 static final double trainingProportion = 0.5;
 int numberOfTerms1 = 0, numberOfTerms2 = 0, numberOfTerms = 0;
 int numberOfDocs1 = 0, numberOfDocs2 = 0;
 int numberOfIncidences1 = 0, numberOfIncidences2 = 0;
 String[] dictionary1 = null, dictionary2 = null, dictionary = null; 
 int[] postingsLists1 = null, postingsLists2 = null;
 int[] postings1 = null, postings2 = null;
 int[] tfs1 = null, tfs2 = null;
 boolean[] training1 = null, training2 = null;
 int trainingSetSize1 = 0, trainingSetSize2 = 0;
 int[] trainingCfs1 = null, trainingCfs2 = null; 
 int trainingLength1 = 0, trainingLength2 = 0;
 double[][] scores1 = null, scores2 = null;     
 DecimalFormat decimalf = new DecimalFormat("#0.000");

 void readInvertedIndexes(String[] filenames){ // reading two indexes
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
    tfs1 = new int[numberOfIncidences1];
    postingsLists1 = new int[numberOfTerms1 + 1];
    postings1 = new int[numberOfIncidences1];
    int n = 0;
    for (int i = 0; i < numberOfTerms1; i++){
       postingsLists1[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary1[i] = tokens[0];
       int df = tokens.length / 2;
       for (int j = 0; j < df; j++){
         postings1[n] = Integer.parseInt(tokens[2 * j + 1]);
         tfs1[n] = Integer.parseInt(tokens[2 * j + 2]);
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
    tfs2 = new int[numberOfIncidences2];
    postingsLists2 = new int[numberOfTerms2 + 1];
    postings2 = new int[numberOfIncidences2];
    n = 0;
    for (int i = 0; i < numberOfTerms2; i++){
       postingsLists2[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary2[i] = tokens[0];
       int df = tokens.length / 2;
       for (int j = 0; j < df; j++){
         postings2[n] = Integer.parseInt(tokens[2 * j + 1]);
         tfs2[n] = Integer.parseInt(tokens[2 * j + 2]);
         n++;
       }
    }
    postingsLists2[numberOfTerms2] = n;
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

 void combineTerms(){ // making a combined dictionary with numberOfTerms
   TreeSet<String> terms = new TreeSet<String>();
   for (int i = 0; i < numberOfTerms1; i++) terms.add(dictionary1[i]);
   for (int i = 0; i < numberOfTerms2; i++) terms.add(dictionary2[i]);
   numberOfTerms = terms.size();
   dictionary = new String[numberOfTerms];
   int n = 0;
   for (String s : terms) dictionary[n++] = s;
 }

  void selectTrainingSet(){ 
 // randomly select about half (trainingProportion) of docs for training
 // and the other half for testing
 // trainingSetSize1 is Nc for C and trainingSetSize2 is Nc for -C
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

  void trainMultinomialNB(){  // Fig. 13.2 of iir, first algorithm
    trainingCfs1 = new int[numberOfTerms1]; // Tct for C
    int lo = 0; int hi = 0;
    for (int termID = 0; termID < numberOfTerms1; termID++){
      lo = hi; hi = postingsLists1[termID + 1];
      trainingCfs1[termID] = 0;
      for (int k = lo; k < hi; k++) if (training1[postings1[k]])
         trainingCfs1[termID] += tfs1[k];
      trainingLength1 += trainingCfs1[termID];  
       // trainingLength1 is sum of Tct for C, used in line 10 or (13.7) 
    }
    trainingCfs2 = new int[numberOfTerms2];
    lo = 0; hi = 0;
    for (int termID = 0; termID < numberOfTerms2; termID++){
      lo = hi; hi = postingsLists2[termID + 1];
      trainingCfs2[termID] = 0;
      for (int k = lo; k < hi; k++) if (training2[postings2[k]])
         trainingCfs2[termID] += tfs2[k];
      trainingLength2 += trainingCfs2[termID];
    }
  }

  void initializeScores(){ // line 3 of ApplyMultinomialNB
    scores1 = new double[numberOfDocs1][2];     
    scores2 = new double[numberOfDocs2][2]; 
  // compute logPrior[c]
    double trainingSetSize = trainingSetSize1 + trainingSetSize2;
    double logPrior1 = Math.log(trainingSetSize1 / trainingSetSize);
    double logPrior2 = Math.log(trainingSetSize2 / trainingSetSize);
    for (int i = 0; i < numberOfDocs1; i++){
       scores1[i][0] = logPrior1;    scores1[i][1] = logPrior2;
    }
    for (int i = 0; i < numberOfDocs2; i++){
       scores2[i][0] = logPrior1;    scores2[i][1] = logPrior2;
    }
  }

  void accumulateScores(){ // line 5 of ApplyMultinomialNB
    double logDenominator1 = 
      Math.log((double)(trainingLength1 + numberOfTerms));
    double logDenominator2 = 
      Math.log((double)(trainingLength2 + numberOfTerms));
    for (int termID = 0; termID < numberOfTerms; termID++){
      int termID1 = find(dictionary[termID], dictionary1);
      int termID2 = find(dictionary[termID], dictionary2);
      if (termID1 >= 0)
        for (int k = postingsLists1[termID1]; 
           k < postingsLists1[termID1 + 1]; k++){
// postings1[k] is a doc containing dictionary[termID] and we need to
// add log cond prob[termID][c] to scores1[doc][c]
// when c = C, this is log(cf + 1) - logDenominator1 as below
         scores1[postings1[k]][0] += 
             Math.log(trainingCfs1[termID1] + 1.0) - logDenominator1;
// when c = -C, this is log(cf + 1) - logDenominator2 if dictioary2 has term
// and is - logDenominator2 = log (1/denominator2) otherwise (cf = 0)
         scores1[postings1[k]][1] += termID2 >= 0 ?
             Math.log(trainingCfs2[termID2] + 1.0) - logDenominator2 :
             -logDenominator2;
        }
      if (termID2 >= 0)
        for (int k = postingsLists2[termID2]; 
           k < postingsLists2[termID2 + 1]; k++){
         scores2[postings2[k]][1] +=
                 Math.log(trainingCfs2[termID2] + 1.0) - logDenominator2;
         scores2[postings2[k]][0] += (termID1 >= 0 ?
                 Math.log(trainingCfs1[termID1] + 1.0) - logDenominator1 : -logDenominator1);
        }
    }
  }

  void classifyTestSamples(){ // line 6 of ApplyMultinomialNB
    int truePositives = 0, trueNegatives = 0, 
        falsePositives = 0, falseNegatives = 0;
   // match these four counts with the four increment statements below.
    for (int i = 0; i < numberOfDocs1; i++) if (!training1[i])
       if (scores1[i][0] >= scores1[i][1]) truePositives++;
       else falseNegatives++;
    for (int i = 0; i < numberOfDocs2; i++) if (!training2[i])
       if (scores2[i][0] >= scores2[i][1]) falsePositives++;
       else trueNegatives++;
    System.out.println(truePositives + " " + falsePositives);
    System.out.println(falseNegatives + " " + trueNegatives);
  }
    
  void applyMultinomialNB(){
    initializeScores();
    accumulateScores();
    classifyTestSamples();
  }

 public static void main(String[] args){
   IR9 ir9 = new IR9();
     String[] filename = new String[2];
     filename[0]= "adInvertedTf.txt";
     filename[1]= "isrInvertedTf.txt";
   ir9.readInvertedIndexes(filename);
   ir9.combineTerms();
   ir9.selectTrainingSet();
   ir9.trainMultinomialNB();
   ir9.applyMultinomialNB();
 }
}