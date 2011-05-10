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

package thesaurus;

import org.xml.sax.helpers.DefaultHandler;
import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

/**
 * @author aeromero
 *
 */
public class XMLReader extends DefaultHandler 
{
    /**  Internal index builder */
    IndexBuilder ib;
    
    /** SAX parser  */
    SAXParser saxParser;
    
    /** Buffer for the strings that are read  */
    String mystring;
    
    public void parseFile(String name) 
    {
        // Use the default (non-validating) parser
        try {
            // Parse the input
            System.out.println("Parsing file ... "  + name);
            
            ib.startDocument();
            saxParser.parse( new File(name), this );
            ib.endDocument();
            
            System.out.println("File "  + name + " parsed!");
            
        } catch (Throwable t) {
        }
    }
    
    @Override
    public void startElement(String namespaceURI,
        String sName, // simple name (localName)
        String qName, // qualified name
        Attributes attrs) throws SAXException 
    {
        ib.startUnit(qName);
        this.mystring = null;
    }
    
    @Override
    public void endElement(String namespaceURI,
            String sName, // simple name
            String qName  // qualified name
            )
            throws SAXException {
        if (this.mystring!=null) 
        {
            ib.characters(this.mystring);
            this.mystring = null;
        }
        ib.endUnit(qName);
    }
    
    @Override
    public void characters(char buf[], int offset, int len)
    throws SAXException 
    {
        //   System.out.println("CHARACTERS!!!");
        String s = new String(buf, offset, len);
        if (!s.trim().equals("")) 
        {    
            // valid characters
            if (this.mystring == null)
                this.mystring = s;
            else this.mystring += s;
            //ib.characters(s);
        } else s = null;
    }
    
    public void setBuilder(IndexBuilder _ib) 
    {
        ib = _ib;
    }
    
    private void setFeature(String string, boolean b) 
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public XMLReader() 
    {
        super();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try{
            this.saxParser = factory.newSAXParser();
        } catch (Throwable t) {
        }
    }
}
