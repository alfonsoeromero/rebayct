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
 * TextFileDocument.java
 *
 * Created on 31 de enero de 2007, 23:43
 *
 */
package document;

import java.io.*;
/**
 *
 * @author aeromero
 */
public class TextFileDocument extends Document {
    /** Stream of the file */
    BufferedReader buff;
    
    /** Next line to be returned, null if any */
    String myLine;
    
    private static String encoding = "UTF8";
    
    private void nextLine() {
        try {
            if (buff.ready())
                myLine = buff.readLine();
            else myLine = null;
        } catch (IOException ex) {
            System.err.println("ERROR: file could not be read. Exiting. " + ex);
            System.exit(-1);
        } 
    }
    
    /**
     * Creates a new instance of TextFileDocument
     */
    public TextFileDocument(File f, String name, int id) {
        super(name, id);
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(f), encoding);
            buff = new BufferedReader(isr);            
            
            //buff = new BufferedReader(new FileReader(f));
            nextLine();
            
        } catch (IOException ex) {
            System.err.println("ERROR: file " + name + "could not be read. Exiting. " + ex);
            System.exit(-1);
        }
    }
    
    public boolean hasMoreLines() {
        return myLine != null;
    }
    
    public String getLine() {
        String s = myLine;
        this.nextLine();
        return s;
    }
    
}
