package com.uc;// IR22C.java CS6054 2015 Cheng
// show RBM codes for a pair of documents with given docIDs
// Usage: java IR21C docID1 docID2 < isrCodes.txt

import java.io.*;
import java.util.*;

public class IR22C{

  String code1, code2;

  void getCodes(int docID1, int docID2, String filename){
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
      int larger = docID1 < docID2 ? docID2 : docID1;
      int smaller = docID1 < docID2 ? docID1 : docID2;
      for (int i = 0; i < larger; i++)
        if (i == smaller) code1 = in.nextLine();
        else in.nextLine();
      code2 = in.nextLine();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  void codeDistance(){
    System.out.println(code1);
    System.out.println(code2);
    int distance = 0;
    for (int i = 0; i < 16; i++){
      int a = code1.charAt(i);
      int b = code2.charAt(i);
      if (a >= 'a') a = a - 'a' + 10; else a -= '0';
      if (b >= 'a') b = b - 'a' + 10; else b -= '0';
      int d = a ^ b;
      if (d > 0) for (int j = 0; j < 4; j++){
        if ((d & 1) == 1) distance++;
        d >>= 1;
      }
    }
    System.out.println("Hamming distance: " + distance);
  }

 public static void main(String[] args){
   IR22C ir22 = new IR22C();
   ir22.getCodes(1604, 1451, "/Users/samridhi/workspace/IR/src/com/uc/adInvertedTf.txt");
   ir22.codeDistance();
 }
}

