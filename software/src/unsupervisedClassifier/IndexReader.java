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

package unsupervisedClassifier;

import thesaurus.*;

import java.util.*;
import java.io.*;

public class IndexReader {
    /** String indexer */
    protected ThesaurusStringIndexer si;
    
    /** Lexicon */
    public static ThesaurusLexicon lex = new ThesaurusLexicon();
    
    /** List of non descriptors */
    protected static Map<Integer, Descriptor> ld = new TreeMap<Integer,Descriptor>();
    
    /** List of descriptors */
    protected static Map<Integer,NonDescriptor> lnd = new TreeMap<Integer,NonDescriptor>();
    
    /** Microtesauri */
    protected static Map<Integer, List<Integer>> MT = new HashMap<Integer, List<Integer>>();
    
    /** Microtesaurus */
    protected static Map<Integer, String> micro = new HashMap<Integer, String>();
   
    /** Number of descriptors */
    protected static int numDescriptors = 0;
    
    /** Number of nondescriptors */
    protected static int numNonDescriptors = 0;
    
    
    public IndexReader() {
        si = new ThesaurusStringIndexer();
    }
    
    public void readIndex(String indexName) {
        FileReader fp = null;
        try {
            fp = new FileReader(indexName);
        } catch (FileNotFoundException ex){
            System.out.println("File " + indexName + " not found. " + ex);
            System.exit(0);
        }
        
        BufferedReader in = new BufferedReader(fp);
        
        try{
            lex.read(in);
            numDescriptors = Integer.parseInt(in.readLine());
            for (int i=0; i<numDescriptors; ++i) {
                Descriptor desc = new Descriptor(in.readLine());
                ld.put(desc.getId(), desc);
            }
            
            numNonDescriptors = Integer.parseInt(in.readLine());
            for (int i=0; i<numNonDescriptors; ++i) {
                NonDescriptor nonDesc = new NonDescriptor(in.readLine());
                lnd.put(nonDesc.getId(), nonDesc);
            }
           
            // 4th.- The list of microthesaurus
            int _size = Integer.parseInt(in.readLine());
            for (int i=0; i<_size; ++i)
            {
              int id = Integer.parseInt(in.readLine());
              int mysize = Integer.parseInt(in.readLine());
              List<Integer> l = new LinkedList<Integer>();
              for (int j=0; j<mysize; ++j)
                  l.add(Integer.parseInt(in.readLine()));
              
              MT.put(id, l);
            }
            
            // 5th.- The list of microthesaurus identifiers
            _size = Integer.parseInt(in.readLine());
            for (int i=0; i<_size; ++i)
            {
              int identifier = Integer.parseInt(in.readLine());
              String s = in.readLine().replaceAll("_", " ");
              micro.put(identifier, s);
            }
            
            si.setLexicon(lex);
            
        }  catch (IOException ex){
            System.out.println("File " + indexName + " could not be read. Media error. " + ex);
            System.exit(-1);
        }  catch (NumberFormatException ex) {
            System.out.println("Error reading index, integer could not be converted. " + ex);
            System.exit(-1);
        }
    }
    
}
