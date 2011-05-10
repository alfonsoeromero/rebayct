/*
 *
 *   ReBayCT: a tool for classification on a Thesaurus
 *
 *	For details see:
 *	 L. M. de Campos, A. E. Romero, Bayesian Network Models for Hierarchical Text 
 *	Classification from a Thesaurus, Int. J. Approx. Reasoning 50(7): 932-944 (2009).
 *
 *
 *   Copyright (C) 2006-2008 Alfonso E. Romero <alfonsoeromero (AT) gmail (DOT) com>
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * APrioriReader.java
 *
 * Created on 21 de febrero de 2007, 12:39
 *
 */

package todelete;
import java.util.*;
import java.io.*;

/**
 *
 * @author aeromero
 */
public class APrioriReader {
    //! A priori probability of a class in which a document does not appears in (for the case of all a prioris are equal)
    double apriori;
    
    //! A priori probability of a class in which a document does not appears in (for the case of all a prioris are distict)
    Map<Integer, Double> myApriori; 
    
    //! Data appearing in the file: for each document the pairs (class, probability) for which we
    //! have classified them
    Map<Integer, HashMap<Integer, Double>> data;
    
    private static final boolean DEBUGGING = false;
    
    /** RETURNS A PRIORI FOR DOCUMENT I (from 1 to NUM_DOCS) IN CLASS J (from 1 to NUM_CLASSES) */
    public double getAPrioriOfDocIInClassJ(int i, int j)
    {
      if (data.containsKey(i))
      {
         //! we get the map for the document
         HashMap<Integer,Double> h = data.get(i);
         
         //! if the key (class j) is present, we return its value
         if (h.containsKey(j))
            return h.get(j);
         else {
             if (myApriori == null)
                 return apriori; // else we return 1.0/Number_of_classes (if they are equal)
             else if ( this.myApriori.containsKey(j) )
                 return myApriori.get(j);
             else {
                 System.err.println("WARNING: class " + j + " invalid. Returning 0.");
                 return 0.0; // class is not defined
             }
         }
             
      } else {
        System.err.println("WARNING: document " + i + " not present in a priori file. Returning 0.");
        return 0.0;
      }
    }
    
    public Set<Integer> getSetOfAprioriClasses(int docId)
    {
        return data.get(docId).keySet();
    }
    
    
    /** Creates a new instance of APrioriReader 
     @param numClasses number of classes of the system
     @param resultFileName name of the file with the results
        format: an uncertain number of lines, having each line DOC_ID CLASS1 PROB1 ... CLASSN PROBN
     @param d array with the a priori of each class. If null they are all computed as 1.0/NUM_CLASSES
     */
    public APrioriReader(int numClasses, String resultFileName, HashMap<Integer, Double> d) {
        System.out.println("Using apriori file " + resultFileName);
        
        
        if (d == null) // if an array with the aprioris is not given...
          apriori = 1.0 / ((double) numClasses); // ...we compute the a priori probability first
        else myApriori = d; // else we use the array
        
        data = new HashMap<Integer, HashMap<Integer, Double>>();
        int line = 1;
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(resultFileName));
            
            // while the buffer is ready, we read lines and process them
            while (in.ready())
            {
              // the line read is tokeniked
              StringTokenizer st = new StringTokenizer(in.readLine());
                
              // we count the tokens 
              int numTokens = st.countTokens();
              
              // the first token of each line is the document id
              int documentId = Integer.parseInt(st.nextToken());
              
              if (APrioriReader.DEBUGGING)
                System.out.println("Processing document id= " +documentId);              
              
              // we create the hash map for the results of that document
              HashMap<Integer, Double> h = new HashMap<Integer, Double>();
              
              if (numTokens > 1)
                for (int i=0; i<(numTokens-1)/2; ++i)
                {
                  int myClassId = Integer.parseInt(st.nextToken());
                  double value = Double.parseDouble(st.nextToken());
                  h.put(myClassId, value);         
                }
            
              data.put(documentId, h);
              ++line;
            }
            
        } catch (NumberFormatException ex) {
            System.err.println("ERROR: wrong format number at line " + line + ". Exiting.");
            
        } catch (IOException ex) {
            System.err.println("ERROR: Media not ready. " + ex + ". Exiting");
            System.exit(-1);
        } 
    }
}

