/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

/**
 *  Defines some common data structures and methods that all types of
 *  index should implement.
 */
public interface Index {

    /** Mapping from document identifiers to document names. */
    public HashMap<Integer,String> docNames = new HashMap<Integer,String>();
    
    /** Mapping from document identifier to document length. */
    public HashMap<Integer,Integer> docLengths = new HashMap<Integer,Integer>();

    public ArrayList<Double> docNorms = new ArrayList<Double>();
    //public HashMap<Integer,Double> docNorms = new HashMap<Integer,Double>();
    


    /** Inserts a token into the index. */
    public void insert( String token, int docID, int offset );

    public void calDocNorm();

    /** Returns the postings for a given term. */
    public PostingsList getPostings( String token );

    /** This method is called on exit. */
    public void cleanup();

}
		    