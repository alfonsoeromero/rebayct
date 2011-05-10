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
 * ClassIndexer.java
 *
 * Created on 19 de febrero de 2007, 13:33
 *
 */

package indexation;

import java.util.*;
import java.io.*;
import base.IndexedDocument;

/**
 *
 * @author aeromero
 */
public class ClassIndexer {
    //! Number of documents classified (to compute 'a priori' probabilities)
    int numDocuments;
    
    //! Number of classes
    int numClasses;
    
    //! For each class, it stores the number of documents classified over it
    HashMap<Integer, Integer> numDocs;
    
    //! For each class, it stores the total frequency of terms appearing in it
    HashMap<Integer, Integer> totalFreq;
    
    //! ClassifiedLexicon stores the inverted list of occurrences of term in classes
    ClassifiedLexicon cl;
    
    //! Map to store the data from the classified file
    Map<Integer, List<Integer>> mp;
    
    //! Current document being processed
    int currentDocument;
    
    public void writeIndex(String indexName)
    {
        try{
            //1.- we open the buffer
            BufferedWriter out = new BufferedWriter(new FileWriter(indexName));
            
            //2.- we write the lexicon
            cl.write(out);
            
            //3.- we write the number of classes
            out.write(numClasses + "\n");
            
            //3.- we write the number of documents used to create this index of classes
            out.write(this.numDocuments + "\n");
            
            //5.- for each class, we write its id, the number of documents and the total frequency
            for (int i : numDocs.keySet())
              out.write(i + " " + numDocs.get(i) + " " + totalFreq.get(i) + "\n" );
            
            out.write("\n");
            
            //6.- we flush the buffer
            out.close();
        } catch(Exception ex) {
            System.err.println("ERROR: ");
            System.err.println(". Exiting.");
            System.exit(-1);       
        }
    }
    
    private void processDocument(IndexedDocument id, ProcessedLexicon pl) 
    {        
        //1.- we get the list of classes for the current document, from the "classified file"
        List<Integer> myClasses = mp.get(currentDocument);
        
        // total frequency of the terms in the document
        int totalFrequency = 0;
        
        //2.- for each term "k" in the document...
        for (int k : id.getListOfTerms()) 
        {    
            //2.1.- we get the string of the term "k"
            String word = (pl.getTermById(k)).getString();
            
            //2.2.- we look for that term in the classified lexicon, if it is not in, we create it
            ClassifiedTerm ct = null;
            int myTermId = cl.getTermIdByString(word);
            if (myTermId == -1) {
                ct = new ClassifiedTerm(word);
                cl.add(ct);
            } else
                ct = cl.getTermById(myTermId);
            
            // we increment the number of documents of this term
            ct.incNumDocs();
            
            //2.3.- we get the term identifier in "ct"
            myTermId = ct.getId();
            
            //2.4.- we get the frequency of the term in termFrequency
            int termFrequency = id.getFreqAtTerm(k);
            totalFrequency += termFrequency;
            
            // we add the frequency on this document to the total frequency of the term
            ct.addTotalDocumentalFreq(termFrequency);
            
            // 3.- for each class "j" this document is been classified under it, we arrange the data of the term
            for (int j : myClasses)
                ct.add(j, termFrequency);
        }
        
        //3.- we arrange statistical counters for each class
        for (int j : myClasses) 
        {
          this.totalFreq.put(j, this.totalFreq.get(j) + totalFrequency);
          this.numDocs.put(j, this.numDocs.get(j) + 1);
        }
        
        // 4.- we remove the list of classes of that document from memory
        mp.remove(currentDocument);
    }
    
    
    /** Creates a new instance of ClassIndexer */
    public ClassIndexer(String lexicalIndexName, String classifiedFileName) {
        //1.- we read the classified file
        ClassifiedFileReader cf = new ClassifiedFileReader(classifiedFileName);
        
        //2.- we read the LexicalIndex (the ProcessedLexicon and an Array of IndexedDocument)
        ProcessedLexicon pl = new ProcessedLexicon();
        
        //3.- we initiate the classifiedLexicon
        cl = new ClassifiedLexicon();
        
        //4.- we read the data from the classifiedfile
        mp = cf.getData();
        numClasses = cf.getNumClasses();
        
        //5.- we initialize the arrays, with one integer per class
        Set<Integer> classes = cf.getSetOfClasses();
        
        // number of documents for each class
        numDocs = new HashMap<Integer, Integer>();
        
        // total frequency for each class
        totalFreq = new HashMap<Integer, Integer>();
        for (int i : classes)
        {
            numDocs.put(i, 0);
            totalFreq.put(i, 0);
        }
        
        //6.- we set the current document to be processed to 1
        currentDocument = 1;
        
        System.out.println("Reading lexical index...");
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(lexicalIndexName));
            pl.read(in);
            
            System.out.println("Lexicon read...");
            
            // we read the number of documents
            this.numDocuments = Integer.parseInt(in.readLine());
            
            // for each document in the vectors file
            for (int i=0; i<this.numDocuments ; ++i) {
                // we read an indexed document from this file
                IndexedDocument id = new IndexedDocument(in.readLine()); 
                currentDocument = id.getId();
                
                // we process (index) it
                processDocument(id, pl);
            }
            
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: File " + lexicalIndexName + " does not exist. Exiting");
            System.exit(-1);
        } catch (NumberFormatException ex) {
            System.err.println("ERROR: Number expected, found string");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("ERROR: File " + lexicalIndexName + " could not be read. Premature EOF.");
            System.exit(-1);
        }
        
        // we free memory
        mp.clear();
        mp = null;
    }
    
}
