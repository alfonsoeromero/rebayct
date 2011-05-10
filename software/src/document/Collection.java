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
 * Collection.java
 *
 * Created on 31 de enero de 2007, 21:02
 *
 */

package document;

/**
 *
 * @author aeromero
 */
abstract public class Collection {
    
    //! Size of the collection (number of documents)
    protected int size;
    
    protected void setSize(int _size)
    {
      size = _size;
    }
    
    /** Creates a new instance of Collection */
    public Collection() 
    {
        size = 0;
    }
    
    public int getSize()
    {
      return size;
    }
    
    abstract public Document getNextDocument( );    
}
