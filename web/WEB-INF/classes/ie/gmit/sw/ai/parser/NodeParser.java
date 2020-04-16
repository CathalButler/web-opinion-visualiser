package ie.gmit.sw.ai.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * This Node class handles parsing a webpage for urls
 * Class implements {@link Callable} interface
 * TODO - Implement depth
 *
 * @author Cathal Butler
 */

public class NodeParser implements Callable<NodeParser> {
    // === M e m b e r V a r i a b l e s =============================
    private URL url;
    private int depth;
    private Set<URL> urlsOnThisPage = new HashSet<>(); // All url on this page
    private static final Logger LOGGER = Logger.getLogger(NodeParser.class.getName());

    // Constructor
    public NodeParser(URL url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    @Override
    public NodeParser call() throws Exception {
        LOGGER.info("Thread " + Thread.currentThread().getName() + " Started");
        Document document = null;

        LOGGER.info("Visiting (" + depth + "): " + url);
        document = Jsoup.connect(url.toString()).get();

        processLinksForEach(document);
        return this;
    }

    /**
     * Method process a URL passed to it using jsoup, all other URL on that web page are collected
     * and returned
     *
     * @param document
     */
    private void processLinksForEach(Document document) {
        Elements links = document.select("a[href]"); // Get hyperlink elements

        links.forEach(link -> { // Loop though each one
            //String href = link.attr("href"); // just href
            String newLink = link.absUrl("href"); // absolute link

            try {
                if (newLink != null && !newLink.startsWith("#") && !urlsOnThisPage.contains(new URL(newLink))) {
                    try {
                        // Add new link to list
                        //   LOGGER.info(newLink);
                        URL nextUrl = new URL(newLink);
                        urlsOnThisPage.add(nextUrl);
                        //LOGGER.info(String.valueOf(nextUrl));
                    } catch (MalformedURLException e) {
                        LOGGER.info("Couldn't Process: " + e.getMessage());
                        return;
                    }
                }
            } catch (MalformedURLException e) {
                LOGGER.info("Couldn't Process: " + e.getMessage());
                return;
            }
        });
        LOGGER.info(String.valueOf(urlsOnThisPage.size()));
    }//End method

    public Set<URL> getUrlsOnThisPage() {
        return urlsOnThisPage;
    }

    public int getDepth() {
        return depth;
    }
}//End class
