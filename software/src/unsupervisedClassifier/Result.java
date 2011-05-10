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

import thesaurus.Descriptor;

public class Result implements Comparable
{
	/** Associated descriptor */
	private Descriptor desc;
	
	/** Probability of being classified by 
	 * this descriptor */
	private double prob;
	
	public int compareTo (Object o)
	{
	  if (prob > ((Result) o).getProb())
	    return -1;
	  else if (prob < ((Result) o).getProb())
	    return 1;
	  else return 0;
	}
        
        public Result()
        {
          prob = 0.0;
        }

	public Result(Descriptor d, double proba) 
        {
	  desc = d;
          prob = proba;
	}
	
	public double getProb()
	{
	  return prob;
	}
	
	public void setProb(double _prob)
	{
	  this.prob = _prob;
	}
	
	public Descriptor getDescriptor()
        {
	  return this.desc;
	}
        
        public String toEval(int queryId, int posRanking)
        {
          if (desc != null)
            return queryId + " " + this.desc.getId() + " " + posRanking + " " + this.prob + "\n";
          else return queryId + " " + 0 + " " + posRanking + " " + this.prob + "\n";
        }
}
