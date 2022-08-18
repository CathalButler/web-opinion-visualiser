package ie.gmit.sw.ai.services;

import ie.gmit.sw.ai.cloud.LogarithmicSpiralPlacer;
import ie.gmit.sw.ai.cloud.WeightedFont;
import ie.gmit.sw.ai.cloud.WordFrequency;
import ie.gmit.sw.ai.parser.FuzzyHeuristic;
import ie.gmit.sw.ai.parser.NodeParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This class manages search threads
 *
 * @author Cathal Butler
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
    private WordFrequency[] wordFrequencies;
    private FuzzyHeuristic fuzzyHeuristic;
    private int cloudSize;


    // Constructor
    public ServiceManager(String searchTerm, int maxPages, String fuzzyFile, int cloudSize) {
        this.searchTerm = searchTerm; // Get starting term
        this.maxPages = maxPages;
        this.fuzzyHeuristic = new FuzzyHeuristic(fuzzyFile);
        this.cloudSize = cloudSize;
    }

    /**
     * Method to startUrl url parsing followed by document parsing
     */
    public void go(URL startUrl, String voidFile) throws InterruptedException {

        // Start parsing urls using a threaded search
        createJobs(startUrl);
        while (checkNodes()) ;

        //Shutdown threads after checknodes has exited
        executorNodeService.shutdown();
        executorNodeService.awaitTermination(20, TimeUnit.SECONDS);

        // Read ignore word file
        wordService.voidFile(voidFile);
        // Filter words, make sure no ignore words are present
        wordService.filterWords();
//        wordService.print(); // For testing
//        LOGGER.info("Found " + visitedList.size() + " urls");
    }//End go


    /**
     * Method which handles the status of the thread pools
     * 1. Sleep for a little to stop manager thread using all the resources
     * 2. Loop thought futures (Future objects are how we can check the status of a thread and get the results of the operation)
     * 3. WHen a future is done, remove from the polling list
     * 4. For now if an execution exception is thrown then the task is just removed
     * 5. Go through the competed node list looking for more URL to process
     * 6. return true if there are still jobs to run
     *
     * @return boolean
     */
    private boolean checkNodes() throws InterruptedException {

        Thread.sleep(PAUSE_TIME);

        // Loop though jobs (Nodes)
        // Grab list of futures waiting to be run
        // If node is complete, remove it
        nodeFutures.removeIf(Future::isDone);

        return (nodeFutures.size() > 0);
    }//End checkNode method


    /**
     * Method which creates a new search node and adds it to the execution queue.
     * This method is called first, it searches the search term on duckduckgo and
     * grabs the links found. The URLs are looped though creating thread jobs
     * that {@link NodeParser} looks after.
     *
     * @param url - url to connect to
     */
    private void createJobs(URL url) {
        //Variables
        Document document;

        try {
            document = Jsoup.connect(url.toString())
                    .ignoreHttpErrors(true)
                    .timeout(0).get();
            //Grab link from web search
            Elements elements = document.getElementById("links").getElementsByClass("results_links");

            //Create jobs from links
            elements.forEach(element -> {
                Element links = element.getElementsByClass("links_main").first().getElementsByTag("a").first();
                URL jobUrl;


                try {
                    jobUrl = new URL(links.attr("href"));
                } catch (MalformedURLException e) {
                    LOGGER.info(e.getMessage());
                    //e.printStackTrace();
                    return;
                }

                if (shouldVisit(jobUrl)) {
                    visitedList.add(jobUrl); // Update the visited list as its going to be processed
                    // Create new job and at to future
                    // Job will run once thread is ready
                    NodeParser nodeParser = new NodeParser(jobUrl, searchTerm, fuzzyHeuristic);
                    Future<NodeParser> future = executorNodeService.submit(nodeParser);
                    nodeFutures.add(future);  //Add to queue to be processed
//            LOGGER.info(String.valueOf(visitedList.size()));
                }//end if
            });
        } catch (IOException e) {
//            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }

    }//End method


    /**
     * Method which handles checking if a URL should be visited.
     * If the visited list has an url it will return false.
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

    /**
     * Method which creates the word cloud
     *
     * @return word cloud image
     */
    public BufferedImage createCloud() {
        wordFrequencies = new WordFrequency[cloudSize];
        //Variables
        List<WordFrequency> popularWords = wordService.getPopularWords(); //Set popular words
        // Sort the list of popular words
        Collections.sort(popularWords);

        //Add the top 32 to an array list
        for (int i = 0; i < cloudSize; i++) {
            wordFrequencies[i] = popularWords.get(i);
        }

        // Calculate the weighted font
        WordFrequency[] words = new WeightedFont().getFontSizes(wordFrequencies);
        // Sort based on frequency
        Arrays.sort(words, Comparator.comparing(WordFrequency::getFrequency, Comparator.reverseOrder()));

        // Spira Mirabilis
        LogarithmicSpiralPlacer logarithmicSpiralPlacer = new LogarithmicSpiralPlacer(800, 600);

        for (WordFrequency word : words) {
            // Place each word on the canvas starting with the largest
            logarithmicSpiralPlacer.place(word);
        }

        popularWords.clear();
        wordService.clear();

        return logarithmicSpiralPlacer.getImage();
    }//End method

    public Set<URL> getVisitedList() {
        return visitedList;
    }
}//End class
