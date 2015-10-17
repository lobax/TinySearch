/*
 *--------------------------
 *- TinySearchEngine v 2.a -
 *--------------------------
 *- Kristian Alvarez 2015  -
 *--------------------------
 *-   No rights reserved   -
 *--------------------------
 *
 *
 *
 *
 *
 *
 *
 */

package se.kth.krijor;
import java.util.*;
import se.kth.id1020.util.*;
import se.kth.id1020.*;
import java.lang.Math;

class TinySearchEngine implements TinySearchEngineBase {
    HashMap<String, WordContainer> wordIndex; 
    HashMap<Document, Integer> docWordCount;


    int numberOfWords; 
    int totalWords; 
    public void preInserts() {
        wordIndex = new HashMap<String, WordContainer>(1000000); 
        docWordCount = new HashMap<Document, Integer>(1000); 
    }

    public void insert (Word word, Attributes attr) {
        String wordName = word.word;
        Document doc = attr.document; 

        boolean docExists = docWordCount.containsKey(doc);        
        if (docExists) {
            docWordCount.put(doc, docWordCount.get(doc) + 1);
        }

        else {
            docWordCount.put(doc, 1); 
        }
 
        boolean wordExists = wordIndex.containsKey(wordName); 

        if (wordExists) {
            WordContainer wrd = wordIndex.get(wordName); 
            wrd.add(doc);
        }

        else {
            wordIndex.put(wordName, new WordContainer(word,attr)); 
        }


       
    }  

    public void insert (Sentence sentence, Attributes attr) {
        for(Word word : sentence.getWords()) {
            totalWords++;
            if (word.word.matches("^\\w+$")) {
                insert(word, attr); 
                numberOfWords++; 
            }
        }
    }
 
    public void postInserts () {
        double numberOfDocuments = docWordCount.size(); 
        System.out.println("Number of documents: " + numberOfDocuments); 

        System.out.println("Number of words in index: " + numberOfWords); 
        System.out.println("Total number of words: " + totalWords); 
    }

    public List<Document> search(String query) {
        try {
        String[] q = query.split("\\s+"); 
        int size = q.length; 
        int orderby = query.lastIndexOf("orderby");  
        boolean order;
        String p; 

        if (orderby > 0 && "orderby".equals(q[size -3])) {
            p = parse(query.substring(0,orderby)); 
            order = true; 
        }
        else {
            p = parse(query); 
            order = false; 
        }

        WordContainer result = wordIndex.get(p);  
        if (result == null) {
            result = new WordContainer(); 
        }
        

        ArrayList<Document> results = result.get();     
        
        if (order) {
            String arg = q[size -2]; 
            boolean asc = true; 

            if ("desc".equals(q[size - 1])) {
                asc = false;
            }
            Comparator cmp = new DocComparator(arg, result, asc);  
            Collections.sort(results, cmp); 
        }
        
        return results;
        } 
        catch (NoSuchElementException e) {
            return null; 
        }

    }


    public String infix(String arg) {
        try{
            return parse(arg);
        }
        catch (NoSuchElementException e) {
            return "INVALID ARGUMENT(s)"; 
        }
    }

    /* HELPER FUNCTION parse: 
     *
     * Parses the string in prefix notation, returns infix notation. 
     * 
     * SIDE EFFECTS: 
     * Caches subqueries, and saves the resulting documents in the hash table
     * with the return string (in infix) as key. 
     * 
     * Ex: - the | for on -> (the - (for | on))
     *
     * Before evaluating a subquery, it also checks if it has been cached. It
     * takes commutability into account, eg: "| for on" == "| on for". 
     *
     * Since all subqueries are cached (including the main query), the returned
     * string will be the HashMap key for the evaluated answer of a query.
     *
     *
     */
    private String parse(String query) {
        String[] q = query.split("\\s+"); 
        WordContainer result; 
        Deque<String> stack = new ArrayDeque<String>();
        for (int i = q.length - 1; i >= 0; i--) {
            String s = q[i];
            if (s.equals("+")) {
                String first  = stack.removeFirst(); 
                String second = stack.removeFirst(); 
                String arg = "(" + first + " + " + second + ")"; 
                String revArg = "(" + second + " + " + first + ")"; 

                if (wordIndex.containsKey(arg)) {
                    stack.addFirst(arg); 
                    continue;
                }


                else if (wordIndex.containsKey(revArg)) {
                    stack.addFirst(revArg);
                    continue; 
                }

                else {
                    result = wordIndex.get(first);
                    if (result == null) {
                        result = new WordContainer(); 
                    }
                    result = result.intersection(wordIndex.get(second));
                    wordIndex.put(arg,result); 
                    stack.addFirst(arg); 
                }
            }
            else if (s.equals("-")) {
                String first  = stack.removeFirst(); 
                String second = stack.removeFirst(); 
                String arg = "(" + first + " - " + second + ")"; 

                if (wordIndex.containsKey(arg)) {
                    stack.addFirst(arg); 
                    continue;
                }

                else {
                    result = wordIndex.get(first);
                    if (result == null) {
                        result = new WordContainer(); 
                    }
                    result = result.difference(wordIndex.get(second));
                    wordIndex.put(arg,result); 
                    stack.addFirst(arg); 
                }
            }
            else if (s.equals("|")) {
                String first  = stack.removeFirst(); 
                String second = stack.removeFirst(); 
                String arg  = "(" + first + " | "+  second + ")"; 
                String revArg = "(" + second + " | " + first + ")"; 

                if (wordIndex.containsKey(arg)) {
                    stack.addFirst(arg); 
                    continue;
                }


                else if (wordIndex.containsKey(revArg)) {
                    stack.addFirst(revArg);
                    continue; 
                }

                else {
                    result = wordIndex.get(first);
                    if (result == null) {
                        result = new WordContainer(); 
                    }

                    result = result.union(wordIndex.get(second));
                    wordIndex.put(arg,result); 
                    stack.addFirst(arg);                
                }
            }
            else {
                    stack.addFirst(s); 
            }
        }

        return stack.removeFirst();
     
    }



    /*
     * HELPER CLASS: DocsComparator
     *
     * A comparator for various sortings of documents. 
     *
     *
     *
     */
    private class DocComparator implements Comparator <Document> {
        boolean asc;
        String arg;
        WordContainer wrd; 
        public DocComparator(String arg, WordContainer wrd, boolean asc) {
            this.arg = arg;
            this.wrd = wrd; 
            this.asc = asc;
        }
 
        private int popularity(Document doc1, Document doc2) {

            int diff = wrd.popularity.get(doc1) - wrd.popularity.get(doc2);
            diff = asc ? diff : -diff;
 
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else
                return 0;
 
        }
 
        private int relevance(Document doc1, Document doc2) {
            int diff = wrd.relevance.get(doc1).compareTo(wrd.relevance.get(doc2));
            diff = asc ? diff : -diff;
 
            if (diff > 0)
                return 1;
            else if (diff < 0)
                return -1;
            else {
                    return 1;
            }
        }
        public int compare(Document doc1, Document doc2) {
            if (arg.equals("popularity"))
                return popularity(doc1,doc2);
            else if (arg.equals("relevance"))
                return relevance(doc1,doc2);
            else
                return 0; 
        }
           
       
    }
 

    /* HELPER CLASS WordContainer:
     *
     * Hold a map of DocContainers (ie, all documents that contain the word),
     * as well as the name of the associated word. 
     *
     *
     *
     */
    private class WordContainer {
        public boolean initiated;
        public HashMap<Document, Double> relevance = new HashMap<Document, Double>(100);
        public HashMap<Document, Integer> popularity = new HashMap<Document, Integer>(100); 
        public ArrayList<Document> documents = new ArrayList<Document>(100); 
 
        public WordContainer(Word word, Attributes attr) {
            initiated = false;
            Document doc = attr.document; 
            relevance.put(doc,1.0);
            popularity.put(doc,doc.popularity); 
            documents.add(doc); 
        }
 
        public WordContainer( WordContainer wrd) {
            this.relevance = new HashMap<Document,Double>(wrd.relevance);
            this.documents = wrd.get(); 
            this.popularity = new HashMap<Document,Integer>(wrd.popularity); 
            initiated = true;
        }

        public WordContainer( ) {
            initiated = true;
        }
 
        public void add(Document doc) {
            boolean documentExists = relevance.containsKey(doc);

            if (documentExists) {
                relevance.put(doc, relevance.get(doc) + 1);
            }

            else {
                relevance.put(doc,1.0);
                popularity.put(doc,doc.popularity); 
                documents.add(doc); 
            }
        }

        public void add(Attributes attr) {
            add(attr.document); 
        }
       
        /*
         * METHOD get():
         *
         * Returns the ArrayList of DocContainer(s)
         *
         * NOTE: This creates a shallow copy!
         */
        
        public ArrayList<Document> get() {
            if (documents == null) 
                return new ArrayList<Document>();
            else
                return new ArrayList<Document>(documents);
        }
        
        private void addDocAndRelevance(Document doc, Double d) {
            if (!initiated) {
                setRelevance(); 
            }
            relevance.put(doc, d);
            popularity.put(doc, doc.popularity); 
            documents.add(doc); 
        }

        private void sumRelevance(Document doc, Double d) {
            if (!initiated) {
                setRelevance(); 
            }
            relevance.put(doc, relevance.get(doc) + d);

        }

        private void setRelevance() {
            for (Document doc : documents) {
                double docTerm = docWordCount.get(doc).doubleValue();
                double termFreq = relevance.get(doc) / docTerm ; 
                double totalSize = docWordCount.size(); 
                double docsSize = documents.size(); 
                double invTermFreq = Math.log10(totalSize/docsSize); 
                double res = termFreq * invTermFreq; 

                relevance.put(doc, res); 
            }

            initiated = true; 
        }


        public Double getRelevance(Document doc) {
            if (!initiated) {
                setRelevance(); 
            }
            return relevance.get(doc);
        }

        public boolean hasDocument(Document doc) {
            return relevance.containsKey(doc); 
        }

        public WordContainer intersection (WordContainer wrd) {
            WordContainer result = new WordContainer();
            if (wrd == null) {
                wrd = new WordContainer(); 
            }

            for (Document doc: wrd.get()) {
                if (this.hasDocument(doc)) {
                    result.addDocAndRelevance(doc, wrd.getRelevance(doc)); 
                    result.sumRelevance(doc, this.getRelevance(doc)); 
                }
            }
            return result; 
        }

        public WordContainer difference(WordContainer wrd) {
            WordContainer result = new WordContainer();
            if (wrd == null) {
                return result; 
            }

            for (Document doc : this.get()) {
                if (!wrd.hasDocument(doc)) {
                    result.addDocAndRelevance(doc, this.getRelevance(doc)); 
                    
                }
            }
            return result; 
            
        }
        public WordContainer union(WordContainer wrd) {
            WordContainer result = new WordContainer(this); 
            if (wrd == null) {
                wrd = new WordContainer(); 
            }
            for(Document doc: wrd.get()){
                if (result.hasDocument(doc)) {
                    result.sumRelevance(doc, result.getRelevance(doc));
                }
                else {
                    result.addDocAndRelevance(doc, wrd.getRelevance(doc)); 

                }
            }

            return result;

        }
 
    }
}


