/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
      //System.out.printf("Token: %s, docID: %d, offset: %d",token, docID,offset);
      // YOUR CODE HERE
      if(index.get(token) != null){
        (index.get(token)).add(docID,offset);
      }
      else{
        PostingsList newPL = new PostingsList();
        newPL.add(new PostingsEntry(docID,offset));
        index.put(token,newPL);
      }
    }

    //public void calDocNorm(){
      //for(int i=0;i<docLengths.size();++i){
        //docNorms.add(.0);
      //}
      //for(PostingsList pl: index.values()){
        //for(PostingsEntry pe: pl.list){
          //assert docNorms.get(pe.docID) != null;
          //docNorms.set(pe.docID,docNorms.get(pe.docID) + tfidf(pe.size(),pl.size(),docLengths.get(pe.docID)));
        //}
      //}
    //}
    public void calDocNorm(){
      for(int i=0;i<docLengths.size();++i){
        docNorms.add(.0);
      }
      for(PostingsList pl: index.values()){
        for(PostingsEntry pe:pl.list){
          docNorms.set(pe.docID,docNorms.get(pe.docID) + Math.pow((double)pe.size(),2));
        }
      }
      for(double norm: docNorms){
        System.out.println(norm);
        System.out.println(Math.sqrt(norm));
      }
    }
    
    private double tfidf(int tf,int df,int norm){
      return (tf* Math.log((double)docLengths.size()/df))/(double)norm;

    }

    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
    // REPLACE THE STATEMENT BELOW WITH YOUR CODE
      return index.get(token);
    }



    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
