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
 * LexicalIndexer.java
 *
 * Created on 13 de febrero de 2007, 12:52
 *
 */

package indexation;

import base.IndexedDocument;
import base.StringIndexer;
import base.Lexicon;
import document.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
public class LexicalIndexer {
    
    int numdocs;
    
   /** Lexicon */ 
    protected static ProcessedLexicon lex = new ProcessedLexicon();
    
    //! collection we are indexing
    document.Collection col;
    
    /** Creates a new instance of LexicalIndexer */
    public LexicalIndexer(document.Collection c) 
    {
        col = c;
        numdocs = 0;
    }
    
    private void writeIndex(String indexName, File tempFileName, Lexicon<ProcessedTerm> lex) 
    {
        FileWriter fp = null;
        
        try {
            fp = new FileWriter(indexName);
        } catch (FileNotFoundException ex){
            System.err.println("ERROR. File " + tempFileName + " not found. Exiting. " + ex);
            System.exit(-1);
        } catch (IOException ex){
            System.err.println("ERROR. File " + tempFileName + " not writable. Exiting. " + ex);
            System.exit(-1);
        }
        
        BufferedWriter out = new BufferedWriter(fp);
        
        try{
            BufferedReader in = new BufferedReader(new FileReader(tempFileName));
            
            // 1st.- The lexicon
            lex.write(out);
            
            // 2nd.- The number of documents
            out.write(numdocs + "\n");
            
            // 3rd.- The documents themselves
            while (in.ready())
                out.write( in.readLine() + "\n");
            
            out.close();
            
        }  catch (IOException ex){
            System.out.println("File " + indexName + " could not be written. Media error. " + ex);
            System.exit(0);
        }
        
    }
    
    
    public void makeIndex(String indexName, boolean stemming, String stopwordsFileName) {
        StringIndexer.setIndexing(true);
        StringIndexer.setStem(stemming);
        StringIndexer.setStopwordList(stopwordsFileName);
        
        FileWriter fp = null;
        File tempFile = null;
        
        // ------------------------------------------------------------------------
        // 1.- We prepare a temporal file to write the document vectors
        // ------------------------------------------------------------------------
        
        try {
            tempFile = File.createTempFile(indexName, "temp");
            // Delete temp file when program exits
            tempFile.deleteOnExit();
            fp = new FileWriter(tempFile);
            
        } catch (FileNotFoundException ex){
            System.err.println("ERROR. File " + indexName + " not found. Exiting. " + ex);
            System.exit(-1);
        } catch (IOException ex){
            System.err.println("ERROR. File " + indexName + " not writable. Exiting. " + ex);
            System.exit(-1);
        }
        
        BufferedWriter out = new BufferedWriter(fp); // temporal file
        
        // ------------------------------------------------------------------------
        // 2.- We process the document collection
        // ------------------------------------------------------------------------
        
        LexicalStringIndexer si = new LexicalStringIndexer();
        si.setLexicon(lex);
        
        try{
            int size_collection = col.getSize();
            
            for (int i=0; i<size_collection; ++i)
            {
              Document d = col.getNextDocument();
              
              ++numdocs;
              
              si.clear();
              
              while (d.hasMoreLines())
                si.add(d.getLine());
              
              HashMap<Integer, Integer> mp = (HashMap<Integer, Integer>) si.getFreq();
              
              for (int _t : mp.keySet())
              {
                ProcessedTerm t = new ProcessedTerm();
                t = lex.getTermById(_t);
                t.incNumDocs();              
              }
              
              IndexedDocument id = new IndexedDocument( mp, d.getDocumentName(), d.getId() );
              
              out.write(id.toString());
            }
            
            out.close();
            
        }  catch (IOException ex){
            System.out.println("File " + indexName + " could not be written. Media error. " + ex);
            System.exit(0);
        }
        
        System.err.println("End of lexical processing, writing indexes");
        // Finally, we write the index
        this.writeIndex(indexName, tempFile, lex);
    }
}

