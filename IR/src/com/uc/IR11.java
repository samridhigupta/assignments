package com.uc;// IR11.java CS6054 2015 Cheng
// BernoulliNB classifier learning odds version with feature selection
// Usage:  java IR11 adInvertedTf.txt isrInvertedTf.txt

import java.io.*;
import java.util.*;

public class IR11{

 static final double trainingProportion = 0.5;
 static final int tops = 10000;
 static final int[] featureSetSizes = new int[]{
    2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000 };
 int numberOfTerms1 = 0, numberOfTerms2 = 0, numberOfTerms = 0;
 int numberOfDocs1 = 0, numberOfDocs2 = 0;
 int numberOfIncidences1 = 0, numberOfIncidences2 = 0;
 String[] dictionary1 = null, dictionary2 = null, dictionary = null; 
 int[] postingsLists1 = null, postingsLists2 = null;
 int[] postings1 = null, postings2 = null;
 boolean[] training1 = null, training2 = null;
 int trainingSetSize1 = 0, trainingSetSize2 = 0;
 int[] trainingDfs1 = null, trainingDfs2 = null; 
 double[] odds1 = null, odds2 = null;  
 double[] MI = null;  
 double[] Chi2 = null;
 int[] topTerms = new int[tops + 1]; 

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
    int n = 0;
    for (int i = 0; i < numberOfTerms1; i++){
       postingsLists1[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary1[i] = tokens[0];
       int df = tokens.length / 2;
       for (int j = 0; j < df; j++)
         postings1[n++] = Integer.parseInt(tokens[2 * j + 1]);
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
    n = 0;
    for (int i = 0; i < numberOfTerms2; i++){
       postingsLists2[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary2[i] = tokens[0];
       int df = tokens.length / 2;
       for (int j = 0; j < df; j++)
         postings2[n++] = Integer.parseInt(tokens[2 * j + 1]);
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

  void trainBernoulliNB(){
    trainingDfs1 = new int[numberOfTerms1];
    int lo = 0; int hi = 0;
    for (int termID = 0; termID < numberOfTerms1; termID++){
      lo = hi; hi = postingsLists1[termID + 1];
      trainingDfs1[termID] = 0;
      for (int k = lo; k < hi; k++) if (training1[postings1[k]])
         trainingDfs1[termID]++;
    }
    trainingDfs2 = new int[numberOfTerms2];
    lo = 0; hi = 0;
    for (int termID = 0; termID < numberOfTerms2; termID++){
      lo = hi; hi = postingsLists2[termID + 1];
      trainingDfs2[termID] = 0;
      for (int k = lo; k < hi; k++) if (training2[postings2[k]])
         trainingDfs2[termID]++;
     }
    odds1 = new double[numberOfDocs1];     
    odds2 = new double[numberOfDocs2]; 
  }

  void computeFeatureUtility(){
    double Nx1 = trainingSetSize1, Nx0 = trainingSetSize2;
    double N = Nx1 + Nx0;
    double MIFactor = 1.0 / Math.log(2.0) / N;
    MI = new double[numberOfTerms];
    Chi2 = new double[numberOfTerms];
    double log2 = Math.log(2.0);
    for (int termID = 0; termID < numberOfTerms; termID++){
      int termID1 = find(dictionary[termID], dictionary1);
      int termID2 = find(dictionary[termID], dictionary2);
      double N11 = termID1 >= 0 ? trainingDfs1[termID1] : 0;
      double N10 = termID2 >= 0 ? trainingDfs2[termID2] : 0;
      double N01 = Nx1 - N11, N00 = Nx0 - N10;
      double N1x = N11 + N10, N0x = N01 + N00;
      MI[termID] = 0;
      if (N11 > 0) MI[termID] += N11 * Math.log(N * N11 / N1x / Nx1);  
      if (N01 > 0) MI[termID] += N01 * Math.log(N * N01 / N0x / Nx1);  
      if (N10 > 0) MI[termID] += N10 * Math.log(N * N10 / N1x / Nx0);  
      if (N00 > 0) MI[termID] += N00 * Math.log(N * N00 / N0x / Nx0); 
      MI[termID] *= MIFactor;
      if (N11 == 0 || N01 == 0 || N10 == 0 || N00 == 0) Chi2[termID] = 0;
      else{
          Chi2[termID] = (N11 + N10 + N01 + N00) * (Math.pow((N11 * N00) - (N10 * N01),2)) /
                  ((N11 + N01) * (N11 + N10) * (N10 + N00) * (N01 + N00));
      }
   }
  } 

 void rankTermsByMeasure(double[] measure){  
     int numberOfTopTerms = 0;
     for (int termID = 0; termID < numberOfTerms; termID++){
      int k = numberOfTopTerms - 1; for (; k >= 0; k--)
        if (measure[termID] > measure[topTerms[k]])
            topTerms[k + 1] = topTerms[k];
        else break;
      if (k < tops - 1) topTerms[k + 1] = termID;
      if (numberOfTopTerms < tops) numberOfTopTerms++;
     }
//    for (int i = 0; i < 100; i++)  // want to see the top terms?
//     System.out.println(dictionary[topTerms[i]] + " " + measure[topTerms[i]]);
 }

  void initializeOdds(int numberOfSelectedTerms){
// initialize all odds[d] to 
// log Nc - log Nc' - M (log(Nc + 2) - log(Nc' + 2))
//       + sum_t (log(Nc - Nct + 1) - log(Nc' - Nc't + 1)) 

   double defaultOdds = 
     Math.log((double)trainingSetSize1) - Math.log((double)trainingSetSize2)
       - numberOfSelectedTerms * (Math.log(trainingSetSize1 + 2.0)
          - Math.log(trainingSetSize2 + 2.0));
    for (int i = 0; i < numberOfSelectedTerms; i++){
      int termID = topTerms[i];
      int termID1 = find(dictionary[termID], dictionary1);
      int termID2 = find(dictionary[termID], dictionary2);
      defaultOdds += (termID1 >= 0 ?
         Math.log(trainingSetSize1 - trainingDfs1[termID1] + 1.0) :
         Math.log(trainingSetSize1 + 1.0))
        - (termID2 >= 0 ?
         Math.log(trainingSetSize2 - trainingDfs2[termID2] + 1.0) :
         Math.log(trainingSetSize2 + 1.0));
    }
    for (int i = 0; i < numberOfDocs1; i++) odds1[i] = defaultOdds; 
    for (int i = 0; i < numberOfDocs2; i++) odds2[i] = defaultOdds;
  }

  void accumulateOdds(int numberOfSelectedTerms){
// for all d  on postings list of term t,
// add log(Nct + 1) – log(Nc – Nct + 1) - log(Nc't + 1) + log(Nc' – Nc't + 1)
// to odds[d]

    for (int i = 0; i < numberOfSelectedTerms; i++){
      int termID = topTerms[i];
      int termID1 = find(dictionary[termID], dictionary1);
      int termID2 = find(dictionary[termID], dictionary2);
      double increment = (termID1 >= 0 ?
            Math.log(trainingDfs1[termID1] + 1.0) 
            - Math.log(trainingSetSize1 - trainingDfs1[termID1] + 1.0) :
            - Math.log(trainingSetSize1 + 1.0))
         - (termID2 >= 0 ?
            Math.log(trainingDfs2[termID2] + 1.0) 
            - Math.log(trainingSetSize2 - trainingDfs2[termID2] + 1.0) :
            - Math.log(trainingSetSize2 + 1.0));
      if (termID1 >= 0)
        for (int k = postingsLists1[termID1]; 
          k < postingsLists1[termID1 + 1]; k++)
           odds1[postings1[k]] += increment; 
      if (termID2 >= 0)
        for (int k = postingsLists2[termID2]; 
           k < postingsLists2[termID2 + 1]; k++)
           odds2[postings2[k]] += increment; 
    }
  }

  void classifyTestSamples(int numberOfSelectedTerms){
    int truePositives = 0, trueNegatives = 0, 
        falsePositives = 0, falseNegatives = 0;
    for (int i = 0; i < numberOfDocs1; i++) if (!training1[i])
       if (odds1[i] >= 0) truePositives++;
       else falseNegatives++;
    for (int i = 0; i < numberOfDocs2; i++) if (!training2[i])
       if (odds2[i] >= 0) falsePositives++;
       else trueNegatives++;
    int misclassified = falsePositives + falseNegatives;
    double accuracy = 1 - (double)misclassified /
       (misclassified + truePositives + trueNegatives); 
    System.out.println(numberOfSelectedTerms + " " + accuracy);
  }
    
  void applyBernoulliNB(){
    for (int i : featureSetSizes){
       initializeOdds(i);
       accumulateOdds(i);
       classifyTestSamples(i);
    }
  }

 public static void main(String[] args){
   IR11 ir11 = new IR11();
   ir11.readInvertedIndexes("adInvertedTf.txt isrInvertedTf.txt".split(" "));
   ir11.combineTerms();
   ir11.selectTrainingSet();
   ir11.trainBernoulliNB();
   ir11.computeFeatureUtility();
   ir11.rankTermsByMeasure(ir11.MI);
   ir11.applyBernoulliNB();
 }
}