package graph;

import java.io.File;
import java.util.List;

public class Application {

    private final String WORD_LIST_FILE = "wordlist.txt";

    public static void main(String[] args) {
        Application app = new Application();
        app.launch();
    }

    private void launch() {


        Graph g = new Graph();

//        VERY SLOW - Around 2.5mins!
        File f = new File(
                this.getClass().getResource(WORD_LIST_FILE).getFile());
        g.buildGraphFromFile(f);

        long start = getNanoTime();
        List<String> path = g.findPath("read", "over");
        long end = getNanoTime();

        prettyPrintPath(path);
        printTimeTaken(start, end);
    }

    private void prettyPrintPath(List<String> path) {
        StringBuilder sb = new StringBuilder();

        if (path == null) {
            System.out.println("No path found!");
        } else {
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i));
                if (i < path.size() - 1) {
                    sb.append(" -> ");
                }
            }
            System.out.println(sb.toString());
        }
    }

    private long getNanoTime() {
        return System.nanoTime();
    }

    private void printTimeTaken(long start, long end) {
        System.out.println("Time taken: " + ((end - start) / 1000000) + "ms");
    }
}
