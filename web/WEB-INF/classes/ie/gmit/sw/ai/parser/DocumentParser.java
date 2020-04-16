package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.cloud.WordFrequency;
import ie.gmit.sw.ai.models.DocumentNode;
import ie.gmit.sw.ai.models.FuzzyData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class handles parsing documents, it connects to a URL
 * and parsing elements that are scored and returned.
 * Implements {@link Callable}
 */

public class DocumentParser implements Callable<DocumentNode> {
    // == C O N S T A N T S ==========================================
    static final int TIMEOUT = 60000;   // one minute
    private static final int TITLE_WEIGHT = 50;
    private static final int H1_WEIGHT = 20;
    private static final int P_WEIGHT = 1;
    // === M e m b e r V a r i a b l e s =============================
    private static final Logger LOGGER = Logger.getLogger(DocumentParser.class.getName());
    private URL url;
    private String searchTerm;
    private DocumentNode documentNode;

    public DocumentParser(DocumentNode documentNode) {
        this.documentNode = documentNode;
    }

    @Override
    public DocumentNode call() {
        LOGGER.info("Thread " + Thread.currentThread().getName() + " Started");
        return processDocument();
    }//End call method


    private DocumentNode processDocument() {
        //Variables
        Document document;
        try {
            document = Jsoup.connect(documentNode.getUrlLink().toString()).get();
            LOGGER.info(documentNode.getUrlLink().toString());
            double score = getHeuristicScore(document); // score it

            return new DocumentNode(documentNode.getUrlLink(), document, score);
        } catch (IOException e) {
            LOGGER.info("Couldn't Process: " + e.getMessage());
            return null;
        }
    }//ENd method


    private double getHeuristicScore(Document doc) {
        //Variables
        double score = 0;
        int titleScore = 0;
        int headingScore = 0;

        // Title
        String title = doc.title();
        //CALCULATIONS FOR HEURISTIC
        titleScore = +getFrequency(title) * TITLE_WEIGHT; // Calculate heuristic

        LOGGER.info(title + "-> Score: " + titleScore);

        // Heading
        Elements headings = doc.select("h1"); // Walk jsoup tree


        for (Element heading : headings) {
            String h1 = heading.text(); // Gives text inside heading
            LOGGER.info("\t" + h1);
            headingScore += getFrequency(h1) * H1_WEIGHT; // Calculate heuristic
        }

        LOGGER.info(String.valueOf(headingScore));
        String body = doc.body().text(); // TODO: check for nul

        body += getFrequency(body) * P_WEIGHT; // Calculate heuristic
        int bodyScore = getFrequency(body) * P_WEIGHT;

        score = getFuzzyHeuristic(titleScore, headingScore, bodyScore); // TODO: include bodyScore -- fix first (will bomb out)
        return score; // return score after computation
    }


    private double getFuzzyHeuristic(int title, int headings, int body) {
        FuzzyHeuristic fuzzyHeuristic = new FuzzyHeuristic("/webapps/wcloud/res/WordCloud.fcl");

        double score = fuzzyHeuristic.process(new FuzzyData(title, headings, body));

        LOGGER.info("Score == " + score);
        return score;
    }

    /**
     * Check string of search term that was passed by user at run time
     *
     * @param s
     * @return
     */
    private int getFrequency(String s) {
        Stream<String> stream = Stream.of(s.toLowerCase().split("\\W+")).parallel();

        Map<String, Long> wordFreq = stream
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()));

        return wordFreq.size();
    }

    /**
     * List of frequent words, currently in this method
     * TODO - Look at may parsing from a file, user may be able to define there own
     *
     * @return wf
     */
    private WordFrequency[] getWordFrequencyKeyValue() {
        WordFrequency[] wf = new WordFrequency[32];

        wf[0] = new WordFrequency("Galway", 65476);
        wf[1] = new WordFrequency("Sligo", 43242);
        wf[2] = new WordFrequency("Roscommon", 2357);
        wf[4] = new WordFrequency("Clare", 997);
        wf[5] = new WordFrequency("Donegal", 876);
        wf[17] = new WordFrequency("Armagh", 75);
        wf[6] = new WordFrequency("Waterford", 811);
        wf[7] = new WordFrequency("Tipperary", 765);
        wf[8] = new WordFrequency("Westmeath", 643);
        wf[9] = new WordFrequency("Leitrim", 543);
        wf[10] = new WordFrequency("Mayo", 456);
        wf[11] = new WordFrequency("Offaly", 321);
        wf[12] = new WordFrequency("Kerry", 221);
        wf[13] = new WordFrequency("Meath", 101);
        wf[14] = new WordFrequency("Wicklow", 98);
        wf[18] = new WordFrequency("Antrim", 67);
        wf[3] = new WordFrequency("Limerick", 1099);
        wf[15] = new WordFrequency("Kildare", 89);
        wf[16] = new WordFrequency("Fermanagh", 81);
        wf[19] = new WordFrequency("Dublin", 12);
        wf[20] = new WordFrequency("Carlow", 342);
        wf[21] = new WordFrequency("Cavan", 234);
        wf[22] = new WordFrequency("Down", 65);
        wf[23] = new WordFrequency("Kilkenny", 45);
        wf[24] = new WordFrequency("Laois", 345);
        wf[25] = new WordFrequency("Derry", 7);
        wf[26] = new WordFrequency("Longford", 8);
        wf[27] = new WordFrequency("Louth", 34);
        wf[28] = new WordFrequency("Monaghan", 101);
        wf[29] = new WordFrequency("Tyrone", 121);
        wf[30] = new WordFrequency("Wexford", 144);
        wf[31] = new WordFrequency("Cork", 522);
        return wf;
    }

}
