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
 * MNaiveBayes.java
 *
 * Created on 3 de marzo de 2007, 19:52
 *
 */

package programs;
import java.io.*;
import indexation.*;
import java.util.*;
import supervisedClassifier.*;

/**
 *
 * @author aeromero
 */
public class ORGate {
    //! name of the input (vectors) file
    String vectorsFileName;
    
    //! Name of the input (classified documents) file
    String classIndexFileName;
    
    //! name of the output (indexed classes) file
    String outputFileName;
    
    //! name of the file with apriori probabilities (obtained with an unsupervised classifier)
    String aprioriFileName;
    
    /** Creates a new instance of MNaiveBayes */
    public ORGate() {
    }
    
    private void usage(){
        System.err.println("ERROR. The list of argument is the following:");
        System.err.println(" -vectors=NAME : name of the file with indexed documents");
        System.err.println(" -classindex=NAME : name of the file with indexed classes");
        System.err.println(" -output=FILENAME : name of the file with the categorization results");
        System.exit(-1);
    }
    
    public void processArguments(String args[]) {
        if (args.length != 3) this.usage();
        boolean usedArgs[] = new boolean[3];
        for (int i=0; i<usedArgs.length; ++i) usedArgs[i] = false;
        
        // we set to null the optional parameter
        this.aprioriFileName = null;
        
        for (String arg : args) {
            if (arg.trim().startsWith("-output=")) {
                outputFileName = arg.trim().substring(8).trim();
                File route = new File(outputFileName);
                if (route.exists()) {
                    System.err.println("ERROR: could not overwrite file " + outputFileName + ". It already exists. Exiting.");
                    System.exit(-1);
                }
                usedArgs[0] = true;
            }  else if (arg.trim().startsWith("-vectors=")) {
                vectorsFileName = arg.trim().substring(9).trim();
                File route = new File(vectorsFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: vectors file " + vectorsFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                usedArgs[1] = true;
            } else if (arg.trim().startsWith("-classindex=")) {
                classIndexFileName = arg.trim().substring(12).trim();
                File route = new File(classIndexFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: classified file " + classIndexFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                usedArgs[2] = true;
            } else {
                System.err.println("Unrecognized argument " + arg);
                this.usage();
            }
            
        }
        
        int count = 0;
        for (int i=0; i<usedArgs.length; ++i) if (usedArgs[i]) ++count;
        
        if (count != usedArgs.length) {
            System.err.println("Runaway arguments.");
            this.usage();
        }
    }
    
    public static void main(String args[]) {
        ORGate mn = new ORGate();
        
        // arguments processing
        mn.processArguments(args);
        
        // class initialization
        ORGateClassifier naive = new ORGateClassifier(mn.classIndexFileName);
        
        // we read the collection and the lexicon
        try {
            BufferedReader in = new BufferedReader(new FileReader(mn.vectorsFileName));
            BufferedWriter out = new BufferedWriter(new FileWriter(mn.outputFileName));
            
            ProcessedLexicon pl = new ProcessedLexicon();
            pl.read(in);
            
            
            in.readLine(); // we discard the number of documents...
            
            naive.setProcessedLexicon(pl);
            
            while (in.ready()) {
                base.IndexedDocument id = new base.IndexedDocument(in.readLine());
                
                HashMap<Integer, Double> list = naive.classifyDocument(id.getMap(), id.getId());
                
                //StringBuffer sb = new StringBuffer(id.getId() + " ");
                StringBuilder sb = new StringBuilder("");
                                
                // Get a list of the entries in the map
                List<Map.Entry<Integer, Double>> lst = new ArrayList<Map.Entry<Integer, Double>>(list.entrySet());
                
                // Sort the list using an annonymous inner class implementing Comparator for the compare method
                java.util.Collections.sort(lst, new Comparator<Map.Entry<Integer, Double>>(){
                    public int compare(Map.Entry<Integer, Double> entry, Map.Entry<Integer, Double> entry1) {
                        // Return 0 for a match, -1 for less than and +1 for more then
                        return (entry.getValue().equals(entry1.getValue()) ? 0 : (entry.getValue() < entry1.getValue() ? 1 : -1));
                    }
                });
                
                // Clear the map
                list.clear();
                
                int i = 1;
                
                // Copy back the entries now in order
                for (Map.Entry<Integer, Double> entry: lst) {
                    list.put(entry.getKey(), entry.getValue());
                    sb.append(id.getId()).append(" ").append(entry.getKey()).append(" ").append(i).append(" ").append(entry.getValue()).append("\n");
                    ++i;
                }
                
                out.write(sb.toString());
                
            }
            
            out.flush();
            out.close();
            
        } catch (FileNotFoundException ex) {
            System.exit(-1);
        } catch (IOException ex) {
            System.exit(-1);
            
        }
    }
}
