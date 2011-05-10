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

import java.util.*;
import java.io.*;
import thesaurus.NonDescriptor;
import thesaurus.ThesaurusStringIndexer;
import thesaurus.ThesaurusTerm;

abstract public class ClassifierModel extends IndexReader {
    private static final boolean OUTPUT = false;
    
    /** Map with term/frequency pairs */
    protected Map<Integer, Integer> m;    
    
    private void computeIDFS(Map<Integer, Double> idf) {
        // Idf computation for each term
        double sum = 0.0;
        double N = ClassifierModel.numDescriptors + ClassifierModel.numNonDescriptors;
        for (Map.Entry<Integer, Integer> termsOfDocument : m.entrySet())
        {
            ThesaurusTerm t = lex.getTermById(termsOfDocument.getKey().intValue());
            double Ni = t.getDescriptors().size() + t.getNonDescriptors().size();
            double myIdf = Math.log10(N/Ni);
            sum += myIdf;
            idf.put(t.getId(), myIdf);
        }
        
        // Idf normalization
        sum = 1.0 / sum;
        
        for (Map.Entry<Integer, Double> idfs : idf.entrySet())
            idf.put(idfs.getKey(), idfs.getValue()*sum);
        
        if (ClassifierModel.OUTPUT)
            for (Map.Entry<Integer, Double> idfs : idf.entrySet()) 
                System.out.println("IDF de " + idfs.getKey() + " = " + idfs.getValue());
    }
    
    
    protected void computeNIDF(Map<Integer, Double> probFin) {
        Map<Integer, Double> idf = new HashMap<Integer, Double>();
        
        computeIDFS(idf);
        
        // Nidf data structure
        Map<Integer, Double> nidf = new HashMap<Integer, Double>();
        
        // For each term, we compute the set of descendants, and we add to a set
        for (Map.Entry<Integer, Integer> termsOfDocument : m.entrySet()) {
            Set<Integer> ancestors = new HashSet<Integer>();
            Set<Integer> descendants = new HashSet<Integer>();
            
            Queue<Integer> toProcess = new LinkedList<Integer>();
            Queue<Integer> toProcess2 = new LinkedList<Integer>();
            ThesaurusTerm t = lex.getTermById(termsOfDocument.getKey().intValue());
            for (Map.Entry<Integer, Integer> descriptors : t.getDescriptors()) 
                toProcess.add(descriptors.getKey());
            
            for (Map.Entry<Integer, Integer> descriptors : t.getNonDescriptors()) 
            {
                NonDescriptor nd = lnd.get(descriptors.getKey());
                toProcess.add(nd.getDescriptor());
            }
                        
            while(!toProcess2.isEmpty()) 
            {
                Integer top = toProcess2.poll();
                
                if (!descendants.contains(top)) 
                {
                    descendants.add(top);
                    for (Integer i : (ld.get(top)).getDescendants())
                        if (!descendants.contains(i))
                            toProcess2.add(i);
                }
            }
            
            
            if (ClassifierModel.OUTPUT)
            {
                for (Integer d : ancestors)
                    System.out.println(" => " + d);
            }
            
            for (Integer d : descendants)
                if (nidf.containsKey(d))
                    nidf.put(d, nidf.get(d) + idf.get(t.getId()));
                else nidf.put(d, idf.get(t.getId()));
        } // for each term's descendant
   
        // we print nidfs
        
        if (ClassifierModel.OUTPUT)
        {
            System.out.println("Nidf values:");
            for (Map.Entry<Integer, Double> idfs : nidf.entrySet()) 
                System.out.println(" => " + idfs.getKey() + ": " + idfs.getValue());
        }
   
        
        // we recalculate probFin as probFin * nidf
        
        for (Map.Entry<Integer, Double> it : probFin.entrySet()) {
            int i = it.getKey();
            if (nidf.containsKey(i)) {
                //  double _nidf = nidf.get(i);
                double prob = it.getValue();
                probFin.put( i, prob * nidf.get(i) );
            } else probFin.put( i, 0.0 );
        }
    }    
    
    protected String internationalizeText(String s) {
        String _s = s.toLowerCase();
        _s = _s.replaceAll("á", "a");
        _s = _s.replaceAll("é", "e");
        _s= _s.replaceAll("í", "i");
        _s= _s.replaceAll("ó", "o");
        _s= _s.replaceAll("ú", "u");
        _s= _s.replaceAll("û", "u");
        return _s;  
    }
    
     
    abstract List<Result> classify(String textToClassify);
        
    public List<Result> classifyString(String s) {
        m.clear();
        si = new ThesaurusStringIndexer();
        si.setLexicon(IndexReader.lex);
        si.add(s);
        m = si.getFreq();
        return this.classify(s);
    }
    
    public List<Result> classifyFile(String fileName) {
        m.clear();
        super.si = new ThesaurusStringIndexer();
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader buff = new BufferedReader(new FileReader(fileName));
            
            while (buff.ready()) {
                String s = buff.readLine();
                si.add(s);
                sb.append(s);
            }
            
        } catch (IOException ex) {
            System.err.println("ERROR: file could not be read. Exiting. " + ex);
            System.exit(-1);
        }
        
        m = si.getFreq();
        return this.classify(sb.toString());
    }
    
    
    public ClassifierModel(String indexName, String stopwordsFileName) {
        super();
        // TODO Auto-generated constructor stub
               
        super.readIndex(indexName);
        
        ThesaurusStringIndexer.setStopwordList(stopwordsFileName);
        ThesaurusStringIndexer.setIndexing(false);
       
        m = new TreeMap<Integer, Integer>();
    }
}


