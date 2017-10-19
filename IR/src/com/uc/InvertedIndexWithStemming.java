package com.uc;

/**
 * Created by samridhi on 01/09/15.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
public class InvertedIndexWithStemming {

    int numberOfDocs = 0;
    int numberOfTerms = 0;
    TreeSet<String> terms = new TreeSet<String>();

    void readCollection() throws FileNotFoundException {
        Scanner in = new Scanner(new File(""));
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
        for (String s: tokens){
            /*int len = s.length();
            if (len > 0 && s.charAt(0) > '9'){
                int k = 1; for (; k < len; k++) if (s.charAt(k) <= '9') break;
                if (k < len) terms.add(s); // no stemming
                else{
                    Porter p = new Porter(s);
                    p.stem();
                    terms.add(p.toString());
                }
            }*/
            if (s.length() > 0 && s.charAt(0) > '9') terms.add(s);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        InvertedIndexWithStemming invertedIndexWithStemming = new InvertedIndexWithStemming();
        invertedIndexWithStemming.readCollection();
    }
}

