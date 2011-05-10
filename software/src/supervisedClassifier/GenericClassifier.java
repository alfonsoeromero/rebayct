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
 * GenericClassifier.java
 *
 * Created on 1 de marzo de 2007, 12:52
 *
 */

package supervisedClassifier;

import indexation.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
abstract public class GenericClassifier {
    //! Number of documents classified (to compute a priori probabilities)
    int numDocuments;
   
    //! Number of classes
    int numClasses;
    
    //! For each class, it stores the number of documents classified over it
    HashMap<Integer, Integer> numDocs;
    
    //! For each class, it stores the total frequency of terms appearing in it
    HashMap<Integer, Integer> totalFreq;
    
    //! A priori probability for each class
    HashMap<Integer, Double> apriori;
    
    //! ClassifiedLexicon stores the inverted list of occurrences of term in classes
    ClassifiedLexicon cl;
    
    //! Lexicon to translate vectors
    ProcessedLexicon pl;
   
    public GenericClassifier(String classIndexName) {
        int line = 1;
        try {
          BufferedReader in = new BufferedReader(new FileReader(classIndexName));
        
          // 1.- we read the lexicon
          cl = new ClassifiedLexicon();
          cl.read(in);
          
          // 2.- we read the number of classes and documents
          this.numClasses = Integer.parseInt(in.readLine());
          this.numDocuments = Integer.parseInt(in.readLine());
          
          // 3.- we initialize the arrays and fill them
          numDocs = new HashMap<Integer, Integer>(numClasses);
          totalFreq = new HashMap<Integer, Integer>(numClasses);
          
          // for each class (stored in one line...)
          for (int i=0; i<numClasses; ++i)
          {
            // we read the line and tokenize it
            StringTokenizer st = new StringTokenizer(in.readLine());
            
            // should have three tokens...
            int classID = Integer.parseInt(st.nextToken());
            int numDocz = Integer.parseInt(st.nextToken());
            int myFreq = Integer.parseInt(st.nextToken());
            
            // we insert the values on the arrays
            numDocs.put(classID, numDocz);
            totalFreq.put(classID, myFreq);          
          }
        
        } catch (FileNotFoundException ex) {
          System.err.println("ERROR: file " + classIndexName + " does not exist. Exiting.");
          System.exit(-1);
        } catch (NumberFormatException ex) {
          System.err.println("ERROR: number expected at line " + line + " of file " + classIndexName + " was not found. Exiting.");
          System.exit(-1);
        } catch (IOException ex){
          System.err.println("ERROR: file " + classIndexName + " could not be read. Exiting.");
          System.exit(-1);
        }
              
        // 4.- we compute the apriori for each class
        apriori = new HashMap<Integer, Double>();
        
        double invNumDocs = 1.0/(double)numDocuments;
        for (int i : totalFreq.keySet())
        {
          if (numDocs.get(i) > 0)
            apriori.put(i, numDocs.get(i)*invNumDocs);
          else apriori.put(i, 0.0);
        }
        
        pl = null;
    }
    
    public void setProcessedLexicon(ProcessedLexicon myPl)
    {
        pl = myPl;
    }
    
    public Map<Integer, Integer> translateVector(Map<Integer, Integer> document)
    {
        HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (int t : document.keySet())
        {
            String myString = pl.getTermById(t).getString();
            int myid = cl.getTermIdByString(myString);
            if (myid != -1)
                result.put(myid, document.get(t));
        }
        return result;
    }
    
    
    /** Receives a list of pairs (term_identifier, frequency) and the identifier of the document and returs a list of (class_identifiers, relevance)     
     */
    abstract public HashMap<Integer, Double> classifyDocument(HashMap<Integer, Integer> mp, int docId);    
}

