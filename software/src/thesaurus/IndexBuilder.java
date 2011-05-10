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

package thesaurus;

import java.util.*;
import java.io.*;

enum XMLFile {none, desc_es, relation_bt, uf_es};

enum DocumentProcessed {descriptor, nondescriptor, none};

public class IndexBuilder {
    /** String to be processed */
    String desc;
    
    /**  True if characters must be stored in "desc" */
    public boolean mustProcessString;
    
    /** True if characters must be processed as an integer */
    protected boolean mustProcessInteger;
    
    /** Number of open tags, to beware malformed xml files */
    private int tag;
    
    /** Processed integer */
    protected int processedInteger;
    
    /** String indexer */
    protected ThesaurusStringIndexer si;
    
    /** Lexicon */
    protected static ThesaurusLexicon lex = new ThesaurusLexicon();
    
    /** List of non descriptors */
    protected static Map<Integer, Descriptor> ld = new HashMap<Integer, Descriptor>();
    
    /** List of descriptors */
    protected static Map<Integer, NonDescriptor> lnd = new HashMap<Integer, NonDescriptor>();
    
    /** Microtesaurus */
    protected static Map<Integer, List<Integer> > MT = new HashMap<Integer, List<Integer>>();
 
    /** Name of the identifier by id */
    protected static Map<Integer, String> micro = new HashMap<Integer, String>();    
    
    /** Number of descriptors */
    protected static int numDescriptors = 0;
    
    /** Number of nondescriptors */
    protected static int numNonDescriptors = 0;
    
   
    protected Descriptor findDescriptorById(int id) {
        return ld.get(id);
    }
    
    public void startDocument() {
        tag = 0;
    }
    
    public void endDocument() {
        if (tag != 0)
            System.err.println("Warning: parse error processing file");
    }
    
    public void startUnit(String mystring) {
        ++tag;
        mustProcessString = false;
        desc = null;
    }
    
    public void endUnit(String typetag) {
        --tag;
    }
    
    public void characters(String s) {
        if (this.mustProcessInteger) {
            try{
                this.processedInteger = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                System.err.println("ERROR: trying to process integer " + s);
            }
            
        } else {
            si.add(s);
            if (this.mustProcessString) {
                if(this.desc == null)
                    desc = s;
                else desc += s;
            }
        }
    }
    
    public IndexBuilder() {
        if (lex == null)
            lex = new ThesaurusLexicon();
        
        if (si == null)
            si = new ThesaurusStringIndexer();
        else si.clear();
        
        si.setLexicon(lex);
        
        
        this.desc = null;
    }
    
    public static void setStopwordList(String stopwords) {
        ThesaurusStringIndexer.setStopwordList(stopwords);
        ThesaurusStringIndexer.setIndexing(true);
    }
    
    public static void normalizeWeights() 
    {
        // For each descriptor we store in "sum" the sum of the weights of his terms
        Map<Integer, Double> sum = new HashMap<Integer, Double>();
        for (Descriptor d : ld.values()) {
            double _sum = 0.0;
            for (Integer id : d.getTerms())
                _sum += ((ThesaurusTerm) lex.getTermById(id.intValue()) ).getWeightOnDescriptor(d.getId());
            
            d.setSumWeights(_sum);
            
            sum.put(d.getId(), 1.0/_sum);
        }
                
        for (int i=0; i<lex.getSize(); ++i) {
            ThesaurusTerm t = (ThesaurusTerm) lex.getTermById(i);
            for (Map.Entry<Integer, Double> e : t.getWeightDescriptors())
                t.setWeightOnDescriptor(e.getKey(), e.getValue() * sum.get(e.getKey()));
        }
        
        // ------------------------------------
        // For each NONdescriptor we store in "sum" the sum of the weights of his terms
        sum = null;
        sum = new HashMap<Integer, Double>();
        for (NonDescriptor nd : lnd.values()) {
            double _sum = 0.0;
            for (Integer id : nd.getTerms())
                _sum += ((ThesaurusTerm) lex.getTermById(id.intValue())).getWeightOnNonDescriptor(nd.getId());
            nd.setSumWeights(_sum);
            sum.put(nd.getId(), 1.0/_sum);
        }
        
        for (int i=0; i<lex.getSize(); ++i) 
        {
            ThesaurusTerm t = (ThesaurusTerm) lex.getTermById(i);
            for (Map.Entry<Integer, Double> e : t.getWeightNonDescriptors())            
                t.setWeightOnNonDescriptor(e.getKey(), e.getValue() * sum.get(e.getKey()));
        }
    }
    
    public static void computeWeights() 
    {        
        int size = lex.getSize();
        double[] idf = new double[size];
        double N = ld.size() + lnd.size();
        
        System.out.println("Lexicon size: " + size);
        
        for (int i=0; i<size; ++i) 
        {
            ThesaurusTerm t = (ThesaurusTerm) lex.getTermById(i);
            int id = t.getId();
            double Ni = t.getDescriptors().size() + t.getNonDescriptors().size();
            
            // 1.- Compute idf
            idf[id] = Math.log10(N/Ni);
            
            // 2.- Compute weights
            
            // 2.1.- In descriptors, tf * idf
            for (Map.Entry<Integer, Integer> m : t.getDescriptors())
                t.addWeightDescriptor(m.getKey(), ((double) m.getValue() )*idf[id]);
            
            // 2.2.- In nondescriptors, tf * idf
            for (Map.Entry<Integer, Integer> m : t.getNonDescriptors())
                t.addWeightNonDescriptor(m.getKey(), ((double) m.getValue() )*idf[id]);
            
        }
    }
    
    public static void writeIndex(String fileName) {
        FileWriter fp = null;
        try {
            fp = new FileWriter(fileName);
        } catch (FileNotFoundException ex){
            System.err.println("File " + fileName + " not found. " + ex);
            System.exit(-1);
        } catch (IOException ex){
            System.err.println("File " + fileName + " not writable. " + ex);
            System.exit(-1);
        }
        
        BufferedWriter out = new BufferedWriter(fp);
        
        System.out.println("LEXICON SIZE: " + lex.getSize());
        System.out.println("NUMBER OF DESCRIPTORS: " + ld.size());
        System.out.println("NUMBER OF NONDESCRIPTORS: " + lnd.size());
        
        try{
            // 1st.- The lexicon
            lex.write(out);
            
            // 2nd.- The list of descriptors
            out.write(new Integer(ld.size()).toString() + "\n");
            
            for (Descriptor d : ld.values())
                out.write(d.toString());
            
            // 3rd.- The list of nondescriptors
            out.write(new Integer(lnd.size()).toString() + "\n");
            
            for (NonDescriptor nd : lnd.values())
                out.write(nd.toString());
            
            // 4th.- The list of microthesaurus
            out.write(new Integer(MT.size()).toString() + "\n");
            for (Map.Entry<Integer, List<Integer>> m : MT.entrySet())
            {
              out.write(m.getKey().toString() + "\n");
              out.write(m.getValue().size() + "\n");
              for (Integer i : m.getValue())
                  out.write(i.toString() + "\n");
            }
            
            // 5th.- The pairs (thesaurus_id, description)
            out.write(new Integer(micro.size()).toString() + "\n");
            for (Map.Entry<Integer, String> m : micro.entrySet())            
              out.write(m.getKey().toString() + "\n" + m.getValue().trim().replaceAll(" ", "_") + "\n");
                       
            out.close();
            
        }  catch (IOException ex){
            System.out.println("File " + fileName + " could not be written. Media error. " + ex);
            System.exit(0);
        }
    }
}
