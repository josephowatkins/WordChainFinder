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

    private final static int NUMBER_OF_THREADS = 16;

    public Graph() {
        if (wordMap == null && new File(fileName).exists()){
            readMap();
        }
    }

    // BFS - examine all children first, then repeat.
    public Path findPath(String start, String end) {

        if (wordsAreNotTheSameLength(start, end)) {
            throw new IllegalArgumentException("Words must be same length");
        }

        if (wordNotInMap(start) || wordNotInMap(end)) {
            return Path.EMPTY_PATH;
        }

        examinedWords = new HashSet<>();
        examinedWords.add(start);

        List<Path> queue = new LinkedList<>();
        Path path = new Path();

        path.appendWordToPath(start);
        queue.add(path);

        while(queue.size() != 0) {
            Path currentPath = queue.remove(0);
            String currentWord = currentPath.getCurrentWord();

            // get all the words associated with the current word
            List<String> neighbours = wordMap.get(currentWord);

            for (String neighbour : neighbours) {

                if (isNewWord(neighbour)) {

                    Path newPath = new Path(currentPath);
                    newPath.appendWordToPath(neighbour);

                    examinedWords.add(neighbour);

                    if (neighbour.equals(end)) {
                        return newPath;
                    } else {
                        queue.add(newPath);
                    }
                }
            }
        }
        return Path.EMPTY_PATH;
    }

    /**
     * Builds the graph from the input file and saves the result.
     * @param file list of legal words.
     */
    public void buildGraphFromFile(File file) {

        long startList = getNanoTime();
        populateWordListFromFile(file);
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
        boolean result = true;

        if (wordsAreTheSame(word1, word2)) {
            result = false;
        } else if (wordsAreNotTheSameLength(word1, word2)) {
            result = false;
        } else if (differenceIsGreaterThanOneCharacter(word1, word2)) {
            result = false;
        }

        return result;
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

    @SuppressWarnings("unchecked")
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
