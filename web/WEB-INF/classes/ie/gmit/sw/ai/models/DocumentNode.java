package ie.gmit.sw.ai.models;

import org.jsoup.nodes.Document;

import java.net.URL;

/**
 * This class is for creating Document Nodes, it stores the HTML document and
 * URL for the document
 * TODO - Look at this design again with the thread pool, feel I can improve data flow
 *
 * @author Cathal Butler
 */

public class DocumentNode {
    // === M e m b e r V a r i a b l e s =============================
    private URL urlLink;
    private Document document;
    private double score;

    public DocumentNode(URL urlLink) {
        this.urlLink = urlLink;
    }

    public DocumentNode(URL urlLink, Document document, double score) {
        this.urlLink = urlLink;
        this.document = document;
        this.score = score;
    }

    public URL getUrlLink() {
        return urlLink;
    }

    public Document getDocument() {
        return document;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "DocumentNode{" +
                "urlLink=" + urlLink +
                ", document=" + document +
                ", score=" + score +
                '}';
    }
}//End class