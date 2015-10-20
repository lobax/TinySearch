package se.kth.krijor;
import se.kth.id1020.*; 

/**
 * <h1>Tiny Search App </h1>
 * 
 * A simple search engine, made for the course
 * Algorithms and Data structures (ID1020).
 * 
 * @author Kristian Alvarez Jörgensen
 * @version 2.0
 * @see TinySearchEngine
 *
 */
public class App {

    public static void main( String[] args ) throws Exception {
        System.out.println("Tiny Search:");
        TinySearchEngineBase searchEngine = new TinySearchEngine(); 
        Driver.run(searchEngine); 

    }
}
