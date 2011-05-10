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

package base;
import java.util.*;
import java.io.*;

public class Lexicon <T extends BaseTerm> {
    /** Number of terms of the lexicon */
    protected int size;
    
    /** Terms by identifier */
    protected Map<Integer, T> termById;
    
    /** Identifiers by string */
    protected Map<String, Integer> termIdByString;
    
    /** Empty constructor */
    public Lexicon() {
        // map initialization
        size = 0;
        termById = new TreeMap<Integer, T>();
        termIdByString = new TreeMap<String, Integer>();
    }
    
    /** Returns lexicon's size */
    public int getSize() {
        return size;
    }
    
    /** Returns the term associated to a certain identifier
     * @param id identifier of the term we want to obtain
     * @return corresponding object, null if it did not exist
     * */
    public T getTermById(int id) {
        return termById.get(id);
    }
    
    /** Returns the identifier associated to a string, if any
     * @param s string we want to obtain its identifier
     * @return identifier of term, -1 if it did not exist
     * */ 
    public int getTermIdByString(String s) {
        if (termIdByString.containsKey(s))
            return termIdByString.get(s);
        else return -1;
    }
    
    /** Returns the number of terms of the lexicon
     * @return the number of terms of the lexicon
     * */
    public int size(){ return size; }
    
    /** Adds a new term to the Lexicon and sets its identifier
     * @param t new term to add
     * */
    public void add(T t) {
        int newId = size;
        t.setId(newId);
        this.termById.put(newId, t);
        this.termIdByString.put(t.getString(), newId);
        ++size;
    }
    
    public void write(BufferedWriter out) throws IOException {
        out.write(new Integer(size).toString() + "\n");
        
        for (int i=0; i<size; ++i)
            out.write(this.termById.get(i).toString());
    }
    
    
}
