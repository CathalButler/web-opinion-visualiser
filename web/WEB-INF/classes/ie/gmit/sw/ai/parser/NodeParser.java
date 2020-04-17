package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.models.FuzzyData;
import ie.gmit.sw.ai.services.WordService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This Node class handles parsing a web pages.
 * Class implements {@link Callable} interface
 *
 * @author Cathal Butler
 */

public class NodeParser implements Callable<NodeParser> {
    // == C O N S T A N T S ==========================================
    private static final int TITLE_WEIGHT = 50;
    private static final int H1_WEIGHT = 20;
    private static final int P_WEIGHT = 1;
    private static final int MAX = 10; //Max amout of nested web pages a thread can parse
    private static final double THRESHOLD = 5;
    // === M e m b e r V a r i a b l e s =============================
    private static final Logger LOGGER = Logger.getLogger(NodeParser.class.getName());
    private Set<String> closedList = new ConcurrentSkipListSet<>();
    private WordService wordService = WordService.getInstance();
    private Queue<Document> queue = new PriorityQueue<>();
    private FuzzyHeuristic fuzzyHeuristic;
    private String searchTerm;
    private URL url;
    private double score = 5;
    private int titleScore = 0;
    private int headingScore = 0;
    private int bodyScore = 0;

    // Constructor
    public NodeParser(URL url, String searchTerm, FuzzyHeuristic fuzzyHeuristic) {
        this.url = url;
        this.searchTerm = searchTerm;
        this.fuzzyHeuristic = fuzzyHeuristic;
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
     * Method process parsing and scoring documents on this thread
     * If a nested URL contains search term continue...
     * If the new document scores is greater then the set threshold add the body of the document words to {@link WordService}
     * Update nestedURL for states
     * Add new document to queue
     */
    private void processLinksForEach() {

        while (closedList.size() <= MAX && !queue.isEmpty()) {
            Document doc = queue.poll(); // Grab a document
            Elements element = doc.select("a[href]");

            addWordsToList(doc.body().text());
            element.forEach(elem -> {
                String newLink = elem.absUrl("href"); // get absolute link

                try {
                    if (newLink != null && !closedList.contains(newLink) && closedList.size() <= MAX) {
                        // Check for search term
                        if (newLink.contains(searchTerm)) {
//                            LOGGER.info("======== HAS SEARCH TERM ============");
//                            LOGGER.info(newLink);
                            // Needed to add user agent to stop timeouts on some sites
                            Document nestedDoc = Jsoup.connect(newLink)
                                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                                    .ignoreHttpErrors(true)
                                    .timeout(0).get();

                            closedList.add(newLink);

                            // Check the heuristic score of the document
                            // if it is greater then or = to the set score
                            // process it
                            if (getHeuristicScore(nestedDoc) >= THRESHOLD) {
                                addWordsToList(nestedDoc.body().text());

                                LOGGER.info("CLOSED LIST SIZE " + closedList.size());
                                queue.offer(nestedDoc);
                            }
                            LOGGER.info("Not within threshold boundary");
                        }
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                    LOGGER.info("Couldn't Process: " + e.getMessage());
//                    return;
                }
            });
        }//End foreach
    }//End method

    /**
     * Method which adds to the {@link WordService} class.
     * Words are abstracted from the document.body.text()
     *
     * @param parserInput - document element
     */
    private void addWordsToList(String parserInput) {

        Pattern pattern = Pattern.compile("[a-z]");

        // Collect all words and count them
        // filter, only [a-z]
        Stream<String> stream = Stream.of(parserInput.
                toLowerCase()
                .split("\\W+"))
                .filter(pattern.asPredicate())
                .parallel();

        //Pass them onto word service
        //Staying safe with ConcurrentHashMap
        wordService.updateWordList(stream.collect(
                Collectors.groupingBy(String::toString,
                        ConcurrentHashMap::new,
                        Collectors.counting())));
    }//End addWordsToList method


    /**
     * Method that calculates heuristic scores on elements in a Document.
     *
     * @param doc - Jsoup Document
     * @return Fuzzy score
     */
    private double getHeuristicScore(Document doc) {
        // Title
        String title = doc.title();
        titleScore += (int) (getFrequency(title) * TITLE_WEIGHT);

        // Heading
        Elements headings = doc.select("h1, h2, h3, h4");
        for (Element heading : headings) {
            String h1 = heading.text();
            LOGGER.info("\t" + h1);
            headingScore += getFrequency(h1) * H1_WEIGHT;
        }

        //Body
        String body = doc.body().text();
        bodyScore = (int) (getFrequency(body) * P_WEIGHT);

        // Output states before fuzzy business
        LOGGER.info("\n=========================" +
                "\nScores before fuzzy" +
                "\nTitle Score:\t" + titleScore
                + "\nHeading Score:\t" + headingScore
                + "\nBody Score:\t" + bodyScore + "\n=========================");

        // Get the FuzzyHeuristic Score
        score = getFuzzyHeuristic(titleScore, headingScore, bodyScore);

        return score; // return score after computation
    }//End getHeuristicScore method

    /**
     * This method handles get the fuzzy score
     *
     * @param title    - document element
     * @param headings - document element
     * @param body     - document element
     * @return fuzzy score
     */
    private double getFuzzyHeuristic(int title, int headings, int body) {
//        FuzzyHeuristic fuzzyHeuristic = new FuzzyHeuristic("/webapps/wcloud/res/WordCloud.fcl");
        double score = fuzzyHeuristic.process(new FuzzyData(title, headings, body));
        LOGGER.info(
                "\n========================" +
                        "\nFuzzy Score:  " + score +
                        "\n========================");
        return score;
    }//End getFuzzyHeuristic method

    /**
     * This get the frequency of words in a string, add the words to a list and return an amount to the user
     * https://stackoverflow.com/questions/21771566/calculating-frequency-of-each-word-in-a-sentence-in-java
     * https://www.logicbig.com/how-to/code-snippets/jcode-java-8-streams-collectors-groupingby.html
     * https://www.geeksforgeeks.org/stream-in-java/
     *
     * @param parserInput - Document string
     * @return the frequency of the search term in the input
     */
    private long getFrequency(String parserInput) {
        // Lower string case, chop string into words
        Stream<String> stream = Stream.of(parserInput.
                toLowerCase()
                .split("\\W+"))
                .parallel();

        // Convert to ConcurrentHashMap & count words
        ConcurrentHashMap<String, Long> localWordFreq = stream.collect(
                Collectors.groupingBy(String::toString,
                        ConcurrentHashMap::new,
                        Collectors.counting()));


        // localWordFreq.values().stream().forEach(LOGGER::info);
        //Check for search term in this localWordFreq
        // If word is in list, return the amount of times
        if (localWordFreq.containsKey(searchTerm)) {
            return localWordFreq.get(searchTerm);
        }
        return 0;
    }//End getInstance method
}//End class
