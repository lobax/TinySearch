package se.kth.krijor;
import se.kth.id1020.*; 

/**
 * Hello world!
 *
 */
public class App {

    public static void main( String[] args ) throws Exception {
        System.out.println("Tiny Search:");
        TinySearchEngineBase searchEngine = new TinySearchEngine(); 
        Driver.run(searchEngine); 

    }
}
