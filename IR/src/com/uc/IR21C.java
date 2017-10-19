package com.uc;// IR21C.java CS6054 2015 Cheng
// show a pair of documents with given docIDs
// Usage: java IR21C docID1 docID2 < isr4.txt

import java.io.*;
import java.util.*;

public class IR21C{

  void getDocuments(int docID1, int docID2, String fileName){
    Scanner in = null;
	try {
		in = new Scanner(new File(fileName));
	
    int larger = docID1 < docID2 ? docID2 : docID1;
    int smaller = docID1 < docID2 ? docID1 : docID2;
    for (int i = 0; i < larger; i++)
      if (i == smaller) for (int k = 0; k < 3; k++)
        System.out.println(in.nextLine());
      else for (int k = 0; k < 3; k++) in.nextLine();
    for (int k = 0; k < 3; k++) System.out.println(in.nextLine());
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

 public static void main(String[] args){
   IR21C ir21 = new IR21C();
   ir21.getDocuments(1604,1451 ,"isr4.txt");
 }

}

