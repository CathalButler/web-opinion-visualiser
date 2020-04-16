package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.cloud.WordFrequency;

import java.util.List;

/**
 * Interface that will be implemented by parsers
 *
 * @author Cathal Butler.
 */

public interface ParserInterface {

    List<WordFrequency> wordList();

    boolean parse() throws Exception;
}//End interface
