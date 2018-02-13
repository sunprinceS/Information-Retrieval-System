/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.nio.charset.*;
import java.io.*;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm {
      String term;
      double weight;
      QueryTerm( String t, double w ) {
          term = t;
          weight = w;
      } 
      public void addWeight(double inc){
        weight += inc;
      }
      public void normalize(double d){
        weight /= d;
      }
      public void multiply(double d){
        weight *= d;
      }
    }

    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.2;

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
      StringTokenizer tok = new StringTokenizer( queryString );
      while ( tok.hasMoreTokens() ) {
        String token = tok.nextToken();
        int tokenIdx = findToken(token);
        //System.out.println(tokenIdx);
        if(tokenIdx != -1){
          queryterm.get(tokenIdx).addWeight(1.0);
        }
        else{
          queryterm.add( new QueryTerm(token, 1.0) );
        }
      }
      
    }

    private int findToken(String term){
      //System.out.println(term);
      for(int i=0;i<queryterm.size();++i){
        if(queryterm.get(i).term.equals(term)){
          return i;
        }
      }
      return -1;
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
      return queryterm.size();
    }

    public void normalize(){
      for(int i=0;i<queryterm.size();++i){
        queryterm.get(i).normalize(length());
      }
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
      double len = 0;
      for ( QueryTerm t : queryterm ) {
          len += t.weight; 
      }
      return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
      Query queryCopy = new Query();
      for ( QueryTerm t : queryterm ) {
          queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
      }
      return queryCopy;
    }
    
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Engine engine ) {
	//
	//  YOUR CODE HERE
	//
    }
}


