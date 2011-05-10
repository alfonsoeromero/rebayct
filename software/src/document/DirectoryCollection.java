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
 * DirectoryCollection.java
 *
 * Created on 7 de febrero de 2007, 12:15
 *
 */

package document;
import java.io.File;

/**
 *
 * @author aeromero
 */
public class DirectoryCollection extends Collection {
    //! index of the next document to return
    int i;    
    
    /** List of the representation of files */
    File[] fileList;    
    
    /** List of the file names */
    String[] filenameList;
    
    /** Base address */
    String baseDir;
    
    /**
     * Creates a new instance of DirectoryCollection
     */
    public DirectoryCollection(String directory) throws SecurityException
    {
        i = 1;
        baseDir = directory;
        File myFile = new File ( baseDir ) ;
        
        fileList = myFile.listFiles();
        filenameList = myFile.list();
        
        setSize(fileList.length);
    }
    
    /** Return i-th document 
       @returns null if there were some problem     
     */
    public Document getNextDocument( )
    { 
      if (i <= size && i >= 1) // i goes from 1 to N, the array from 0 to N-1
      {
        Document d = new TextFileDocument(fileList[i-1], filenameList[i-1], i); 
        ++i;
        return d;
      } else {
        System.err.println("WARNING: no more documents left. Trying to acess beyond boundaries of the collection.");          
        return null;
      }
    }
}


