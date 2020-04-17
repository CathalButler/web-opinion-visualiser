package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.cloud.WordFrequency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * This class handles looking after words are there frequency's and filtering
 * words against the ignore file.
 *
 * @author Cathal Butler
 */
public class WordService {
    private static final int MIN_WORD_LENGTH = 3;
    // === M e m b e r V a r i a b l e s =============================
    private static WordService wordService;
    private VoidParser voidParser;
    private static final Logger LOGGER = Logger.getLogger(WordService.class.getName());
    private ConcurrentHashMap<String, Long> wordMap = new ConcurrentHashMap<>();
    private List<WordFrequency> popularWords = new ArrayList<>();
    private List<WordFrequency> voidWords = new ArrayList<>();

    public WordService() {
    }

    public static WordService getInstance() {
        if (wordService == null) {
            synchronized (WordService.class) {
                if (wordService == null) wordService = new WordService();
            }
        }//End if
        return wordService;
    }//End getInstance method

    /**
     * Method to parse the void file (Words that should be ignored from the application)
     *
     * @param filePath
     */
    public void voidFile(String filePath) {
        this.voidParser = new VoidParser(filePath);
        voidParser.parse();
        voidWords = voidParser.wordList(); // Get ignored words from parser

    }

    public void print() {
//        LOGGER.info("\n== Size of void word list: " + voidWords.size() +
//                "\n== Size of Popular List: " + popularWords.size());
        popularWords.forEach((value) -> LOGGER.info("Word: " + value.getWord() + " " + value.getFrequency()));
//        LOGGER.info("List size -> " + voidWords.size());
        // wordMap.forEach((key, value) -> LOGGER.info(key + " " + value));
    }

    /**
     * This method updates the local list with new words and frequency's gathered from threads
     *
     * @param wordFreq
     */
    public void updateWordList(ConcurrentHashMap<String, Long> wordFreq) {
        wordMap.putAll(wordFreq); // Update the map with new words and frequency
    }

    /**
     * Method filters words and adds them to the popularWords list
     */
    public void filterWords() {
        for (ConcurrentMap.Entry<String, Long> word : wordMap.entrySet()) {
            WordFrequency wordFrequency = new WordFrequency(word.getKey(), word.getValue());

            if (!containsWord(voidWords, wordFrequency.getWord()) && wordFrequency.getWord().length() > MIN_WORD_LENGTH) {
                popularWords.add(wordFrequency);
            }
        }
    }

    /**
     * Method which checks a list of WordFrequency objects for one element
     * Ref: https://stackoverflow.com/questions/18852059/java-list-containsobject-with-field-value-equal-to-x
     *
     * @param list - WordFrequency Objects
     * @param word - element to query
     * @return boolean
     */
    public boolean containsWord(final List<WordFrequency> list, final String word) {
        return list.stream().anyMatch(o -> o.getWord().equals(word));
    }

    /**
     * Returns list of popular words that are used to create word cloud
     *
     * @return
     */
    public List<WordFrequency> getPopularWords() {
        return popularWords;
    }
}//End class
