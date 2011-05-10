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
 * IndexedDocument.java
 *
 * Created on 13 de febrero de 2007, 12:53
 *
 */

package base;

import java.util.*;

/**
 *
 * @author aeromero
 */
public final class IndexedDocument {
    //! identifier of the document
    String identifier;
    
    //! id of the document
    int id;
    
    //! document length (sum of all freq)
    int length;
    
    //! pair term frequencies
    HashMap<Integer, Integer> freq;
    
    /** Empty constructor: creates a new instance of IndexedDocument */
    public IndexedDocument() {
        freq = new HashMap<Integer, Integer>();
        length = 0;
    }
    
    public IndexedDocument(HashMap<Integer, Integer> myfreq, String identifier, int _id)
    {
      id = _id;
      length = 0;
      freq = myfreq;
      for (int t : freq.values())
        length += t;
    }
        
    public int getId(){ return id; }
    
    /** Creates a new instance of IndexedDocument from a representing string
       @param s string representing a IndexedDocument
     */
    public IndexedDocument(String s)
    {
      this.readFromString(s);
    }
    
    /** Returns the frequency of a certain term in this
        document, 0 if the term does not appear in it   
     */
    public int getFreqAtTerm(int term)
    {
      if (freq.containsKey(term))
          return freq.get(term);
      else return 0;
    }
    
    /** Returns one document's length (i.e. the sum of all frequencies for all term ids)
       @return document length
     */
    public int getLength()
    {
      return length;
    }
    
    /** Returns the list of terms with positive freqs
      @return set of terms
     */
    public Set<Integer> getListOfTerms()
    {
      return freq.keySet();
    }
    
    
    /** Returns a string with the representation of the document with this format
     *  NUMBER_OF_ELEMENTS TERM1 FREQ(TERM1) ... TERMN FREQ(TERMN)
     *  @return string with the representation     
     */
    protected String getRepresentation()
    {
      String s = "";
      s += id;
      s += " ";
      s += length;
      s += " ";
      s += freq.size();
      s += " ";
      for (int t : freq.keySet())
      {
        s += t;
        s += " ";
        s += freq.get(t);
        s += " ";
      }
      
      s += "\n";
      
      return s;
    }
    
    @Override
    public String toString()
    {
      return this.getRepresentation();
    }
    
    
    public HashMap<Integer, Integer> getMap(){
        return this.freq;
    }
    /**
     
     */
    protected void readFromString(String s)
    {
      freq = new HashMap <Integer, Integer>();
      StringTokenizer st = new StringTokenizer(s);
      int myLength = 0;
      
      try{
        
        id = Integer.parseInt(st.nextToken());
        length = Integer.parseInt(st.nextToken());
        
        int tam = Integer.parseInt(st.nextToken());
        
        for (int i=0; i<tam; i++)
        {
          int term = Integer.parseInt(st.nextToken());
          int termfreq = Integer.parseInt(st.nextToken());
          freq.put(term, termfreq);
          myLength += termfreq;
        }
        
      } catch (Exception ex){
        System.err.println("Error when processing string: '" + s + "'. Exiting");
        System.exit(-1);
      }
      
      if (myLength != length)
      {
        System.err.println(length + " != " + myLength);
        System.err.println("Error when processing string: '" + s + "'. Corrupted frequencies. Exiting");
        System.exit(-1);
      }
    }    
}
