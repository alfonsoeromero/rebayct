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
 *
 * Created on 3 de marzo de 2007, 19:52
 *
 */

package programs;
import java.io.*;
import indexation.*;
import java.util.*;
import supervisedClassifier.*;
import unsupervisedClassifier.*;

/**
 *
 * @author aeromero
 */
public class SupervisedBNClassifier {
    //! collection file name (not vectors)
    String collectionFileName;
    
    //! name of the stopwords file
    String classIndexFileName;
    
    //! name of the stopwords file
    String stopwordsFileName;
    
    //! name of the index with the thesaurus
    String thesaurusIndexFileName;
    
    //! name of the output filename
    String outputFileName;
    
    //! tells if we are doing stemming or not
    boolean doingStemming;
    
    //! tells if we are using nidf
    boolean usenIdf;
    
    //! strength of the relationship among descriptors
    double strengthThesaurus;
    
    //! strength of the relationship of the structure
    double strengthStructure;
    
    /** Creates a new instance of MNaiveBayes */
    public SupervisedBNClassifier() {
        this.doingStemming = false;
    }
    
    private void usage(){
        System.err.println("ERROR. The list of argument is the following:");
        System.err.println(" -collection=NAME : route or file with the collection");
        System.err.println(" -index=FILENAME : name of the file with the thesaurus index");
        System.err.println(" -classindex=NAME : name of the file with indexed classes");
        System.err.println(" -output=FILENAME : name of the file with the (unsupervised) classification results");
        System.err.println(" [-stemming=(en/es)] : tells if we are doing stemming (optional parameter)");
        System.err.println(" [-strengthThesaurus=0.0-1.0] : strength of a the thesaurus informationin its corresponding concept (double from 0.0 to 1.0, optional parameter), 1.0 by default");
        System.err.println(" [-strengthStructure=0.0-1.0] : strength of the structure (double from 0.0 to 1.0, optional parameter), 1.0 by default");
        System.err.println(" [-useNidf=yes|no]: using nidf or not (false by default)");
        System.err.println(" -stopwords=FILENAME : name of the stopwords file");
        System.exit(-1);
    }
    
    public void processArguments(String args[]) {
        if (args.length < 5 || args.length > 9) this.usage();
        boolean usedArgs[] = new boolean[5];
        for (int i=0; i<usedArgs.length; ++i) usedArgs[i] = false;
        
        this.strengthThesaurus = 1.0;
        this.strengthStructure = 1.0;
        this.usenIdf = false;
        
        for (String arg : args) {
            if (arg.trim().startsWith("-output=")) {
                outputFileName = arg.trim().substring(8).trim();
                File route = new File(outputFileName);
                if (route.exists()) {
                    System.err.println("ERROR: could not overwrite file " + outputFileName + ". It already exists. Exiting.");
                    System.exit(-1);
                }
                usedArgs[0] = true;
            }  else if (arg.trim().startsWith("-collection=")) {
                this.collectionFileName = arg.trim().substring(12).trim();
                File route = new File(this.collectionFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: collection file " + this.collectionFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                usedArgs[1] = true;
            } else if (arg.trim().startsWith("-classindex=")) {
                classIndexFileName = arg.trim().substring(12).trim();
                File route = new File(classIndexFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: class index file " + classIndexFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                usedArgs[2] = true;
            } else if (arg.trim().startsWith("-index=")) {
                this.thesaurusIndexFileName = arg.trim().substring(7).trim();
                File route = new File(this.thesaurusIndexFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: file " + this.thesaurusIndexFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                usedArgs[3] = true;
            } else if (arg.trim().startsWith("-stopwords=")) {
                this.stopwordsFileName= arg.trim().substring(11).trim();
                File route = new File(this.stopwordsFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: file " + this.stopwordsFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                usedArgs[4] = true;
            } else if (arg.trim().startsWith("-useNidf=")) {
                
                String s = arg.trim().substring(9).trim();
                
                if (s.equals("yes")) {
                    this.usenIdf = true;
                } else this.usenIdf = false;
                
            } else if (arg.trim().startsWith("-stemming=")) {
                String myLang = arg.trim().substring(10).trim();
                LexicalStringIndexer.setStemLanguage(myLang);
                doingStemming = true;            
            } else if (arg.trim().startsWith("-strengthThesaurus=")) {
                String s = arg.trim().substring(19).trim();
                try {
                    this.strengthThesaurus = Double.parseDouble(s);
                    if (this.strengthThesaurus > 1.0 || this.strengthThesaurus < 0.0) {
                        System.out.println("ERROR: strengthThesaurus must be between 0.0 and 1.0");
                        this.usage();
                    }
                    
                } catch (NumberFormatException ex) {
                    System.out.println("ERROR: stregth should be a floating point number");
                    this.usage();
                }
            } else if (arg.trim().startsWith("-strengthStructure=")) {
                String s = arg.trim().substring(19).trim();
                try {
                    this.strengthStructure = Double.parseDouble(s);
                    if (this.strengthStructure> 1.0 || this.strengthStructure < 0.0) {
                        System.out.println("ERROR: strengthStructure must be between 0.0 and 1.0");
                        this.usage();
                    }
                    
                } catch (NumberFormatException ex) {
                    System.out.println("ERROR: stregth should be a floating point number");
                    this.usage();
                }
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
        SupervisedBNClassifier mn = new SupervisedBNClassifier();
        
        // arguments processing
        mn.processArguments(args);
        
        // class initialization
        SupervisedBN bayes = new SupervisedBN(mn.classIndexFileName, mn.thesaurusIndexFileName, mn.stopwordsFileName, mn.doingStemming, mn.usenIdf, false,
                mn.strengthThesaurus, mn.strengthStructure);
        
        try {
            BufferedReader buff = new BufferedReader(new FileReader(mn.collectionFileName));
            BufferedWriter obuff = new BufferedWriter(new FileWriter(mn.outputFileName));
            
            String s = buff.readLine(); // number of lines... it is not used here
            
            int id = 1;
            while (buff.ready()) {
                s = buff.readLine();
                
                StringTokenizer st = new StringTokenizer(s);
                String num = st.nextToken();
                
                s = st.nextToken("\n");
                
                List<Result> l = bayes.classifyString(s);
                
                StringBuilder sb = new StringBuilder();
                int i=1;
                if (l.size()>0)
                    for (Result r : l) {
                        sb.append(num).append(" ").append(r.getDescriptor().getId()).append(" ").append(i).append(" ").append(r.getProb()).append("\n");
                        ++i;
                    } else sb.append(num).append(" ").append(0).append(" ").append(i).append(" ").append(0.0).append("\n");
                
                obuff.write(sb.toString());
            }
            
            obuff.flush();
            obuff.close();
            
        } catch (IOException ex) {
            System.err.println("ERROR: input file could not be read. Exiting. " + ex);
            System.exit(-1);
        }
    }
}
