package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.models.DocumentNode;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
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
    private URL urlBase;
    private static final Logger LOGGER = Logger.getLogger(ServiceManager.class.getName());
    //ThreadPool for NodePaser Jobs
    private ExecutorService executorNodeService = Executors.newFixedThreadPool(THREAD_COUNT);
    //ThreadPool for DocumentParsing
    private ExecutorService executorDocumentService = Executors.newFixedThreadPool(THREAD_COUNT);
    // Array list of node jobs
    private List<Future<NodeParser>> nodeFutures = new ArrayList<>();
    // Array list of document jobs
    private List<Future<DocumentNode>> documentFutures = new ArrayList<>();
    // Relate words to frequencies
    private Map<String, Integer> map = new ConcurrentHashMap<>(); // TODO: Map this to WordFrequency Array -- All
    // User will be able to enter a term word when entering a url
    private String searchTerm; // Store search term to refer back to
    // Store nodes visited -- Concurrent version of TreeSet
    private Set<URL> visitedList = new HashSet<>();

    private Queue<DocumentNode> queue = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));


    // Constructor
    public ServiceManager(String searchTerm, int maxPages) {
        this.searchTerm = searchTerm; // Get starting term
        this.maxPages = maxPages;
    }

    /**
     * Method to start url parsing followed by document parsing
     */
    public void go(URL start) throws InterruptedException, ExecutionException {

        //TODO - Stay within the same url?
        //urlBase = start;

        // Start parsing urls using a threaded search
        submitNewURL(start, 0);
        while (checkNodes()) ;

        LOGGER.info("Found " + visitedList.size() + " urls");

        // Start parsing documents using a threaded service
        addDocumentParsingJobs();
        while (parserDocuments()) ;

        //checkQueue();

        LOGGER.info("Documents Parsed: " + queue.size());
    }//End go

    private void checkQueue() {
        for (DocumentNode documentNode : queue) {
            LOGGER.info(documentNode.toString());
        }
    }

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
        Set<NodeParser> pageSet = new HashSet<>();
        Iterator<Future<NodeParser>> iterator = nodeFutures.iterator(); // Grab list of futures waiting to be run

        // Loop though jobs (Nodes)
        while (iterator.hasNext()) {
            Future<NodeParser> future = iterator.next();

            // If node is complete, remove it and add to page set,
            // This will add all new urls from that job to this classes list of visted URL,
            // addNewURLs() will check to see if the site has been visited to stop
            // duplicates
            if (future.isDone()) {
                // PAGE JOBS
                iterator.remove();
                try {
                    pageSet.add(future.get());
                } catch (ExecutionException e) {
                    LOGGER.info(e.getMessage());
                }//End try catch
            }//End if

        }//End while loop

        // Look thought page set and add the new urls
        for (NodeParser nodeParser : pageSet) {
            addNewURLs(nodeParser);
        }
        return (nodeFutures.size() > 0);
    }//End checkNode method

    /**
     * Method which handles document parsing jobs
     *
     * @return boolean
     * @throws InterruptedException
     */
    private boolean parserDocuments() throws InterruptedException {
        Thread.sleep(PAUSE_TIME);
        Iterator<Future<DocumentNode>> documentIterator = documentFutures.iterator(); // Grab document list

        // Once the page paring complete, now I need a search url for term service to start
        // and loop though a document with jsoup, calculating the frequency and fuzzy business
        // Loop though jobs (Nodes)
        while (documentIterator.hasNext()) {
            Future<DocumentNode> docFuture = documentIterator.next();

            if (docFuture.isDone()) {
                LOGGER.info("Document Parsing Jobs left " + documentFutures.size());
                // DOCUMENT JOBS
                documentIterator.remove(); // Remove job as its starting
                //Run document job
                try {
                    queue.add(docFuture.get()); // Add parsed doc to queue
                } catch (ExecutionException | InterruptedException e) {
                    LOGGER.info(e.getMessage());
                }
            }
        }

        return (documentFutures.size() > 0);
    }


    /**
     * Method gets all urls NodeParser has collected
     * and submit it to new url, this will check that
     * it has not been visited before creating a new jobs
     *
     * @param nodeParser
     */
    private void addNewURLs(NodeParser nodeParser) {
        for (URL url : nodeParser.getUrlsOnThisPage()) {
            if (url.toString().contains("#")) {
                submitNewURL(url, nodeParser.getDepth() + 1);
            }
            submitNewURL(url, nodeParser.getDepth() + 1);
        }
    }//End method

    /**
     * Method which creates new document jobs
     */
    private void addDocumentParsingJobs() {
        for (URL url : visitedList) {
            LOGGER.info(url.toString());

            Future<DocumentNode> futureDocuments = executorDocumentService.submit(new DocumentParser(new DocumentNode(url)));
            documentFutures.add(futureDocuments);  //Add to queue to be processed
        }
    }//End method

    /**
     * Method which creates a new node and adds it to the execution queue.
     *
     * @param url
     * @param depth
     */
    private void submitNewURL(URL url, int depth) {
        if (shouldVisit(url, depth)) {
            visitedList.add(url); // Update the visited list as its going to be processed

            // Create new job and at to future
            // Job will run once thread is ready
            NodeParser nodeParser = new NodeParser(url, depth);
            Future<NodeParser> future = executorNodeService.submit(nodeParser);
            nodeFutures.add(future);  //Add to queue to be processed

//            LOGGER.info(String.valueOf(visitedList.size()));
        }//
    }//End method

    /**
     * Method which handles checking if a URL should be visited.
     * If the visited list has a url it will return false.
     * TODO - Add more checks & checks to {@link NodeParser}
     *
     * @param url
     * @param depth
     * @return
     */
    private boolean shouldVisit(URL url, int depth) {

        if (visitedList.contains(url)) {
            // LOGGER.info("visitedList.contains(url)");
            return false;
        }

        //TODO - Maybe look at keeping the search inside a single site
//        if (!url.toString().startsWith(urlBase.toString())) {
////            LOGGER.info("(!url.toString().startsWith(urlBase.toString())");
//            return false;
//        }

        if (url.toString().endsWith(".pdf")) {
//            LOGGER.info("(url.toString().endsWith(\".pdf\")");
            return false;
        }
        return visitedList.size() < maxPages;
    }//End method
}//End class
