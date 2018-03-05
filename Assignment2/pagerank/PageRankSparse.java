import java.util.*;
import java.io.*;

public class PageRankSparse {

  private class Pair implements Comparable < Pair > {
    public String docName;
    public double rank;

    public Pair(String d, double r) {
      this.docName = d;
      this.rank = r;
    }

    public int compareTo(Pair p) {
      if (this.rank == p.rank) return 0;
      else if (this.rank < p.rank) return 1;
      else return -1;
    }

    public void print() {
      System.out.println(docName + ": " + rank);
    }
  }

  /**  
   *   Maximal number of documents. We're assuming here that we
   *   don't have more docs than we can keep in main memory.
   */
  final static int MAX_NUMBER_OF_DOCS = 2000000;

  public ArrayList < Pair > pagerank = new ArrayList < Pair > ();
  public ArrayList < Integer > sinkDoc = new ArrayList < Integer > ();
  public ArrayList < Integer > nonSinkDoc = new ArrayList < Integer > ();


  /**
   *   Mapping from document names to document numbers.
   */
  HashMap < String, Integer > docNumber = new HashMap < String, Integer > ();

  /**
   *   Mapping from document numbers to document names
   */
  String[] docName = new String[MAX_NUMBER_OF_DOCS];

  /**  
   *   A memory-efficient representation of the transition matrix.
   *   The outlinks are represented as a HashMap, whose keys are 
   *   the numbers of the documents linked from.<p>
   *
   *   The value corresponding to key i is a HashMap whose keys are 
   *   all the numbers of documents j that i links to.<p>
   *
   *   If there are no outlinks from i, then the value corresponding 
   *   key i is null.
   */
  HashMap < Integer, HashMap < Integer, Boolean >> link = new HashMap < Integer, HashMap < Integer, Boolean >> ();

  /**
   *   The number of outlinks from each node.
   */
  int[] out = new int[MAX_NUMBER_OF_DOCS];

  /**
   *   The probability that the surfer will be bored, stop
   *   following links, and take a random jump somewhere.
   */
  final static double BORED = 0.15;

  /**
   *   Convergence criterion: Transition probabilities do not 
   *   change more that EPSILON from one iteration to another.
   */
  final static double EPSILON = 0.0001;


  /* --------------------------------------------- */


  public PageRankSparse(String filename) {
    int noOfDocs = readDocs(filename);
    iterate(noOfDocs, 1000);
  }


  /* --------------------------------------------- */


  /**
   *   Reads the documents and fills the data structures. 
   *
   *   @return the number of documents read.
   */
  int readDocs(String filename) {
    int fileIndex = 0;
    try {
      System.err.print("Reading file... ");
      BufferedReader in = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = in .readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS) {
        int index = line.indexOf(";");
        String title = line.substring(0, index);
        Integer fromdoc = docNumber.get(title);
        //  Have we seen this document before?
        if (fromdoc == null) {
          // This is a previously unseen doc, so add it to the table.
          fromdoc = fileIndex++;
          docNumber.put(title, fromdoc);
          docName[fromdoc] = title;
        }
        // Check all outlinks.
        StringTokenizer tok = new StringTokenizer(line.substring(index + 1), ",");
        while (tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS) {
          String otherTitle = tok.nextToken();
          Integer otherDoc = docNumber.get(otherTitle);
          if (otherDoc == null) {
            // This is a previousy unseen doc, so add it to the table.
            otherDoc = fileIndex++;
            docNumber.put(otherTitle, otherDoc);
            docName[otherDoc] = otherTitle;
          }
          // Set the probability to 0 for now, to indicate that there is
          // a link from fromdoc to otherDoc.
          if (link.get(fromdoc) == null) {
            link.put(fromdoc, new HashMap < Integer, Boolean > ());
          }
          if (link.get(fromdoc).get(otherDoc) == null) {
            link.get(fromdoc).put(otherDoc, true);
            out[fromdoc]++;
          }
        }
      }
      if (fileIndex >= MAX_NUMBER_OF_DOCS) {
        System.err.print("stopped reading since documents table is full. ");
      } else {
        System.err.print("done. ");
      }
    } catch (FileNotFoundException e) {
      System.err.println("File " + filename + " not found!");
    } catch (IOException e) {
      System.err.println("Error reading file " + filename);
    }
    System.err.println("Read " + fileIndex + " number of documents");
    return fileIndex;
  }


  /* --------------------------------------------- */


  /*
   *   Chooses a probability vector a, and repeatedly computes
   *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
   */
  void iterate(int numberOfDocs, int maxIterations) {

    int numIter = 0;
    double[] a = new double[numberOfDocs];
    Arrays.fill(a, 1.0 / numberOfDocs);
    double[] old_a = new double[numberOfDocs];
    int[] cnt = new int[numberOfDocs];

    double p;
    double eps;

    //long startTime, endTime;
    while (numIter++ < maxIterations) {
      //startTime = System.nanoTime();
      old_a = Arrays.copyOf(a, a.length);
      Arrays.fill(cnt, 0);

      p = 0.0;
      for (int i = 0; i < numberOfDocs; ++i) {
        if (out[i] == 0) {
          p += old_a[i];
        }
      }
      Arrays.fill(a, p / numberOfDocs);


      for (int col = 0; col < numberOfDocs; ++col) {
        if (out[col] != 0) {
          for (int i = 0; i < numberOfDocs; ++i) {
            if (cnt[col] < out[col] && link.get(col).get(i) != null) {
              ++cnt[col];
              a[i] += (old_a[col] * (((1 - BORED) / out[col]) + (BORED / numberOfDocs)));
            } else { // non link
              a[i] += (old_a[col] * BORED / numberOfDocs);
            }
          }
        }
      }


      eps = 0;
      for (int i = 0; i < numberOfDocs; ++i) {
        eps += Math.abs(old_a[i] - a[i]);
      }
      if (eps < EPSILON) {
        break;
      }
      //endTime = System.nanoTime();
      //System.out.println("Iter " + numIter + " take " + (endTime - startTime) / 1E9 + " seconds");
    }

    for (int i = 0; i < numberOfDocs; ++i) {
      pagerank.add(new Pair(docName[i], a[i]));
    }
    Collections.sort(pagerank);

    for (int i = 0; i < numberOfDocs; ++i) {
      pagerank.get(i).print();
    }

  }
  /* --------------------------------------------- */


  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Please give the name of the link file");
    } else {
      new PageRankSparse(args[0]);
    }
  }
}
