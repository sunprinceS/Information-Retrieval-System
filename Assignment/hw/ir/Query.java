/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.nio.charset.*;
import java.io.*;


import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm implements Comparable<QueryTerm>{
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
      public int compareTo(QueryTerm other){
        return Double.compare(other.weight,weight);
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
        if(tokenIdx != -1){
          queryterm.get(tokenIdx).addWeight(1.0);
        }
        else{
          queryterm.add( new QueryTerm(token, 1.0) );
        }
      }
      
    }

    private int findToken(String term){
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
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Engine engine ,int ll) {
      
      //Query Normalization
      normalize(); 

      // related document id
      ArrayList<Integer> d_r = new ArrayList<Integer>();
      for(int i=0;i<ll;++i){
        if(docIsRelevant[i]) {
          d_r.add(results.get(i).docID);
        }
      }
      if(d_r.size() == 0) return; // no relevant docs
      
      //assume the query word is unique
      Map<String,Double> qm = new HashMap<>();
      double _beta = beta / d_r.size();

      //alpha * tf-idf weight
      for(QueryTerm q: queryterm){
        qm.put(q.term, alpha * q.weight * Math.log((double)engine.index.docLengths.size()/engine.index.getPostings(q.term).size()));
      }
      
      // read the document again
      for(int doc_idx: d_r){
        int doc_len = engine.index.docLengths.get(doc_idx);
        try{
          Reader reader = new InputStreamReader( new FileInputStream(engine.index.docNames.get(doc_idx)), StandardCharsets.UTF_8 );
          Tokenizer tok = new Tokenizer( reader, true, false, true, engine.patterns_file );

          while(tok.hasMoreTokens()){
            String token = tok.nextToken();
            // let the tf be 1 here, since if the term occur multiple times, it
            // will be added multiple times as well
            double v = _beta * Math.log((double)engine.index.docLengths.size()/engine.index.getPostings(token).size()) / doc_len;

            if(qm.get(token) != null){ // only insert unexist token
              qm.put(token,qm.get(token) + v);
            }
            else{
              qm.put(token,v);
            }
          }
          reader.close();
        }
        catch (IOException e){
          System.err.println( "Warning: IOException during relevance feedback." );
        }
      }
      //sort by value of each vector envty
      Map<String,Double> sorted = qm.entrySet()
                            .stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                      LinkedHashMap::new));
      //new query 
      queryterm.clear();
      int cnt = 0; // only the top 20 weights
      System.out.println("Top 20 weighted token");
      for(Map.Entry<String,Double> entry: sorted.entrySet()){
        cnt += 1;
        queryterm.add(new QueryTerm(entry.getKey(),entry.getValue()));
        System.out.println(entry.getKey() + " : " + entry.getValue());
        if(cnt == 20) break;
      }
    }
}


