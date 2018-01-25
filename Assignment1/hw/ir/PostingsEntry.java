/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score = 0;
    public ArrayList<Integer> positions = new ArrayList<Integer>();


    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
      return Double.compare( other.score, score );
    }
    
    public int size(){
      return positions.size();
    }
    
    //
    // YOUR CODE HERE
    //
    public PostingsEntry(int docID){
      this.docID = docID;
    }
    public int get(int i){
      return this.positions.get(i);
    }

    public void add(int offset){
      this.positions.add(offset);
    }

    public PostingsEntry(int docID, int offset){
      this.docID = docID;
      this.positions.add(offset);
    }
}

    
