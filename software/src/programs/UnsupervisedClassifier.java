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
package programs;

import thesaurus.*;
import unsupervisedClassifier.BNClassifier;
import unsupervisedClassifier.ClassifierModel;
import unsupervisedClassifier.Result;
import java.io.*;
import java.util.*;
import unsupervisedClassifier.SimpleVSMClassifier;

public class UnsupervisedClassifier {
    //! collection file name

    String collectionFileName;
    //! name of the stopwords file
    String stopwordsFileName;
    //! name of the index with the thesaurus
    String thesaurusIndexFileName;
    //! name of the output filename
    String outputFileName;
    //! are we doing stemming?
    boolean doingStemming;
    private boolean phrases;
    //private boolean nIdf;
    private static final int BN = 0;
    private static final int VSM = 1;
    private static final int HVSM = 2;
    int MODEL;

    public UnsupervisedClassifier() {
        doingStemming = false;
    }

    private boolean fileExists(String fileName) {
        File f = new File(fileName);
        return f.exists();
    }

    private void processArguments(String args[]) {
        boolean processedOptions[] = new boolean[6];
        for (int i = 0; i < processedOptions.length; ++i) {
            processedOptions[i] = false;
        }

        for (String arg : args) {
            if (arg.trim().startsWith("-collection=")) {
                this.collectionFileName = arg.trim().substring(12).trim();
                //this.c = new BatteryCollection(this.collectionFileName);
                File route = new File(this.collectionFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: file " + this.collectionFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                processedOptions[0] = true;
            } else if (arg.trim().startsWith("-stopwords=")) {
                this.stopwordsFileName = arg.trim().substring(11).trim();
                File route = new File(this.stopwordsFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: file " + this.stopwordsFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                processedOptions[1] = true;
            } else if (arg.trim().startsWith("-output=")) {
                this.outputFileName = arg.trim().substring(8).trim();
                File route = new File(this.outputFileName);
                if (route.exists()) {
                    System.err.println("ERROR: file " + this.outputFileName + " already exists and will not be overwritten. Exiting.");
                    System.exit(-1);
                }
                processedOptions[2] = true;
            } else if (arg.trim().startsWith("-index=")) {
                this.thesaurusIndexFileName = arg.trim().substring(7).trim();
                File route = new File(this.thesaurusIndexFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: file " + this.thesaurusIndexFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                processedOptions[3] = true;
            } else if (arg.trim().startsWith("-stemming=")) {
                String myLang = arg.trim().substring(10).trim();
                ThesaurusStringIndexer.setStemLanguage(myLang);
                doingStemming = true;
            } else if (arg.trim().startsWith("-exactPhrases=")) {
                this.phrases = ("yes".compareTo(arg.trim().substring(14).trim()) == 0);
                processedOptions[4] = true;
            } /*else if (arg.trim().startsWith("-nIdf=")) {
                this.nIdf = ("yes".compareTo(arg.trim().substring(6).trim()) == 0);
                processedOptions[5] = true;
            } */else if (arg.trim().startsWith("-model=")) {
                String myModel = arg.trim().substring(7).trim();

                if (myModel.compareTo("BN") == 0) {
                    this.MODEL = BN;
                } else if (myModel.compareTo("VSM") == 0) {
                    this.MODEL = VSM;
                } else if (myModel.compareTo("HVSM") == 0) {
                    this.MODEL = HVSM;
                } else {
                    System.err.println("ERROR: wrong model");
                    this.usage();
                }

                processedOptions[5] = true;

            } else {
                System.err.println("Unrecognized argument " + arg);
                this.usage();
            }
        }
        int count = 0;
        for (int i = 0; i < processedOptions.length; ++i) {
            if (processedOptions[i]) {
                ++count;
            }
        }

        if (count != processedOptions.length) {
            System.err.println("Runaway argument");
            this.usage();
        }
    }

    public void classifyBattery() {
        ClassifierModel cm = null;

        switch (this.MODEL) {
            case BN:
                cm = new BNClassifier(this.thesaurusIndexFileName, this.stopwordsFileName, this.doingStemming, false, this.phrases);
                break;
            case VSM:
                cm = new SimpleVSMClassifier(this.thesaurusIndexFileName, this.stopwordsFileName, false, this.doingStemming, false);
                break;
            case HVSM:
                cm = new SimpleVSMClassifier(this.thesaurusIndexFileName, this.stopwordsFileName, true, this.doingStemming, false);
                break;
            default:
                cm = new BNClassifier(this.thesaurusIndexFileName, this.stopwordsFileName, this.doingStemming, false, this.phrases);
        }

        System.out.println("Classifier running...");

        try {
            BufferedReader buff = new BufferedReader(new FileReader(this.collectionFileName));
            BufferedWriter obuff = new BufferedWriter(new FileWriter(this.outputFileName));

            String s = buff.readLine(); // number of lines... it is not used here

            while (buff.ready()) {
                s = buff.readLine();

                StringTokenizer st = new StringTokenizer(s);
                String num = st.nextToken();

                s = st.nextToken("\n");

                List<Result> l = cm.classifyString(s);

                StringBuilder sb = new StringBuilder(num + " ");

                if (l.size() > 0) {
                    int rank = 1;
                    for (Result r : l) {
                        obuff.write(num + " " + r.getDescriptor().getId() + " " + rank + " " + r.getProb());
                        obuff.newLine();
                        ++rank;
                    }
                } else {
                    obuff.write(num + " " + 0 + " " + 0 + " " + 0.0);
                    obuff.newLine();
                }
            }

            obuff.flush();
            obuff.close();

        } catch (IOException ex) {
            System.err.println("ERROR: input file could not be read. Exiting. " + ex);
            System.exit(-1);
        }
        System.out.println("Classifier finished!");
    }

    public void usage() {
        System.err.println("ERROR. The list of argument is the following:");
        System.err.println(" -model=(BN|VSM|HVSM) : Model used here => Bayesian Network,");
        System.err.println("\tVector Space Model or Hierarchical Vector Space Model");
        System.err.println(" -collection=NAME : route or file with the collection");
        System.err.println(" -index=FILENAME : name of the file with the thesaurus index");
        System.err.println(" -output=FILENAME : name of the file with the (unsupervised) classification results");
        System.err.println(" -exactPhrases=(yes|no): tells if we are using exact phrases or not (no effect with VSM and HVSM models)");
        //System.err.println(" -nIdf=(yes|no): tells if we are using nidf or not ");
        System.err.println(" [-stemming=(en/es)] : tells if we are doing stemming (optional parameter)");
        System.err.println(" -stopwords=FILENAME : name of the stopwords file");
        System.exit(-1);
    }

    public static void main(String[] args) {
        UnsupervisedClassifier classifier = new UnsupervisedClassifier();

        classifier.processArguments(args);

        //! argument processing
        if (args.length != 7 && args.length != 8) {
            classifier.usage();
        }

        //! document processing
        classifier.classifyBattery();
    }
}
