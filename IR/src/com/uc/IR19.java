package com.uc;

import java.io.*;
import java.util.*;
import java.text.*;
import Jama.*;

public class IR19{
    int N = 0; // number of docs
    int M = 0; // number of terms
    String[] dictionary = null;
    Matrix C = null;
    Matrix U = null;
    Matrix S = null;
    Matrix V = null;
    DecimalFormat decimalf = new DecimalFormat("#0.000");

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
        C = new Matrix(M, N);
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
                C.set(m, n, 1.0);
            }
            n++;
        }
        in.close();
    }

    void svd(){
        SingularValueDecomposition usv = C.svd();
        U = usv.getU();
        S = usv.getS();
        V = usv.getV();
    }

    void computeC2(){

        for(int m=2;m<N;m++)
        {
            S.set(m,m,0);

        }
        Matrix C2 = U.times(S).times(V.transpose());
        C2.print(M, N);
    }

    public static void main(String[] args){
        IR19 ir19 = new IR19();
        ir19.readData("spanishEnglish.txt");
        ir19.svd();
        ir19.computeC2();
    }
}