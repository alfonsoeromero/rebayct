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
 * BaseTerm.java
 *
 * Created on 13 de febrero de 2007, 13:29
 *
 */

package base;

/**
 *
 * @author aeromero
 */
abstract public class BaseTerm {
    /** Identifier of the term */
    protected int id;
    
    /** Number of documents the term appears in */
    protected int numDocs;
    
    /** String this term represents */
    protected String myString;
    
    public BaseTerm() { }
    
    /** Creates a new instance of BaseTerm */
    public BaseTerm(int myId, String mystr) {
        id = myId;
        myString = mystr;
        numDocs = 0;
    }
    
    public int getId() {
        return id;
    }
    
    public String getString(){
        return myString;
    }
    
    public int getNumDocs(){
        return numDocs;
    }
    
    public void setId(int myid) {
        id = myid;
    }
    
    public void incNumDocs() {
        ++numDocs;
    }
    
    abstract public void readFromString(String s);
    
}

