/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

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
      return index.getPostings(query.queryterm.get(0).term);

      //
      //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
      //

    }
}
