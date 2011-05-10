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
 * InMemoryDocument.java
 *
 * Created on 7 de febrero de 2007, 13:00
 *
 */

package document;

/**
 *
 * @author aeromero
 */
public class InMemoryDocument extends Document {
    
    /** Text of the document (in memory) */
    String text;
    
    /** count calls to getLine() */
    int more;
    
    /**
     * Creates a new instance of InMemoryDocument
     */
    public InMemoryDocument(String mytext, String name, int id) {
        super(name, id);
        text = mytext;
        more = 0;
    }
    
    public String getLine()
    {
      ++more;
      return text;
    }
    
    public boolean hasMoreLines()
    {
      return more == 0;
    }
}

