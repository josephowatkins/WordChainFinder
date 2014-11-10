package graph;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Graph {

    private List<String> wordList;
    private Map<String, List<String>> wordMap;

    private final String fileName = "map.ser";
    private Set<String> examinedWords;

    private final static int NUMBER_OF_THREADS = 8;

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
        if (wordNotInMap(start) || wordNotInMap(end)) {
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

        long startList = getNanoTime();
        populateWordListFromFile(f);
        long listFinished = getNanoTime();

        System.out.println("List finished - time elapsed: " + (listFinished - startList) / 1000000);

        long startMap = getNanoTime();
        populateWordMap();
        long mapFinished = getNanoTime();

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
        ExecutorService executors = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        for (String s : wordList) {
            executors.execute(new MakeList(s));
        }
        executors.shutdown();
        try {
            executors.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** could modify transformation rules to support word chains of different lengths */
    private boolean isTransformationLegal(String word1, String word2) {
        if (wordsAreTheSame(word1, word2)) {
            return false;
        } else if (wordsAreNotTheSameLength(word1, word2)) {
            return false;
        } else if (differenceIsGreaterThanOneCharacter(word1, word2)) {
            return false;
        } else {
           return true;
        }
    }

    private boolean differenceIsGreaterThanOneCharacter(String word1, String word2) {
        int delta = 0;

        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                delta++;
                // stop comparison if difference is greater than 1.
                if (delta > 1){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNewWord(String s) {
        return !examinedWords.contains(s);
    }

    private boolean wordsAreTheSame(String word1, String word2) {
        return word1.equals(word2);
    }

    private boolean wordsAreNotTheSameLength(String word1, String word2) {
        return word1.length() != word2.length();
    }

    private boolean wordNotInMap(String start) {
        return !wordMap.containsKey(start);
    }

    private long getNanoTime() {
        return System.nanoTime();
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
                if (isTransformationLegal(currentWord, s2)) {
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
