package com.uc;

import java.util.*;

public class InvertedIndex {
    int numberOfDocs = 0;
    int numberOfTerms = 0;
    TreeSet<String> terms = new TreeSet<String>();

    void readCollection(){
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()){
            String[] parts = in.nextLine().split("\t");
            tokenize(parts[1], numberOfDocs); // title
            in.nextLine(); // ignore journal
            tokenize(in.nextLine(), numberOfDocs); // abstract
            numberOfDocs++;
        }
        numberOfTerms = terms.size();
        System.out.println(numberOfDocs + " " + numberOfTerms);
    }

    void tokenize(String line, int doc){
        String[] tokens = line.toLowerCase().split("[^a-z0-9]");
        // tokens contains only a-z0-9 but may be empty string or numbers
        for (String s: tokens)
            if (s.length() > 0 && s.charAt(0) > '9') terms.add(s);
    }

    public static void main(String[] args) {
        InvertedIndex ir1 = new InvertedIndex();
        ir1.readCollection();
    }
}
