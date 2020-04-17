package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.cloud.LogarithmicSpiralPlacer;
import ie.gmit.sw.ai.cloud.WeightedFont;
import ie.gmit.sw.ai.cloud.WordFrequency;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * This class manages threaded nodes
 */
public class ServiceManager {
    // == C O N S T A N T S ==========================================
    public static final int THREAD_COUNT = 5;
    private static final long PAUSE_TIME = 1000;
    // === M e m b e r V a r i a b l e s =============================
    private int maxPages; // visit 100 pages, then stop
    private static final Logger LOGGER = Logger.getLogger(ServiceManager.class.getName());
    //ThreadPool for NodePaser Jobs
    private ExecutorService executorNodeService = Executors.newFixedThreadPool(THREAD_COUNT);
    // Array list of node jobs
    private List<Future<NodeParser>> nodeFutures = new ArrayList<>();
    // User will be able to enter a term word when entering a url
    private String searchTerm; // Store search term to refer back to
    // Store nodes visited -- Concurrent version of TreeSet
    private Set<URL> visitedList = new HashSet<>();
    private WordService wordService = WordService.getInstance();
    private WordFrequency[] wordFrequencies = new WordFrequency[32];
    private List<WordFrequency> popularWords = new ArrayList<>();


    // Constructor
    public ServiceManager(String searchTerm, int maxPages) {
        this.searchTerm = searchTerm; // Get starting term
        this.maxPages = maxPages;
    }

    /**
     * Method to startUrl url parsing followed by document parsing
     */
    public void go(URL startUrl, String voidFile) throws InterruptedException, IOException {

        // Start parsing urls using a threaded search
        createJobs(startUrl);
        while (checkNodes()) ;

        wordService.voidFile(voidFile);
        wordService.filterWords();
        wordService.print();

        LOGGER.info("Found " + visitedList.size() + " urls");

    }//End go


    /**
     * Method which handles the status of the thread pools
     * 1. Sleep for a little to stop manager thread using all the resources
     * 2. Loop thought futures (Future objects are how we can check the status of a thread and get the results of the operation)
     * 3. WHen a future is done, remove from the polling list
     * 4. For now if an execution exception is thrown then the task is just removed
     * 5. Go through the competed node list looking for more URL to process
     * 6. return tre if there are still process to run
     *
     * @return boolean
     */
    private boolean checkNodes() throws InterruptedException {

        Thread.sleep(PAUSE_TIME);

        // Loop though jobs (Nodes)
        // Grab list of futures waiting to be run
        // If node is complete, remove it and add to page set,
        // This will add all new urls from that job to this classes list of visted URL,
        // addNewURLs() will check to see if the site has been visited to stop
        nodeFutures.removeIf(Future::isDone);

        return (nodeFutures.size() > 0);
    }//End checkNode method


    /**
     * Method which creates a new node and adds it to the execution queue.
     *
     * @param url - url to connect to
     */
    private void createJobs(URL url) throws IOException {
        //Variables
        Document document;
        document = Jsoup.connect(url.toString()).get();
        Elements elements = document.getElementById("links").getElementsByClass("results_links");

        elements.forEach(element -> {
            Element links = element.getElementsByClass("links_main").first().getElementsByTag("a").first();
            LOGGER.info("\nURLs: " + links.attr("href"));

            URL jobUrl = null;
            try {
                jobUrl = new URL(links.attr("href"));
            } catch (MalformedURLException e) {
                LOGGER.info(e.getMessage());
                //e.printStackTrace();
                return;
            }

            if (shouldVisit(jobUrl)) {
                visitedList.add(url); // Update the visited list as its going to be processed

                // Create new job and at to future
                // Job will run once thread is ready
                NodeParser nodeParser = new NodeParser(jobUrl, searchTerm);
                Future<NodeParser> future = executorNodeService.submit(nodeParser);
                nodeFutures.add(future);  //Add to queue to be processed
//            LOGGER.info(String.valueOf(visitedList.size()));
            }//end if
        });
    }//End method

    /**
     * Method which handles checking if a URL should be visited.
     * If the visited list has a url it will return false.
     *
     * @param url
     * @return boolean - if job should start on URL provided
     */
    private boolean shouldVisit(URL url) {
        //Check if it has already been visited
        if (visitedList.contains(url)) {
            // LOGGER.info("visitedList.contains(url)");
            return false;
        }
        //Check if URL contains extensions that aren't valid for our needs
        if (url.toString().endsWith(".pdf") || url.toString().endsWith("mailto")) {
            return false;
        }
        return visitedList.size() < maxPages;
    }//End method

    public BufferedImage createCloud() {
        popularWords = wordService.getPopularWords();
        // Sort the list of popular words
        Collections.sort(popularWords);


        for (int i = 0; i < 32; i++) {
            LOGGER.info(popularWords.get(i).toString());
            wordFrequencies[i] = popularWords.get(i);
        }

        WordFrequency[] words = new WeightedFont().getFontSizes(wordFrequencies);
        Arrays.sort(words, Comparator.comparing(WordFrequency::getFrequency, Comparator.reverseOrder()));



        // Spira Mirabilis
        LogarithmicSpiralPlacer logarithmicSpiralPlacer = new LogarithmicSpiralPlacer(800, 600);

        for (WordFrequency word : words) {
            // Place each word on the canvas starting with the largest
            logarithmicSpiralPlacer.place(word);
        }

        return logarithmicSpiralPlacer.getImage();
    }//End method

}//End class
