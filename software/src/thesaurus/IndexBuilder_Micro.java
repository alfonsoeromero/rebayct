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
 * IndexBuilder_Micro.java
 *
 * Created on 16 de octubre de 2006, 21:34
 *
 */
package thesaurus;

/**
 *
 * @author aeromero
 */
public class IndexBuilder_Micro extends IndexBuilder {

    private int identifier;
    private String s;

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

        if (mystring.compareTo("THESAURUS_ID") == 0) {
            this.mustProcessInteger = true;
            this.mustProcessString = false;
        } else if (mystring.compareTo("LIBELLE") == 0) {
            this.mustProcessInteger = false;
            this.mustProcessString = true;
        }
    }

    @Override
    public void endUnit(String typetag) {
        super.endUnit(typetag);
        if (typetag.compareTo("THESAURUS_ID") == 0) {
            this.identifier = super.processedInteger;
        } else if (typetag.compareTo("LIBELLE") == 0) {

            this.s = super.desc;
        } else if (typetag.compareTo("RECORD") == 0) {
            micro.put(identifier, s);
        }
    }

    /** Creates a new instance of IndexBuilder_Micro */
    public IndexBuilder_Micro() {
        super();
    }
}
