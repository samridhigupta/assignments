package com.uc;// IR23A.java CS6054 2015 Cheng
// Restricted Boltzmann Machine (RBM) using faces800.txt
// Usage:  java IR23A

import java.io.*;
import java.util.*;

public class IR23A{

 static final int numberOfTopics = 64;  // number of bits in hash value
 static final int miniBatchSize = 100; // learning before updating W
 double stepSize = 0.1;  // to be decreased
 int numberOfUsefulTerms = 24 * 24 * 4;
 int picLen = 24 * 24;
 int numberOfDocs = 800;
 String[] pics = new String[numberOfDocs];
 int numberOfIncidences = 0;
 int[] documents = new int[numberOfDocs + 1];  // start of a doc in inverted inverted index
 int[] words = null;  // array for all words in all docs
 double[][] W = null;  // symmetric interaction between words and topics
 boolean[][] codes = null; // one 64-bit hash value for each document
 Random random = new Random();

 void readPics(String filename){
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    for (int i = 0; i < numberOfDocs; i++) pics[i] = in.nextLine();
    in.close();
  }

  void countOnes(){  // compute numberOfIncidences and allocate words[]
    int[] counts = new int[16];
    for (int i = 0; i < 16; i++) counts[i] = 0;
    for (int i = 0; i < numberOfDocs; i++){
      for (int j = 0; j < picLen; j++) {
          int c = pics[i].charAt(j);
          if (c < 'a') counts[c - '0']++;
          else {
              if (c != ' ')
                  counts[c - 'a' + 10]++;
          }
      }
    }
    numberOfIncidences = counts[1] + counts[2] + counts[4] + counts[8] +
     (counts[3] + counts[5] + counts[6] + counts[9] + counts[10] + counts[12]) * 2
     + (counts[7] + counts[11] + counts[13] + counts[14]) * 3 + counts[15] * 4;
    words = new int[numberOfIncidences];
  }

  void makePostings(){ // each pic is like a doc, each one is a word
    int n = 0;
    for (int i = 0; i < numberOfDocs; i++){
      documents[i] = n;
      int base = 0;
      for (int j = 0; j < picLen; j++){
        switch(pics[i].charAt(j)){
          case '1': words[n++] = base + 3; break;
          case '2': words[n++] = base + 2; break;
          case '3': words[n++] = base + 2; words[n++] = base + 3; break;
          case '4': words[n++] = base + 1; break;
          case '5': words[n++] = base + 1; words[n++] = base + 3; break;
          case '6': words[n++] = base + 1; words[n++] = base + 2; break;
          case '7': words[n++] = base + 2; words[n++] = base + 3;
                    words[n++] = base + 1; break;
          case '8': words[n++] = base; break;
          case '9': words[n++] = base; words[n++] = base + 3; break;
          case 'a': words[n++] = base; words[n++] = base + 2; break;
          case 'b': words[n++] = base + 2; words[n++] = base + 3;
                    words[n++] = base; break;
          case 'c': words[n++] = base; words[n++] = base + 1; break;
          case 'd': words[n++] = base + 1; words[n++] = base + 3;
                    words[n++] = base; break;
          case 'e': words[n++] = base + 1; words[n++] = base + 2;
                    words[n++] = base; break;
          case 'f': words[n++] = base + 1; words[n++] = base + 2;
                    words[n++] = base; words[n++] = base + 3; break;
          default: ;
        }
        base += 4;
      }
    }
    documents[numberOfDocs] = n;
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
   IR23A ir23 = new IR23A();
   ir23.readPics("faces800.txt");
   ir23.countOnes();
   ir23.makePostings();
   ir23.initialize();
   ir23.epochs();
   ir23.displayCodes();
 }
}
