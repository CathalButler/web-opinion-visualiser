package ie.gmit.sw.ai.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import static org.jsoup.internal.StringUtil.isBlank;

public class Node implements Callable<Node> {
    // == C O N S T A N T S ==========================================
    static final int TIMEOUT = 60000;   // one minute
    // === M e m b e r V a r i a b l e s =============================
    private String url;
    private int depth;
    private Set<String> urlList = new HashSet<>();
    private static final Logger LOGGER = Logger.getLogger(Node.class.getName());

    // Constructor
    public Node(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    @Override
    public Node call() throws Exception {
        Document document = null;

        LOGGER.info("Visiting (" + depth + "): " + url);

        document = Jsoup.connect(url).get();

        processLinks(document.select("a[href]"));
        return this;
    }

    private void processLinks(Elements links) {
        // Loop over links
        for (Element link : links) {
            String href = link.absUrl("href");
            LOGGER.info(href);
            if (isBlank(href) || href.startsWith("#")) {
                continue;
            }
            urlList.add(url);
        }//End for loop
        LOGGER.info(String.valueOf(urlList.size()));
    }//End method

    public Set<String> getUrlList() {
        return urlList;
    }

    public int getDepth() {
        return depth;
    }
}//End class
