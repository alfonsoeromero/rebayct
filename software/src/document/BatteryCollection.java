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
 * BatteryCollection.java
 *
 * A collection that is stored in a directory of separated text files
 *
 */

package document;

import java.io.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
public class BatteryCollection extends Collection
{    
    //! Current line of the cursor reading the battery
    int cursor;
    
    //! Name of the file containing the battery
    String batteryFileName;
    
    //! The battery itself can be considered as a text file document
    TextFileDocument battery;
    
    //! Document to return
    InMemoryDocument doc;
    
    /**
     * Creates a new instance of BatteryCollection
     */
    public BatteryCollection(String mybatteryFileName) 
    {
        doc = null;
        batteryFileName = mybatteryFileName;
        cursor = 1; // because we are prefetching line 1
        battery = new TextFileDocument (new File(batteryFileName), batteryFileName, 0);
        String number;
        if (battery.hasMoreLines())
        {
          number = battery.getLine();
          try{
            size = ((Integer) Integer.parseInt(number)).intValue();
          } catch(NumberFormatException ex) {
            System.err.println("ERROR: Malformed battery file " + batteryFileName + ". First line should contain the number of documents of the collection. Exiting.");
            System.exit(-1);
          }
          processNextDocument();
        } else {
            System.err.println("ERROR: Empty battery file. " + mybatteryFileName);
            System.exit(-1);
        }
    }
    
    private void processNextDocument()
    {
      // cursor must be equal to the number found on the line
        
      // 1.- we get a whole line of the battery
      String myLine = this.battery.getLine();
      
      StringTokenizer st = new StringTokenizer(myLine);
      
      int number = 0;
      
      try {
          
        // 2.- we extract the first token (string representing an integer)
        String firstToken = st.nextToken();
        
        // 2.1.- we convert the integer (NumberFormatException is thrown if an error happens)
        number = ((Integer) Integer.parseInt(firstToken)).intValue();
        
        // 3.- we take the rest of the line as a document representantion
        String documentText = st.nextToken("\n");
        
        // 4.- we build the document
        doc = new InMemoryDocument(documentText, this.batteryFileName + "_" + number, number);
        
      } catch (NoSuchElementException ex) {
        System.out.println("ERROR: malformed battery file " + this.batteryFileName + " at line " + cursor + ". Exiting.");
        System.exit(-1);
      } catch(NumberFormatException ex) {
        System.err.println("ERROR: Malformed battery file " + this.batteryFileName + " at line " + cursor + ". Number " + number + " found instead of " + cursor + ". Exiting.");
        System.exit(-1);
      }
    }   
    
    public Document getNextDocument( )
    {
      InMemoryDocument mydoc = null;
      if (doc != null)
      {
        mydoc = doc;
        if (battery.hasMoreLines())
        {
            processNextDocument();
            ++cursor;
        } else doc = null;
     
      } else 
        System.err.println("WARNING: no more documents left on the battery. Trying to acess beyond boundaries of the collection.");
      
      return mydoc;
    }
}
