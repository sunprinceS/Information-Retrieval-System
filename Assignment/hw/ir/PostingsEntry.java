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
    //public double norm = 0;
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

    String toStr(){
      String ret = Integer.toString(this.docID);
      for(int offset: this.positions){
        ret = ret + " " + Integer.toString(offset);
      }
      return ret;
    }
    
    public int size(){
      return positions.size();
    }
    
    public PostingsEntry(String s){
      String[] offsets = s.split(" ");
      this.docID = Integer.parseInt(offsets[0]);
      for(int i=1;i<offsets.length;++i){
        this.positions.add(Integer.parseInt(offsets[i]));
      }
    }
    public PostingsEntry(int docID){
      this.docID = docID;
    }
    public PostingsEntry(PostingsEntry rhs){
      this.docID = rhs.docID;
      this.score = rhs.score;
      //this.norm = rhs.norm;
      this.positions = rhs.positions;
    }
    public PostingsEntry(int docID,double s){
      this.docID = docID;
      this.score = s;
      //this.norm = nm;
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
