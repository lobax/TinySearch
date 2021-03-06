package se.kth.krijor;
import java.util.*;
import se.kth.id1020.util.*;
import se.kth.id1020.*;
import java.lang.Math;

/**
 * <h1>TinySearchEngine</h1>
 * A simple search engine, made for the course
 * Algorithms and Data structures (ID1020).
 * <p>
 * Implements {@link TinySearchEngineBase}. 
 * 
 *
 * @author Kristian Alvarez Jörgensen
 * @version 2.0
 * @see TinySearchEngineBase
 *
 */
class TinySearchEngine implements TinySearchEngineBase {

    HashMap<String, WordContainer> wordIndex; 
    HashMap<Document, Integer> docWordCount;


    public void preInserts() {
        wordIndex = new HashMap<String, WordContainer>(1000000); 
        docWordCount = new HashMap<Document, Integer>(1000); 
    }

    /**
     * Adds a {@link Word} and its corresponding {@link Attributes} to the Index. 
     *
     * @param word the word added to the index.
     * @param attr the attributes to the word. 
     *
     */
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

    /** 
     * Adds a {@link Sentence} and it's correspong {@link Attributes} to the Index. 
     *
     * @param sentence a list of <i>Word</i>s added to the index. 
     * @param attr the Attributes to the words. 
     *
     *
     */

    public void insert (Sentence sentence, Attributes attr) {
        for(Word word : sentence.getWords()) {
            insert(word,attr); 
        }
    }
 
    public void postInserts () {
        double numberOfDocuments = docWordCount.size(); 
        System.out.println("Number of documents: " + numberOfDocuments); 

    }

    /**
     * Searches the index and returns a list documents that match the query.
     * The query needs to be in prefix notion - {@link #infix(String) infix}
     * can be used to understand how a prefix query is evaluated in infix.  
     * 
     * @param query a String containing a search query (in prefix notation).
     * @return A list of documents matching the query.
     * @see <a href="https://github.com/lobax/TinySearch/blob/master/README.md#query-syntax">README.md</a>
     */
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


    /**
     * Takes a query in prefix notation and returns its infix represention.
     *
     * @param arg - an argument in prefix notation.
     * @return argument in infix notation. 
     */
    public String infix(String arg) {
        try{
            String[] q = arg.split("\\s+"); 
            int size = q.length; 
            int orderby = arg.lastIndexOf("orderby");  
            boolean order;
            String p; 

            if (orderby > 0 && "orderby".equals(q[size -3])) {
                p = parse(arg.substring(0,orderby)); 
                order = true; 
            }
            else {
                p = parse(arg); 
                order = false; 
            }

            if (order) {
                p = p + " " + arg.substring(orderby, arg.length()).toUpperCase(); 
            }

            return p; 

        }
        catch (NoSuchElementException e) {
            return "INVALID ARGUMENT(s)"; 
        }
    }

    /**  
     * Parses the string in prefix notation, returns infix notation. 
     * <p>
     * SIDE EFFECTS:<br />
     * Caches subqueries, and saves the resulting documents in the hash table
     * with the return string (in infix) as key. 
     * 
     * <br />Ex: <tt>- the | for on</tt> -> <tt>(the - (for | on))</tt>
     *
     * <br />Before evaluating a subquery, it also checks if it has been cached. It
     * takes commutability into account, eg: <tt>| for on</tt> == <tt>| on for</tt>. 
     *
     * <br />Since all subqueries are cached (including the main query), the returned
     * string will be the HashMap key for the evaluated answer of a query.
     * @param query - query in prefix notaion
     * @return a infix representation of query
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
     * An extendable comparator that allows the sorting 
     * of documents by popularity or relevance in ascending
     * or descending form. 
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

            int diff = doc1.popularity - doc2.popularity;
            diff = asc ? diff : -diff;
 
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else
                return 0;
 
        }
 
        private int relevance(Document doc1, Document doc2) {
            int diff = wrd.getRelevance(doc1).compareTo(wrd.getRelevance(doc2));
            diff = asc ? diff : -diff;
 
            if (diff > 0)
                return 1;
            else if (diff < 0)
                return -1;
            else 
                return 0;
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
     * Holds a list of documents associated with a query. 
     * Also holds a HashMap of Doubles (representing relevance) with the documents as key. 
     *
     * The actual relevance for each documents is calculated when getRelevance()
     * (or similar) is first called, and no sooner.  
     *
     * Importantly, WordContainer holds methods to compute unions, intersections and 
     * differences with other WordContainers. The ouput of these methods are new 
     * WordContainer instances. 
     *
     *
     */
    private class WordContainer {
        public boolean initiated;
        public HashMap<Document, Double> relevance = new HashMap<Document, Double>(100);
        public ArrayList<Document> documents = new ArrayList<Document>(100); 
 
        public WordContainer(Word word, Attributes attr) {
            initiated = false;
            Document doc = attr.document; 
            relevance.put(doc,1.0);
            documents.add(doc); 
        }
 
        public WordContainer( WordContainer wrd) {
            this.relevance = new HashMap<Document,Double>(wrd.relevance);
            this.documents = wrd.get(); 
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
                documents.add(doc); 
            }
        }

        public void add(Attributes attr) {
            add(attr.document); 
        }
       
        /*
         * METHOD get():
         *
         * Returns the ArrayList of Documents
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
                //System.out.print("Relevance for " + doc.name + " set to: " + res);  //DEBUG
                //System.out.println(" - docTerm" + docTerm + " docCount " + relevance.get(doc)); //DEBUG 
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
                wrd = new WordContainer(); 
            }

            for (Document doc : this.get()) {
                if (!wrd.hasDocument(doc)) {
                    result.addDocAndRelevance(doc, this.getRelevance(doc)); 
                    
                }
            }
            return result; 
            
        }
        public WordContainer union(WordContainer wrd) {
            if (!initiated) {
                this.setRelevance(); 
            }

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


