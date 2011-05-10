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
 * ProcessedTerm.java
 *
 * Created on 15 de febrero de 2007, 20:07
 *
 */

package indexation;

import base.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
public class ProcessedTerm extends BaseTerm {
    
    public ProcessedTerm() {}
    
    public ProcessedTerm(String s) { myString = s; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("" + id);
        sb.append(" ");
        sb.append(myString);
        sb.append(" ");
        sb.append(numDocs);
        sb.append("\n");
        return sb.toString();       
    }
    
    public void readFromString(String s) {
        StringTokenizer st = new StringTokenizer(s);
        try{
            id = Integer.parseInt(st.nextToken());
            myString = st.nextToken();
            numDocs = Integer.parseInt(st.nextToken());
        } catch (NumberFormatException  nex) {
            System.err.println("Error constructing Term " + id + "\n");
            System.out.println(s);
            System.exit(0);
        }
    }
}
