package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.models.FuzzyData;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Cathal Butler
 */
public class FuzzyHeuristic {
    // === M e m b e r V a r i a b l e s =============================
    private final FIS fis;
    private static final Logger LOGGER = Logger.getLogger(FuzzyHeuristic.class.getName());
    final String dir = System.getProperty("user.dir"); //TODO - Need to test deployment on another platform due to path

    //Constructor
    public FuzzyHeuristic(String fclFile) {
//        LOGGER.info("System directory = " + dir);

//        LOGGER.info(fclFile);
//        File file = new File(dir + fclFile);
//        LOGGER.info(String.valueOf(file.exists()));
        this.fis = FIS.load(dir + fclFile, true);
    }

    /**
     * Method which returns an instance of this class
     *
     * @param fclFile - FCL file to be read
     * @return FuzzyHeuristic Instances
     */
    public static FuzzyHeuristic fuzzyHeuristic(String fclFile) {
        return new FuzzyHeuristic(fclFile);
    }

    public double process(FuzzyData fuzzyData) {
        // Function block
        FunctionBlock fb = fis.getFunctionBlock("wordcloud");

        // Set inputs
        fis.setVariable("title", fuzzyData.getTitle());
        fis.setVariable("headings", fuzzyData.getH1());
        fis.setVariable("body", fuzzyData.getPara());

        // Evaluate
        fis.evaluate();

        // Show output variable's chart
        Variable score = fb.getVariable("score");

        double result = score.getLatestDefuzzifiedValue();

        return result;
    }
}//End class
