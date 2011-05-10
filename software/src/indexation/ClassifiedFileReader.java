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
 * ClassifiedFileReader.java
 *
 * Created on 19 de febrero de 2007, 20:59
 *
 */

package indexation;

import java.io.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
public class ClassifiedFileReader {
    //! reader of the file
    BufferedReader in;
    
    //! for each class stores the list of documents classified by that class
    Map<Integer, List<Integer>> data;
    
    //! set of the different classes
    Set<Integer> classes;
    
    //! number of classes
    int numClasses;
    
    //! returns the number of classes
    public int getNumClasses()
    {
        return numClasses;
    }
    
    //! returns the set of classes
    public Set<Integer> getSetOfClasses()
    {
        return this.classes;
    } 
    
    /**
     * Creates a new instance of ClassifiedFileReader
     */
    public ClassifiedFileReader(String fileName) 
    {
        try {
          in = new BufferedReader(new FileReader(fileName));
        } catch (Exception ex) {
            System.err.println("ERROR: Media not ready. " + ex + ". Exiting");
            System.exit(-1);            
        }
        
        data = new HashMap<Integer, List<Integer>>();
        classes = new HashSet<Integer>();
        int line = 1;
        
        try {
           line++;
           
           while (in.ready())
           {
             String myline = in.readLine();
             StringTokenizer sb = new StringTokenizer(myline);
             int myDoc = Integer.parseInt(sb.nextToken());
             
             // for each document identifier... we read the corresponding classes
             
             List<Integer> l = new ArrayList<Integer>();
             
             while(sb.hasMoreTokens())
             {
               int myClass = Integer.parseInt(sb.nextToken()); // class for document myDoc
               if (!l.contains(myClass))
                 l.add(myClass);  // we avoid duplicated classes
               
               if (!this.classes.contains(myClass))
                this.classes.add(myClass);
             }
             
             data.put(myDoc, l);
           }
           
           numClasses = this.classes.size();
           
        } catch (NumberFormatException ex) {
          System.err.println("ERROR: wrong format number at line " + line + ". Exiting.");
          System.exit(-1);
          
        } catch (IOException ex) {
            System.err.println("ERROR: Media not ready. " + ex + ". Exiting");
            System.exit(-1);            
        }
        System.out.println("Class file read");
    }
    
    public Map<Integer, List<Integer>> getData()
    {
      return data;
    }
    
} 

