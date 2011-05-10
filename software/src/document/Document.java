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
 * Document.java
 *
 * Created on 31 de enero de 2007, 21:16
 *
 */

package document;
 
/**
 * Represents a document
 * @author aeromero
 */
abstract public class Document 
{
    /** String with the name of the document */
    String documentName;
    
    /** Identificator of the document */
    int documentId;
    
    /**
     * Creates a new instance of Document
     */
    public Document(String name, int id) 
    {
        documentName = name;
        documentId = id;
    }
    
    abstract public boolean hasMoreLines();
    
    public String getDocumentName()
    {
      return documentName;
    }
    
    public int getId(){
        return documentId;
    }
    
    abstract public String getLine();
}
