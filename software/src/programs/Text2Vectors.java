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
 * Text2Vectors.java
 *
 * Created on 1 de marzo de 2007, 20:10
 *
 */

package programs;
import java.io.*;
import document.*;
import indexation.*;

/**
 *
 * @author aeromero
 */
public class Text2Vectors {
    
    //! tells if we are doing stemming or not
    boolean doingStemming;
    
    //! tells if we are removing the numbers or not
    boolean removingNumbers;
    
    //! name of the stopwords file
    String stopwordsFileName;
    
    //! name of the output (vectors) file
    String vectorsFileName;
    
    /** Usage instructions for the program */
    private void usage() {
        System.err.println("ERROR. The list of argument is the following:");
        System.err.println(" -collection=NAME : route or file with the collection");
        System.err.println(" -output=FILENAME : name of the file with the processed collection");
        System.err.println(" [-stemming=(en/es)] : tells if we are doing stemming (optional parameter)");
        System.err.println(" [-removingNumbers=(yes/no)]: tells if we are removing numbers or not (\"no\" by default)");
        System.err.println(" -stopwords=FILENAME : name of the stopwords file");
        System.exit(-1);
    }
    
    private Collection processCollection(String name) {
        Collection c = null;
        File route = new File(name);
        if ( ! route.exists() ) {
            System.err.println("ERROR: " + name + " does not exist. Exiting.");
            System.exit(-1);
        }
        
        if (route.isDirectory()) {
            try{
                // Collection from directory
                c = new DirectoryCollection(name);
            } catch (Exception ex) {
                System.err.println("ERROR: directory "+ name + " is not readable. Exiting.");
                System.exit(-1);
            }
        } else
            // Collection from file
            c = new BatteryCollection(name);
        
        
        return c;
    }
    
    
    private Collection processArguments(String args[]) {
        Collection c = null;
        removingNumbers = false;
        boolean processedOptions[] = new boolean[3];
        for (int i=0; i<processedOptions.length; ++i) processedOptions[i] = false;
        
        for (String arg : args) {
            if (arg.trim().startsWith("-collection=")){
                c = this.processCollection(arg.trim().substring(12).trim());
                processedOptions[0] = true;
            } else if (arg.trim().startsWith("-stopwords=")) {
                stopwordsFileName = arg.trim().substring(11).trim();
                File route = new File(stopwordsFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: file " + stopwordsFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                processedOptions[1] = true;
            } else if (arg.trim().startsWith("-output=")) {
                vectorsFileName = arg.trim().substring(8).trim();
                File route = new File(vectorsFileName);
                if (route.exists()) {
                    System.err.println("ERROR: file " + vectorsFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                processedOptions[2] = true;
            } else if (arg.trim().startsWith("-stemming=")) {
                String myLang = arg.trim().substring(10).trim();
                LexicalStringIndexer.setStemLanguage(myLang);
                doingStemming = true;
            } else if (arg.trim().startsWith("-removingNumbers")) {
                String myOpt = arg.trim().substring(17).trim();
                this.removingNumbers = myOpt.equalsIgnoreCase("yes");
                LexicalStringIndexer.setRemoveNumbers(this.removingNumbers);
            } else {
                System.err.println("Unrecognized argument " + arg);
                this.usage();
            }
        }
        int count = 0;
        for (int i=0; i<processedOptions.length; ++i) if(processedOptions[i]) ++count;
        
        if (count != processedOptions.length) {
            System.err.println("Runaway argument");
            this.usage();
        }
        return c;
    }
    
    /** Creates a new instance of Text2Vectors */
    public Text2Vectors() {
        doingStemming = false;
    }
    
    
    public static void main(String args[]) {
        Text2Vectors t = new Text2Vectors();
        
        // argument processing
        if (args.length < 3)
            t.usage();
        
        Collection c = t.processArguments(args);
        
        // we create the index
        LexicalIndexer lex = new LexicalIndexer(c);
        
        // we write the index
        lex.makeIndex(t.vectorsFileName, t.doingStemming, t.stopwordsFileName);
        
    }
    
}

