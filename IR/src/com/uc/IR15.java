package com.uc;// IR15.java CS6054 2015 Cheng
// k-means clustering with comparison of clusterings
// Usage:  java IR15 isrInvertedTf.txt

import java.io.*;
import java.util.*;

public class IR15{

 static final int K = 13;  // k in k-means
 int numberOfTerms = 0;
 int numberOfDocs = 0;
 int numberOfIncidences = 0;
 String[] dictionary = null;
 int[] postingsLists = null;
 int[] postings = null;
 double[] tfidfs = null;
 double[][] centroids = null;
 int[] clusterAssignment = null;
 int[] clusterAssignment2 = null;
 int[] clusterSizes = new int[K];
 int[] clusterSizes2 = new int[K];
 Random random = new Random();
 int[][] confusion = new int[K][K];
 double H;
 double I;

 void readInvertedIndexes(String filename){
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
    clusterAssignment = new int[numberOfDocs];
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

 void selectRandomSeeds(){ // lines 1-3 of K-MEANS (Fig.16.5)
   int[] medoids = new int[K];
   for (int k = 0; k < K; k++) medoids[k] = random.nextInt(numberOfDocs);
   for (int i = 0; i < numberOfDocs; i++) clusterAssignment[i] = -1;
   for (int k = 0; k < K; k++) clusterAssignment[medoids[k]] = k;
   centroids = new double[K][numberOfTerms];
   int lo = 0, hi = 0;
   for (int termID = 0; termID < numberOfTerms; termID++){
     for (int k = 0; k < K; k++) centroids[k][termID] = 0;
     lo = hi; hi = postingsLists[termID + 1];
     for (int j = lo; j < hi; j++) if (clusterAssignment[postings[j]] >= 0)
       centroids[clusterAssignment[postings[j]]][termID] = tfidfs[j];
   }
for (int k = 0; k < K; k++) System.err.print(medoids[k] + " ");
System.err.println();
 }

 boolean partition(){  // lines 6-9 of K-MEANS (Fig. 16.5)
   boolean changes = false;  // return true when partition changes
   double[][] distances = new double[numberOfDocs][K];
   for (int i = 0; i < numberOfDocs; i++)
     for (int k = 0; k < K; k++) distances[i][k] = 0;
   for (int k = 0; k < K; k++) clusterSizes[k] = 0;
   int lo = 0, hi = 0;
   for (int termID = 0; termID < numberOfTerms; termID++){
     lo = hi; hi = postingsLists[termID + 1];
     for (int j = lo; j < hi; j++){
       int docID = postings[j];
       for (int k = 0; k < K; k++){
         double d = centroids[k][termID] - tfidfs[j];
         distances[docID][k] += d * d;
       }
     }
   }
   for (int i = 0; i < numberOfDocs; i++){
     int closest = -1;  double minDist = 10000.0;
     for (int k = 0; k < K; k++) if (distances[i][k] < minDist){
       minDist = distances[i][k]; closest = k;
     }
     if (closest != clusterAssignment[i]){
        changes = true; clusterAssignment[i] = closest;
     }
     clusterSizes[closest]++;
   }
   for (int k = 0; k < K; k++) System.err.print(clusterSizes[k] + " ");
   System.err.println();
   return changes;
 }

  void trainRacchio(){  // lines 10-11 of K-MEANS (Fig. 16.5)
   int lo = 0, hi = 0;
   for (int termID = 0; termID < numberOfTerms; termID++){
     for (int k = 0; k < K; k++) centroids[k][termID] = 0;
     lo = hi; hi = postingsLists[termID + 1];
     for (int j = lo; j < hi; j++)
       centroids[clusterAssignment[postings[j]]][termID] += tfidfs[j];
   }
   for (int termID = 0; termID < numberOfTerms; termID++)
     for (int k = 0; k < K; k++) centroids[k][termID] /= clusterSizes[k];
  }

  void kmeans(){ // Fig. 16.5
    selectRandomSeeds();
    while (partition()) trainRacchio();
  }

  void kmeansTwice(){  // first time results in clusterAssignment2, clusterSizes2
   kmeans();
   clusterAssignment2 = new int[numberOfDocs];
   for (int i = 0; i < numberOfDocs; i++)
     clusterAssignment2[i] = clusterAssignment[i];
   for (int k = 0; k < K; k++) clusterSizes2[k] = clusterSizes[k];
   kmeans();   // second time in clusterAssignment, clusterSizes
   for (int k = 0; k < K; k++) for (int l = 0; l < K; l++)
     confusion[k][l] = 0;
   for (int i = 0; i < numberOfDocs; i++)
        confusion[clusterAssignment2[i]][clusterAssignment[i]]++;
   for (int k = 0; k < K; k++){
     for (int l = 0; l < K; l++) System.out.print(confusion[k][l] + " ");
     System.out.println();
   }
  }

  void computePurity(){
    double purity = 0;
    for (int k = 0; k < K; k++){
      int maxInter = 0;
      for (int l = 0; l < K; l++) if (confusion[l][k] > maxInter)
         maxInter = confusion[l][k];
      purity += maxInter;
    }
    purity /= numberOfDocs;
    System.out.println("Purity = " + purity);
  }

void computeNMI(){  // (16.2-6)
	double H1 = 0;
	double H2 = 0;
	double MI = 0;
    double N = numberOfDocs;
    double[] Nkx = new double[K], Nxk = new double[K];
    for (int k = 0; k < K; k++){
       Nkx[k] = clusterSizes2[k]; Nxk[k] = clusterSizes[k];
	   for (int j = 0; j < K; j++){
		   if (confusion[k][j]>0) //picking up confusion matrix values which are > 0 to avoid going into NaN error
		    MI += (double)(confusion[k][j]/N)*(Math.log((N*confusion[k][j])/(Nkx[k]*Nxk[k])));
	   }
       H1 += (double)((Nkx[k]/N)*(Math.log((Nkx[k]/N))));// |wk| = Nkx[k], |cj| = Nxk[j] in 16.4 and 16.6
	   H2 += (double)((Nxk[k]/N)*(Math.log((Nxk[k]/N))));
    }
    // |wk n cj| = confusion[k][j] in 16.4
    // Your code for NMI
    double NMI = 0;
	H1 = (-1)*H1; H2 = (-1)*H2;
	NMI = MI/((H1+H2)/2);
    System.out.println("NMI = " + NMI);
  }

  void computeRIF5(){
    int tp = 0, tn = 0, fp = 0, fn = 0;
    int tpfp = 0, tpfn = 0;
    int numberOfPairs = numberOfDocs * (numberOfDocs - 1) / 2;
    for (int k = 0; k < K; k++) if (clusterSizes2[k] > 1)
      tpfn += clusterSizes2[k] * (clusterSizes2[k] - 1) / 2;
    for (int k = 0; k < K; k++) if (clusterSizes[k] > 1)
      tpfp += clusterSizes[k] * (clusterSizes[k] - 1) / 2;
    for (int k = 0; k < K; k++)
      for (int l = 0; l < K; l++) if (confusion[k][l] > 1)
         tp += confusion[k][l] * (confusion[k][l] - 1) / 2;
    fp = tpfp - tp;  fn = tpfn - tp;
    tn = numberOfPairs - tpfp - fn;
    System.out.println(tp + " " + fn);
    System.out.println(fp + " " + tn);
    double RI = 0;  double F5 = 0;
    RI=(double)(tp+tn)/(tp+tn+fp+fn);
	double precision = 0; double recall = 0;
	double beta = 5; //taking beta value as 5
	precision = (double)(tp)/(tp+fp);
	recall = (double)(tp)/(tp+fn);
	F5 = (double)(((beta*beta)+1)*precision*recall)/((beta*beta*precision)+recall);
// Your code for computing Rand index (RI) and F5 measure
    System.out.println("RI = " + RI);
    System.out.println("F5 = " + F5);
 }


  public static void main(String[] args){
   IR15 ir15 = new IR15();
   ir15.readInvertedIndexes("isrInvertedTf.txt");
   ir15.normalizeVectors();
   ir15.kmeansTwice();
   ir15.computePurity();
   ir15.computeNMI();
   ir15.computeRIF5();
  }
}



