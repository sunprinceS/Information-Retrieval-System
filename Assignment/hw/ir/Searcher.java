/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import java.lang.System;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;
    boolean bNormCount;
    
    /** Constructor */
    public Searcher( Index index ) {
        this.index = index;
        bNormCount = false;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType ) { 
      //System.out.println("Total number of document: " + index.docLengths.size());
      //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
      if(queryType == QueryType.INTERSECTION_QUERY){
        return intersect(query,false);
      }
      else if(queryType == QueryType.PHRASE_QUERY){
        return intersect(query,true);
      }
      else{
        Query q = query.copy();
        if (!bNormCount){
          index.calDocNorm();
          bNormCount = true;
        }
        PostingsList ret = union(q);
        if(q.queryterm.size() > 1){
          rankSort(ret,false);
        }
        else{
          rankSort(ret, true);
        }
        return ret;
      }
    }
    private void rankSort(PostingsList pl,boolean bNorm){
      if(bNorm){
        for(PostingsEntry pe:pl.list){
          //pe.score /= Math.sqrt(Math.pow((double)index.docNorms.get(pe.docID),2));
          //pe.score /= (double)index.docNorms.get(pe.docID);
          pe.score /= (double)index.docLengths.get(pe.docID);
        }
      }
      Collections.sort(pl.list);
    }

    private PostingsList union(Query query){
      int curQuery = 0 ;
      PostingsList ret = index.getPostings(query.queryterm.get(curQuery).term);
      while(ret == null && curQuery < query.queryterm.size()-1){
        ret = index.getPostings(query.queryterm.get(++curQuery).term);
      }
      if(ret != null){
        //System.out.println(query.queryterm.get(curQuery).term);
        //System.out.println("The df: " + ret.size());
        //System.out.println("idf: " + Math.log((double)index.docLengths.size()/ret.size()));
        ret = reduce(ret,query.queryterm.get(curQuery).weight * Math.log((double)index.docLengths.size()/ret.size()));
        //ret = reduce(ret,query.queryterm.get(curQuery).weight);
        for(int i=curQuery+1;i<query.queryterm.size();++i){
          PostingsList merged = index.getPostings(query.queryterm.get(i).term);
          if(merged != null){
            ret = merge(ret,merged,query.queryterm.get(i).weight * Math.log((double)index.docLengths.size()/merged.size()));
            //ret = merge(ret,merged,query.queryterm.get(i).weight);
          }
        }
        return ret;
      }
      else{
        return (new PostingsList()) ;
      }
    }

    private PostingsList intersect(Query query,Boolean bNear){
      //TODO: start from the least element postingList
      if(query.queryterm.size() > 1){
        PostingsList pl1 = index.getPostings(query.queryterm.get(0).term);
        PostingsList pl2 = index.getPostings(query.queryterm.get(1).term);
        if(pl1 == null || pl2 == null){
          return (new PostingsList());
        }

        PostingsList ret = intersect_two(pl1,pl2,bNear);
        for(int i=2;i<query.queryterm.size();++i){
          PostingsList pl = index.getPostings(query.queryterm.get(i).term);
          if(pl == null){
            return (new PostingsList()) ;
          }
          ret = intersect_two(ret,pl,bNear);
        }
        return ret;
      }
      else{
        PostingsList pl = index.getPostings(query.queryterm.get(0).term);
        if(pl == null){
          return (new PostingsList());
        }
        return pl;
      }
    }
    private PostingsEntry positionIntersect_two(PostingsEntry pe1,PostingsEntry pe2){
      assert pe1.docID == pe2.docID;
      
      int it1 = 0;
      int it2 = 0;

      PostingsEntry ret = new PostingsEntry(pe1.docID);

      while(it1 < pe1.size() && it2 < pe2.size()){
        if(pe1.get(it1) == pe2.get(it2)-1){
          ret.add(pe2.get(it2));
          ++it1;
          ++it2;
        }
        else if(pe1.get(it1) < pe2.get(it2)-1){
          ++it1;
        }
        else{
          ++it2;
        }
      }
      return ret;

    }
    private PostingsList reduce(PostingsList pl, double w){
      PostingsList ret = new PostingsList();
      double s;
      for(int i=0;i<pl.size();++i){
        s = tfidf(pl.get(i).size(),pl.size(),index.docLengths.get(pl.get(i).docID),index.docNames.get(pl.get(i).docID),pl.get(i).docID);
        ret.add(new PostingsEntry(pl.get(i).docID,w * s));
      }
      return ret;
    }
    private PostingsList intersect_two(PostingsList pl1, PostingsList pl2, Boolean bPosition){
      //use QueryTerm rather than String for search according to weight in the
      //future
      
      int it1 = 0;
      int it2 = 0;

      PostingsList ret = new PostingsList();
      //position is not important here, just use a dummy value 0
      while(it1 < pl1.size() && it2 <pl2.size()){
        if(pl1.get(it1).docID == pl2.get(it2).docID){
          if(bPosition){
            PostingsEntry retEntry = positionIntersect_two(pl1.get(it1),pl2.get(it2));
            if(retEntry.size() != 0){
              ret.add(retEntry);
            }
          }
          else{
            ret.add(new PostingsEntry(pl1.get(it1).docID,0));
          }
          ++it1;
          ++it2;
        }
        else if(pl1.get(it1).docID < pl2.get(it2).docID){
          ++it1;
        }
        else{
          ++it2;
        }
      }
      return ret;
    }
    private PostingsList merge(PostingsList basePl,PostingsList mergedPl,double w){
      //basePl must be reduced
      PostingsList ret = new PostingsList();
      int it1 = 0;
      int it2 = 0;
      while(it1 < basePl.size() && it2 < mergedPl.size()){
        if(basePl.get(it1).docID == mergedPl.get(it2).docID){
          double s = tfidf(mergedPl.get(it2).size(),mergedPl.size(),index.docLengths.get(basePl.get(it1).docID),index.docNames.get(mergedPl.get(it2).docID),mergedPl.get(it2).docID);
          ret.add(new PostingsEntry(basePl.get(it1).docID,basePl.get(it1).score + s*w));
          ++it1;
          ++it2;
        }
        else if(basePl.get(it1).docID > mergedPl.get(it2).docID){ //add it2
          double s = tfidf(mergedPl.get(it2).size(),mergedPl.size(),index.docLengths.get(mergedPl.get(it2).docID),index.docNames.get(mergedPl.get(it2).docID),mergedPl.get(it2).docID);
          ret.add(new PostingsEntry(mergedPl.get(it2).docID,s*w));
          ++it2;
        }
        else{ //add it1
          ret.add(new PostingsEntry(basePl.get(it1)));
          ++it1;
        }
      }
      assert (it1 ==basePl.size()) || (it2 == mergedPl.size());
      while(it1 < basePl.size()){
        ret.add(new PostingsEntry(basePl.get(it1++)));
      }
      double s;
      while(it2 < mergedPl.size()){
        s = tfidf(mergedPl.get(it2).size(),mergedPl.size(),index.docLengths.get(mergedPl.get(it2).docID),index.docNames.get(mergedPl.get(it2)),mergedPl.get(it2).docID);
        ret.add(new PostingsEntry(mergedPl.get(it2++).docID,s*w));
      }
      return ret ;
    }
    private double tfidf(int tf, int df,int norm, String docName,int docID){
      //System.out.println("For doc-" +docName + " : " + "tf =  " + tf + ",  1+ln(tf) = " + (1  + Math.log(tf)));
      //System.out.println("For doc-" +docName + " : " + "tf =  " + tf + ",  df = " + df + ", docLength = " + norm );
      return (tf * Math.log((double)index.docLengths.size()/df))/(double)norm;
    }
}
