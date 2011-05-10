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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class NonDescriptor {

    /** Identifier of this object */
    private int id;
    /** Descriptor it references */
    private int descriptor;
    /** Sum of the weights of the terms in the descriptor */
    private double sumWeights;
    /** Content of the nondescriptor */
    private String description;
    /** List of terms */
    private List<Integer> terms;

    NonDescriptor(List<Integer> _terms, int desc, int newId, String descrip) {
        terms = _terms;
        descriptor = desc;
        id = newId;
        description = descrip;
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

    public List<Integer> getTerms() {
        return this.terms;
    }

    public int getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        String s = "";
        s += id;
        s += " ";
        s += descriptor;
        s += " ";
        s += this.sumWeights;
        s += " ";
        s += terms.size();
        s += " ";
        for (Integer i : terms) {
            s = s + i.toString() + " ";
        }

        s += this.description.replaceAll("\\s+", "_").trim();

        s += "\n";
        return s;
    }

    public NonDescriptor(String s) {
        StringTokenizer st = new StringTokenizer(s);
        try {
            id = Integer.parseInt(st.nextToken());
            descriptor = Integer.parseInt(st.nextToken());
            this.sumWeights = Double.parseDouble(st.nextToken());
            int tam = Integer.parseInt(st.nextToken());
            terms = new ArrayList<Integer>();
            for (int i = 0; i < tam; ++i) {
                terms.add(Integer.parseInt(st.nextToken()));
            }

            this.description = st.nextToken().replaceAll("_", " ").trim();

        } catch (NumberFormatException nex) {
            System.err.println("Error constructing NonDescriptor " + id + "\n");
            System.exit(-1);
        }
    }
}
