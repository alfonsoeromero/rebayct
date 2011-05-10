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
 * Created on 16 de mayo de 2007, 0:33
 *
 */

package supervisedClassifier;
import indexation.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
public class MultinomialNaiveBayes extends GenericNaiveBayes 
{    
    //! inverse of the denominator for positive probabilities
    protected HashMap<Integer, Double> inverseDenomY;
    
    //! inverse of the denominator for negative probabilities
    protected HashMap<Integer, Double> inverseDenomN;
    
    public double computeProbN(ClassifiedTerm ct, int freqOnDoc, int classId )
    {
        //double probN = (1.0 + ct.getTotalFreq() - ct.getFreqOnClassI(classId))*this.inverseDenomN.get(classId);
        double probN = (1.0 + ct.getTotalDocumentalFreq() - ct.getFreqOnClassI(classId))*this.inverseDenomN.get(classId);
        return Math.pow(probN, freqOnDoc);
    }
    
    public double computeProbY(ClassifiedTerm ct, int freqOnDoc, int classId )
    {
        double probY = (1.0 + ct.getFreqOnClassI(classId))*this.inverseDenomY.get(classId);
        if (super._DEBUGGING)
        {
            System.out.println(" numerator = 1.0 + " + ct.getFreqOnClassI(classId));
            System.out.println(" denominator =" + 1.0/this.inverseDenomY.get(classId));
            System.out.println(" denominator; N = " + super.cl.size() + "; Nti= " + super.totalFreq.get(classId));
        }
        return Math.pow(probY, freqOnDoc);
    }
    
    /** Creates a new instance of MultinomialNaiveBayes */
    public MultinomialNaiveBayes(String classIndexName) {
        super(classIndexName, FOR_EACH_TERM_IN_THE_DOCUMENT);
        super.mode_naiveBayes = super.BINARY_NAIVE_BAYES_FOR_EACH_CLASS;
        super.USE_TERM_SELECTION = false;
        //super.mode_naiveBayes = super.STANDALONE_BINARY_NAIVE_BAYES;
        inverseDenomY = new HashMap<Integer, Double>();
        inverseDenomN = new HashMap<Integer, Double>();
        
        int N = super.cl.size();
        int totalFreqOfTerms = 0;
        
        Set<Integer> classes = super.numDocs.keySet();
        
        for (int t=0; t<N; ++t)
            totalFreqOfTerms += cl.getTermById(t).getTotalDocumentalFreq();
        
        for (int i : classes)
        {
            double Ni = super.totalFreq.get(i);
            this.inverseDenomY.put(i, 1.0 / (N + Ni));
            this.inverseDenomN.put(i, 1.0 / (N - Ni + totalFreqOfTerms));        
        }
        
    }
    
}
