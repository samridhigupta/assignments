package com.uc;
// IR20.java CS6054 2015 Cheng
// latent Dirichlet allocation
// Usage:  java IR20 isrInvertedTf.txt

import java.io.*;
import java.util.*;

public class IR20{

 static final int numberOfTopics = 10;
 static final double alpha = 0.1;
 static final double gamma = 0.01;
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null; 
 int[] postingsLists = null;
 int[] postings = null;
 int[] tfs = null;
 int[] topicAssignment = null;
 int[] docLengths = null;
 int totalLength = 0;
 int[] topicSizes = new int[numberOfTopics];
 int[][] docTopicConfusion = null;
 int[][] topicTermConfusion = null;
 Random random = new Random();

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
    docLengths = new int[numberOfDocs];
    dictionary = new String[numberOfTerms];
    postingsLists = new int[numberOfTerms + 1];
    postings = new int[numberOfIncidences];
    tfs = new int[numberOfIncidences];
    for (int i = 0; i < numberOfDocs; i++) docLengths[i] = 0;
    int n = 0;
    for (int i = 0; i < numberOfTerms; i++){
       postingsLists[i] = n;
       tokens = in.nextLine().split(" ");
       dictionary[i] = tokens[0];
       int df = tokens.length / 2;
       for (int j = 0; j < df; j++){
         postings[n] = Integer.parseInt(tokens[2 * j + 1]);
         tfs[n] = Integer.parseInt(tokens[2 * j + 2]);
         docLengths[postings[n]] += tfs[n];
         n++;
       }
    }
    postingsLists[numberOfTerms] = n;
    in.close();
  }

 void initialize(){
   topicAssignment = new int[numberOfIncidences];
   for (int i = 0; i < numberOfIncidences; i++) 
     topicAssignment[i] = random.nextInt(numberOfTopics);
   docTopicConfusion = new int[numberOfDocs][numberOfTopics];
   topicTermConfusion = new int[numberOfTopics][numberOfTerms];
   for (int i = 0; i < numberOfDocs; i++) totalLength += docLengths[i];
 }

 void MStep(){
   for (int k = 0; k < numberOfTopics; k++){
      topicSizes[k] = 0;
      for (int i = 0; i < numberOfDocs; i++) docTopicConfusion[i][k] = 0;
      for (int i = 0; i < numberOfTerms; i++) topicTermConfusion[k][i] = 0;
   }
   int lo = 0, hi = 0;
   for (int i = 0; i < numberOfTerms; i++){
     lo = hi; hi = postingsLists[i + 1];
     for (int j = lo; j < hi; j++){
        topicSizes[topicAssignment[j]] += tfs[j];
        docTopicConfusion[postings[j]][topicAssignment[j]] += tfs[j];
        topicTermConfusion[topicAssignment[j]][i] += tfs[j];
     }
   }
  }

 void EStep(){
   int changes = 0;
   int lo = 0, hi = 0;
   for (int i = 0; i < numberOfTerms; i++){
     lo = hi; hi = postingsLists[i + 1];
     for (int j = lo; j < hi; j++){
       double maxStrength = 0;  int winner = -1;
       double topicStrength = 0;
       for (int k = 0; k < numberOfTopics; k++){
           if (k == topicAssignment[j]) {
               topicStrength =
                       ((topicTermConfusion[k][i] + gamma)-tfs[j])/((topicSizes[k] + numberOfTerms * gamma)-tfs[j])*((docTopicConfusion[postings[j]][k] + alpha)-tfs[j])/((docLengths[postings[j]] + numberOfTopics * alpha)-tfs[j]);
           }
           else
         topicStrength = 
          (topicTermConfusion[k][i] + gamma)
             / (topicSizes[k] + numberOfTerms * gamma)
          * (docTopicConfusion[postings[j]][k] + alpha)
          / (docLengths[postings[j]] + numberOfTopics * alpha);
         if (topicStrength > maxStrength){ 
            maxStrength = topicStrength; winner = k;
         }
       }
       if (winner != topicAssignment[j]){
          topicAssignment[j] = winner; changes++;
       }
     }
   }
   System.err.println(changes);
  }

  void LDA(){
    for (int iter = 0; iter < 30; iter++){
      MStep(); EStep();
    }
  }

  void display(){
    for (int i = 0; i < numberOfDocs; i++){
      for (int k = 0; k < numberOfTopics; k++) 
        System.out.print(docTopicConfusion[i][k] + " ");
      System.out.println();
    }
  }

  void computePurity(){
    double purity = 0;
    for (int k = 0; k < numberOfDocs; k++){
      int maxInter = 0;
      for (int l = 0; l < numberOfTopics; l++) 
        if (docTopicConfusion[k][l] > maxInter)
         maxInter = docTopicConfusion[k][l];
      purity += maxInter;
    }
    purity /= totalLength;
    System.out.println("Purity = " + purity);
  }

  void computePurity2(){ // based on two major topics for each doc
    double purity = 0;
    for (int k = 0; k < numberOfDocs; k++){
      int maxInter = 0; int second = 0;
      for (int l = 0; l < numberOfTopics; l++) 
        if (docTopicConfusion[k][l] > maxInter){
         second = maxInter;
         maxInter = docTopicConfusion[k][l];
        }else if (docTopicConfusion[k][l] > second)
         second = docTopicConfusion[k][l];
      purity += maxInter + second;
    }
    purity /= totalLength;
    System.out.println("Purity = " + purity);
  }
   

 public static void main(String[] args){
   IR20 ir20 = new IR20();
   ir20.readInvertedIndex("isrInvertedTf.txt");
   ir20.initialize();
   ir20.LDA();
   ir20.display();
   ir20.computePurity2();
 }
}
