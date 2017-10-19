package com.uc;
// IR21B.java CS6054 2015 Cheng
// Jaccard coeff between shingle sets as near-duplicate detection
// Usage: java IR21B isrShingles.txt

import java.io.*;
import java.util.*;

public class IR21B{

  static final int HashRange = 1048576;
  static final int numberOfPermutations = 200;
  static final int threshold = 160;  // Jaccard coefficient of 0.8
  int numberOfDocs = 0;
  int numberOfShingleDocs = 0;
  int[] docStarts = null;
  int[] shingles = null;
  int[] smallestShingles = null;
  int[][] sketchIntersections = null;
  Integer[] permutation = null;

  void readShingles(String filename){
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println(e.getMessage());
      System.exit(1);
    }
    while (in.hasNextLine()){
      String[] shingles = in.nextLine().split(" ");
      numberOfDocs++;
      numberOfShingleDocs += shingles.length;
    }
    in.close();
    docStarts = new int[numberOfDocs + 1];
    shingles = new int[numberOfShingleDocs];
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println(e.getMessage());
      System.exit(1);
    }
    int n = 0;
    for (int i = 0; i < numberOfDocs; i++){
      docStarts[i] = n;
      String[] terms = in.nextLine().split(" ");
      for (String s: terms) shingles[n++] = Integer.parseInt(s);
    }
    in.close();
    docStarts[numberOfDocs] = n;
  }

  void initialize(){
    permutation = new Integer[HashRange];
    for (int i = 0; i < HashRange; i++) permutation[i] = i;
    smallestShingles = new int[numberOfDocs];
    sketchIntersections = new int[numberOfDocs][numberOfDocs];
    for (int i = 0; i < numberOfDocs; i++)
      for (int j = 0; j < i; j++) sketchIntersections[i][j] = 0;
  }

  void permute(){
    List<Integer> l = Arrays.asList(permutation);
    java.util.Collections.shuffle(l);
    l.toArray(permutation);
  }

  void findSmallestShingles(){
	    int lo = 0, hi = 0;
	    for (int i = 0; i < numberOfDocs; i++){
	      lo = hi; hi = docStarts[i + 1];
	      smallestShingles[i] = HashRange;
	      int smallestPermutation = permutation[shingles[lo]];
	      for (int j = lo; j < hi; j++) 
		  {
			  if(smallestPermutation < permutation[shingles[j]])
			  {
				  smallestPermutation = permutation[shingles[j]];
				  smallestShingles[i] = smallestPermutation;
			  }
		  }
	    }
	  }

  void incrementIntersections(){
    for (int i = 0; i < numberOfDocs; i++)
      for (int j = 0; j < i; j++) 
        if (smallestShingles[i] == smallestShingles[j])
           sketchIntersections[i][j]++;
  }

  void approximateJaccard(){
    initialize();
    for (int p = 0; p < numberOfPermutations; p++){
System.err.println(p);
       permute();
       findSmallestShingles();
       incrementIntersections();
    }
  }

  void reportNearDuplicates(){
    for (int i = 0; i < numberOfDocs; i++)
      for (int j = 0; j < i; j++) 
         if (sketchIntersections[i][j] >= threshold) 
           System.out.println(i + " " + j + " " + sketchIntersections[i][j]);
  }
    

 public static void main(String[] args){
   IR21B ir21 = new IR21B();
   ir21.readShingles("shingles.txt");
   ir21.approximateJaccard();
   ir21.reportNearDuplicates();
 }
}

      