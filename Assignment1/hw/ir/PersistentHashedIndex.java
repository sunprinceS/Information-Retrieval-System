/*  
 i   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, KTH, 2018
 */  

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;


/*
 *   Implements an inverted index as a hashtable on disk.
 *   
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks. 
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "./index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The data file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final long TABLESIZE = 611953L;  // 50,000th prime number

    public static final long ENTRYSIZE = (Long.SIZE*2)/8;//!!!!! can not use int + long

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    // only used in building index in the beginning
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    // ===================================================================

    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */ 
    public class Entry {
      long ptr;
      int size_read;

      public Entry(long ptr, int size_read){
        this.ptr = ptr;
        this.size_read = size_read;
      }
    }


    // ==================================================================

    
    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created. 
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
        try {
            readDocInfo();
        }
        catch ( FileNotFoundException e ) {
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */ 
    int writeData(String word, String dataString, long ptr ) {
        try {
            dataFile.seek( ptr ); 
            byte[] k = word.getBytes();
            byte[] data = dataString.getBytes();
            dataFile.write(k);
            dataFile.write( data );
            return data.length;
        }
        catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */ 
    boolean checkWord(long ptr, String word){
      try{
        dataFile.seek(ptr);
        byte[] w = new byte[word.length()];
        dataFile.readFully(w);
        //System.out.println("checkWord " + word + " (read " + (new String(w)) + " )");
        return (word.equals(new String(w)));
      }
      catch ( IOException e ) {
          e.printStackTrace();
          return false;
      }
    }
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        }
        catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file. 
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr) {
      try{
        dictionaryFile.seek(ptr);
        dictionaryFile.writeLong(entry.size_read);
        dictionaryFile.writeLong(entry.ptr);

      }
      catch(IOException e){
        e.printStackTrace();
      }
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry( long ptr ) {   
      long data_ptr = 0L;
      int size_read = 0;
      try{
        dictionaryFile.seek(ptr);
        size_read = (int)dictionaryFile.readLong();
        data_ptr = dictionaryFile.readLong();

      }
      catch (IOException e){
          e.printStackTrace();
      }
      return (new Entry(data_ptr,size_read));
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for (Map.Entry<Integer,String> entry : docNames.entrySet()) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write(docInfoEntry.getBytes());
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try (BufferedReader br = new BufferedReader(freader)) {
            String line;
            while ((line = br.readLine()) != null) {
               String[] data = line.split(";");
               docNames.put(new Integer(data[0]), data[1]);
               docLengths.put(new Integer(data[0]), new Integer(data[2]));
            }
        }
        freader.close();
    }


    /**
     *  Write the index to files.
     */
    public void writeIndex() {
        int collisions = 0;
        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // Write the dictionary and the postings list
            int cnt = 0;
            //int max_val = 0;//940
            boolean[] b_occupied = new boolean[(int)TABLESIZE];//??????
            for(Map.Entry<String,PostingsList> entry: index.entrySet()){
              //int single_round = 0;
              long h = myHash(entry.getKey());
              long old_h =h;

              while(b_occupied[(int)h]){
                ++collisions;
                //++single_round;
                ++h;
                if(h == TABLESIZE){
                  h = 0L;
                }
              }
              //if(single_round > max_val){
                //max_val = single_round;
              //}
            
              ++cnt;
              if(cnt%10000 == 0) System.err.println("Saved " +cnt+ " indexes");
              b_occupied[(int)h] = true;

              int num_bytes = writeData(entry.getKey(),entry.getValue().toStr(),free);
              writeEntry(new Entry(free,num_bytes),h*(ENTRYSIZE));
              free += (num_bytes+entry.getKey().length());
          }
          //System.out.println("Largest offset " + max_val);
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );
    }

 
    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
      long hash_v = myHash(token);
      int cnt = 0;
      Entry entry = readEntry(hash_v * ENTRYSIZE);
      while(!checkWord(entry.ptr,token) && cnt < 1000){ //1000 is got via running once :p
        ++hash_v ;
        ++cnt;
        if(hash_v == TABLESIZE){
          hash_v = 0L;
        }
        entry = readEntry(hash_v * ENTRYSIZE);
      }
      if(checkWord(entry.ptr,token)){
        return (new PostingsList(readData(entry.ptr + token.length(),entry.size_read)));
      }
      else{
        return null;
      }
      
    }
    

    /**
     *  Inserts this token in the main-memory hashtable.
     */
    public void insert( String token, int docID, int offset ) {
      if(index.get(token) != null){
        (index.get(token)).add(docID,offset);
      }
      else{
        PostingsList newPL = new PostingsList();
        newPL.add(new PostingsEntry(docID,offset));
        index.put(token,newPL);
      }
    }


    /**
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.println( "Writing index to disk..." );
        long startTime = System.currentTimeMillis();
        writeIndex();
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.err.println( "done in " + elapsedTime/1000 + " seconds!");
     }

    /*Hash function*/
    private long myHash(String word){
      //long ret = 1125899906842597L;
      long ret = 5381;

      for(int i=0;i<word.length();i++){
        ret = ((ret << 5) + ret) + word.charAt(i);
      }
      ret %= TABLESIZE;
      if(ret < 0){
        ret += TABLESIZE;
      }

      return ret;

    }

    //private long myHash2(String word){
      //long ret = 0;

      //for(int i=0;i<word.length();i++){
        //ret = Character.getNumericValue(word.charAt(i)  + ret*65599;
      //}

      //if(ret < 0){
        //ret += Long.MAX_VALUE;
      //}
      //return ret;
    //}

}
