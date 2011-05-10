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

public class IndexBuilder_Desc extends IndexBuilder {

    @Override
    public void startDocument() {
        super.startDocument();
        super.mustProcessInteger = false;
    }

    @Override
    public void endDocument() {
        super.endDocument();
    }

    @Override
    public void startUnit(String mystring) {
        super.startUnit(mystring);
        super.mustProcessString = false;

        if (mystring.compareTo("DESCRIPTEUR_ID") == 0) {
            super.mustProcessInteger = true;
        } else {
            if (mystring.compareTo("LIBELLE") == 0) {
                super.mustProcessString = true;
            }
            super.mustProcessInteger = false;
        }
    }

    @Override
    public void endUnit(String typetag) {
        super.endUnit(typetag);

        /** Do we need to build a descriptor? */
        if (typetag.compareTo("RECORD") == 0) {
            if (si.size() > 0) {

                // == We process the pairs term/frequency of each doc ==
                // 1.- we insert into the lexicon the pairs AND
                // 2.- for each term, we increase its document number
                Map<Integer, Integer> m = si.getFreq();
                List<Integer> l = new ArrayList<Integer>();

                for (Map.Entry<Integer, Integer> e : m.entrySet()) {
                    int id = e.getKey();
                    int freq = e.getValue();
                    ThesaurusTerm t = (ThesaurusTerm) lex.getTermById(id);

                    if (t != null) {
                        t.addDescriptor(this.processedInteger, freq);
                        t.incNumDocs();
                        l.add(id);

                    } else {
                        System.err.println("ERROR: Term " + id + " was uncorrectly processed");
                        System.exit(-1);
                    }
                }

                super.desc = super.desc.replaceAll("\\[V4\\.2\\]", " ");

                ld.put(this.processedInteger, new Descriptor(this.processedInteger, l, super.desc.trim()));

                ++numDescriptors;
                si.clear();
            }
        }
    }

    @Override
    public void characters(String s) {
        super.characters(s);
    }
}
