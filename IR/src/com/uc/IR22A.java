package com.uc;// IR22A.java CS6054 2015 Cheng
// Restricted Boltzmann Machine (RBM)
// Usage:  java IR22A isrInvertedTf.txt

import java.io.*;
import java.util.*;

public class IR22A{

class IntInt implements Comparable{
  int a; int b;
  public IntInt(int aa, int bb){
    a = aa; b = bb;
  }
  public int compareTo(Object obj){
    IntInt ii = (IntInt)obj;
    int diff = a - ii.a;
    if (diff == 0) diff = b - ii.b;
    return diff;
  }
}

 static final int numberOfTopics = 64;  // number of bits in hash value
 static final int miniBatchSize = 100; // learning before updating W
 double stepSize = 0.1;  // to be decreased
 static final int maxDf = 100; // terms with df larger than this are not useful
 static final int minDf = 10;  // terms with df smaller than this are not useful
 int numberOfUsefulTerms = 0;  
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null; 
 boolean[] usefulTerms = null;  // selected terms with dfs within range
 int[] usefulTermIndex = null;  // mapping usefulTerms to terms
 int[] documents = null;  // start of a doc in inverted inverted index 
 int[] words = null;  // array for all words in all docs
 double[][] W = null;  // symmetric interaction between words and topics
 boolean[][] codes = null; // one 64-bit hash value for each document
 Random random = new Random();

 void readInvertedIndex(String filename){  // then invert it
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
    usefulTerms = new boolean[numberOfTerms];
    documents = new int[numberOfDocs + 1];
    int numberOfUsefulIncidences = 0;
    for (int i = 0; i < numberOfTerms; i++){
       tokens = in.nextLine().split(" ");
       dictionary[i] = tokens[0];
       int df = tokens.length / 2;
       if (df <= maxDf && df >= minDf){  // select useful terms
          numberOfUsefulTerms++; usefulTerms[i] = true; numberOfUsefulIncidences += df;
       }else usefulTerms[i] = false;
    }
    in.close();
    words = new int[numberOfUsefulIncidences];
    usefulTermIndex = new int[numberOfUsefulTerms];
    TreeSet<IntInt> tset = new TreeSet<IntInt>();
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    in.nextLine();
    int m = 0;
    for (int i = 0; i < numberOfTerms; i++){
       if (!usefulTerms[i]){ in.nextLine(); continue; }
       usefulTermIndex[m] = i;
       tokens = in.nextLine().split(" ");
       int df = tokens.length / 2;
       for (int j = 0; j < df; j++)  // sort <docID, usefulTermID>
         tset.add(new IntInt(Integer.parseInt(tokens[2 * j + 1]), m));
       m++;
    }
    in.close();
    int curDoc = -1;  // install inverted inverted index in arrays documents and words
    int n = 0;
    for (IntInt ii: tset){
      if (ii.a > curDoc){
        curDoc = ii.a;
        documents[curDoc] = n;
      }
      words[n++] = ii.b;
    }
  }

  double sigmoid(double eta){
   return 1.0 / (1.0 + Math.exp(-eta));
  }

 void initialize(){ // random initial values from [0, 1] for W
   W = new double[numberOfUsefulTerms][numberOfTopics];
   for (int i = 0; i < numberOfUsefulTerms; i++)
     for (int j = 0; j < numberOfTopics; j++)
        W[i][j] = random.nextDouble();
   codes = new boolean[numberOfDocs][numberOfTopics];
 }

 void runMiniBatch(int batch){  // accumulate gradient and update W
   double[][] batchGradient = new double[numberOfUsefulTerms][numberOfTopics];
   boolean[] topicOn = new boolean[numberOfTopics]; // hidden boolean values
   boolean[] fantasyData = new boolean[numberOfUsefulTerms]; // reconstructed words
   for (int i = 0; i < numberOfUsefulTerms; i++)
     for (int j = 0; j < numberOfTopics; j++)
        batchGradient[i][j] = 0;  // zeros gradient
   int minDoc = batch * miniBatchSize; // range of docs for the minibatch
   int maxDoc = minDoc + miniBatchSize;
   if (maxDoc > numberOfDocs) maxDoc = numberOfDocs;
   for (int docID = minDoc; docID < maxDoc; docID++){  // for each doc in minibatch
     for (int j = 0; j < numberOfTopics; j++){  // h = Wv (Fig.3) or (2)
        double sum = 0;
        for (int k = documents[docID]; k < documents[docID + 1]; k++)
          sum += W[words[k]][j];
        double h = sigmoid(sum);
        for (int k = documents[docID]; k < documents[docID + 1]; k++)
          batchGradient[words[k]][j] += h; // <v_i h_j>_data in (5)
        topicOn[j] = random.nextDouble() < h; // sample according to distribution h
     }
     for (int i = 0; i < numberOfUsefulTerms; i++){ // v2 = Wh (Fig.3) or (1)
        double sum = 0;
        for (int j = 0; j < numberOfTopics; j++) if (topicOn[j])
           sum += W[i][j];
        fantasyData[i] = random.nextDouble() < sigmoid(sum);
     }
     for (int j = 0; j < numberOfTopics; j++){ // <v_i h_j>_model in (5)
        double sum = 0;
        for (int i = 0; i < numberOfUsefulTerms; i++) if (fantasyData[i])
          sum += W[i][j];
        double h = sigmoid(sum);
        for (int i = 0; i < numberOfUsefulTerms; i++) if (fantasyData[i])
          batchGradient[i][j] -= h;  // to be subtracted from <v_i h_j>_data
     }
   }
   for (int i = 0; i < numberOfUsefulTerms; i++)  // W += delta W
     for (int j = 0; j < numberOfTopics; j++) if (batchGradient[i][j] != 0)
        W[i][j] += stepSize * batchGradient[i][j] / miniBatchSize;
 }
              

 void epochs(){  // If there is no time, reduce number of epochs to 10
   int numberOfBatches = numberOfDocs / miniBatchSize;
   for (int j = 0; j < 30; j++){
    System.err.println("epoch " + j);
    for (int i = 0; i < numberOfBatches; i++) runMiniBatch(i);
    evaluateCodes();  // this allows us to see the convergence
    stepSize *= 0.9;  // reduce stepsize after each epoch
   }
 }   

 void evaluateCodes(){
   int changes = 0;
   for (int docID = 0; docID < numberOfDocs; docID++)
     for (int j = 0; j < numberOfTopics; j++){
        double sum = 0;
        for (int k = documents[docID]; k < documents[docID + 1]; k++)
          sum += W[words[k]][j];
        boolean bit = sigmoid(sum) > 0.5;
        if (bit != codes[docID][j]){
            changes++;
            codes[docID][j] = bit;
        }
     }
   System.err.println(changes); // number of bits changed in hash values for all docs
 }

 void displayCodes(){  // print hash values in hex as codes to docs, to be used by IR22C
   for (int docID = 0; docID < numberOfDocs; docID++){
     int n = 0; String code = "";
     for (int i = 0; i < 16; i++){
       int b = 0;
       for (int j = 0; j < 4; j++){
           b <<= 1; if (codes[docID][n++]) b |= 1;
       }
      code += Integer.toHexString(b);
     }
     System.out.println(code);
   }
  }
      

 public static void main(String[] args){
   IR22A ir22 = new IR22A();
   ir22.readInvertedIndex("isrInvertedTf.txt");
   ir22.initialize();
   ir22.epochs();
   ir22.displayCodes();
 }
}
