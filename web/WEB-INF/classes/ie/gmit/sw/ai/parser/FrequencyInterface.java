package ie.gmit.sw.ai.parser;

import java.util.Map;

public interface FrequencyInterface {

    void put(String word);

    Map<String, Integer> getMap();
}//End interface class
