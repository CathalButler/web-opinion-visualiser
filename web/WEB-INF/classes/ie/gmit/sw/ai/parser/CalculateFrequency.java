package ie.gmit.sw.ai.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CalculateFrequency implements FrequencyInterface {
    // === M e m b e r V a r i a b l e s ============================
    private Map<String, Integer> wordsAndFreq = new ConcurrentHashMap<>();

    //Default Constructor
    public CalculateFrequency() {
    }

    @Override
    public void put(String word) {
        // If it contains key update the freq
        if (wordsAndFreq.containsKey(word)) {
            int freq = wordsAndFreq.get(word) + 1;
            wordsAndFreq.put(word, freq);
        } else {
            // Else set its freq to 1
            wordsAndFreq.put(word, 1);
        }//End if else
    }//End method

    @Override
    public Map<String, Integer> getMap() {
        //Return map of words and there frequency's
        return new HashMap<>(wordsAndFreq);
    }
}//End class
