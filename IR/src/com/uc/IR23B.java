package com.uc;// IR23B.java CS6054 2015 Cheng
// IR23B.java CS6054 2015 Cheng
// Find nearest neighbors of 80 random pics
// Usage:  java IR23B < IR23Aoutput

import java.io.*;
import java.util.*;

public class IR23B{

    static final int numberOfTopics = 64;  // number of bits in hash value
    static final int tops = 10;
    static final int numberOfSelectedPics = 80; // to be randomly selected
    int numberOfDocs = 800;
    String[] pics = new String[numberOfDocs];
    String[] codes = new String[numberOfDocs];
    int[][] distances = new int[numberOfDocs][numberOfDocs];
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

    void readCodes(String fileName){
        Scanner in;
        try {
            in = new Scanner(new File(fileName));

            for (int i = 0; i < numberOfDocs; i++) codes[i] = in.nextLine();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void codeDistances(){  // Hamming distances between codes in distances[][]
        for (int p = 0; p < numberOfDocs; p++){
            distances[p][p] = 0;
            for (int q = p + 1; q < numberOfDocs; q++){
                int distance = 0;
                for (int i = 0; i < 16; i++){
                    int a = codes[p].charAt(i);
                    int b = codes[q].charAt(i);
                    if (a >= 'a') a = a - 'a' + 10; else a -= '0';
                    if (b >= 'a') b = b - 'a' + 10; else b -= '0';
                    int d = a ^ b;
                    if (d > 0) for (int j = 0; j < 4; j++){
                        if ((d & 1) == 1) distance++;
                        d >>= 1;
                    }
                }
                distances[p][q] = distances[q][p] = distance;
            }
        }
    }

    void nineClosest(int docID){ // find 10 nearest pics to docID, including self
        int[] nearest = new int[tops + 1];
        int numberOfTops = 0;
        for (int i = 0; i < numberOfDocs; i++){
            int j = numberOfTops - 1; for (; j >= 0; j--)
                if (distances[docID][i] < distances[docID][nearest[j]])
                    nearest[j + 1] = nearest[j];
                else break;
            if (j < tops) nearest[j + 1] = i;
            if (numberOfTops < tops) numberOfTops++;
        }
        for (int i = 0; i < tops; i++) System.out.println(pics[nearest[i]]); // rearrange pics
    }

    void test(){
        for (int i = 0; i < numberOfSelectedPics; i++){
            int docID = random.nextInt(numberOfDocs);
            nineClosest(docID);
        }
    }


    public static void main(String[] args){
        IR23B ir23 = new IR23B();
        ir23.readPics("faces800.txt");
        ir23.readCodes("output.txt");
        ir23.codeDistances();
        ir23.test();
    }
}
