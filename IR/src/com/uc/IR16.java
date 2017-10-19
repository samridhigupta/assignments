package com.uc;

// IR16.java CS6054 2015 Cheng
// EM clustering with BernoulliNB (16.16) and (16.17)
// Usage:  java IR16

import java.io.*;
import java.util.*;
import java.text.*;

public class IR16{
  static final int K = 3; // number of clusters
  static final double smoother = 0.0001;
  int N = 0; // number of docs
  int M = 0; // number of terms
  String[] dictionary = null;
  boolean[][] termDocMatrix = null;
  double[][] r = null;
  double[][] q = null;
  double[] alpha = new double[K];
  DecimalFormat decimalf = new DecimalFormat("#0.000");
  Random random = new Random();

  void readData(String filename){
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    TreeSet<String> tset = new TreeSet<String>();
    while (in.hasNextLine()){
      N++;
      String[] terms = in.nextLine().split(" ");
      for (String s: terms) tset.add(s);
    }
    in.close();
    M = tset.size();
    dictionary = new String[M];
    int l = 0;
    for (String s: tset) dictionary[l++] = s;
    termDocMatrix = new boolean[M][N];
    for (int m = 0; m < M; m++)
     for (int n = 0; n < N; n++) termDocMatrix[m][n] = false;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    int n = 0;
    while (in.hasNextLine()){
      String[] terms = in.nextLine().split(" ");
      for (String s: terms){
        int m = 0; for (; m < M; m++)
         if (dictionary[m].equals(s)) break;
        termDocMatrix[m][n] = true;
      }
      n++;
    }
    in.close();
  }

 void initialize(){  // for K = 2 and the example in IIR
    r = new double[N][K];
    q = new double[M][K];
    for (int n = 0; n < N; n++) for (int k = 0; k < K; k++) r[n][k] = 1.0 / K;
    r[5][0] = 1.0; r[6][1] = 1.0; r[5][1] = 0; r[6][0] = 0;
 }

 void randomInitialize(){
    r = new double[N][K];
    for (int n = 0; n < N; n++) for (int k = 0; k < K; k++) r[n][k] = 1.0 / K;
    q = new double[M][K];
    int[] seeds = new int[K];
    for (int k = 0; k < K; k++){
       seeds[k] = random.nextInt(N);
       System.out.print(seeds[k] + " ");
       for (int l = 0; l < K; l++) r[seeds[k]][l] = l == k ? 1.0 : 0;
    }
    System.out.println();
    boolean repeat = false;
    int k = 1; for (; k < K; k++){
     int l = 0; for (; l < k; l++) if (seeds[l] == seeds[k]) break;
     if (l < k) break;
    }
    if (k < K) randomInitialize();
 }

 void MStep(){  // (16.16) See Table 16.3 about using smoother = 0.0001
    for (int k = 0; k < K; k++){
      double sumOfR = 0;
      for (int m = 0; m < M; m++) q[m][k] = 0;
      for (int n = 0; n < N; n++){
         sumOfR += r[n][k]; // + smoother;
         for (int m = 0; m < M; m++)
           if (termDocMatrix[m][n]) q[m][k] += r[n][k]; // + smoother;
      }
      alpha[k] = sumOfR / N;
      for (int m = 0; m < M; m++) q[m][k] /= sumOfR;
    }
  }

 void EStep(){  // (16.17)
    for (int n = 0; n < N; n++){  // for each doc
      double sum = 0;  // accumulate for the denominator in (16.17)
      for (int k = 0; k < K; k++){  // for each cluster
        r[n][k] = alpha[k];  // the prior for cluster k
        for (int m = 0; m < M; m++) // for each term (tm in (16.17)
// Your code here
          r[n][k] *= termDocMatrix[m][n] ? q[m][k] : 1-q[m][k] ;
        sum += r[n][k];
      }
      for (int k = 0; k < K; k++) r[n][k] = r[n][k] / sum;
    }
 }

 void EM(){
   //initialize();  // may be replaced with randomInitialize()
   randomInitialize();
   for (int i = 0; i < 10; i++){
     System.out.println("\r\nIteration " + (i + 1));
     MStep(); EStep(); showR();
   }
   showQ();
 }

 void showR(){
  for (int n = 0; n < N; n++){
   for (int k = 0; k < K; k++) System.out.print(decimalf.format(r[n][k]) + " ");
   System.out.println();
  }
 }

 void showQ(){
  System.out.print("priors: ");
  for (int k = 0; k < K; k++) System.out.print(" " + decimalf.format(alpha[k]));
  System.out.println();
  for (int m = 0; m < M; m++){
   System.out.print(dictionary[m]);
   for (int k = 0; k < K; k++) System.out.print(" " + decimalf.format(q[m][k]));
   System.out.println();
  }
 }

 public static void main(String[] args){
   IR16 ir16 = new IR16();
   ir16.readData("isrInverted.txt");
   ir16.EM();
 }
}



