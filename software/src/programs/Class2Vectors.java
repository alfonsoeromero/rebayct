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
public class Class2Vectors 
{    
    //! name of the input (vectors) file
    String vectorsFileName;
    
    //! Name of the input (classified documents) file
    String classifiedDocumentsFileName;

    //! name of the output (indexed classes) file
    String outputFileName;
    
    /** Usage instructions for the program */
    private void usage()
    {
        System.err.println("ERROR. The list of argument is the following:");
        System.err.println(" -vectors=NAME : name of the file with indexed documents");
        System.err.println(" -classfile=NAME : name of the file with classified documents");        
        System.err.println(" -output=FILENAME : name of the file with the indexed classes");
        System.exit(-1);   
    }
    
    
    private void processArguments(String args[])
    {
        boolean processedOptions[] = new boolean[3];
        for (int i=0; i<processedOptions.length; ++i) processedOptions[i] = false;
        
        for (String arg : args)
        {
            if (arg.trim().startsWith("-output=")) {
                outputFileName = arg.trim().substring(8).trim();
                File route = new File(outputFileName );
                if (route.exists())
                {
                  System.err.println("ERROR: could not overwrite file " + outputFileName + ". It already exists. Exiting.");   
                  System.exit(-1);
                }
                processedOptions[0] = true;
            }  else if (arg.trim().startsWith("-vectors=")) {
                vectorsFileName = arg.trim().substring(9).trim();
                File route = new File(vectorsFileName);
                if (!route.exists())
                {
                  System.err.println("ERROR: vectors file " + vectorsFileName + " does not exist. Exiting.");   
                  System.exit(-1);
                }
                processedOptions[1] = true;
            } else if (arg.trim().startsWith("-classfile=")) {
                classifiedDocumentsFileName = arg.trim().substring(11).trim();
                File route = new File(classifiedDocumentsFileName);
                if (!route.exists())
                {
                  System.err.println("ERROR: classfile " + classifiedDocumentsFileName + " does not exist. Exiting.");   
                  System.exit(-1);
                }
                processedOptions[2] = true;
            } else {
                System.err.println("Unrecognized argument " + arg);
                this.usage();
            }
        }     
        int count = 0;
        for (int i=0; i<processedOptions.length; ++i) if(processedOptions[i]) ++count;
        
        if (count != processedOptions.length)
        {
                System.err.println("Runaway arguments.");
                this.usage();        
        }
    }
    
    /** Creates a new instance of Text2Vectors */
    public Class2Vectors() { }
    
    
    public static void main(String args[])
    {
       Class2Vectors t = new Class2Vectors();
       
       // argument processing
       if (args.length != 3)
         t.usage();
       
       t.processArguments(args);
       
       // file processing
       ClassIndexer ci = new ClassIndexer(t.vectorsFileName, t.classifiedDocumentsFileName);
       
       // writing the output file..
       ci.writeIndex(t.outputFileName);
       
    }
    
}

