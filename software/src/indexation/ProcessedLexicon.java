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
 * ProcessedLexicon.java
 *
 * Created on 15 de febrero de 2007, 20:02
 *
 */

package indexation;

import base.Lexicon;
import java.io.*;

/**
 *
 * @author aeromero
 */
public class ProcessedLexicon extends Lexicon<ProcessedTerm> {

    
    public void read(BufferedReader in) throws IOException, NumberFormatException {
        size = Integer.parseInt(in.readLine());
        
        for (int i=0; i<size; ++i) {
            ProcessedTerm t = new ProcessedTerm();
            t.readFromString(in.readLine());
            this.termById.put(t.getId(), t);
            this.termIdByString.put(t.getString(), t.getId());
        }
    }
     
}
