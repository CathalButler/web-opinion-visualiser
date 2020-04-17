package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.cloud.WordFrequency;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class will parse the file of words that should be ignored
 * when create a Word Cloud
 *
 * @author Cathal Butler
 */

public class VoidParser implements ParserInterface {
    // === M e m b e r V a r i a b l e s ============================
    private List<WordFrequency> words = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(VoidParser.class.getName());
    private String file;

    // Default Constructor
    public VoidParser() {
    }

    public VoidParser(String file) {
        this.file = file;
    }

    @Override
    public List<WordFrequency> wordList() {
        return new ArrayList<>(words);
    }

    @Override
    public boolean parse() {
        LOGGER.info("Void Word parsing thread has started...");
        //Load File
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
            String line;

            // Read till EOF and add words to list with a frequency of 1
            while ((line = br.readLine()) != null) {
                words.add(new WordFrequency(line, (long) 1));
            }//Enf while loop
            br.close(); //Close file
            LOGGER.info("Void Word Parsing Complete");

            return true; //Complete
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            // e.printStackTrace();
            return false;
        }//End try catch
    }//End parse method
}//End class
