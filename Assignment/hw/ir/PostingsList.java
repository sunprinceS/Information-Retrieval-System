/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.Collections;

public class PostingsList implements Comparable<PostingsList>{
    
    /** The postings list */
    public ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();


    /** Number of postings in this list. */
    public int size() {
      return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
      return list.get( i );
    }

    public int compareTo(PostingsList rhs){
        return this.size() - rhs.size();
    }

    public PostingsList(){
      //used for intersection and phrase
      //System.out.println("Hihi");
    }

    public PostingsList(String s){
      String[] pes = s.split("\n");
      for(String pe : pes){
        this.list.add(new PostingsEntry(pe));
      }
    }

    public String toStr(){
      String ret = "";
      for(PostingsEntry pe:this.list){
        ret = ret + pe.toStr() + "\n";
      }
      return ret.substring(0,ret.length()-1);
    }
    //public PostingsList(PostingsEntry entry){
      //assert size() == 0;
      //list.add(entry);
    //}

    public void add(PostingsEntry entry){
      list.add(entry);
    }

    public void add(int docID, int offset){
      // Indexing the file according to docID, then according to offset
      assert size() != 0;
      PostingsEntry lastEntry = list.get(list.size()-1);
      if(lastEntry.docID != docID){
        list.add(new PostingsEntry(docID,offset));
      }
      else{
        lastEntry.positions.add(offset);
      }
    }

}
