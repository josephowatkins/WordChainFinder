package graph;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Graph {

    private List<String> wordList;
    private Map<String, List<String>> wordMap;

    private final String fileName = "map.ser";

    private Set<String> examinedWords;


    public Graph() {

        if (wordMap == null && new File(fileName).exists()){
            readMap();
        }
    }

    // BFS - examine all children first, then repeat.
    public List<String> findPath(String start, String end) {

        //check both words are the same length
        if (start.length() != end.length()) {
            throw new IllegalArgumentException("Words must be same length");
        }

        // check both words are in the map -
        if (!wordMap.containsKey(start) || !wordMap.containsKey(end)) {
            return null;
        }

        examinedWords = new HashSet<>();
        examinedWords.add(start);

        List<List<String>> queue = new LinkedList<>();
        List<String> path = new ArrayList<>();

        path.add(start);
        queue.add(path);

        while(queue.size() != 0) {
            List<String> currentPath = queue.remove(0);
            String currentWord = currentPath.get(currentPath.size() - 1);

            // get all the words associated with the current word
            List<String> currentList = wordMap.get(currentWord);
            for (String child : currentList) {

                if (isNewWord(child)) {

                    List<String> newPath = createNewPathWithChildAppended(currentPath, child);
                    examinedWords.add(child);

                    if (child.equals(end)) {
                        return newPath;
                    } else {
                        queue.add(newPath);
                    }
                }
            }
        }
        return null;
    }



    /**
     * Builds the graph from the input file and saves the result.
     * @param f
     */
    public void buildGraphFromFile(File f) {

        long startList = System.nanoTime();
        populateWordListFromFile(f);
        long listFinished = System.nanoTime();
        System.out.println("List finished - time elapsed: " + (listFinished - startList) / 1000000);

        long startMap = System.nanoTime();
        populateWordMap();
        long mapFinished = System.nanoTime();
        System.out.println("Map finished - time elapsed: " + (mapFinished - startMap) / 1000000);

        // save results;
        flush();
    }

    private void populateWordListFromFile(File f) {
        wordList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(f))){

            reader.lines().forEach(wordList::add);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateWordMap() {

        wordMap = new ConcurrentHashMap<>();
        ExecutorService executors = Executors.newCachedThreadPool();

        for (String s : wordList) {
            executors.execute(new MakeList(s));
        }
        executors.shutdown();
    }


    private boolean oneStep(String word1, String word2) {

        // check not same
        if (word1.equals(word2)) {
            return false;
        }
        // check length
        if (word1.length() != word2.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                diff++;
            }
        }
        return diff <= 1;
    }

    private boolean isNewWord(String s) {
        return !examinedWords.contains(s);
    }

    private List<String> createNewPathWithChildAppended(List<String> currentPath, String child) {
        List<String> newPath = new ArrayList<>();
        newPath.addAll(currentPath);
        newPath.add(child);
        return newPath;

    }

    private void flush() {

        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(wordMap);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMap() {

        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            wordMap = (Map<String, List<String>>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private class MakeList implements Runnable {

        private final String currentWord;

        public MakeList(String s) {
            this.currentWord = s;
        }

        @Override
        public void run() {

            // try to shrink the number of comparisons needed.
            List<String> neighbours = null;
            for (String s2 : wordList) {
                if (oneStep(currentWord, s2)) {
                    if (neighbours == null) {
                        neighbours = new ArrayList<>();
                    }
                    neighbours.add(s2);
                }
            }
            if (neighbours != null) {
                wordMap.put(currentWord, neighbours);
            }
        }
    }
}
