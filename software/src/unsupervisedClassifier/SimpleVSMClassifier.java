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
 * SimpleVSMClassifier.java
 *
 * Created on 24 de diciembre de 2006, 3:10
 *
 */

package unsupervisedClassifier;
import java.util.*;
import thesaurus.Descriptor;
import thesaurus.NonDescriptor;
import base.StringIndexer;


/**
 *
 * @author aeromero
 */
public class SimpleVSMClassifier extends ClassifierModel {    
    // Map with the idf for each term (different to those computed for BNClassifier!!!)
    Map <Integer, Double> idf;
    
    // tells if we are using the degree of coverage or not
    protected boolean useNidf = false;
    
    // For every descriptor, we get its associated tf*idf vector
    Map <Integer, Map<Integer, Double>> vectors;
    
    /** Computes the modulus of a certain vector
     * @param mp vector which modulus we want to compute
     */
    private double modulus(Map<Integer, Double> mp) {
        double myMod = 0.0;
        for (double d : mp.values())
            myMod += d*d;
        return Math.sqrt(myMod);
    }
    
    /** Computes the scalar product of two vectors
     * @param v1 first vector
     * @param v2 second vector
     */
    private double scalarProduct(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        double result = 0.0;
        for (int i : v1.keySet())
            if (v2.containsKey(i))
                result += v1.get(i)*v2.get(i);
        return result;
    }
    
    private Map<Integer, Double> scaleVector(double scalar, Map<Integer, Double> mp)
    {
      HashMap<Integer, Double> mp2 = new HashMap<Integer, Double>();
      for (Map.Entry<Integer, Double> me : mp.entrySet())
          mp2.put(me.getKey(), me.getValue()*scalar);
      return mp2;
    }
    
    /** Makes a vector to be 1-module (i. e. its norm to be 1)
     * @param v vector to normalize
     */
    private void normalizeVector(Map<Integer, Double> v) {
        double inv_mod = 1.0/modulus(v);
        for (Map.Entry<Integer, Double> component : v.entrySet())
            v.put( component.getKey(), component.getValue()*inv_mod );
    }
    
    
    
    private Map<Integer, Map<Integer, Double>> computeVectorsFromDescriptorsHierarchically()
    {
     // Return vector
      Map<Integer, Map<Integer, Double>> result = new HashMap<Integer, Map<Integer, Double>>(), simple;
      
      simple = this.computeVectorsFromDescriptors();
      
      Set<Integer> ids =  new HashSet<Integer>(ld.keySet());
      
      while (!ids.isEmpty())
      {
          Set<Integer> toRemove = new HashSet<Integer>();
          
          for (int i : ids)
          {
            Descriptor d = ld.get(i);
            List<Integer> desc = d.getDescendants();
            
            if (desc.isEmpty()) // if topTerm...
            {
              // we put in the result
                
              HashMap<Integer, Double> v = (HashMap<Integer,Double> ) simple.get(i); // own vector
              HashMap<Integer, Double> v2 = ((HashMap<Integer,Double>) v.clone());
              
              result.put(i, v2);
              toRemove.add(i);
              
            } else if ( result.keySet().containsAll(desc) ) {
              
              HashMap<Integer, Double> v = (HashMap<Integer,Double> ) simple.get(i); // own vector
              Map<Integer, Double> v2 = (HashMap<Integer,Double>) v.clone();
              for (int j : desc)
                v2 = this.sumVectors (v2, result.get(j) );
              
              result.put(i, v2);
              toRemove.add(i);
            }
            
          }
          ids.removeAll(toRemove);
      }
      
      return result;
    }
    
    
    
    private Map<Integer, Map<Integer, Double>> computeVectorsFromDescriptors() {
        // Return vector
        Map<Integer, Map<Integer, Double>> result = new HashMap<Integer, Map<Integer, Double>>();
        
        // frecuency vector
        Map <Integer, Integer> freqs = new HashMap<Integer, Integer>();
        
        // ---------- 1.- We compute the frecuency vectors --------------------
        
        // for every "i" descriptor
        for (int _d : ld.keySet()) {
            Descriptor d = ld.get(_d); // we recover it
            
            // we create its vector
            Map <Integer, Double> v = new HashMap<Integer, Double>();
            
            // for every term of the descriptor... we increase its frequency (initially 1)
            for (int t : d.getTerms())
                v.put(t, 1.0);
            
            // for every associated non-descriptor, we recover it and we add to the vector
            for (int _nd : d.getNonDescriptors()) {
                NonDescriptor nd = lnd.get(_nd);
                for (int t : nd.getTerms())
                    if (v.containsKey(t))
                        v.put(t, v.get(t) + 1.0);
                    else v.put(t, 1.0);
            }
            
            // we add to the result vector
            result.put(_d, v);
            
            // we update the frecuency vector
            for (int t : v.keySet())
                if (freqs.containsKey(t))
                    freqs.put(t, freqs.get(t) + 1);
                else freqs.put(t, 1);   
        }
        
        // ------------ 2.- We compute the idfs ----------------------
        
        idf = new HashMap<Integer, Double>();
        
        for (int i=0; i<lex.size(); ++i)
            if (freqs.containsKey(i))
                idf.put(i, Math.log(ld.size()/freqs.get(i)) );
            else idf.put(i, Math.log(ld.size()));
        
        // we free memory
        freqs.clear();
        freqs = null;
        
        // ------------ 3.- We multiply the vectors by the idf -------
        
        // for every "i" descriptor
        for (int i : ld.keySet())
        {
            for (int t : result.get(i).keySet()) // for each term of the i-th vector
              result.get(i).put(t, result.get(i).get(t) * idf.get(t));            
        }
        
        return result;
    }
    
    
    /** Sums two vectors and return the result
     @param v1 first vector
     @param v2 second vector
     @return v1 + v2     
     */
    public Map<Integer, Double> sumVectors(Map<Integer, Double> v1, Map<Integer, Double> v2)
    {
      Map<Integer, Double> result = new HashMap <Integer, Double>();      
      
      // for every key in the first set 
      for (int i : v1.keySet())
      {
        double val = v1.get(i);
        if (v2.containsKey(i))
            val += v2.get(i);        
        result.put(i, val);      
      }
      
      // for every key in the second set - first set
      for (int i : v2.keySet())
      {
        if (!v1.containsKey(i))
            result.put(i, v2.get(i)); 
      }
      
      return result;
    }
        
    /** Creates a new instance of SimpleVSMClassifier */
    public SimpleVSMClassifier(String indexName, String stopwordsFileName, boolean hierarchical, boolean stemming, boolean usingNidf) {
        super(indexName, stopwordsFileName);
        
        if (!hierarchical)
            vectors = this.computeVectorsFromDescriptors(); // simple method, no hierarchy considered
        else 
            vectors = this.computeVectorsFromDescriptorsHierarchically();
        
        
        for (Map<Integer, Double> v : vectors.values())
            this.normalizeVector(v);
        
        StringIndexer.setStem(stemming);
        
        this.useNidf = usingNidf;
    }
       
    List<Result> classify(String textToClassify) {
        
        List<Result> l = new ArrayList<Result>();
        
        // 1.- We create the vector from m, called "query" (with tf*idf of the text to classify)
        Map<Integer, Double> query = new HashMap<Integer, Double>();
                
        int i =0;
        
        for (Map.Entry<Integer, Integer> val : m.entrySet())
        {
            i = val.getKey();
            double tfidf = val.getValue() * idf.get(val.getKey());
            query.put(val.getKey(), tfidf);
        }

        // 2.- We normalize this vector
        this.normalizeVector(query);
        
        // 3.- For every descriptor vector, we compute the angle with the query vector (score)
        Map<Integer, Double> score = new HashMap<Integer, Double>();
        
        for (Map.Entry<Integer, Map<Integer, Double>> vi : vectors.entrySet())
        {
          
          double cos_angle = this.scalarProduct(query, vi.getValue());
          
          // REMEMBER VECTORS ARE ALREADY NORMALIZED!!
          if (cos_angle > 0.0)
              score.put(vi.getKey(), cos_angle);
          
        }
        
        // nidf usage?        
        if(useNidf)
          this.computeNIDF(score);        
        
        // 4.- We sort the list by its value of score (angle) (class Result is comparable, so we only
        // have to call the method "sort" of the class Collections passing "l").
        
        for (Map.Entry<Integer, Double> result : score.entrySet() ) {
            Descriptor d = ld.get(result.getKey());     
            l.add(new Result( d, result.getValue() ));
        }
        
        Collections.sort(l, new Comparator<Result>() { public int compare(Result s1, Result s2) { if (s1.getProb() > s2.getProb()) return -1; else return 1; } });

        
        return l;
    }
}

