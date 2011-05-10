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
import java.util.Map;

public class IndexBuilder_uf extends IndexBuilder {

    /** current descriptor whose nondescriptors are
     * processing */
    private Descriptor current;

    @Override
    public void startDocument() {
        super.startDocument();
    }

    @Override
    public void endDocument() {
        super.endDocument();
    }

    @Override
    public void startUnit(String mystring) {
        super.startUnit(mystring);

        if (mystring.compareTo("DESCRIPTEUR_ID") == 0) {
            super.mustProcessInteger = true;
        } else {
            super.mustProcessInteger = false;
        }

        if (mystring.compareTo("UF_EL") == 0) {
            this.mustProcessString = true;
        } else {
            this.mustProcessString = false;
        }
    }

    @Override
    public void endUnit(String typetag) {
        super.endUnit(typetag);
        if (typetag.compareTo("DESCRIPTEUR_ID") == 0) {
            this.current = ld.get(processedInteger);

        } else if (typetag.compareTo("UF_EL") == 0) {
            // we create a nondescriptor

            if (si.size() > 0) {

                // == We process the pairs term/frequency of each doc ==
                // 1.- we insert into the lexicon the pairs AND
                // 2.- for each term, we increase its document number
                Map<Integer, Integer> m = si.getFreq();
                List<Integer> l = new ArrayList<Integer>();

                for (Map.Entry<Integer, Integer> e : m.entrySet()) {
                    int _id = e.getKey();
                    int freq = e.getValue();
                    ThesaurusTerm t = (ThesaurusTerm) lex.getTermById(_id);

                    if (t != null) {
                        t.addNonDescriptor(numNonDescriptors, freq);
                        t.incNumDocs();
                        l.add(_id);

                    } else {
                        System.err.println("ERROR: Term " + numNonDescriptors + " was uncorrectly processed");
                        System.exit(-1);
                    }
                }

                NonDescriptor nondesc = new NonDescriptor(l, current.getId(), numNonDescriptors, super.desc.trim());

                lnd.put(nondesc.getId(), nondesc);
                current.addNonDescriptor(nondesc.getId());

                ++numNonDescriptors;
                si.clear();
                this.mustProcessString = false;
            }
        }
    }

    public IndexBuilder_uf() {
        super();
        // TODO Auto-generated constructor stub
    }
}
