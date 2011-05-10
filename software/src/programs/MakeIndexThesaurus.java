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

import base.StringIndexer;
import java.io.File;
import thesaurus.IndexBuilder;
import thesaurus.IndexBuilder_Desc;
import thesaurus.IndexBuilder_Micro;
import thesaurus.IndexBuilder_Thes;
import thesaurus.IndexBuilder_relation_bt;
import thesaurus.IndexBuilder_uf;
import thesaurus.ThesaurusStringIndexer;

/**
 *
 * @author aeromero
 */
public class MakeIndexThesaurus {

    public static void main(String args[]) {
        MakeIndexThesaurus indexer = new MakeIndexThesaurus();

        indexer.processArguments(args);

        //! argument processing
        if (args.length != 4 && args.length != 5) {
            indexer.usage();
        }

        //! document processing
        indexer.makeIndex();

    }
    private String thesaurusIndexFileName;
    private String stopwordsFileName;
    private String baseDirectory;
    private boolean doingStemming;

    private void processArguments(String[] args) {
        if (args.length < 3 || args.length > 4) {
            this.usage();
        }

        boolean processedOptions[] = new boolean[3];
        for (int i = 0; i < processedOptions.length; ++i) {
            processedOptions[i] = false;
        }

        for (String arg : args) {
            System.out.println(arg);
            if (arg.trim().startsWith("-stopwords=")) {
                this.stopwordsFileName = arg.trim().substring(11).trim();
                File route = new File(this.stopwordsFileName);
                if (!route.exists()) {
                    System.err.println("ERROR: file " + this.stopwordsFileName + " does not exist. Exiting.");
                    System.exit(-1);
                }
                processedOptions[0] = true;
            } else if (arg.trim().startsWith("-index=")) {
                this.thesaurusIndexFileName = arg.trim().substring(7).trim();
                File route = new File(this.thesaurusIndexFileName);
                if (route.exists()) {
                    System.err.println("ERROR: file " + this.thesaurusIndexFileName + " already exist. Exiting.");
                    System.exit(-1);
                }
                processedOptions[1] = true;
            } else if (arg.trim().startsWith("-baseDirectory=")) {
                this.baseDirectory = arg.trim().substring(15).trim();
                if (!this.baseDirectory.endsWith("/")) {
                    this.baseDirectory += "/";
                }
                processedOptions[2] = true;
            } else if (arg.trim().startsWith("-stemming=")) {
                String myLang = arg.trim().substring(10).trim();
                ThesaurusStringIndexer.setStemLanguage(myLang);
                doingStemming = true;
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

    private void makeIndex() {
        StringIndexer.setStem(this.doingStemming);

        String base = this.baseDirectory;

        System.out.println(base);

        thesaurus.XMLReader reader = new thesaurus.XMLReader();

        String fileDescriptors = base + "DESC_ES.XML";
        String fileBTRelation = base + "RELATION_BT.XML";
        String fileUFRelation = base + "UF_ES.XML";
        String fileMicrothesaurus = base + "DESC_THES.XML";
        String fileThesaurus = base + "THES_ES.XML";

        if (!this.fileExists(fileDescriptors) || !this.fileExists(fileBTRelation)
                || !this.fileExists(fileUFRelation) || !this.fileExists(fileMicrothesaurus)
                || !this.fileExists(fileThesaurus)) {
            System.out.println("" + this.fileExists(fileDescriptors) + "" + this.fileExists(fileBTRelation));
            System.out.println("" + this.fileExists(fileUFRelation) + "" + this.fileExists(fileMicrothesaurus) + "" + this.fileExists(fileThesaurus));
            System.out.println("ERROR: Eurovoc files not found. Please, check availability in ./eurovoc/. Exiting");
            System.exit(-1);
        }

        IndexBuilder.setStopwordList("./" + stopwordsFileName);

        reader.setBuilder(new IndexBuilder_Desc());
        reader.parseFile(fileDescriptors);

        reader.setBuilder(new IndexBuilder_relation_bt());
        reader.parseFile(fileBTRelation);

        reader.setBuilder(new IndexBuilder_uf());
        reader.parseFile(fileUFRelation);

        reader.setBuilder(new IndexBuilder_Thes());
        reader.parseFile(fileMicrothesaurus);

        reader.setBuilder(new IndexBuilder_Micro());
        reader.parseFile(fileThesaurus);

        System.out.println("Computing weights...");
        IndexBuilder.computeWeights();
        IndexBuilder.normalizeWeights();

        System.out.println("Writing index to disk...");
        IndexBuilder.writeIndex(this.thesaurusIndexFileName);

        System.out.println("Indexation finished, everything OK");
    }

    private boolean fileExists(String fileName) {
        File f = new File(fileName);
        return f.exists();
    }

    private void usage() {
        System.err.println("ERROR. The list of argument is the following:");
        System.err.println(" -baseDirectory=DIRECTORY : route to the thesaurus files");
        System.err.println(" -index=FILENAME : name of the file with the thesaurus index we are producing");
        System.err.println(" [-stemming=(en/es)] : tells if we are doing stemming (optional parameter)");
        System.err.println(" -stopwords=FILENAME : name of the stopwords file");
        System.exit(-1);
    }
}
