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
import base.*;

public class ThesaurusTerm extends BaseTerm {
	
	/** Frequency of the term in each descriptor */
	private Map<Integer, Integer> freqD;
	
	/** Frequency of the term in each nonDescriptor */
	private Map<Integer, Integer> freqND;
	
	/** Frequency of the term in each descriptor */
	private Map<Integer, Double> weightD;
	
	/** Frequency of the term in each nonDescriptor */
	private Map<Integer, Double> weightND;
	
	public ThesaurusTerm() {}
		
	public ThesaurusTerm(int _id, String _myString)
	{
		id = _id;
		myString = _myString;	
		freqD = new TreeMap<Integer, Integer>();
		freqND = new TreeMap<Integer, Integer>();
		weightD = new TreeMap<Integer, Double>();
		weightND = new TreeMap<Integer, Double>();
	}
	
	public ThesaurusTerm(String s)
	{
		myString = s;
		freqD = new TreeMap<Integer, Integer>();
		freqND = new TreeMap<Integer, Integer>();
		weightD = new TreeMap<Integer, Double>();
		weightND = new TreeMap<Integer, Double>();		
	}
			
	public void addDescriptor(int _id, int freq)
	{
		this.freqD.put(_id, freq);
	}
	
	public void addNonDescriptor(int _id, int freq)
	{
		this.freqND.put(_id, freq);
	}
	
	public void addWeightDescriptor(int _id, double w)
	{
		this.weightD.put(_id, w);
	}
	
	public void addWeightNonDescriptor(int _id, double w)
	{
		this.weightND.put(_id, w);
	}
	
	public Set< Map.Entry<Integer, Integer> > getDescriptors()
	{
		return this.freqD.entrySet();
	}
	
	public Set< Map.Entry<Integer, Integer> > getNonDescriptors()
	{
		return this.freqND.entrySet();
	}
	
	public Set< Map.Entry<Integer, Double> > getWeightDescriptors()
	{
		return this.weightD.entrySet();
	}
        
        public double getWeightOnDescriptor(int idDesc)
        {
          if (weightD.containsKey(idDesc))
              return weightD.get(idDesc);
          else return 0.0;            
        }
        
        public void setWeightOnDescriptor(int idDesc, double w)
        {
          if (weightD.containsKey(idDesc))
              weightD.put(idDesc, w);        
        }
        
         public double getWeightOnNonDescriptor(int idDesc)
        {
          if (weightND.containsKey(idDesc))
              return weightND.get(idDesc);
          else return 0.0;            
        }
        
        public void setWeightOnNonDescriptor(int idDesc, double w)
        {
          if (weightND.containsKey(idDesc))
              weightND.put(idDesc, w);        
        }

        
	public Set< Map.Entry<Integer, Double> > getWeightNonDescriptors()
	{
		return this.weightND.entrySet();
	}
	
	public void readFromString(String s)
	{
		StringTokenizer st = new StringTokenizer(s);
		try{
			  id = Integer.parseInt(st.nextToken());
			  myString = st.nextToken();
			  numDocs = Integer.parseInt(st.nextToken());
			  
			  int tam = Integer.parseInt(st.nextToken());
			  this.freqD = new TreeMap<Integer, Integer>();
			  for (int i=0; i<tam; ++i)
			       this.freqD.put(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
			  
			  tam = Integer.parseInt(st.nextToken());
			  this.freqND = new TreeMap<Integer, Integer>();
			  for (int i=0; i<tam; ++i)
			  	this.freqND.put(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
			  
			  tam = Integer.parseInt(st.nextToken());
			  this.weightD = new TreeMap<Integer, Double>();
			  for (int i=0; i<tam; ++i)
			  	  this.weightD.put(Integer.parseInt(st.nextToken()), Double.parseDouble(st.nextToken()));
			  
			  tam = Integer.parseInt(st.nextToken());
			  this.weightND = new TreeMap<Integer, Double>();
			  for (int i=0; i<tam; ++i)
			  	  this.weightND.put(Integer.parseInt(st.nextToken()), Double.parseDouble(st.nextToken()));
			  
		} catch (NumberFormatException  nex) {
			  System.err.println("Error constructing Term " + id + "\n");
			  System.exit(0);
		}
	}
	
    @Override
    public String toString() {
		String s = "";
		s += id;
		s += " ";
		s += myString;
		s += " ";
		s += numDocs;
		s += " ";
		
		s += freqD.size();
		s += " ";
		Set<Map.Entry<Integer, Integer>> myset = this.getDescriptors();
		for (Map.Entry<Integer, Integer> m : myset)
		  s = s + m.getKey() + " " + m.getValue() + " ";			
				
		s += freqND.size();
		s += " ";
		myset = this.getNonDescriptors();
		for (Map.Entry<Integer, Integer> m : myset)
		  s = s + m.getKey() + " " + m.getValue() + " ";			
				
		s += this.weightD.size();
		s += " ";
		Set<Map.Entry<Integer, Double> > _myset = this.getWeightDescriptors();
		for (Map.Entry<Integer, Double> m : _myset)
		  s = s + m.getKey() + " " + m.getValue() + " ";			
				
		s += this.weightND.size();
		s += " ";
		_myset = this.getWeightNonDescriptors();
		for (Map.Entry<Integer, Double> m : _myset)
		  s = s + m.getKey() + " " + m.getValue() + " ";			
					
		s += "\n";
		return s;
	}
	
}
