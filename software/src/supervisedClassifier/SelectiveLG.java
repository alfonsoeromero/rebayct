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

package supervisedClassifier;
import java.util.*;
import indexation.*;
import supervisedClassifier.GenericClassifier;
import todelete.Chi2;

/**
 *
 * @author aeromero
 */
public class SelectiveLG extends GenericClassifier
{    
    protected boolean USE_FREQ_IN_SEL = true; //to use the frequency of the terms in the selection process
    
    protected boolean USE_LOCAL_SEL = true; //to use local term selection, otherwise we use global term selection
    
    protected boolean SHOWSHORTINFO = true;//to show information
    
    protected boolean SHOWINFO = false;//to show information
    
    //! total size of the lexicon
    protected int N;
    
    protected int totalFreqOfTerms; //total number of words (total frequency of terms)
    
    protected double confidence = 0.9999;
    
    protected HashMap<Integer, ArrayList<Integer>> usedselectedterms; //list of the terms being selected and for each one of them the list of classes where they are used
    
    public void setConfidenceAndMode(double d, boolean local) {
        confidence = d;
        if (local) {
            usedselectedterms = localselection();
        } else usedselectedterms = globalselection();
    }
    
    public SelectiveLG(String classIndexName) {
        
        super(classIndexName);
        totalFreqOfTerms = 0;
        N = cl.size();
        for (int i=0; i<N; ++i)
            totalFreqOfTerms += cl.getTermById(i).getTotalDocumentalFreq();
        if (SHOWSHORTINFO)  {
            System.out.println("The total frequency of terms (number of words) is " + totalFreqOfTerms);
            System.out.println("The initial number of terms (number of different words) is " +N);
        }
        
//term selection
        if (SHOWSHORTINFO) {
            if (this.USE_LOCAL_SEL) System.out.println("Using local term selection");
            else System.out.println("Using global term selection");
            if (USE_FREQ_IN_SEL) System.out.println("Using frequencies of terms in the terms selection process");
            else System.out.println("Without using frequencies of terms in the terms selection process");
        }
    }
    
    
    public double computeMI(double ntc, double nt, double nc, double ntot) {
        
        double mi = 2.0 * (ntc*Math.log(ntc*ntot/(nt*nc)) + (nt-ntc)*Math.log((nt-ntc)*ntot/(nt*(ntot-nc))) + (nc-ntc)*Math.log((nc-ntc)*ntot/(nc*(ntot-nt))) + (ntot+ntc-nt-nc)*Math.log((ntot+ntc-nt-nc)*ntot/((ntot-nt)*(ntot-nc))) );
        return mi;
    }
    
    public double computeGlobalMI(int i, int ntot) {
        
        double mimig = 0.0;
        double nt = (double) (cl.getTermById(i)).getTotalDocumentalFreq();
        double nc;
        double ntc;
        for (Map.Entry<Integer, Integer> entry : ((cl.getTermById(i)).getOccurrence()).entrySet()) {
            nc = (double) super.totalFreq.get(entry.getKey());
            ntc = (double) entry.getValue();
            mimig += ntc*Math.log(ntc*ntot/(nt*nc)) + (nc-ntc)*Math.log((nc-ntc)*ntot/(nc*(ntot-nt)));
        }
        mimig *= 2;
        return mimig;
    }
    
    public double computeGlobalMInoF(int i) {
        
        double ntot = (double) super.numDocuments;
        double mimignoF = 0.0;
        double nt = (double) (cl.getTermById(i)).getNumDocs();
        double nc;
        double ntc;
        for (Map.Entry<Integer, Integer> entry : ((cl.getTermById(i)).numDocsPerClass).entrySet()) {
            nc = (double) super.numDocs.get(entry.getKey());
            ntc = (double) entry.getValue();
            mimignoF += ntc*Math.log(ntc*ntot/(nt*nc)) + (nc-ntc)*Math.log((nc-ntc)*ntot/(nc*(ntot-nt)));
        }
        mimignoF *= 2;
        return mimignoF;
    }
    
    
//para seleccion global de terminos basada en informacion mutua
    public HashMap<Integer, Double> computegloballypreselectedtermsF(double criticalg) {
        HashMap<Integer, Double> globallypreselectedtermsF = new HashMap<Integer, Double>();
        double mimigF;
        for (int i=0; i<N; ++i) {
            mimigF = computeGlobalMI(i,this.totalFreqOfTerms);
            if ((mimigF - criticalg) > 0.0) //pasa el test global
                globallypreselectedtermsF.put(i,mimigF);
        }
        return globallypreselectedtermsF;
    }
    
    
//para seleccion global de terminos basada en informacion mutua pero sin usar frecuencias de terminos, solo numeros de documentos que contienen a t y son de clase c ...
    public HashMap<Integer, Double> computegloballypreselectedtermsnoF(double criticalg) {
        
        HashMap<Integer, Double> globallypreselectedtermsnoF = new HashMap<Integer, Double>();
        double mimignoF;
        for (int i=0; i<N; ++i) {
            mimignoF = computeGlobalMInoF(i);
            if ((mimignoF - criticalg) > 0.0) //pasa el test global
                globallypreselectedtermsnoF.put(i,mimignoF);
        }
        return globallypreselectedtermsnoF;
    }
    
//para (pre)seleccion local de terminos basada en test de informacion mutua
    public HashMap<Integer, HashMap<Integer, Double>> computepreselectedtermsperclassF(double critical1) {
        
        double mimi;
        HashMap<Integer, HashMap<Integer, Double>> preselectedtermsperclassF = new HashMap<Integer, HashMap<Integer, Double>>();//para cada clase la lista de terminos preseleccionados junto con su valor de mutual info
        HashMap<Integer, Integer> hay = new HashMap<Integer, Integer>();//num de terminos en cada clase
        HashMap<Integer, Integer> uso = new HashMap<Integer, Integer>();// num de terminos que se usaran para cada clase
        for (int i=0; i<N; ++i) {
            //if (globallyselectedterms.containsKey(i)) {
            ClassifiedTerm ct = cl.getTermById(i);
            Map<Integer, Integer> mp = ct.getOccurrence();
            for (Map.Entry<Integer, Integer> entry : mp.entrySet()) {
                if (hay.containsKey(entry.getKey()))
                    hay.put(entry.getKey(), hay.get(entry.getKey())+1);
                else {
                    // si aparece por primera vez esa clase, inicializo preselectedtermsperclassF
                    hay.put(entry.getKey(), 1);
                    HashMap<Integer, Double> k = new HashMap<Integer, Double>();
                    preselectedtermsperclassF.put(entry.getKey(), k);
                }
                //calculo la informacion mutua
                mimi = computeMI((double) entry.getValue(), (double) ct.getTotalDocumentalFreq(), super.totalFreq.get(entry.getKey()), this.totalFreqOfTerms);
                //se hace el test
                if ((mimi - critical1) > 0.0) { //pasa el test y se selecciona el termino
                    // uso sirve para ver cuantos terminos se (pre)seleccionan para cada clase
                    if (uso.containsKey(entry.getKey()))
                        uso.put(entry.getKey(), uso.get(entry.getKey())+1);
                    else uso.put(entry.getKey(), 1);
                    // se incluye el termino y su valor de mutual info en preselectedtermsperclassF
                    (preselectedtermsperclassF.get(entry.getKey())).put(i, mimi);
                }
            }
            //}
        }
// ahora imprimo hay y uso
        if (SHOWINFO) {
            System.out.println("Information about initial terms per class and preselected terms per class using frequencies:");
            for (int myhay : hay.keySet())
                System.out.println("class: "+ myhay +" total number of terms: " + hay.get(myhay) +" number of preselected terms: " + uso.get(myhay));
            System.out.println();
        }
        return preselectedtermsperclassF;
    }
    
    
//para (pre)seleccion local de terminos basada en test de informacion mutua sin frecuencias de terminos
    public HashMap<Integer, HashMap<Integer, Double>> computepreselectedtermsperclassnoF(double critical1) {
        
        double mimi;
        HashMap<Integer, HashMap<Integer, Double>> preselectedtermsperclassnoF = new HashMap<Integer, HashMap<Integer, Double>>();//para cada clase la lista de terminos seleccionados junto con su valor de mutual info
        HashMap<Integer, Integer> haynoF = new HashMap<Integer, Integer>();//num de terminos en cada clase
        HashMap<Integer, Integer> usonoF = new HashMap<Integer, Integer>();// num de terminos que se usaran para cada clase
        for (int i=0; i<N; ++i) {
            //if (globallyselectedterms.containsKey(i)) {
            ClassifiedTerm ct = cl.getTermById(i);
            for (Map.Entry<Integer, Integer> entry : ct.numDocsPerClass.entrySet()) {
                if (haynoF.containsKey(entry.getKey()))
                    haynoF.put(entry.getKey(), haynoF.get(entry.getKey())+1);
                else {
                    // si aparece por primera vez esa clase, inicializo selectedtermsperclassnoF
                    haynoF.put(entry.getKey(), 1);
                    HashMap<Integer, Double> k = new HashMap<Integer, Double>();
                    preselectedtermsperclassnoF.put(entry.getKey(), k);
                }
                //calculo la informacion mutua
                mimi = computeMI((double) entry.getValue(), (double) (cl.getTermById(i)).getNumDocs(), super.numDocs.get(entry.getKey()), super.numDocuments);
                //se hace el test
                if ((mimi - critical1) > 0.0) { //pasa el test y se selecciona el termino
                    // usonoF sirve para ver cuantos terminos se (pre)seleccionan para cada clase
                    if (usonoF.containsKey(entry.getKey()))
                        usonoF.put(entry.getKey(), usonoF.get(entry.getKey())+1);
                    else usonoF.put(entry.getKey(), 1);
                    // se incluye el termino y su valor de mutual info en selectedtermsperclassnoF
                    (preselectedtermsperclassnoF.get(entry.getKey())).put(i, mimi);
                }
            }
            //}
        }
// ahora imprimo hay y uso
        if (SHOWINFO) {
            System.out.println("Information about initial terms per class and preselected terms per class without using frequencies:");
            for (int myhay : haynoF.keySet())
                System.out.println("class: "+ myhay +" total number of terms: " + haynoF.get(myhay) +" number of preselected terms: " + usonoF.get(myhay));
            System.out.println();
        }
        return preselectedtermsperclassnoF;
    }
    
//para ordenar un HashMap por sus valores
    public List<Map.Entry<Integer, Double>> ordenaHMid(HashMap<Integer, Double> map) {
        
        List<Map.Entry<Integer, Double>> lista = new ArrayList<Map.Entry<Integer, Double>>(map.entrySet());
        java.util.Collections.sort(lista, new Comparator<Map.Entry<Integer, Double>>(){
            public int compare(Map.Entry<Integer, Double> entry, Map.Entry<Integer, Double> entry1) {
                // Return 0 for a match, -1 for less than and +1 for more then
                return (entry.getValue().equals(entry1.getValue()) ? 0 : (entry.getValue() < entry1.getValue() ? 1 : -1));
            }
        });
        return lista;
    }
    
// proceso final de seleccion local de terminos, que toma como entrada los terminos preseleccionados
    public HashMap<Integer, HashMap<Integer, Double>> computeselectedtermsperclass(HashMap<Integer, HashMap<Integer, Double>> preselectedtermsperclass) {
        
        HashMap<Integer, HashMap<Integer, Double>> selectedtermsperclass = new HashMap<Integer, HashMap<Integer, Double>>();
        int counter; //cuenta cuantos terminos se seleccionan para cada clase;
        if (SHOWINFO)
            System.out.println("Information about the number of locally selected terms per class");
        for (int clase : preselectedtermsperclass.keySet()) {
            HashMap<Integer, Double> k = new HashMap<Integer, Double>();
            selectedtermsperclass.put(clase, k);
            
// se ordenan los terminos seleccionados de cada clase en orden decreciente de importancia y se almacenan en lst
            List<Map.Entry<Integer, Double>> lst = ordenaHMid(preselectedtermsperclass.get(clase));
            
//seleccion de terminos basada en: suma de informaciones mutuas entre ti y clase menos suma de valores chi con crecimiento lineal de grados de libertad, y greedy search
            counter = 0;
            double sumami = 0.0;
            double sumachi = 0.0;
            int df = 1;
            double previousvalue = 0.0;
            for (Map.Entry<Integer, Double> termino: lst) {
                sumami += termino.getValue();
                sumachi += Chi2.critchi(1.0-this.confidence,df);
                if ((sumami - sumachi) > previousvalue) {
                    (selectedtermsperclass.get(clase)).put(termino.getKey(), termino.getValue());
                    counter++;
                    df++;
                    previousvalue = sumami - sumachi;
                } else break;
            }
// se imprime cuantos terminos se han seleccionado al final
            if (SHOWINFO)
                System.out.println("class "+ clase +" number of selected terms "+ counter);
//se imprime informacion sobre los terminos finalmente seleccionados en orden de creciente de mi
        } //fin del proceso de seleccion final de terminos por cada clase
        if (SHOWINFO) System.out.println();
        return selectedtermsperclass;
    }
    
    public HashMap<Integer, ArrayList<Integer>> computeselectedterms(HashMap<Integer, HashMap<Integer, Double>> selectedtermsperclass) {
        
        HashMap<Integer, ArrayList<Integer>> selectedterms = new HashMap<Integer, ArrayList<Integer>>();
        for (int clase : selectedtermsperclass.keySet())
            for (int sterm : (selectedtermsperclass.get(clase)).keySet())
                if (selectedterms.containsKey(sterm))
                    (selectedterms.get(sterm)).add(clase);
                else {
                ArrayList<Integer> vectorclases = new ArrayList<Integer>();
                selectedterms.put(sterm,vectorclases);
                (selectedterms.get(sterm)).add(clase);
                }
            return selectedterms;
    }
    
    
    
// proceso final de seleccion global de terminos, que toma como entrada los terminos preseleccionados globalmente
    public HashMap<Integer, ArrayList<Integer>> computegselectedterms(HashMap<Integer, Double> globallypreselectedterms) {
        
//primero se obtiene la lista de tyerminos seleccionados
        HashMap<Integer, ArrayList<Integer>> globallyselectedterms = new HashMap<Integer, ArrayList<Integer>>();
        int counter; //cuenta cuantos terminos se seleccionan
        
// se ordenan los terminos preseleccionados en orden decreciente de importancia y se almacenan en lst
        List<Map.Entry<Integer, Double>> lst = ordenaHMid(globallypreselectedterms);
        
//seleccion de terminos basada en: suma de informaciones mutuas entre ti y clase menos suma de valores chi con crecimiento lineal de grados de libertad, y greedy search
        counter = 0;
        double sumami = 0.0;
        double sumachi = 0.0;
        int df = 1;
        double previousvalue = 0.0;
        for (Map.Entry<Integer, Double> termino: lst) {
            sumami += termino.getValue();
            sumachi += Chi2.critchi(1.0-this.confidence,df);
            if ((sumami - sumachi) > previousvalue) {
                ArrayList<Integer> vectorclases = new ArrayList<Integer>();
                globallyselectedterms.put(termino.getKey(), vectorclases);
                counter++;
                df++;
                previousvalue = sumami - sumachi;
            } else break;
        }
// se imprime cuantos terminos se han seleccionado al final
        if (SHOWINFO) {
            System.out.println("The global selection process has selected "+ counter +" terms");
            System.out.println();
        }
        
//fin del proceso de seleccion global de terminos
//ahora se obtiene la estructura de salida, asociando a cada termino seleccionado la lista de clases donde aparece
        
        for (int sterm : globallyselectedterms.keySet()) {
            ClassifiedTerm ct = cl.getTermById(sterm);
            for (int clase : ct.getOccurrence().keySet())
                (globallyselectedterms.get(sterm)).add(clase);
        }
        
        return globallyselectedterms;
    }
    
//seleccion global de terminos
    public HashMap<Integer, ArrayList<Integer>> globalselection() {
        
        HashMap<Integer, ArrayList<Integer>> globallyselectedterms = new HashMap<Integer, ArrayList<Integer>>();
        double criticalg = Chi2.critchi(1.0-this.confidence,super.numClasses -1);
        if (this.USE_FREQ_IN_SEL) {//para preseleccion global de terminos basada en informacion mutua usando frecuencias de terminos
            //se obtiene la lista de terminos preseleccionados y sus valores de mi
            HashMap<Integer, Double> globallypreselectedtermsF = computegloballypreselectedtermsF(criticalg);
            //se obtiene la lista de terminos seleccionados y se le asocia a cada termino la lista de clases donde aparece
            globallyselectedterms = computegselectedterms(globallypreselectedtermsF);
        } else {//para preseleccion global de terminos basada en informacion mutua pero sin usar frecuencias de terminos, solo numero de documentos que contienen a t y son de clase c...
            //se obtiene la lista de terminos preseleccionados y sus valores de mi
            HashMap<Integer, Double> globallypreselectedtermsnoF = computegloballypreselectedtermsnoF(criticalg);
            //se obtiene la lista de terminos seleccionados y se le asocia a cada termino la lista de clases donde aparece
            globallyselectedterms = computegselectedterms(globallypreselectedtermsnoF);
        }
        return globallyselectedterms;
    }
    
//preseleccion local de terminos
    public HashMap<Integer, ArrayList<Integer>> localselection() {
        
        HashMap<Integer, ArrayList<Integer>> locallyselectedterms = new HashMap<Integer, ArrayList<Integer>>();
        double critical1 = Chi2.critchi(1.0-this.confidence,1);
        if (this.USE_FREQ_IN_SEL) {//para (pre)seleccion local de terminos basada en test de informacion mutua
            HashMap<Integer, HashMap<Integer, Double>> preselectedtermsperclassF = computepreselectedtermsperclassF(critical1);
            // proceso final de seleccion local de terminos
            HashMap<Integer, HashMap<Integer, Double>> selectedtermsperclassF = computeselectedtermsperclass(preselectedtermsperclassF);
            //obtiene para cada termino seleccionado la lista de clases donde aparece
            locallyselectedterms = computeselectedterms(selectedtermsperclassF);
        } else {//para (pre)seleccion local de terminos basada en test de informacion mutua sin frecuencias de terminos
            HashMap<Integer, HashMap<Integer, Double>> preselectedtermsperclassnoF = computepreselectedtermsperclassnoF(critical1);
            // proceso final de seleccion local de terminos
            HashMap<Integer, HashMap<Integer, Double>> selectedtermsperclassnoF = computeselectedtermsperclass(preselectedtermsperclassnoF);
            //obtiene para cada termino seleccionado la lista de clases donde aparece
            locallyselectedterms = computeselectedterms(selectedtermsperclassnoF);
        }
        return locallyselectedterms;
    }
    
// para ver las diferencias entre las distintas selecciones
    public void compareselectionmethods() {
        
        double critical1 = Chi2.critchi(1.0-this.confidence,1);
        double criticalg = Chi2.critchi(1.0-this.confidence,super.numClasses -1);
        
        System.out.println("The total frequency of terms (number of words) is " + this.totalFreqOfTerms);
        System.out.println("The initial number of terms (number of different words) is " +N);
        
        HashMap<Integer, Double> globallypreselectedtermsF = computegloballypreselectedtermsF(criticalg);
        System.out.println("The number of terms (after global pre-selection) in GPSF is "+globallypreselectedtermsF.size());
        
        HashMap<Integer, Double> globallypreselectedtermsnoF = computegloballypreselectedtermsnoF(criticalg);
        System.out.println("The number of terms (after global pre-selection) in GPSnoF is "+globallypreselectedtermsnoF.size());
        
        HashMap<Integer, ArrayList<Integer>> globallyselectedtermsF = computegselectedterms(globallypreselectedtermsF);
        System.out.println("The number of terms (after global selection) in GSF is "+globallyselectedtermsF.size());
        
        HashMap<Integer, ArrayList<Integer>> globallyselectedtermsnoF = computegselectedterms(globallypreselectedtermsnoF);
        System.out.println("The number of terms (after global selection) in GSnoF is "+globallyselectedtermsnoF.size());
        
        HashMap<Integer, HashMap<Integer, Double>> preselectedtermsperclassF = computepreselectedtermsperclassF(critical1);
        ArrayList<Integer> locallypreselectedtermsF = new ArrayList<Integer>();
        for (int clase : preselectedtermsperclassF.keySet())
            for (int sterm : (preselectedtermsperclassF.get(clase)).keySet())
                if (!locallypreselectedtermsF.contains(sterm))
                    locallypreselectedtermsF.add(sterm);
            System.out.println("The number of terms (after local pre-selection) in LPSF is "+locallypreselectedtermsF.size());
            
            HashMap<Integer, HashMap<Integer, Double>> preselectedtermsperclassnoF = computepreselectedtermsperclassnoF(critical1);
            ArrayList<Integer> locallypreselectedtermsnoF = new ArrayList<Integer>();
            for (int clase : preselectedtermsperclassnoF.keySet())
                for (int sterm : (preselectedtermsperclassnoF.get(clase)).keySet())
                    if (!locallypreselectedtermsnoF.contains(sterm))
                        locallypreselectedtermsnoF.add(sterm);
                System.out.println("The number of terms (after local pre-selection) in LPSnoF is "+locallypreselectedtermsnoF.size());
                
                HashMap<Integer, HashMap<Integer, Double>> selectedtermsperclassF = computeselectedtermsperclass(preselectedtermsperclassF);
                HashMap<Integer, ArrayList<Integer>> locallyselectedtermsF = computeselectedterms(selectedtermsperclassF);
                System.out.println("The number of terms (after local selection) in LSF is "+locallyselectedtermsF.size());
                
                HashMap<Integer, HashMap<Integer, Double>> selectedtermsperclassnoF = computeselectedtermsperclass(preselectedtermsperclassnoF);
                HashMap<Integer, ArrayList<Integer>> locallyselectedtermsnoF = computeselectedterms(selectedtermsperclassnoF);
                System.out.println("The number of terms (after local selection) in LSnoF is "+locallyselectedtermsnoF.size());
                
                System.out.println();
//compara GPSF y GPSnoF
                int engpsFnoengpsnoF = 0;
                for (int sterm : globallypreselectedtermsF.keySet())
                    if (!globallypreselectedtermsnoF.containsKey(sterm)) engpsFnoengpsnoF++;
                int engpsnoFnoengpsF = 0;
                for (int sterm : globallypreselectedtermsnoF.keySet())
                    if (!globallypreselectedtermsF.containsKey(sterm)) engpsnoFnoengpsF++;
                System.out.println("hay "+engpsFnoengpsnoF+" terminos en GPSF que no estan en GPSnoF");
                System.out.println("hay "+engpsnoFnoengpsF+" terminos en GPSnoF que no estan en GPSF");
                System.out.println();
//compara GSF y GSnoF
                int engsFnoengsnoF = 0;
                for (int sterm : globallyselectedtermsF.keySet())
                    if (!globallyselectedtermsnoF.containsKey(sterm)) engsFnoengsnoF++;
                int engsnoFnoengsF = 0;
                for (int sterm : globallyselectedtermsnoF.keySet())
                    if (!globallyselectedtermsF.containsKey(sterm)) engsnoFnoengsF++;
                System.out.println("hay "+engsFnoengsnoF+" terminos en GSF que no estan en GSnoF");
                System.out.println("hay "+engsnoFnoengsF+" terminos en GSnoF que no estan en GSF");
                System.out.println();
//compara LPSF y GPSF
                int enlpsFnoengpsF = 0;
                for (int i=0; i< locallypreselectedtermsF.size(); i++) {
                    int sterm = locallypreselectedtermsF.get(i);
                    if (!globallypreselectedtermsF.containsKey(sterm)) enlpsFnoengpsF++;
                }
                int engpsFnoenlpsF = 0;
                for (int sterm : globallypreselectedtermsF.keySet())
                    if (!locallypreselectedtermsF.contains(sterm)) engpsFnoenlpsF++;
                System.out.println("hay "+enlpsFnoengpsF+" terminos en LPSF que no estan en GPSF");
                System.out.println("hay "+engpsFnoenlpsF+" terminos en GPSF que no estan en LPSF");
                System.out.println();
//compara LPSnoF y GPSnoF
                int enlpsnoFnoengpsnoF = 0;
                for (int i=0; i< locallypreselectedtermsnoF.size(); i++) {
                    int sterm = locallypreselectedtermsnoF.get(i);
                    if (!globallypreselectedtermsnoF.containsKey(sterm)) enlpsnoFnoengpsnoF++;
                }
                int engpsnoFnoenlpsnoF = 0;
                for (int sterm : globallypreselectedtermsnoF.keySet())
                    if (!locallypreselectedtermsnoF.contains(sterm)) engpsnoFnoenlpsnoF++;
                System.out.println("hay "+enlpsnoFnoengpsnoF+" terminos en LPSnoF que no estan en GPSnoF");
                System.out.println("hay "+engpsnoFnoenlpsnoF+" terminos en GPSnoF que no estan en LPSnoF");
                System.out.println();
                
//compara LSF y GSF
                int enlsFnoengsF = 0;
                for (int sterm : locallyselectedtermsF.keySet())
                    if (!globallyselectedtermsF.containsKey(sterm)) enlsFnoengsF++;
                int engsFnoenlsF = 0;
                for (int sterm : globallyselectedtermsF.keySet())
                    if (!locallyselectedtermsF.containsKey(sterm)) engsFnoenlsF++;
                System.out.println("hay "+enlsFnoengsF+" terminos en LSF que no estan en GSF");
                System.out.println("hay "+engsFnoenlsF+" terminos en GSF que no estan en LSF");
                System.out.println();
                
//compara LSnoF y GSnoF
                int enlsnoFnoengsnoF = 0;
                for (int sterm : locallyselectedtermsnoF.keySet())
                    if (!globallyselectedtermsnoF.containsKey(sterm)) enlsnoFnoengsnoF++;
                int engsnoFnoenlsnoF = 0;
                for (int sterm : globallyselectedtermsnoF.keySet())
                    if (!locallyselectedtermsnoF.containsKey(sterm)) engsnoFnoenlsnoF++;
                System.out.println("hay "+enlsnoFnoengsnoF+" terminos en LSnoF que no estan en GSnoF");
                System.out.println("hay "+engsnoFnoenlsnoF+" terminos en GSnoF que no estan en LSnoF");
                System.out.println();
                
//compara LPSF y LPSnoF
                int enlpsFnoenlpsnoF = 0;
                int enlpsnoFnoenlpsF = 0; 
                for (int i=0; i< locallypreselectedtermsF.size(); i++) {
                    int sterm = locallypreselectedtermsF.get(i);
                    if (!locallypreselectedtermsnoF.contains(sterm)) enlpsFnoenlpsnoF++;
                }
                for (int i=0; i< locallypreselectedtermsnoF.size(); i++) {
                    int sterm = locallypreselectedtermsnoF.get(i);
                    if (!locallypreselectedtermsF.contains(sterm)) enlpsnoFnoenlpsF++;
                }
                System.out.println("hay "+enlpsFnoenlpsnoF+" terminos en LPSF que no estan en LPSnoF");
                System.out.println("hay "+enlpsnoFnoenlpsF+" terminos en LPSnoF que no estan en LPSF");
                System.out.println();
//compara LSF y LSnoF
                int enlsFnoenlsnoF = 0;
                for (int sterm : locallyselectedtermsF.keySet())
                    if (!locallyselectedtermsnoF.containsKey(sterm)) enlsFnoenlsnoF++;
                int enlsnoFnoenlsF = 0;
                for (int sterm : locallyselectedtermsnoF.keySet())
                    if (!locallyselectedtermsF.containsKey(sterm)) enlsnoFnoenlsF++;
                System.out.println("hay "+enlsFnoenlsnoF+" terminos en LSF que no estan en LSnoF");
                System.out.println("hay "+enlsnoFnoenlsF+" terminos en LSnoF que no estan en LSF");
                System.out.println();
    }
    
    //solo por cuestiones de compatibilidad. no sirve para nada realmente
    public HashMap<Integer, Double> classifyDocument(HashMap<Integer, Integer> mp, int docId) {
        return null;
    }
    
    
}
