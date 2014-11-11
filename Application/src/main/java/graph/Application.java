package graph;

import java.io.File;

public class Application {

    private static final String WORD_LIST_FILE = "wordlist.txt";

    public static void main(String[] args) {
        Application app = new Application();
        app.launch();
    }

    private void launch() {

        Graph g = new Graph();

//      VERY SLOW - Around 2.5mins!
//        File f = new File(
//                this.getClass().getResource(WORD_LIST_FILE).getFile());
//        g.buildGraphFromFile(f);

        long start = getNanoTime();
        Path path = g.findPath("code", "band");
        long end = getNanoTime();

        path.prettyPrint();
        printTimeTaken(start, end);
    }

    private long getNanoTime() {
        return System.nanoTime();
    }

    private void printTimeTaken(long start, long end) {
        System.out.println("Time taken: " + ((end - start) / 1000000) + "ms");
    }
}
