package com.uc;

/**
 * Created by samridhi on 03/09/15.
 */

// IR3.java CS6054 2015 Cheng
// spelling correction by indexing 3-grams of a dictionary
// Usage: java IR3 isaInverted.txt (or adInverted.txt)
// type (misspelled) words until you enter an empty line
// top five words in dictionary with largest Jaccard coeff displayed for each

import java.io.*;
        import java.util.*;

class Incidence implements Comparable{
    String term; int doc;
    public Incidence(String t, int d){
        term = t; doc = d;
    }
    public int compareTo(Object obj){
        Incidence i = (Incidence)obj;
        int diff = term.compareTo(i.term);
        if (diff == 0) diff = doc - i.doc;
        return diff;
    }
}

public class Ir3 {

    static final int gramLength = 3;
    static final int tops = 5;
    String[] dictionary = null;
    int dictionarySize = 0;
    TreeSet<Incidence> incidences = new TreeSet<Incidence>();
    TreeSet<String> gramSet = new TreeSet<String>();
    int numberOfGrams = 0;
    String[] grams = null;
    int[] postingsLists = null;
    int[] postings = null;
    int numberOfPostings = 0;
    int[] XnY = null; // intersection sizes for Jaccard

    void readDictionary(String filename){  // read dictionary from inverted index
        Scanner in = null;
        try {
            in = new Scanner(new File(filename));
        } catch (FileNotFoundException e){
            System.err.println("not found");
            System.exit(1);
        }
        String[] tokens = in.nextLine().split(" ");
        dictionarySize = Integer.parseInt(tokens[0]);
        dictionary = new String[dictionarySize];
        XnY = new int[dictionarySize];
        for (int i = 0; i < dictionarySize; i++){
            String line = in.nextLine();
            int pos = line.indexOf(' ');
            dictionary[i] = line.substring(0, pos);
        }
        in.close();
    }


    void gramize(){  // precondition: readDictionary() done
        for (int termID = 0; termID < dictionarySize; termID++){
            int len = dictionary[termID].length();
            if (len > gramLength){
                for (int i = 0; i <= len - gramLength; i++){
                    String gram = dictionary[termID].substring(i, i + gramLength);
                    gramSet.add(gram);
                    incidences.add(new Incidence(gram, termID));
                }
            }
        }
        numberOfGrams = gramSet.size();
        grams = new String[numberOfGrams];
        int n = 0;
        for (String s: gramSet) grams[n++] = s;
    }

    void invertIndex(){  // precondition: gramize() done
        postingsLists = new int[numberOfGrams + 1];
        numberOfPostings = incidences.size();
        postings = new int[numberOfPostings];
        String curTerm = "";
        int gramID = 0;  int postingIndex = 0;
        for (Incidence i: incidences){
            if (!i.term.equals(curTerm)){
                postingsLists[gramID++] = postingIndex;
                curTerm = i.term;
            }
            postings[postingIndex++] = i.doc;
        }
        postingsLists[numberOfGrams] = numberOfPostings;
    }

    // binary search
    int find(String key, String[] array){
        int lo = 0; int hi = array.length - 1;
        while (lo <= hi){
            int mid = (lo + hi) / 2;
            int diff = key.compareTo(array[mid]);
            if (diff == 0) return mid;
            if (diff < 0) hi = mid - 1; else lo = mid + 1;
        }
        return -1;
    }

    void computeXnY(String query){
        // precondition: invertIndex() done, uses binary search function find()
        for (int i = 0; i < dictionarySize; i++) XnY[i] = 0;
        int len = query.length();

        for (int j = 0; j <= len - gramLength; j++){
            int gramID = find(query.substring(j, j + gramLength), grams);
            if (gramID >= 0)
                for (int k = postingsLists[gramID]; k < postingsLists[gramID + 1]; k++)
                    XnY[postings[k]]++;
        }
    }

    void retrieveByRanking(String query){  // precondition: computeXnY(query) donein
        int[] results = new int[tops + 1];
        double[] scores = new double[tops + 1];
        double score = 0;
        int numberOfResults = 0;
        int gramLengthForQuery = getNumberOfGramsFor(query);
        int len = query.length();
        for (int i = 0; i < dictionarySize; i++) if (XnY[i] > 0){
//            score = XnY[i]/(gramLengthForQuery+getNumberOfGramsFor(dictionary[i])-XnY[i]);
            score = XnY[i];
            int k = numberOfResults - 1; for (; k >= 0; k--)
                if (score > scores[k]){  // insertion sort
                    results[k + 1] = results[k];
                    scores[k + 1] = scores[k];
                }else break;
            if (k < tops - 1){ results[k + 1] = i; scores[k + 1] = score; }
            if (numberOfResults < tops) numberOfResults++;
        }
        for (int i = 0; i < numberOfResults; i++)
            System.out.println(dictionary[results[i]] + " " + scores[i]);
    }

    private int getNumberOfGramsFor(String term) {
        return term.length()-gramLength+1;
    }

    void correctSpellings(){  // precondition: invertIndex() done
        Scanner in = new Scanner(System.in);
        System.out.println("Enter a word for spelling correction. j");
        while (in.hasNextLine()){
            String query = in.nextLine();
            if (query.length() < gramLength) break;
            computeXnY(query);
            retrieveByRanking(query);
            System.out.println("Enter a word for spelling correction or empty line for end.");
        }
    }

    public static void main(String[] args){
        Ir3 ir3 = new Ir3();
        ir3.readDictionary("/Users/samridhi/workspace/IR/src/com/uc/isrInverted.txt");
        ir3.gramize();
        ir3.invertIndex();
        ir3.correctSpellings();
    }
}
