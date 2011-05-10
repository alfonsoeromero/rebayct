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
 * GlobalTermSelection.java
 *
 * Created on 29 de mayo de 2007, 18:57
 *
 */

package programs;
import indexation.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
public class GlobalTermSelection {
    //! strings with filenames and parameters
    static String inputFileName, outputFileName, parameters;
    
    //! classified (output) lexicon
    ClassifiedLexicon outputl;
    
    //! classified (original) lexicon
    ClassifiedLexicon cl;
    
    //! Number of documents classified (to compute a priori probabilities)
    int numDocuments;
    
    //! Number of classes
    int numClasses;
    
    //! For each class, it stores the number of documents classified over it
    HashMap<Integer, Integer> numDocs;
    
    //! For each class, it stores the total frequency of terms appearing in it
    HashMap<Integer, Integer> totalFreq;
    
    //! selection method: terms that appears in a minimum number of documents
    private static final int NUMDOCS_MIN = 0;
    
    //! selection method: terms that appear in a percentage of the top of the list
    private static final int NUMDOCS_PERC = 1;
    
    private void writeIndex() {
        
        // filling of the new maps
        HashMap<Integer, Integer> newNumDocs, newTotalFreq;
        
        // the number of documents is the same :), we copy it
        newNumDocs = this.numDocs;
        
        newTotalFreq = new HashMap<Integer, Integer>();
        // new totalFreq computation:
        
        // we initiate it to 0
        for (int myclass : newNumDocs.keySet())
            newTotalFreq.put(myclass, 0);
        
        // for each term appearing in a certain class, we sum its frequency to the total freq
        for (int i=0; i<this.outputl.size(); ++i) {
            ClassifiedTerm ct = cl.getTermById(i);
            Map<Integer, Integer> mp = ct.getOccurrence();
            for (int j : mp.keySet())
                newTotalFreq.put(j, newTotalFreq.get(j) + mp.get(j));
        }
        
        //! writing of the index
        try{
            //1.- we open the buffer
            BufferedWriter out = new BufferedWriter(new FileWriter(GlobalTermSelection.outputFileName));
            
            //2.- we write the lexicon (NOT THE ORIGINAL!!!)
            this.outputl.write(out);
            
            //3.- we write the number of classes
            out.write(numClasses + "\n");
            
            //3.- we write the number of documents used to create this index of classes
            out.write(this.numDocuments + "\n");
            
            //5.- for each class, we write its id, the number of documents and the total frequency
            for (int i : newNumDocs.keySet())
                out.write(i + " " + newNumDocs.get(i) + " " + newTotalFreq.get(i) + "\n" );
            
            out.write("\n");
            
            //6.- we flush the buffer
            out.close();
        } catch(Exception ex) {
            System.err.println("ERROR: ");
            System.err.println(". Exiting.");
            System.exit(-1);
        }
        
    }
    
    private void selectTermsByPerc() {
        
        // parameter processing
        String num = GlobalTermSelection.parameters.trim().replaceAll("\"", "");
        
        if (!num.endsWith("%")) {
            System.err.println("ERROR: specified parameter "+ num + " is not a percentage. Exiting");
            System.exit(-1);
        } else num = num.substring(0, num.length()-1);
        
        double percent = 0.0;
        
        try {
            percent = Double.parseDouble(num);
        } catch (NumberFormatException ex) {
            System.err.println("ERROR: specified parameter " + num + " is not a floating point value. Exiting");
            System.exit(-1);
        }
        
        if (percent < 0.0 || percent > 100.0) {
            System.err.println("ERROR: percentage should be between 0.0 and 100.0");
            System.exit(-1);
        }
        
        // how many terms do we want?
        int NUMTERMS = (int) Math.floor(cl.size()*percent / 100.0);
        
        System.out.println("Selecting only " + percent + "% terms, by freq");
        
        
        PriorityQueue<ClassifiedTerm> terms = new PriorityQueue<ClassifiedTerm>(cl.size(), new Comparator<ClassifiedTerm>() {
            public int compare(ClassifiedTerm i, ClassifiedTerm j) {
                return j.getNumDocs() - i.getNumDocs();
            }
        }
        );
        
        System.out.println("Sorting terms by freq");
        
        for (int i=0; i<cl.size(); ++i)
            terms.add(cl.getTermById(i));
        
        System.out.println("Selecting more frequent terms");
        
        int last = 0;
        for (int i=0; i<NUMTERMS; ++i) {
            ClassifiedTerm nt = terms.poll();
            nt.setId(i);
            this.outputl.add(nt);
            last = nt.getNumDocs();
        }
        
        System.out.println("Completing the selection...");
        
        while (!terms.isEmpty() && terms.peek().getNumDocs()==last) {
            ClassifiedTerm nt = terms.poll();
            nt.setId(NUMTERMS);
            ++NUMTERMS;
            this.outputl.add(nt);
        }
        
        System.out.println("done!");
    }
    
    
    
    //! select terms appearing only in >= LIM documents
    private void selectTermsByNumDoc() {
        int LIM = 0;
        
        // parameter processing
        String num = GlobalTermSelection.parameters.trim().replaceAll("\"", "");
        try {
            LIM = Integer.parseInt(num);
        } catch (NumberFormatException ex) {
            System.err.println("ERROR: specified parameter " + num + " is not a number. Exiting");
            System.exit(-1);
        }
        
        
        int gap = 0;
        int n = cl.size();
        for (int i=0; i<n; ++i) {
            ClassifiedTerm ct = cl.getTermById(i);
            if (ct.getNumDocs() >= LIM) // we select the term
            {
                ClassifiedTerm selected = ct;
                selected.setId(i - gap);
                outputl.add(selected);
                
            } else ++gap; // the term is not selected
        }
    }
    
    
    /** Creates a new instance of GlobalTermSelection */
    public GlobalTermSelection(String inputFileName, String outputFileName, String params) {
        GlobalTermSelection.inputFileName = inputFileName;
        GlobalTermSelection.outputFileName = outputFileName;
        GlobalTermSelection.parameters = params;
        
        //! class index reading and initialization
        int line = 1;
        
        System.out.println("Reading input file: " + inputFileName);
        try {
            BufferedReader in = new BufferedReader(new FileReader(inputFileName));
            
            // 1.- we read the lexicon
            cl = new ClassifiedLexicon();
            this.outputl = new ClassifiedLexicon();
            cl.read(in);
            
            // 2.- we read the number of classes and documents
            this.numClasses = Integer.parseInt(in.readLine());
            this.numDocuments = Integer.parseInt(in.readLine());
            
            // 3.- we initialize the arrays and fill them
            numDocs = new HashMap<Integer, Integer>(this.numClasses);
            totalFreq = new HashMap<Integer, Integer>(this.numClasses);
            
            // for each class (stored in one line...)
            for (int i=0; i<numClasses; ++i) {
                // we read the line and tokenize it
                StringTokenizer st = new StringTokenizer(in.readLine());
                
                // should have three tokens...
                int classID = Integer.parseInt(st.nextToken());
                int numDocz = Integer.parseInt(st.nextToken());
                int myFreq = Integer.parseInt(st.nextToken());
                
                // we insert the values on the arrays
                numDocs.put(classID, numDocz);
                totalFreq.put(classID, myFreq);
            }
            
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: file " + inputFileName + " does not exist. Exiting.");
            System.exit(-1);
        } catch (NumberFormatException ex) {
            System.err.println("ERROR: number expected at line " + line + " of file " + inputFileName + " was not found. Exiting.");
            System.exit(-1);
        } catch (IOException ex){
            System.err.println("ERROR: file " + inputFileName + " could not be read. Exiting.");
            System.exit(-1);
        }
    }
    
    private static void usage() {
        System.err.println("ERROR. The list of argument is the following:");
        System.err.println(" -input=NAME : name of the classIndex to select terms");
        System.err.println(" -output=NAME : name of the output classIndex");
        System.err.println(" -method=METHODNAME : ");
        System.err.println("\t method:NUMDOCS_MIN prunes terms appearing in less number of documents than specified in the parameters (3 by default)");
        System.err.println("\t\t example:  -method=NUMDOCS_MIN -parameters=\"3\" (prunes terms appearing in less than 3 documents)");
        System.err.println("\t method:NUMDOCS_PERC prunes terms appearing in the less documents, leaving a percentage of the terms");
        System.err.println("\t\t example:  -method=NUMDOCS_PERC -parameters=\"10%\" (prunes the 90% most infrequent terms)");
        System.exit(-1);
    }
    
    public static int processArgs(String args[]) {
        int method = 0;
        
        // argument processing
        if (args.length < 3)
            usage();
        boolean processedOptions[] = new boolean[3];
        for (int i=0; i<processedOptions.length; ++i) processedOptions[i] = false;
        
        for (String arg : args) {
            if (arg.trim().startsWith("-output=")) {
                outputFileName = arg.trim().substring(8).trim();
                File route = new File(outputFileName );
                if (route.exists()) {
                    System.err.println("ERROR: could not overwrite file " + outputFileName + ". It already exists. Exiting.");
                    System.exit(-1);
                }
                processedOptions[0] = true;
            } else if (arg.trim().startsWith("-input=")) {
                inputFileName = arg.trim().substring(7).trim();
                
                File route = new File(inputFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: classfile " + inputFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                processedOptions[1] = true;
            } else if (arg.trim().startsWith("-method=")) {
                String myMethod = arg.trim().substring(8).trim();
                System.out.println("method => " + myMethod);
                if (myMethod.trim().compareTo("NUMDOCS_MIN")==0) {
                    method = NUMDOCS_MIN;
                    processedOptions[2] = true;
                } else if (myMethod.trim().compareTo("NUMDOCS_PERC")==0) {
                    method = NUMDOCS_PERC;
                    processedOptions[2] = true;
                } else processedOptions[2] = false;
                
            } else if (arg.trim().startsWith("-parameters=")) {
                parameters = arg.trim().substring(12);
                System.out.println(parameters);
            } else {
                System.err.println("Unrecognized argument " + arg);
                usage();
            }
        }
        int count = 0;
        for (int i=0; i<processedOptions.length; ++i) if(processedOptions[i]) ++count; else System.err.println("falta argumento " + i);
        
        if (count != processedOptions.length) {
            System.err.println("Runaway arguments.");
            usage();
        }
        
        return method;
    }
    
    
    public static void main(String args[]) {
        //! strings with filenames and parameters
        //String inputFileName = "", outputFileName = "", parameters = "";
        
        //! term selection method
        int method = processArgs(args);
        
        GlobalTermSelection gt = new GlobalTermSelection(inputFileName, outputFileName, parameters);
        
        
        if (method == GlobalTermSelection.NUMDOCS_MIN)
            gt.selectTermsByNumDoc();
        else if (method == GlobalTermSelection.NUMDOCS_PERC)
            gt.selectTermsByPerc();
        else {
            System.err.println("ERROR: unknown method, exiting");
            System.exit(-1);
        }
        
        
        gt.writeIndex();
    }
    
}


