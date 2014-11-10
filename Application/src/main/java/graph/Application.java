package graph;

import java.io.File;
import java.util.List;

public class Application {

    public static void main(String[] args) {
        Application app = new Application();
        app.launch();
    }

    private void launch() {
        File f = new File(this.getClass().getResource("wordlist.txt").getFile());
        Graph g = new Graph();

        // VERY SLOW - Around 2.5mins!
        //g.buildGraphFromFile(f);

        long start = System.nanoTime();
        List<String> path = g.findPath("read", "over");
        long end = System.nanoTime();

        prettyPrintPath(path);
        System.out.println("Time taken: " + ((end - start) / 1000000) + "ms");

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
}
