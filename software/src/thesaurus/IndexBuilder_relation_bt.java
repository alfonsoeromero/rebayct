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

public class IndexBuilder_relation_bt extends IndexBuilder {

    /** Source identifier (specific descriptor) */
    private int source;
    /** Destiny identifier (general descriptor) */
    private int destiny;
    /** Number of BT relationships processed */
    private int relations;

    @Override
    public void startDocument() {
        relations = 0;
        super.startDocument();
    }

    @Override
    public void endDocument() {
        super.endDocument();
        System.out.println("BT relations: " + relations);
    }

    @Override
    public void startUnit(String mystring) {
        super.startUnit(mystring);
    }

    @Override
    public void endUnit(String typetag) {
        super.endUnit(typetag);

        if (typetag.compareTo("SOURCE_ID") == 0) {
            source = super.processedInteger;
        } else if (typetag.compareTo("CIBLE_ID") == 0) {
            this.destiny = super.processedInteger;
            Descriptor _source = null, _destiny = null;

            _source = ld.get(source);
            _destiny = ld.get(destiny);

            if (_source == null || _destiny == null) {
                System.out.println(_source);
                System.out.println(_destiny);
                System.err.println("ERROR: bad descriptors pair (" + source + ", " + destiny + "). Exiting.");
                System.exit(-1);
            }

            _source.addDescendant(destiny);
            _destiny.addFather(source);
            ++relations;
        }


    }

    public IndexBuilder_relation_bt() {
        super();
        super.mustProcessInteger = true;
    }
}
