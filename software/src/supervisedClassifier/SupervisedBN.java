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
 * SupervisedBN.java
 *
 * Created on 20 de marzo de 2007, 13:18
 *
 */

package supervisedClassifier;
import unsupervisedClassifier.*;
import thesaurus.*;
import java.util.*;
import indexation.*;
import java.io.*;

/**
 *
 * @author aeromero
 */
public class SupervisedBN extends BNClassifier 
{
    public static boolean LAPLACE = true;
    
    //! limit to select terms
    static double limit = 0.0;
    
    //! Number of documents classified (to compute a priori probabilities)
    int numDocuments;
    
    //! Number of classes
    int numClasses;
    
    //! If we are using or not the thesaurus
    boolean useThesaurus;
    
    //! strength of the thesaurus
    double strengthThesaurus;
    
    //! If we are using or not the structure
    boolean useStructure;
    
    //! strength of the structure
    double strengthStructure;
    
    //! For each class, it stores the number of documents classified over it
    HashMap<Integer, Integer> numDocs;
    
    //! For each class, it stores the total frequency of terms appearing in it
    HashMap<Integer, Integer> totalFreq;
    
    //! ClassifiedLexicon stores the inverted list of occurrences of term in classes
    ClassifiedLexicon cl;
    
    //! for each descriptor, the list of weights of a term in it
    Map<Integer, List<Double>> weights;
    
    //! for each descriptor, the weight obtained by classification
    Map<Integer, Double> probCL;
    
    //! to index the text
    ClassifiedStringIndexer csi;
    
    //! Set of term identifiers relative to classified lexicon obtained after processing the document
    Map<Integer, Integer> classifiedTerms;
    
    /** Creates a new instance of SupervisedBN */
    public SupervisedBN(String classIndexName, String indexName, String stopwordsFileName, boolean doingStemming, boolean usingNidf, boolean usingExactPhrases,
            double _strengthThesaurus, double _strengthStructure) {
        super(indexName, stopwordsFileName, doingStemming, usingNidf, usingExactPhrases);
        csi = new ClassifiedStringIndexer();
        this.readClassIndex(classIndexName);
        weights = new HashMap<Integer, List<Double>>();
        //System.out.println("Classifier => bayesian network");
        si = new ThesaurusStringIndexer();
        si.setLexicon(BNClassifier.lex);
        ThesaurusStringIndexer.setIndexing(false);
        ThesaurusStringIndexer.setStem(true);
        ThesaurusStringIndexer.setStopwordList(stopwordsFileName);
        
        // strength of the thesaurus
        this.strengthThesaurus = _strengthThesaurus;
        BNClassifier.associativity = _strengthThesaurus;
        
        this.strengthStructure = _strengthStructure;
        BNClassifier.associativityb = _strengthStructure;
        
        this.useStructure = this.strengthStructure > 0.0;
        this.useThesaurus = this.strengthThesaurus > 0.0;
    }
    
    private void readClassIndex(String classIndexName) {
        int line = 1;
        try {
            BufferedReader in = new BufferedReader(new FileReader(classIndexName));
            
            // 1.- we read the lexicon
            cl = new ClassifiedLexicon();
            cl.read(in);
            
            // 2.- we read the number of classes
            numClasses = Integer.parseInt(in.readLine());
            
            // 3.- we read the number of documents
            numDocuments = Integer.parseInt(in.readLine());
            
            // 4.- we initialize the arrays and fill them
            numDocs = new HashMap<Integer, Integer>(numClasses);
            totalFreq = new HashMap<Integer, Integer>(numClasses);
            
            // for each class (stored in one line...)
            for (int i=0; i<numClasses; ++i) {
                // we read the line and tokenize it
                StringTokenizer st = new StringTokenizer(in.readLine());
                
                // should have three tokens...
                int classID = Integer.parseInt(st.nextToken());
                int _numDocuments = Integer.parseInt(st.nextToken());
                int myFreq = Integer.parseInt(st.nextToken());
                
                // we insert the values on the arrays
                numDocs.put(classID, _numDocuments);
                totalFreq.put(classID, myFreq);
            }
            
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: file " + classIndexName + " does not exist. Exiting.");
            System.exit(-1);
        } catch (NumberFormatException ex) {
            System.err.println("ERROR: number expected at line " + line + " of file " + classIndexName + " was not found. Exiting.");
            System.exit(-1);
        } catch (IOException ex){
            System.err.println("ERROR: file " + classIndexName + " could not be read. Exiting.");
            System.exit(-1);
        }
        
        // 5.- ClassifiedStringIndexer initialization
        ClassifiedStringIndexer.setIndexing(false);
        csi.setLexicon(cl);
        
    }
    
    private void aggregateWeights() {
        this.probCL = new HashMap<Integer, Double>();
        for (Map.Entry<Integer, List<Double>> dsc : this.weights.entrySet()) {
            double w = 1.0;
            int descriptorID = dsc.getKey();
            List<Double> ws = dsc.getValue();
            for (double val : ws) {
                if (val > SupervisedBN.limit)
                    w = w*(1.0 - val);
            }
            
            probCL.put(descriptorID, w);
        }
    }
    
    
    private void computeWeights(String _textToClassify) {
        csi.clear();
        csi.add(_textToClassify);
        this.classifiedTerms = csi.getFreq();
        weights.clear();
        
        for (int termId : classifiedTerms.keySet()) {
            
            ClassifiedTerm ct = cl.getTermById(termId);
            Map<Integer, Integer> occ = ct.getOccurrence();
            
            for (Map.Entry<Integer, Integer> mp : occ.entrySet()) {
                
                int descriptorId = mp.getKey();
                int frequency = mp.getValue();
                
                double weight = 0.0;
                if (LAPLACE)
                    weight = (frequency + 1.0) / (ct.getTotalDocumentalFreq() + 2.0);
                else weight = frequency / ct.getTotalDocumentalFreq();
                
                if (weights.containsKey(descriptorId)) {
                    weights.get(descriptorId).add(weight);
                } else {
                    List<Double> l = new ArrayList<Double>();
                    l.add(weight);
                    weights.put(descriptorId, l);
                }
            }
        }
    }
    
    /* Propagates probability from Descriptors to themselves */
    protected void propagateDToD2(Map<Integer, Double> probC, Map<Integer, Double> probFin) {
        List<Integer> queue = new ArrayList<Integer> ();
        for (Map.Entry<Integer, Double> desc : probC.entrySet()) {
            Descriptor _D = ld.get(desc.getKey());
            insertDescendantsOnQueue(_D.getId(),queue);
        }
        
        List<Integer> topologicallist = new ArrayList<Integer> ();
        while  (!queue.isEmpty()) {
            Integer S = queue.get(0);
            queue.remove(0);
            if (!topologicallist.contains(S))
                topological(S, topologicallist, queue);
        }
        while  (!topologicallist.isEmpty()) {
            Integer S = topologicallist.get(0);
            topologicallist.remove(0);
            double acum = 0.0;
            Descriptor _S = ld.get(S);
            for (Integer U : _S.getFathers())
                if (probFin.containsKey(U))
                    acum += probFin.get(U)/_S.getFathers().size();
            
            double assoc = this.strengthStructure;
            
            // aquí se pueden probar diferentes esquemas de "assoc"
            
            if (probC.containsKey(S))
                probFin.put(S,1.0-(1.0-assoc*acum)*probC.get(S));
            else probFin.put(S,1.0-(1.0-assoc*acum));
        }
    }
    
    List<Result> classify(String textToClassify) {
        
        if (BNClassifier.DEBUGGING) {
            System.out.println("BAYESIAN");
            System.out.println("=>" + textToClassify + "<=");
        }
        
        // 0.- we get rid of the accents and other similar stuff
        String _textToClassify = internationalizeText(textToClassify);
        
        this.computeWeights(textToClassify);
        this.aggregateWeights(); // fills the value of "probCL"
        
        Map<Integer, Double> probC;
        
        if (this.useThesaurus) {
            // 1.- For each term of the collection...
            Map<Integer, Double> probD = new TreeMap<Integer, Double>();
            Map<Integer, Double> probND = new TreeMap<Integer, Double>();
            
            // 2.1.- ... we propagate textual probabilities: terms to descriptor and nondescriptor nodes
            if (this.useExactPhrases)
                propagateTextToDescriptorsWithExactPhrases(probD, probND, _textToClassify);
            else propagateTextToDescriptorsWithoutExactPhrases(probD, probND);
            
            // 2.2.- For each nondescriptor and descriptor node, we propagate to its
            // 	related descriptor, also receiving the probabilities from training data
            probC = probCL;
            
            propagateNDToD(probD, probND, probC);
            
            // we clear the maps
            probD = null;
            probND = null;
            
        } else {
            // 2.1.- the final probabilities before propagating towards the structure of the thesaurus,
            //   are only those coming from training data
            probC = probCL;
        }
        
        
        // 3.- We propagate among the descriptors, following the BT relation
        Map<Integer, Double> probFin = new HashMap<Integer, Double>();
        
        if (this.useStructure)
            propagateDToD2(probC, probFin);
        
        endsPropagation(probC, probFin); // if structured is not used, it computes each probFin as 1.0 - probC
        // if structure is used, it probably has no effect
        
        // 4.- We compute nIdf_q for each node, and we make the product of
        //   the probability with that
        
        if(useNidf)
            this.computeNIDF(probFin, probCL.keySet());
        
        probC = null;
        List<Result> l = new ArrayList<Result>();
        
        
        for (Map.Entry<Integer, Double> result : probFin.entrySet() ) {
            if (result.getValue() > 0.0) {
                Descriptor d = ld.get(result.getKey());
                l.add(new Result( d, result.getValue() ));
            }
        }
        
        Collections.sort(l, new Comparator<Result>() { public int compare(Result s1, Result s2) { if (s1.getProb() > s2.getProb()) return -1; else return 1; } });
        return l;
    }
    
    private void computeDegCov(Map<Integer, Double> nidf, Map<Integer, Double> degCov) {
        // Idf computation for each term (thesaurus)
        double sum = 0.0;
        double N = SupervisedBN.numDescriptors + SupervisedBN.numNonDescriptors;
        for (Map.Entry<Integer, Integer> termsOfDocument : m.entrySet()) {
            ThesaurusTerm t = lex.getTermById(termsOfDocument.getKey().intValue());
            double Ni = t.getDescriptors().size() + t.getNonDescriptors().size();
            double myIdf = Math.log10(N/Ni);
            sum += myIdf;
            nidf.put(t.getId(), myIdf);
        }
        
        // Idf computation for each term (classified term)
        N = SupervisedBN.numDescriptors;
        
        for (Map.Entry<Integer, Integer> termsOfDocument : this.classifiedTerms.entrySet()) {
            ClassifiedTerm t = this.cl.getTermById(termsOfDocument.getKey().intValue());
            double Ni = t.getNumClasses();
            double myIdf = Math.log10(N/Ni);
            sum += myIdf;
            degCov.put(t.getId(), myIdf);
        }
        
        // Idf normalization
        sum = 1.0 / sum;
        
        for (Map.Entry<Integer, Double> idfs : nidf.entrySet())
            nidf.put(idfs.getKey(), idfs.getValue()*sum);
        
        for (Map.Entry<Integer, Double> idfs : degCov.entrySet())
            degCov.put(idfs.getKey(), idfs.getValue()*sum);
    }
    
    @Override
    public List<Result> classifyString(String s)
    {
        m.clear();
        si = new ThesaurusStringIndexer();
        si.setLexicon(BNClassifier.lex);
        System.err.println("yuyu");
        System.err.println("yeee " + s);
        si.add(s);
        m = si.getFreq();
        return this.classify(s);
    }
    
    private void computeNIDF(Map<Integer, Double> probFin, Set<Integer> s) {
        Map<Integer, Double> idf = new HashMap<Integer, Double>();
        Map<Integer, Double> degCov = new HashMap<Integer, Double>();
        
        computeDegCov(idf, degCov);
        
        // Nidf data structure
        Map<Integer, Double> nidf = new HashMap<Integer, Double>();
        
        // STEP 1: we add the IDF to the nidf (only "thesaurus" information)
        for (Map.Entry<Integer, Integer> termsOfDocument : m.entrySet()) {
            Set<Integer> descendants = new HashSet<Integer>();
            Queue<Integer> toProcess = new LinkedList<Integer>();
            
            ThesaurusTerm t = lex.getTermById(termsOfDocument.getKey().intValue());
            for (Map.Entry<Integer, Integer> descriptors : t.getDescriptors())
                toProcess.add(descriptors.getKey());
            
            for (Map.Entry<Integer, Integer> descriptors : t.getNonDescriptors()) {
                NonDescriptor nd = lnd.get(descriptors.getKey());
                toProcess.add(nd.getDescriptor());
            }
            
            while(!toProcess.isEmpty()) {
                Integer top = toProcess.poll();
                if (!descendants.contains(top)) {
                    descendants.add(top);
                    for (Integer i : (ld.get(top)).getDescendants())
                        if (!descendants.contains(i))
                            toProcess.add(i);
                }
            }
            
            for (Integer d : descendants)
                if (nidf.containsKey(d))
                    nidf.put(d, nidf.get(d) + idf.get(t.getId()));
                else nidf.put(d, idf.get(t.getId()));
        } // for each term's descendant
        
        
        // STEP2: we add to the nidf the idfs of the classified terms
        for (Map.Entry<Integer, Integer> termsOfDocument : this.classifiedTerms.entrySet()) {
            Set<Integer> descendants = new HashSet<Integer>();
            
            Queue<Integer> toProcess = new LinkedList<Integer>();
            ClassifiedTerm t = cl.getTermById(termsOfDocument.getKey().intValue());
            Map<Integer, Integer> myOcc = t.getOccurrence();
            for (Map.Entry<Integer, Integer> descriptors : myOcc.entrySet() )
                toProcess.add(descriptors.getKey());
            
            while(!toProcess.isEmpty()) {
                Integer top = toProcess.poll();
                if (!descendants.contains(top)) {
                    descendants.add(top);
                    for (Integer i : (ld.get(top)).getDescendants())
                        if (!descendants.contains(i))
                            toProcess.add(i);
                }
            }
            
            for (Integer d : descendants)
                if (nidf.containsKey(d))
                    nidf.put(d, nidf.get(d) + degCov.get(t.getId()));
                else nidf.put(d, degCov.get(t.getId()));
        }
        
        // we recalculate the probability as probFin * nidf
        
        for (Map.Entry<Integer, Double> it : probFin.entrySet()) {
            int i = it.getKey();
            if (nidf.containsKey(i)) {
                double prob = it.getValue();
                probFin.put( i, prob * nidf.get(i) );
            } else probFin.put( i, 0.0 );
        }
        
    }
    
}

