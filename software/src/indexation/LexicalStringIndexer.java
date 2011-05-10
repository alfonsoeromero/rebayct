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
 * LexicalStringIndexer.java
 *
 * Created on 10 de marzo de 2007, 19:46
 *
 */

package indexation;
import base.StringIndexer;

/**
 *
 * @author aeromero
 */
public class LexicalStringIndexer extends StringIndexer<ProcessedTerm, ProcessedLexicon> {
    
    /** Creates a new instance of LexicalStringIndexer */
    public LexicalStringIndexer() {
    }
    
    public void createNewTerm(String s)
    {
        ProcessedTerm t = new ProcessedTerm(s);
        int id = l.size();
        id = l.size();
        t.setId(id);
        l.add(t);
        this.freq.put(id, 1);
    }
}
 
