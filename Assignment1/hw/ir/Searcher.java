/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;
import java.util.ArrayList;
import java.util.Collections;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;
    
    /** Constructor */
    public Searcher( Index index ) {
        this.index = index;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType ) { 
      //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
      if(queryType == QueryType.INTERSECTION_QUERY){
        return intersect(query,false);
      }
      else if(queryType == QueryType.PHRASE_QUERY){
        return intersect(query,true);
      }
      else{
        return null;
      }
    }
    private PostingsList intersect(Query query,Boolean bNear){
      //TODO: start from the least element postingList
      if(query.queryterm.size() > 1){
        PostingsList pl1 = index.getPostings(query.queryterm.get(0).term);
        PostingsList pl2 = index.getPostings(query.queryterm.get(1).term);
        if(pl1 == null || pl2 == null){
          return null;
        }

        PostingsList ret = intersect_two(pl1,pl2,bNear);
        for(int i=2;i<query.queryterm.size();++i){
          PostingsList pl = index.getPostings(query.queryterm.get(i).term);
          if(pl == null){
            return null;
          }
          ret = intersect_two(ret,pl,bNear);
        }
        return ret;
      }
      else{
        return index.getPostings(query.queryterm.get(0).term);
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
}
