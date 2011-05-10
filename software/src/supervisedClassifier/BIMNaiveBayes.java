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
 * BIMNaiveBayes.java
 *
 * Created on 16 de mayo de 2007, 1:04
 */

package supervisedClassifier;
import java.util.*;
import indexation.*;

/**
 *
 * @author aeromero
 */
public final class BIMNaiveBayes  extends GenericNaiveBayes
{    
    //! inverse of the denominator for positive probabilities
    protected HashMap<Integer, Double> inverseDenomY;
    
    //! inverse of the denominator for negative probabilities
    protected HashMap<Integer, Double> inverseDenomN;
    
    public double computeProbN(ClassifiedTerm ct, int freqOnDoc, int classId )
    {
        double prob = (1.0 + ct.getNumDocs() - ct.getNumDocsPerClass(classId)) * this.inverseDenomN.get(classId);
        if (freqOnDoc == 0) return 1.0 - prob;
        else return prob;
    }
        
    public double computeProbY(ClassifiedTerm ct, int freqOnDoc, int classId )
    {
        double prob = (1.0 + ct.getNumDocsPerClass(classId)) * this.inverseDenomY.get(classId);
        if (freqOnDoc == 0) return 1.0 - prob;
        else return prob;
    }
    
    /** Creates a new instance of BIMNaiveBayes */
    public BIMNaiveBayes(String classIndexName) {
        super(classIndexName, FOR_EACH_TERM);
        inverseDenomY = new HashMap<Integer, Double>();
        inverseDenomN = new HashMap<Integer, Double>();
        
        Set<Integer> classes = super.numDocs.keySet();
        int N = 2;
        
        for (int i : classes)
        {
            double Ni = super.numDocs.get(i);
            this.inverseDenomY.put(i, 1.0 / (N + Ni));
            this.inverseDenomN.put(i, 1.0 / (N + super.numDocuments - Ni));
            
            double probY = 1.0, probN = 1.0;
            for (int j=0; j<cl.size(); ++j)
            {
                probY *= this.computeProbY(cl.getTermById(j), 0, i);
                probN *= this.computeProbY(cl.getTermById(j), 0, i);
            }
            super.probNTotal.put(i, probN);
            super.probYTotal.put(i, probY);            
        }
        
    }
    
}


