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
 * GenericNaiveBayes.java
 *
 * Created on 15 de mayo de 2007, 23:41
 *
 */
package supervisedClassifier;

import todelete.APrioriReader;
import indexation.*;
import java.util.*;

/**
 *
 * @author aeromero
 */
abstract public class GenericNaiveBayes extends GenericClassifier {

    //! apriori reader
    protected APrioriReader ap;
    //! type of the processing, tells if it computes probability for
    //  each term in the document, or if it computes it for all terms
    protected int mode_evaluation;
    //! type of the processing, tells if it computes probability for
    //  each term in the document, or if it computes it for all terms
    protected int mode_naiveBayes;
    protected HashMap<Integer, Double> probYTotal;
    protected HashMap<Integer, Double> probNTotal;
    //! constant
    protected final static int FOR_EACH_TERM_IN_THE_DOCUMENT = 1;
    //! constant
    protected final static int FOR_EACH_TERM = 2;
    //! constant
    protected final static int BINARY_NAIVE_BAYES_FOR_EACH_CLASS = 3;
    //! constant
    protected final static int STANDALONE_BINARY_NAIVE_BAYES = 4;
    protected static final boolean _DEBUGGING = false;
    protected boolean USE_TERM_SELECTION = true;
    protected HashMap<Integer, ArrayList<Integer>> selectedterms;
    protected String _classIndexName;

    public void enableTermSelection(double confidence, boolean local) {
        SelectiveLG sel = new SelectiveLG(_classIndexName);
        selectedterms = sel.usedselectedterms;
        sel.setConfidenceAndMode(confidence, local);
    }

    /** Creates a new instance of GenericNaiveBayes */
    public GenericNaiveBayes(String classIndexName, int myMode) {
        super(classIndexName);
        _classIndexName = classIndexName;
        ap = null;
        if (myMode == GenericNaiveBayes.FOR_EACH_TERM || myMode == GenericNaiveBayes.FOR_EACH_TERM_IN_THE_DOCUMENT) {
            mode_evaluation = myMode;
        } else {
            mode_evaluation = 0;
        }
        probYTotal = new HashMap<Integer, Double>();
        probNTotal = new HashMap<Integer, Double>();

        //no se usa seleccion de terminos, en selectedterms metemos todos los terminos
        HashMap<Integer, ArrayList<Integer>> alltheterms = new HashMap<Integer, ArrayList<Integer>>();
        int N = cl.size();
        for (int i = 0; i < N; ++i) {
            ArrayList<Integer> vectorclases = new ArrayList<Integer>();
            alltheterms.put(i, vectorclases);
            for (int clase : ((cl.getTermById(i)).getOccurrence()).keySet()) {
                (alltheterms.get(i)).add(clase);
            }
        }
        selectedterms = alltheterms;
    }

    public void setAPrioriFile(String aprioriFileName) {
        ap = new APrioriReader(super.numClasses, aprioriFileName, super.apriori);
    }

    protected double getAPRiori(int classId, int docId) {
        if (ap == null) {
            return this.apriori.get(classId);
        } else {
            return ap.getAPrioriOfDocIInClassJ(docId, classId);
        }
    }

    public abstract double computeProbN(ClassifiedTerm ct, int freqOnDoc, int classId);

    public abstract double computeProbY(ClassifiedTerm ct, int freqOnDoc, int classId);

    /** Receives a list of pairs (term_identifier, frequency) and returs a list of (class_identifiers, relevance)
     */
    public HashMap<Integer, Double> classifyDocument(HashMap<Integer, Integer> _document, int docId) {
        if (GenericNaiveBayes._DEBUGGING) {
            System.out.println("=== Documento " + docId);
        }

        HashMap<Integer, Double> ret = new HashMap<Integer, Double>();
        HashMap<Integer, Double> proby = new HashMap<Integer, Double>();
        HashMap<Integer, Double> probn = new HashMap<Integer, Double>();

        Map<Integer, Integer> document = super.translateVector(_document);

        // set of classes this document could be classified under
        Set<Integer> classes = super.numDocs.keySet();

        // 1.- For each class of the document, we initiate proby and probn with "a priori" probability
        for (int i : classes) // for each class i...
        {
            double prob = getAPRiori(i, docId);
            if (prob > 0.0) {
                proby.put(i, Math.log(prob));
                probn.put(i, Math.log(1.0 - prob));
            }
        }

        // 2.- A posteriori probability computation
        if (this.mode_evaluation == GenericNaiveBayes.FOR_EACH_TERM) {

            Set<Integer> termsOfDocument = document.keySet();

            // ================================= MAL =================================

            // for each class with prior probability
            for (int i : probYTotal.keySet()) {
                double _proby = this.probYTotal.get(i);
                double _probn = this.probNTotal.get(i);

                for (int term : document.keySet()) {
                    ClassifiedTerm cterm = cl.getTermById(term);
                    int freqT = document.get(term);


                    double _py = Math.log(this.computeProbY(cterm, freqT, i));
                    double _pn = Math.log(this.computeProbN(cterm, freqT, i));

                    _proby = _proby + _py - Math.log(1.0 - _py);
                    _probn = _probn + _pn - Math.log(1.0 - _pn);
                }

                proby.put(i, proby.get(i) + _proby);
                probn.put(i, probn.get(i) + _probn);
            }

            // ================================= FIN MAL =================================

        } else if (this.mode_evaluation == GenericNaiveBayes.FOR_EACH_TERM_IN_THE_DOCUMENT) {

            // 2.1.- For each term in the document, proby and probn are computed
            for (int term : document.keySet()) {
                if (this.selectedterms.containsKey(term)) {

                    ClassifiedTerm cterm = cl.getTermById(term);

                    if (GenericNaiveBayes._DEBUGGING) {
                        System.out.println("Term " + term + " " + cterm.getString());
                    }

                    int freqT = document.get(term);

                    if (GenericNaiveBayes._DEBUGGING) {
                        System.out.println(" => Frecuencia : " + freqT);
                    }

                    // 2.2 or each class j with positive prior, we compute posterior probability

                    for (int i : proby.keySet()) // for each class i... (with proby > 0.0)
                    {
                        double __proby = this.computeProbY(cterm, freqT, i), __probn = 0.0;
                        if (this.mode_naiveBayes != GenericNaiveBayes.STANDALONE_BINARY_NAIVE_BAYES) {
                            __probn = this.computeProbN(cterm, freqT, i);
                        }

                        if (__proby != 0.0 && __probn != 0.0) // smoothed probabilities not supposed to be 0.0
                        {
                            double _proby = Math.log(__proby), _probn = 0.0;
                            if (this.mode_naiveBayes != GenericNaiveBayes.STANDALONE_BINARY_NAIVE_BAYES) {
                                _probn = Math.log(__probn);
                            }

                            if (GenericNaiveBayes._DEBUGGING) {
                                System.out.println("\t=> _proby en clase " + i + " = " + __proby + " log =" + _proby);
                                System.out.println("\t=> _probn en clase " + i + " = " + __probn + " log =" + _probn);
                            }

                            proby.put(i, proby.get(i) + _proby);
                            if (this.mode_naiveBayes != GenericNaiveBayes.STANDALONE_BINARY_NAIVE_BAYES) {
                                probn.put(i, probn.get(i) + _probn);
                            }
                        }
                    }

                }
            }
        }


        if (GenericNaiveBayes._DEBUGGING) {
            System.out.println("/// Resultados para las clases:");
        }

        // 3.- we compute and normalize probabilities
        for (int i : proby.keySet()) {
            double _proby = proby.get(i);
            if (GenericNaiveBayes._DEBUGGING) {
                System.out.println(" >>> _proby para clase " + i + " = " + _proby);
                System.out.println(" >>> _probn para clase " + i + " = " + probn.get(i));
                System.out.println(" >>> _proby / (_proby + _probn) para clase i = " + Math.exp(_proby) / (Math.exp(_proby) + Math.exp(probn.get(i))));
            }

            if (this.mode_naiveBayes == GenericNaiveBayes.BINARY_NAIVE_BAYES_FOR_EACH_CLASS) {
                double _probn = probn.get(i);
                //ret.put(i, _proby - Math.log( Math.exp(_proby) + Math.exp(_probn) ) );
                ret.put(i, _proby - this.logsumexp(_proby, _probn));
            } else if (this.mode_naiveBayes == GenericNaiveBayes.STANDALONE_BINARY_NAIVE_BAYES) {
                ret.put(i, _proby);
            }
        }

        return ret;
    }

    private double logsumexp(double a0, double b0) {
        double a = Math.max(a0, b0);
        double b = Math.min(a0, b0);

        if (!(Math.exp(b - a) > 0.0)) {
            return a;
        }
        return a + Math.log1p(Math.exp(b - a));
    }
}
