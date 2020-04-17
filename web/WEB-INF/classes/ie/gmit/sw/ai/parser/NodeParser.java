package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.models.FuzzyData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This Node class handles parsing a webpage for urls
 * Class implements {@link Callable} interface
 * TODO - Implement depth
 *
 * @author Cathal Butler
 */

public class NodeParser implements Callable<NodeParser> {
    // == C O N S T A N T S ==========================================
    static final int TIMEOUT = 60000;   // one minute
    private static final int TITLE_WEIGHT = 50;
    private static final int H1_WEIGHT = 20;
    private static final int P_WEIGHT = 1;
    private static final int MAX = 10; // visit 100 pages, then stop
    // === M e m b e r V a r i a b l e s =============================
    private URL url;
    private String searchTerm;
    private Set<URL> nestedURls = new HashSet<>(); // URL that are found within a page
    private static final Logger LOGGER = Logger.getLogger(NodeParser.class.getName());
    private Queue<Document> queue = new PriorityQueue<>();
    private Set<String> closedList = new ConcurrentSkipListSet<>();
    private WordService wordService = WordService.getInstance();
    private double score = 5;
    private int titleScore = 0;
    private int headingScore = 0;
    private int bodyScore = 0;

    // Constructor
    public NodeParser(URL url, String searchTerm) {
        this.url = url;
        this.searchTerm = searchTerm;
    }

    @Override
    public NodeParser call() throws Exception {
        LOGGER.info("Thread " + Thread.currentThread().getName() + " Started");
        Document document;

        LOGGER.info("Visiting: " + url);
        document = Jsoup.connect(url.toString()).get();

        closedList.add(url.toString());
        queue.add(document);

        processLinksForEach();
        return this;
    }

    /**
     * Method process a URL passed to it using jsoup, all other URL on that web page are collected
     * and returned
     */
    private void processLinksForEach() {

        //Elements links = document.select("a[href]"); // Get hyperlink elements
        while (closedList.size() <= MAX && !queue.isEmpty()) {
            //String href = link.attr("href"); // just href
            //String newLink = link.absUrl("href"); // get absolute link
            //LOGGER.info(String.valueOf(docs.size()));
            Document doc = queue.poll();
            Elements element = doc.select("a[href]");

            addWordsToList(doc.body().text());
            element.forEach(elem -> {
                String newLink = elem.absUrl("href"); // get absolute link
                //LOGGER.info(newLink + " -> Nesteed left " + nestedURls.size());
                try {
                    if (newLink != null && !closedList.contains(newLink) && closedList.size() <= MAX) {
                        // Check for search term
                        if (newLink.contains(searchTerm)) {
                            LOGGER.info("======== HAS SEARCH TERM ============");
                            Document nestedDoc = Jsoup.connect(newLink).get();

                            closedList.add(newLink);

                            if (getHeuristicScore(nestedDoc) >= score) {
                                addWordsToList(nestedDoc.body().text());

                                LOGGER.info("CLOSED LIST SIZE " + closedList.size());

                                nestedURls.add(new URL(newLink));
                                queue.offer(nestedDoc);

                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.info("Couldn't Process: " + e.getMessage());
                    return;
                }
            });
        }//End foreach


        LOGGER.info("NESTED URL SIZE -> " + nestedURls.size());
    }//End method

    /**
     * Method which adds and there fre
     *
     * @param parserInput
     */
    private void addWordsToList(String parserInput) {

        // Collect all words and there frequency (how many there is in a given sentence)
        Stream<String> stream = Stream.of(parserInput.
                toLowerCase()
                .split("\\W+"))
                .parallel();

//            long uniqueWordCount = Stream.of(parserInput)
//                    .map(String::toLowerCase)
//                    .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
//                    .entrySet().stream()
//                    .filter(e -> e.getValue() == 1)
//                    .count();

//            wordService.updateWordList(uniqueWordCount);

        //Pass them onto word service
        wordService.updateWordList(stream.collect(
                Collectors.groupingBy(String::toString,
                        ConcurrentHashMap::new,
                        Collectors.counting())));
    }//End method


    /**
     * Method that calculates heuristic scores on Document elements.
     *
     * @param doc - Jsoup Document
     * @return Fuzzy score
     */
    private double getHeuristicScore(Document doc) {
        // Title
        String title = doc.title();
        //CALCULATIONS FOR HEURISTIC
        titleScore += (int) (getFrequency(title) * TITLE_WEIGHT); // Calculate heuristic

        // Heading
        Elements headings = doc.select("h1"); // Walk jsoup tree
        for (Element heading : headings) {
            String h1 = heading.text(); // Gives text inside heading
            LOGGER.info("\t" + h1);
            headingScore += getFrequency(h1) * H1_WEIGHT; // Calculate heuristic
        }

        String body = doc.body().text(); // Grab text from body
        bodyScore = (int) (getFrequency(body) * P_WEIGHT);  // Calculate heuristic

        LOGGER.info("\n=========================" +
                "\nScores before fuzzy" +
                "\nTitle Score:\t" + titleScore
                + "\nHeading Score:\t" + headingScore
                + "\nBody Score:\t" + bodyScore + "\n=========================");

        score = getFuzzyHeuristic(titleScore, headingScore, bodyScore);

        return score; // return score after computation
    }

    /**
     * This method handles get the fuzzy score
     *
     * @param title    - document element
     * @param headings - document element
     * @param body     - document element
     * @return fuzzy score
     */
    private double getFuzzyHeuristic(int title, int headings, int body) {
        FuzzyHeuristic fuzzyHeuristic = new FuzzyHeuristic("/webapps/wcloud/res/WordCloud.fcl");
        double score = fuzzyHeuristic.process(new FuzzyData(title, headings, body));
        LOGGER.info(
                "\n========================" +
                        "\nFuzzy Score:  " + score +
                        "\n========================");
        return score;
    }

    /**
     * This get the frequency of words in a string, add the words to a list and return an amount to the user
     * https://stackoverflow.com/questions/21771566/calculating-frequency-of-each-word-in-a-sentence-in-java
     * https://www.logicbig.com/how-to/code-snippets/jcode-java-8-streams-collectors-groupingby.html
     *
     * @param parserInput - Document string
     * @return
     */
    private long getFrequency(String parserInput) {


        // Collect all words and there frequency (how many there is in a given sentence)
        Stream<String> stream = Stream.of(parserInput.
                toLowerCase()
                .split("\\W+"))
                .parallel();

        // Convert to ConcurrentHashMap & Filter
        ConcurrentHashMap<String, Long> localWordFreq = stream.collect(
                Collectors.groupingBy(String::toString,
                        ConcurrentHashMap::new,
                        Collectors.counting()));


        // localWordFreq.values().stream().forEach(LOGGER::info);
        //Check for search term in this localWordFreq

        // If word is in list, return the amount of times
        if (localWordFreq.containsKey(searchTerm)) {

            LOGGER.info("Size of the map -> " + (localWordFreq.size()));
            LOGGER.info("Word frequency in map -> " + localWordFreq.get(searchTerm));

            return localWordFreq.get(searchTerm);
        }

        //TODO - Look at returning this to manager class

        // LOGGER.info(String.valueOf(localWordFreq.size()));
        return 0;
    }
}//End class
