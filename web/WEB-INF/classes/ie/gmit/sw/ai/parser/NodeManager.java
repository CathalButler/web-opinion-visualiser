package ie.gmit.sw.ai.parser;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Best First Search - Sorted Queue -> Priority Queue
 */
public class NodeManager {
    // == C O N S T A N T S ==========================================
    public static final int THREAD_COUNT = 5;
    private static final long PAUSE_TIME = 1000;
    // === M e m b e r V a r i a b l e s =============================
    private static final int TITLE_WEIGHT = 50; // Lots of weight -- Found in title
    private static final int H1_WEIGHT = 20; // Significant weight -- Found in H1
    private static final int P_WEIGHT = 1; // Minimal Weight -- Found in body
    private int maxPages; // visit 100 pages, then stop
    private String urlBase;
    private static final Logger LOGGER = Logger.getLogger(NodeManager.class.getName());
    //ThreadPool
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    // Relate words to frequencies
    private Map<String, Integer> map = new ConcurrentHashMap<>(); // TODO: Map this to WordFrequency Array -- All
    private List<Future<Node>> futures = new ArrayList<>();
    // User will be able to enter a term word when entering a url
    private String term; // Store search term to refer back to
    // Store nodes visited -- Concurrent version of TreeSet
    private Set<String> visitedList = new ConcurrentSkipListSet<>();

    // Sorting by Heristic value
    // and also
    // Sorting based on score
    // :: => Method reference
    // TODO: can change this to a ConcurrentLinkList -- Can then push to front (DFS)
   // private Queue<DocumentNode> queue = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));

    // Constructor
    public NodeManager(String searchTerm, int maxPages) {
        this.term = searchTerm; // Get starting term
        this.maxPages = maxPages;
    }

    /**
     * Method which starts threads once a url is passed
     */
    public void go(String start) throws IOException, InterruptedException {

        urlBase = start;

        submitNewURL(start, 0);

        while (checkNodes()) ;

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
        Set<Node> pageSet = new HashSet<>();
        Iterator<Future<Node>> iterator = futures.iterator();

        while (iterator.hasNext()) {
            Future<Node> future = iterator.next();
            if (future.isDone()) {
                iterator.remove();
                try {
                    pageSet.add(future.get());
                } catch (InterruptedException e) {  // skip pages that load too slow
                } catch (ExecutionException e) {
                }
            }
        }

        for (Node node : pageSet) {
            addNewURLs(node);
        }
        return (futures.size() > 0);
    }//End checkNode method

    private void addNewURLs(Node node) {
        for (String url : node.getUrlList()) {
            if (url.contains("#")) {
                submitNewURL(url, node.getDepth() + 1);
            }
        }
    }

    private void submitNewURL(String url, int depth) {
        LOGGER.info(url);
        if (shouldVisit(url, depth)) {
            visitedList.add(url);

            Node node = new Node(url, depth);
            Future<Node> future = executorService.submit(node);
            futures.add(future);
        }
    }

    private boolean shouldVisit(String url, int depth) {
        if (visitedList.contains(url)) {
            return false;
        }
        if (!url.startsWith(urlBase)) {
            return false;
        }
        if (url.endsWith(".pdf")) {
            return false;
        }
        return visitedList.size() < maxPages;
    }//End method
}//End class
