package com.uc;// IR21A.java CS6054 2015 Cheng
// Generate shingle hash values for a collection of documents
// Usage: java IR21A < isr4.txt > isrShingles.txt

import java.io.*;
import java.util.*;
import java.security.*;

public class IR21A{

  static final int K = 4;  // K consecutive tokens as a shingle
  int numberOfDocs = 0;
  int numberOfShingles = 0;
  int numberOfShingleDocs = 0;
  MessageDigest md = null;
  TreeSet<Integer> shingles = new TreeSet<Integer>();
  private PrintWriter pw = null;

  void readCollection(String filename){
    try {
      md = MessageDigest.getInstance("SHA-1");
    } catch(NoSuchAlgorithmException e){
      System.err.println(e.getMessage());
      System.exit(1);
    }
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    while (in.hasNextLine()){
      in.nextLine();  // ignore title
      in.nextLine(); // ignore journal
      shinglize(in.nextLine()); // abstract
      numberOfDocs++;
    }
    in.close();
  }

  void shinglize(String line){  
    shingles.clear();
    byte[] digest = null;
    String[] tokens = line.toLowerCase().split("[^a-z0-9]");
    // tokens contains only a-z0-9 but may be empty string or numbers
    int numberOfUsableTokens = 0;
    int len = tokens.length;
    int[] usableTokens = new int[len];
    for (int i = 0; i < len; i++)
      if (tokens[i].length() > 0 && tokens[i].charAt(0) > '9') 
        usableTokens[numberOfUsableTokens++] = i;
    for (int i = K; i < numberOfUsableTokens; i++){
      String shingle = tokens[usableTokens[i - K]];
      for (int j = i - K + 1; j < i; j++) 
        shingle += " " + tokens[usableTokens[j]];
      md.reset(); md.update(shingle.getBytes()); digest = md.digest();
      int hashValue = digest[0] < 0 ? digest[0] + 256 : digest[0];
      hashValue <<= 8;
      hashValue += digest[1] < 0 ? digest[1] + 256 : digest[1];
      hashValue <<= 4;
      hashValue += digest[2] & 0x0f;
      shingles.add(hashValue);
    }
    for (int s: shingles) System.out.print(s + " ");
    System.out.println();
  }

 public static void main(String[] args){
   IR21A ir21 = new IR21A();
   ir21.readCollection("isr4.txt");
 }
}

      