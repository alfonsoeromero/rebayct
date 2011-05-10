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
 * IndexBuilder_Thes.java
 *
 * Created on 6 de octubre de 2006, 1:31
 *
 */

package thesaurus;

import java.util.*;

/**
 *
 * @author aeromero
 */
public class IndexBuilder_Thes extends IndexBuilder {
    
    /** current descriptor whose nondescriptors are
     * processing */
    private Descriptor current;
    
    /** Current microthesaurus */
    private int microThesaurus;
    
    @Override
    public void startDocument() {
        super.startDocument();
    }
    
    @Override
    public void endDocument() {
        super.endDocument();
    }
    
    @Override
    public void startUnit(String mystring) {
        super.startUnit(mystring);
        this.mustProcessString = false;
        
        if (mystring.compareTo("TOPTERM")==0)
        {
            this.mustProcessInteger = false;    
            this.mustProcessString = true;
        }
        else           
            this.mustProcessInteger = true;
        
    }
    
    @Override
    public void endUnit(String typetag) {
        super.endUnit(typetag);
        
        /** Do we need to build a descriptor? */
        if (typetag.compareTo("THESAURUS_ID")==0) {
            this.microThesaurus = super.processedInteger;
        } else if (typetag.compareTo("DESCRIPTEUR_ID")==0) {
            this.current = ld.get(super.processedInteger);
            this.current.addMicroThesaurus(this.microThesaurus);
        } else if (typetag.compareTo("TOPTERM")==0) {
            if (super.desc.compareTo("O")==0) // is top term
            {
                if (!MT.containsKey(this.microThesaurus)) {
                    List <Integer> l = new LinkedList<Integer>();
                    l.add(this.current.getId());
                    MT.put(this.microThesaurus, l);
                } else {
                    List <Integer> l = MT.get(this.microThesaurus);
                    l.add(this.current.getId());
                    MT.put(this.microThesaurus, l);
                }
                
            }
        }
    }
        
    /** Creates a new instance of IndexBuilder_Thes */
    public IndexBuilder_Thes() {
        super();
    }
    
}
