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

package base;
import java.util.*;
import java.io.*;
import org.tartarus.snowball.*;
import org.tartarus.snowball.ext.*;

public abstract class StringIndexer <T extends BaseTerm, L extends Lexicon> {
    // tells if we should use stemming or not
    protected static boolean doStem = false;
    
    //! tells if we are indexing or consulting
    protected static boolean indexing;
    
    //! stopword list
    protected static ArrayList<String> list;
    
    //! associated lexicon
    protected L l;
    
    //! snowball spanish stemmer
    protected static SnowballProgram myStemmer;
    
    //! freqs by term
    protected Map<Integer, Integer> freq;
    
    private static String encoding = "UTF8";
    
    private static Class nameOfTheClass;
    
    protected static boolean removeNumbers = false;
    
    public static void setStemLanguage(String lng) {
        // currently only spanish and english are supported languages
        if (lng.trim().equalsIgnoreCase("es"))
            myStemmer = new spanishStemmer();
        else if (lng.trim().equalsIgnoreCase("en"))
            myStemmer = new englishStemmer();
        else {
            System.err.println("Error: string '" + lng + "' does not corresponde to any language, use 'es' or 'en' instead.");
            System.exit(-1);
        }
    }
    
    public static void setRemoveNumbers(boolean rem) {
        removeNumbers = rem;
    }
    
    public static void setStem(boolean stem) {
        doStem = stem;
    }
    
    public void clear() {
        freq.clear();
    }
    
    public Map<Integer, Integer> getFreq() {
        return freq;
    }
    
    public int size() {
        return this.freq.size();
    }
    
    public abstract void createNewTerm(String s);
    
    protected boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException nfe){
            return s.matches("\\d+-\\d+");
        }
        
    }
    
    public void add(String s) {
        String _s = s.toLowerCase().toLowerCase();
        _s = _s.replaceAll("\\[V4.2\\]", "");
        _s = _s.replaceAll("<\\/?\\w+>", "");  // remove html/sgml tags
        _s = _s.replaceAll("[\\p{Punct}&&[^-]]+", " ");
        _s = _s.replaceAll("á", "a");
        _s = _s.replaceAll("é", "e");
        _s = _s.replaceAll("í", "i");
        _s = _s.replaceAll("ó", "o");
        _s = _s.replaceAll("ú", "u");
        _s = _s.replaceAll("ü", "u");
        
        StringTokenizer st = new StringTokenizer(_s);
        
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            
            // is the token a number
                       
            if ( !StringIndexer.removeNumbers || !this.isNumber(token))  {
                
                // is that a stopword?
                if (token.length() > 1 && Collections.binarySearch(list, token) < 0) {
                    // stem it
                    if (doStem) {
                        int last_index = token.indexOf('_');
                        if (last_index != -1)
                        {
                            // if contains "_"
                            if (last_index != token.length()-1)
                            {
                                // we extract the two substrings: before the last "_" and after it
                                String preffix = token.substring(0, last_index+1);
                                String newtoken = token.substring(last_index, token.length());
                                
                                StringIndexer.myStemmer.setCurrent(newtoken);
                                StringIndexer.myStemmer.stem();
                                token = preffix + StringIndexer.myStemmer.getCurrent();
                            
                            } // else do nothing (ends with "_")
                        
                        } else {
                            // token does not contains "_"
                            StringIndexer.myStemmer.setCurrent(token);
                            StringIndexer.myStemmer.stem();
                            token = StringIndexer.myStemmer.getCurrent();
                        }
                    }
                    
                    //obtain its identifier
                    int id = l.getTermIdByString(token);
                    
                    if (id == -1) // if the term did not exist in the system
                    {
                        if (indexing) // ... and we were indexing
                        {
                            // we insert it
                            createNewTerm(token);
                        /*id = l.size();
                        t.setId(id);
                        l.add(t);
                        this.freq.put(id, 1);*/
                        } // otherwise we do nothing
                        
                    } else {
                        
                        if (this.freq.containsKey(id))
                            this.freq.put(id, this.freq.get(id) + 1);
                        else this.freq.put(id, 1);
                    }
                    // add to "this.freq"
                    
                } // if (!Collections.binarySearch...
            }
            
        } // while (st.hasMoreTokens())
    }
    
    public static void setIndexing(boolean b) {
        indexing = b;
    }
    
    
    public StringIndexer() {
        freq = new HashMap<Integer, Integer>();
    }
    
    
    public StringIndexer(String s) {
        freq = new HashMap<Integer, Integer>();
        this.add(s);
    }
    
    
    public void setLexicon(L _l) {
        l = _l;
    }
    
    public static void setStopwordList(String stopwordFile) {
        list = new ArrayList<String>();
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(stopwordFile), encoding);
            BufferedReader in = new BufferedReader(isr);
            
            String s = in.readLine();
            
            while(s != null) {
                s = s.trim();
                s = s.toLowerCase();
                list.add(s);
                s = in.readLine();
            }
            
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: File " + stopwordFile + " not found");
            System.exit(-1);
            
        } catch (IOException ex2) {
            System.err.println("ERROR: File " + stopwordFile + " not readable");
            System.exit(-1);
        }
        Collections.sort(list);
    }
}



