package graph;

import java.util.ArrayList;
import java.util.List;

public class Path {

    private List<String> path = new ArrayList<>();

    public void appendWordToPath(String word) {
        this.path.add(word);
    }

    public void addPathToPath(Path path) {
        this.path.addAll(path.path);
    }

    public String getCurrentWord() {
        return path.get(path.size() - 1);
    }

    public void prettyPrint() {
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
