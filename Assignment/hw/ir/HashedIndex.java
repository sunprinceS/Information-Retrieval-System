/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


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
        kgIndex.insert(token);
      }
    }

    public void calDocNorm(){
      for(int i=0;i<docLengths.size();++i){
        docNorms.add(.0);
      }
      for(PostingsList pl: index.values()){
        for(PostingsEntry pe: pl.list){
          assert docNorms.get(pe.docID) != null;
          docNorms.set(pe.docID,docNorms.get(pe.docID) + tfidf(pe.size(),pl.size(),docLengths.get(pe.docID)));
        }
      }
    }
    //public void calDocNorm(){
      //for(int i=0;i<docLengths.size();++i){
        //docNorms.add(.0);
      //}
      //for(PostingsList pl: index.values()){
        //for(PostingsEntry pe:pl.list){
          ////docNorms.set(pe.docID,docNorms.get(pe.docID) + Math.pow((double)pe.size(),2));
          ////docNorms.set(pe.docID,docNorms.get(pe.docID) + pe.size()); //# of terms
        //}
      //}
    //}
    
    private double tfidf(int tf,int df,int norm){
      return (tf* Math.log((double)docLengths.size()/df))/(double)norm;
    }

    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
    // REPLACE THE STATEMENT BELOW WITH YOUR CODE
    // if token contains * or not found -> need token expansion!
      int ast_loc = token.indexOf('*');
      if(ast_loc != -1){ // wildcard query
        return getWildcardPostings(token,ast_loc);
      }
      return index.get(token);
    }
    //abc*
    private List<KGramPostingsEntry> p_wild(String token){
      List<KGramPostingsEntry> candidate_words=kgIndex.getPostings('^'+token.substring(0,KGram-1));
      for(int i=0;i<token.length()-KGram+1;++i){
        candidate_words = kgIndex.intersect(candidate_words,kgIndex.getPostings(token.substring(i,i+KGram)));
      }
      return candidate_words;
    }
    //*abc
    private List<KGramPostingsEntry> b_wild(String token){
      List<KGramPostingsEntry> candidate_words = kgIndex.getPostings(token.substring(token.length()-KGram+1,token.length()) + '$');
      for(int i=0;i<token.length()-KGram+1;++i){
        candidate_words = kgIndex.intersect(candidate_words,kgIndex.getPostings(token.substring(i,i+KGram)));
      }
      return candidate_words;
    }
    public PostingsList getWildcardPostings(String token,int ast_loc){
      List<KGramPostingsEntry> candidate_words = null;
      PostingsList ret = new PostingsList();
      if(ast_loc == 0){
        candidate_words = b_wild(token.substring(ast_loc+1,token.length()));
      }
      else if(ast_loc == token.length()-1){
        candidate_words = p_wild(token.substring(0,ast_loc));
      }
      else{
        candidate_words = kgIndex.intersect(p_wild(token.substring(0,ast_loc)),b_wild(token.substring(ast_loc+1,token.length())));
      }
      for(int i=0;i<candidate_words.size();++i){
        System.out.println(kgIndex.getTermByID(candidate_words.get(i).tokenID));
      }

      //after adding all, do the position check $^
      boolean init = false;
      for(int i=0;i<candidate_words.size();++i){
        String term = kgIndex.getTermByID(candidate_words.get(i).tokenID);
        //System.out.println(term);
        //System.out.println("********");
        //if(ast_loc != 0){
          //System.out.println(token.substring(0,ast_loc));
          //System.out.println(term.substring(0,ast_loc));
        //}
        //if(ast_loc != token.length()-1){
          //System.out.println(token.substring(ast_loc+1,token.length()));
          //System.out.println(term.substring(term.length()-token.length()+ast_loc+1,term.length()));
        //}
        //System.out.println("********");

        if(((ast_loc == 0)||(token.substring(0,ast_loc).equals(term.substring(0,ast_loc)))) && ((ast_loc == token.length()-1)||(token.substring(ast_loc+1,token.length()).equals(term.substring(term.length()-token.length()+ast_loc+1,term.length()))))){
          if(init){
            ret = mergeList(ret,index.get(term));
          }
          else{
            ret = index.get(term);
            init = true;
          }
          //System.out.println(term);
          //for(int ii=0;ii<ret.size();++ii){
            //System.out.print( docNames.get(ret.get(ii).docID) + " ");
          //}
          //System.out.println("");
        }
      }
      return ret;
    }


    private PostingsList mergeList(PostingsList pl1,PostingsList pl2){
      PostingsList ret = new PostingsList();
      int it1 = 0;
      int it2 = 0;
      while(it1 < pl1.size() && it2 < pl2.size()){
        if(pl1.get(it1).docID == pl2.get(it2).docID){
          ret.add(new PostingsEntry(mergePosition(pl1.get(it1),pl2.get(it2))));
          it1++;
          it2++;
        }
        else if(pl1.get(it1).docID > pl2.get(it2).docID){ //add 2
          ret.add(new PostingsEntry(pl2.get(it2)));
          it2++;
        }
        else{ // add 1
          ret.add(new PostingsEntry(pl1.get(it1)));
          it1++;
        }
      }
      assert(it1 == pl1.size() || it2 == pl2.size());
      while(it1 < pl1.size()){
        ret.add(pl1.get(it1++));
      }
      while(it2 < pl2.size()){
        ret.add(pl2.get(it2++));
      }
      return ret;
    }

    private PostingsEntry mergePosition(PostingsEntry pe1,PostingsEntry pe2){
      assert pe1.docID == pe2.docID;
      int it1 = 0;
      int it2 = 0;
      PostingsEntry ret = new PostingsEntry(pe1.docID,1.0);
      while(it1 < pe1.size() && it2 < pe2.size()){
        if(pe1.get(it1) == pe2.get(it2)){
          ret.add(pe2.get(it2));
          it1++;
          it2++;
        }
        else if(pe1.get(it1) > pe2.get(it2)){
          ret.add(pe2.get(it2));
          it2++;
        }
        else{
          ret.add(pe1.get(it1));
          it1++;
        }
      }
      assert(it1 == pe1.size() || it2 == pe2.size());
      while(it1 < pe1.size()){
        ret.add(pe1.get(it1++));
      }
      while(it2 < pe2.size()){
        ret.add(pe2.get(it2++));
      }
      return ret;
    }
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
