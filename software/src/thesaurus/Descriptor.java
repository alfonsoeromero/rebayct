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

package thesaurus;

import java.util.*;

public class Descriptor {
    /** Identifier of this object */
    private int id;
    
    /** Sum of the weights of the terms in the descriptor */
    private double sumWeights;
    
    /** Content of the descriptor */
    private String description;
    
    /** List of terms */
    private List<Integer> terms;
    
    /** Associated nonDescriptors */
    private List<Integer> nonDescriptors;
    
    /** List of more general descriptors that contains
     * this, empty if none
     * **/
    private List<Integer> descendants;
    
    /** More specific descriptors contained in this,
     * empty if basic descriptor
     * */
    private List<Integer> fathers;
   
   /** Trees this descriptor belongs to */
    private List<Integer> microThesaurus;

    
    public Descriptor(int _id, List<Integer> myterms) {
        id = _id;
        terms = myterms;
        descendants = new ArrayList<Integer> ();
        nonDescriptors = new ArrayList<Integer> ();
        fathers = new ArrayList<Integer> ();
        microThesaurus = new ArrayList<Integer> ();
    }
    
    public Descriptor(int _id, List<Integer> myterms, String desc) {
        this(_id, myterms);
        description = desc;
    }
    
    public void setSumWeights(double weight) {
        this.sumWeights = weight;
    }
    
    public double getSumWeights() {
        return this.sumWeights;
    }
    
    public int getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isTopTerm() {
        return descendants.isEmpty();
    }
    
    public boolean isBasicDescriptor() {
        return this.fathers.isEmpty();
    }
    
    public boolean hasNonDescriptors() {
        return this.nonDescriptors.isEmpty();
    }
    
    public List<Integer> getTerms() {
        return this.terms;
    }
    
    public List<Integer> getNonDescriptors() {
        return this.nonDescriptors;
    }    
    
    public List<Integer> getFathers() {
        return this.fathers;
    }
    
    public List<Integer> getDescendants() {
        return this.descendants;
    }
    
    public void addDescendant(Integer desc) {
        descendants.add(desc);
    }
    
    public void addFather(Integer father) {
        fathers.add(father);
    }
    
    public void addNonDescriptor(Integer nd) {
        this.nonDescriptors.add(nd);
    }
    
    public void addMicroThesaurus(int mt) {
        this.microThesaurus.add(mt);
    }
    
    public List<Integer> getMicroThesaurus() {
        return this.microThesaurus;
    }
    
    public Descriptor(String s) {
        StringTokenizer st = new StringTokenizer(s);
        try{
            id = Integer.parseInt(st.nextToken());
            this.sumWeights = Double.parseDouble(st.nextToken());
            int tam = Integer.parseInt(st.nextToken());
            terms = new ArrayList<Integer>();
            for (int i=0; i<tam; ++i)
                terms.add(Integer.parseInt(st.nextToken()));
            
            tam = Integer.parseInt(st.nextToken());
            descendants = new ArrayList<Integer>();
            for (int i=0; i<tam; ++i)
                descendants.add(Integer.parseInt(st.nextToken()));
            
            tam = Integer.parseInt(st.nextToken());
            fathers = new ArrayList<Integer>();
            for (int i=0; i<tam; ++i)
                fathers.add(Integer.parseInt(st.nextToken()));
            
            tam = Integer.parseInt(st.nextToken());
            nonDescriptors = new ArrayList<Integer>();
            for (int i=0; i<tam; ++i)
                nonDescriptors.add(Integer.parseInt(st.nextToken()));
            
            tam = Integer.parseInt(st.nextToken());
            microThesaurus  = new ArrayList<Integer>();
            for (int i=0; i<tam; ++i)
                microThesaurus .add(Integer.parseInt(st.nextToken()));
                       
            this.description = st.nextToken().replaceAll("_", " ").trim();
            
        } catch (NumberFormatException  nex) {
            System.err.println("Error constructing Descriptor " + id + " " + nex + "\n");
            System.exit(0);
        }
    }
    
    @Override
    public String toString() {
        String s = "";
        s += id;
        s += " ";
        s += this.sumWeights;
        s += " ";
        // terms
        s += terms.size();
        s += " ";
        for (Integer i : terms)
            s = s + i.toString() + " ";
        
        // descendants
        s += this.descendants.size();
        s += " ";
        for (Integer i : this.descendants)
            s = s + i.toString() + " ";
        
        // fathers
        s += this.fathers.size();
        s += " ";
        for (Integer i : this.fathers)
            s = s + i.toString() + " ";
        
        // nondescriptors
        s += this.nonDescriptors.size();
        s += " ";
        for (Integer i : this.nonDescriptors)
            s = s + i.toString() + " ";
        
        s += this.microThesaurus.size();
        s += " ";
        for (Integer i : this.microThesaurus)
            s = s + i.toString() + " ";        
        
        s += this.description.replaceAll("\\s+", "_").trim();
        
        s += "\n";
        return s;
    }
}

