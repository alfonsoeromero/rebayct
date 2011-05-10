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
 * ClassifiedTerm.java
 *
 * Created on 19 de febrero de 2007, 13:46
 *
 */

package indexation;

import base.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
public class ClassifiedTerm extends BaseTerm 
{    
    //! total number of appearances of the term SUM_{i \in classes} numDocs.get(i)
    int totalFreq;
    
    //! total number of appearances of the term summing in the documents
    int totalDocumentalFreq;
    
    //! pairs of class/frequency
    public Map<Integer, Integer> occurrence;
    
    //! pairs of class/number of docs
    public Map<Integer, Integer> numDocsPerClass;
    
    
    public ClassifiedTerm(String s) {
        id = 0;
        numDocs = 0;
        totalFreq = 0;
        totalDocumentalFreq = 0;
        myString = s;
        occurrence = new HashMap<Integer, Integer> ();
        numDocsPerClass = new HashMap<Integer, Integer> ();
    }
    
    /** Returns the number of different classes in which this term appears in */
    public int getNumClasses()
    {
      return this.occurrence.size();
    }
    
    public Map<Integer, Integer> getOccurrence()
    {
        return occurrence;
    }
    
    @Override
    public String toString() {
        String s = "";
        s += id;
        s += " ";
        s += myString;
        s += " ";
        s += totalFreq;
        s += " ";
        s += totalDocumentalFreq;
        s += " ";
        s += numDocs;
        s += " ";
        for (Map.Entry<Integer, Integer> e : occurrence.entrySet()) {
            s += e.getKey();
            s += " ";
            s += e.getValue();
            s += " ";
            s += numDocsPerClass.get(e.getKey());
            s += " ";
        }
        s += "\n";
        return s;
    }
    
    public void add(int myclass, int freq) {
        if (occurrence.containsKey(myclass))
        {
            occurrence.put(myclass, occurrence.get(myclass) + freq);
            numDocsPerClass.put(myclass, numDocsPerClass.get(myclass) + 1);
        } else {
            occurrence.put(myclass, freq);
            numDocsPerClass.put(myclass, 1);
        }
        totalFreq += freq;
    }
    
    public int getFreqOnClassI(int i) {
        if  (occurrence.containsKey(i))
            return occurrence.get(i);
        else return 0;
    }
    
    public int getNumDocsPerClass(int i){
        if (this.numDocsPerClass.containsKey(i))
            return numDocsPerClass.get(i);
        else return 0;
    }
    
    public int getTotalFreq() {
        return totalFreq;
    }
    
    public int getTotalDocumentalFreq()
    {
        return totalDocumentalFreq;
    }
    
    public void addTotalDocumentalFreq(int myfreq)
    {
        this.totalDocumentalFreq += myfreq;
    }
    
    public void readFromString(String s) {
        
        try{
            StringTokenizer st = new StringTokenizer(s);
            occurrence = new HashMap<Integer, Integer>();
            numDocsPerClass = new HashMap<Integer, Integer>();
            
            id = Integer.parseInt(st.nextToken());
            myString = st.nextToken();
            totalFreq = Integer.parseInt(st.nextToken());
            totalDocumentalFreq = Integer.parseInt(st.nextToken());
            numDocs = Integer.parseInt(st.nextToken());
            while(st.hasMoreTokens()) 
            {
                int myclass = Integer.parseInt(st.nextToken());
                int myfreq = Integer.parseInt(st.nextToken());
                int myNumDocPerClass = Integer.parseInt(st.nextToken());
                occurrence.put(myclass, myfreq);
                numDocsPerClass.put(myclass, myNumDocPerClass);
            }
            
        } catch (NumberFormatException  nex) {
            System.err.println("Error constructing ClassifiedTerm " + id + "\n. String: " + s + "\n");
            System.exit(0);
        } catch (NullPointerException ex) {
            System.err.println("'" + s + "'");
        }
    }
}
