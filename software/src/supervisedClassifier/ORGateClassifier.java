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
 * MultinomialNaiveBayes.java
 *
 * Created on 1 de marzo de 2007, 17:23
 *
 */

package supervisedClassifier;
import java.util.*;
import indexation.*;

/**
 *
 * @author aeromero
 */
public class ORGateClassifier extends GenericClassifier {
    
    
    //! total size of the lexicon
    protected int N;
    
    protected HashMap<Integer, HashMap<Integer, Double>> weigth;
    
    protected static final boolean LAPLACE = true;
    
    protected static final boolean useApriori = false;
    
    protected static final boolean useAprioriProd = false;
    
    protected static final boolean useAprioriOR = true;
    
    /**
     * Creates a new instance of MultinomialNaiveBayes
     */
    public ORGateClassifier(String classIndexName) {
        super(classIndexName);
        
        N = cl.size();
        
        
        weigth = new HashMap<Integer, HashMap<Integer, Double>>();
        
        // For each term, we compute the weight in its class
        for (int i=0; i<N; ++i) {
            ClassifiedTerm ct = cl.getTermById(i);
            
            Map<Integer, Integer> mp = ct.getOccurrence();
            double inv = 0.0;
            if (ORGateClassifier.LAPLACE)
                inv = 1.0/(ct.getTotalDocumentalFreq() + 2.0);
            else inv = 1.0/ct.getTotalDocumentalFreq();
            
            HashMap<Integer, Double> h = new HashMap<Integer, Double>();
            
            for (Map.Entry<Integer, Integer> entry : mp.entrySet())
                if (!ORGateClassifier.LAPLACE)
                    h.put(entry.getKey(), entry.getValue() * inv);
                else h.put(entry.getKey(), (entry.getValue() + 1.0) * inv);
            
            weigth.put(i, h);
        }
    }
    
    /** Receives a list of pairs (term_identifier, frequency) and returs a list of (class_identifiers, relevance)
     */
    public HashMap<Integer, Double> classifyDocument(HashMap<Integer, Integer> _document, int docId) {
        // return map
        HashMap<Integer, Double> ret = new HashMap<Integer, Double>();
        Map<Integer, Integer> document = super.translateVector(_document);
        
        // set of classes this document could be classified under
        Set<Integer> classes = super.numDocs.keySet();
        //Set<Integer> classes = this.ap.getSetOfAprioriClasses(docId);
        
        // 2.- then, for each term, of the document...
        for (int term : document.keySet()) {
            // we get the term
            ClassifiedTerm cterm = cl.getTermById(term);
            
            // for each class of the occurrence
            Map<Integer, Integer> mp = cterm.getOccurrence();
            
            HashMap<Integer, Double> hm = this.weigth.get(term);
            
            for (int myclass : mp.keySet()) {
                if (ret.containsKey(myclass))
                    ret.put(myclass, ret.get(myclass) * (1.0 - hm.get(myclass)) );
                else ret.put( myclass, 1.0 - hm.get(myclass) );
            }
        }
        
        
        if (ORGateClassifier.useApriori) {
            
            
            if (ORGateClassifier.useAprioriProd)
            {
            
                // 3.- we arrange the values of the or gates putting 1.0-value instead value
                for (int id : ret.keySet())
                    ret.put(id, 1.0 - ret.get(id));
            
                for (int id : ret.keySet())
                    ret.put(id, super.apriori.get(id)*ret.get(id));
            } else if (ORGateClassifier.useAprioriOR) {
            
                for (int id : ret.keySet())
                    ret.put(id, ret.get(id)*(1.0-super.apriori.get(id)));
                
                // 3.- we arrange the values of the or gates putting 1.0-value instead value
                for (int id : ret.keySet())
                    ret.put(id, 1.0 - ret.get(id));
            
            } else System.err.println("ERROR, no apriori combination method is selected");
            
        } else {
        
                // 3.- we arrange the values of the or gates putting 1.0-value instead value
                for (int id : ret.keySet())
                    ret.put(id, 1.0 - ret.get(id));
        }
        
        return ret;
    }
}




