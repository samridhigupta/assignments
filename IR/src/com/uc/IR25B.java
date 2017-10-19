package com.uc;// IR25B.java CS6054 2015 Cheng
// PageRank with power method on a network fro IR25A
// Usage:  java IR25B isrNet.txt

import java.io.*;
import java.util.*;

public class IR25B{

 static final int numberOfNeighbors = 5;
 static final int numberOfIterations = 1000;
 static final int TOPS = 20;
 int numberOfTerms = 0;
 String[] dictionary = null; 
 int[][] neighbors = null;
 double[] PageRank = null;
 int[] indegrees = null;

 void readNet(String filename){
    Scanner in = null;  
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    String[] tokens = in.nextLine().split(" ");
    numberOfTerms = Integer.parseInt(tokens[0]);
    dictionary = new String[numberOfTerms];
    indegrees = new int[numberOfTerms];
    for (int i = 0; i < numberOfNeighbors; i++) indegrees[i] = 0;
    neighbors = new int[numberOfTerms][numberOfNeighbors];
    for (int i = 0; i < numberOfTerms; i++){
       tokens = in.nextLine().split(" ");
       dictionary[i] = tokens[0];
       for (int j = 0; j < numberOfNeighbors; j++){
         neighbors[i][j] = Integer.parseInt(tokens[j + 1]);
         indegrees[neighbors[i][j]]++;
       }
    }
    in.close();
  }

 void powerMethod(){
    double initialPR = 1.0 / numberOfTerms;
    double weight = 1.0 / numberOfNeighbors;
    PageRank = new double[numberOfTerms];
    double[] newRank = new double[numberOfTerms];
    for (int i = 0; i < numberOfTerms; i++) PageRank[i] = initialPR;
    for (int iter = 0; iter < numberOfIterations; iter++){
      for (int i = 0; i < numberOfTerms; i++) newRank[i] = 0;
      for (int i = 0; i < numberOfTerms; i++)
        for (int j = 0; j < numberOfNeighbors; j++)
           newRank[neighbors[i][j]] += PageRank[i] * weight;
      for (int i = 0; i < numberOfTerms; i++) PageRank[i] = newRank[i];
    }
 }
      
 void showTopPageRank(){
   int[] tops = new int[TOPS + 1];
   int numberOfRanked = 0;
   for (int i = 0; i < numberOfTerms; i++){
     int k = numberOfRanked - 1; for (; k >= 0; k--)
      if (PageRank[i] > PageRank[tops[k]]) tops[k + 1] = tops[k];
      else break;
     if (k < TOPS - 1) tops[k + 1] = i;
     if (numberOfRanked < TOPS) numberOfRanked++;
   }
   for (int k = 0; k < TOPS; k++) 
     System.out.println(dictionary[tops[k]] + " " + PageRank[tops[k]] + " " + 
      indegrees[tops[k]]);
 }


 public static void main(String[] args){
   IR25B ir25 = new IR25B();
   ir25.readNet("/Users/samridhi/workspace/IR/src/com/uc/isrInverted.txt");
   ir25.powerMethod();
   ir25.showTopPageRank();
 }
}

