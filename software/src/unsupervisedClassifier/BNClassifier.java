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
 * BNClassifier.java
 *
 * Created on 24 de diciembre de 2006, 0:34
 */

package unsupervisedClassifier;
import java.util.*;
import thesaurus.Descriptor;
import thesaurus.NonDescriptor;
import base.StringIndexer;
import thesaurus.ThesaurusTerm;

/**
 *
 * @author aeromero
 */
public class BNClassifier extends ClassifierModel {
    // tells if we are using stemming or not
    protected boolean useStemming = false;
    
    // tells if we are using the degree of coverage or not
    protected boolean useNidf = false;
    
    // tells if we are using exact phrases or not
    protected boolean useExactPhrases = false;
    
    //
    protected static double associativity = 0.9;
    
    protected static double associativityb = 1.0;
    
    protected static final boolean DEBUGGING = false;
    
    /** Creates a new instance of BNClassifier */
    public BNClassifier(String indexName, String stopwordsFileName, boolean doingStemming, boolean usingNidf, boolean usingExactPhrases) {
        super(indexName, stopwordsFileName);
        this.useStemming = doingStemming;
        this.useNidf = usingNidf;
        this.useExactPhrases = usingExactPhrases;
        StringIndexer.setStem(doingStemming);
    }
    
    protected void topological(int S, List<Integer> topologicallist, List<Integer> queue) {
        Descriptor _S = ld.get(S);
        for (Integer U : _S.getFathers())
            if (queue.contains(U))
                if (!topologicallist.contains(U))
                    topological(U, topologicallist, queue);
        topologicallist.add(S);
    }
    
    /** Inserts a descriptor on a list, and all its descendants, recursively */
    protected void insertDescendantsOnQueue(int S, List<Integer> queue) {
        queue.add(S);
        Descriptor _S = ld.get(S);
        for (Integer U : _S.getDescendants())
            if (!queue.contains(U))
                insertDescendantsOnQueue(U,queue);
    }
    
    /* Propagates probability from Descriptors to themselves */
    protected void propagateDToD (Map<Integer, Double> probC, Map<Integer, Double> probFin) {
        List<Integer> queue = new ArrayList<Integer> ();
        for (Map.Entry<Integer, Double> desc : probC.entrySet()) {
            Descriptor _D = ld.get(desc.getKey());
            insertDescendantsOnQueue(_D.getId(),queue);
        }
        
        List<Integer> topologicallist = new ArrayList<Integer> ();
        while  (!queue.isEmpty()) {
            Integer S = queue.get(0);
            queue.remove(0);
            if (!topologicallist.contains(S))
                topological(S, topologicallist, queue);
        }
        while  (!topologicallist.isEmpty()) {
            Integer S = topologicallist.get(0);
            topologicallist.remove(0);
            double acum = 0.0;
            Descriptor _S = ld.get(S);
            for (Integer U : _S.getFathers())
                if (probFin.containsKey(U))
                    acum += probFin.get(U)/_S.getFathers().size();
            
            if (probC.containsKey(S))
                probFin.put(S,1.0-(1.0-associativityb*acum)*probC.get(S));
            else probFin.put(S,1.0-(1.0-associativityb*acum));
        }
        
        /*
       final double andweight = 0.9;//0.5; //por ejemplo
       while  (!topologicallist.isEmpty()) {
           Integer S = topologicallist.get(0);
           topologicallist.remove(0);
           Descriptor _S = ld.get(S);
           double acum;
           if (_S.getFathers().size()>0) {
               acum = 1.0;
               for (Integer U : _S.getFathers())
                   if (probFin.containsKey(U))
                       acum *= 1.0-andweight*(1.0-probFin.get(U));
                   else acum *= 1.0-andweight;
           } else acum = 0.0;
           if (probC.containsKey(S))
               probFin.put(S,1.0-(1.0-associativityb*acum)*probC.get(S));
           else probFin.put(S,1.0-(1.0-associativityb*acum));
       }
         */
    }
    
    protected void endsPropagation(Map<Integer, Double> probC, Map<Integer, Double> probFin) {
        for (int D : probC.keySet()) 
            if (!probFin.containsKey(D))
                probFin.put(D, 1.0-probC.get(D));
    }
    
    protected void propagateNDToD(Map<Integer, Double> probD, Map<Integer, Double> probND, Map<Integer, Double> probC) {
        // For each non-descriptor...
        for (Map.Entry<Integer, Double> nonDesc : probND.entrySet()) {
            NonDescriptor ND = lnd.get(nonDesc.getKey());
            int D = ND.getDescriptor();
            double prob = nonDesc.getValue();
            
            if (probC.containsKey(D))
                probC.put(D, probC.get(D)*(1.0-prob*associativity) );
            else
                probC.put(D, (1.0-prob*associativity) );
        }
        
        // For each descriptor
        for (Map.Entry<Integer, Double> descr : probD.entrySet()) {
            Descriptor _D = ld.get(descr.getKey());
            int D = _D.getId();
            double prob = descr.getValue();
            
            if (probC.containsKey(D))
                probC.put(D, probC.get(D)*(1.0-prob*associativity) );
            else
                probC.put(D, (1.0-prob*associativity) );
        }
    }
    
    
    protected void propagateTextToDescriptorsWithExactPhrases(Map<Integer, Double> probD,
            Map<Integer, Double> probND, String _text) {
        String text = _text.toLowerCase().trim();
        
        Map<Integer, Integer> countD = new HashMap<Integer, Integer> ();
        Map<Integer, Integer> countND = new HashMap<Integer, Integer> ();
        double K = Math.log10(ld.size() + lnd.size());
        
        // for each term that appears in the text... (in m)
        for (Map.Entry<Integer, Integer> t : m.entrySet()) {
            ThesaurusTerm _t = lex.getTermById(t.getKey());
            
            // For each descriptor...
            for (Map.Entry<Integer, Double> weightD : _t.getWeightDescriptors() ) {
                int id = weightD.getKey();
                Descriptor d = ld.get(id);
                double omega = d.getSumWeights();
                double prob = weightD.getValue()*omega/(omega + K);
                
                if (probD.containsKey(id))
                    probD.put(id, probD.get(id) + prob);
                else probD.put(id, prob);
                
                if (countD.containsKey(id))
                    countD.put(id, countD.get(id)+1);
                else countD.put(id, 1);
                
                if (this.useStemming) {
                    
                    if (countD.get(id)==d.getTerms().size() && text.indexOf(d.getDescription().toLowerCase())!=-1)
                        probD.put(id, 1.0);
                    
                } else {
                    
                    if (d.getTerms().size()==1 && countD.get(id)==1)
                        probD.put(id, 1.0);
                    
                    if ( d.getTerms().size()>1 &&  countD.get(id)==d.getTerms().size() && text.indexOf(d.getDescription().toLowerCase())!=-1)
                        probD.put(id, 1.0);
                }
            }
            
            // For each nondescriptor...
            for (Map.Entry<Integer, Double> weightND : _t.getWeightNonDescriptors()) {
                int id = weightND.getKey();
                NonDescriptor nd = lnd.get(id);
                double omega = nd.getSumWeights();
                double prob = weightND.getValue()*omega/(omega + K);
                
                if (probND.containsKey(id))
                    probND.put(id, probND.get(id) + prob);
                else probND.put(id, prob);
                
                if (countND.containsKey(id))
                    countND.put(id, countND.get(id)+1);
                else countND.put(id, 1);
                
                if (this.useStemming) {
                    if (countND.get(id)==nd.getTerms().size()&& text.indexOf(nd.getDescription().toLowerCase())!=-1)
                        probND.put(id, 1.0);
                    
                } else {
                    if (nd.getTerms().size()==1 && countND.get(id)==1)
                        probND.put(id, 1.0);
                    
                    if (nd.getTerms().size()>1 && countND.get(id)==nd.getTerms().size()&& text.indexOf(nd.getDescription().toLowerCase())!=-1)
                        probND.put(id, 1.0);
                }
            }
        }
        
    }
    
    protected void propagateTextToDescriptorsWithoutExactPhrases(Map<Integer, Double> probD, Map<Integer, Double> probND) {
        // for each term that appears in the text... (in m)
        for (Map.Entry<Integer, Integer> t : m.entrySet()) {
            ThesaurusTerm _t = lex.getTermById(t.getKey());
            
            if (BNClassifier.DEBUGGING)
                System.out.println("Propagando término " + _t.getString());
            
            
            // For each descriptor...
            for (Map.Entry<Integer, Double> weightD : _t.getWeightDescriptors() ) {
                
                int id = weightD.getKey();
                double prob = weightD.getValue();
                
                if (BNClassifier.DEBUGGING)
                    System.out.println("\t En descriptor " + id + " tiene peso " + prob);
                
                if (probD.containsKey(id))
                    probD.put(id, probD.get(id) + prob);
                else probD.put(id, prob);
            }
            
            // For each nondescriptor...
            for (Map.Entry<Integer, Double> weightND : _t.getWeightNonDescriptors()) {
                int id = weightND.getKey();
                double prob = weightND.getValue();
                
                int midesc = lnd.get(id).getDescriptor();
                
                if (BNClassifier.DEBUGGING) {
                    System.out.println("\t En no descriptor " + id + " tiene peso " + prob);
                    System.out.println("\t  Descriptor hijo => " + midesc);
                }
                
                
                if (probND.containsKey(id))
                    probND.put(id, probND.get(id) + prob);
                else probND.put(id, prob);
            }
        }
    }
    
    List<Result> classify(String textToClassify) {
        // 0.- we get rid of the accents and other similar stuff
        String _textToClassify = internationalizeText(textToClassify);
        
        if (BNClassifier.DEBUGGING)
            System.out.println("texto: " + textToClassify);
        
        // 1.- For each term of the collection...
        Map<Integer, Double> probD = new TreeMap<Integer, Double>();
        Map<Integer, Double> probND = new TreeMap<Integer, Double>();
        
        // ... we propagate textual probabilities
        if (this.useExactPhrases)
            propagateTextToDescriptorsWithExactPhrases(probD, probND, _textToClassify);
        else propagateTextToDescriptorsWithoutExactPhrases(probD, probND);
        
        // 2.- For each nondescriptor, we propagate to its
        // 	related descriptor
        Map<Integer, Double> probC = new TreeMap<Integer, Double>();
        propagateNDToD(probD, probND, probC);
        
        if (BNClassifier.DEBUGGING) {
            System.out.println("Imprimiendo probC: ");
            for (int i : probC.keySet())
                System.out.println(" =>" + i + " " + probC.get(i));
        }
        
        // we clear the maps
        probD = null;
        probND = null;
        
        // 3.- We propagate among the descriptors, following the BT relation
        Map<Integer, Double> probFin = new HashMap<Integer, Double>();
        propagateDToD(probC, probFin);
        
        if (BNClassifier.DEBUGGING) {
            System.out.println("Propagate DtoD: ");
            for (int i : probFin.keySet())
                System.out.println(" =>" + i + " " + probFin.get(i));
        }
        
        endsPropagation(probC, probFin);
        
        if (BNClassifier.DEBUGGING) {
            System.out.println("Remata: ");
            for (int i : probFin.keySet())
                System.out.println(" =>" + i + " " + probFin.get(i));
        }
        
        
        // 4.- We compute nIdf_q for each node, and we make the product of
        //   the probability with that
        
        if(useNidf)
            this.computeNIDF(probFin);
        
        if (BNClassifier.DEBUGGING) {
            System.out.println("Idf calculado: ");
            for (int i : probFin.keySet())
                System.out.println(" =>" + i + " " + probFin.get(i));
        }
        
        probC = null;
        List<Result> l = new ArrayList<Result>();
        
        for (Map.Entry<Integer, Double> result : probFin.entrySet() ) {
            if (result.getValue() > 0.0){
                Descriptor d = ld.get(result.getKey());
                l.add(new Result( d, result.getValue() ));       
            }
        }
        
        Collections.sort(l, new Comparator<Result>() { public int compare(Result s1, Result s2) { if (s1.getProb() > s2.getProb()) return -1; else return 1; } });
        return l;
    }
}

