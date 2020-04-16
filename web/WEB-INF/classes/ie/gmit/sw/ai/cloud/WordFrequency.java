package ie.gmit.sw.ai.cloud;

/**
 * Class that defends an object with word and its frequency.
 *
 * @author Cathal Butler.
 * Referance: John Healy - Lecture of the module in GMIT. Online tutorial videos and lecture content.
 * @implmentes Comparable to compare the frequency of the word with another.
 */

public class WordFrequency implements Comparable<WordFrequency> {

    private String word;
    private int frequency;
    private int fontSize = 0;

    public WordFrequency(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    public String getWord() {
        return this.word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(int size) {
        this.fontSize = size;
    }

    @Override
    public int compareTo(WordFrequency wordFrequency) {
        return wordFrequency.frequency - frequency;
    }

    public String toString() {
        return "Word: " + getWord() + "\tFreq: " + getFrequency() + "\tFont Size: " + getFontSize();
    }
}//End class