package ie.gmit.sw.ai.parser;

import ie.gmit.sw.ai.models.FuzzyData;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

import java.util.logging.Logger;

/**
 * This class handles reading the FLC and return the fuzzy score
 * based on the inputs against the rules in the FLC file.
 *
 * @author Cathal Butler
 */
public class FuzzyHeuristic {
    // === M e m b e r V a r i a b l e s =============================
    private final FIS fis;
    private static final Logger LOGGER = Logger.getLogger(FuzzyHeuristic.class.getName());
//    final String dir = System.getProperty("user.dir");

    //Constructor
    public FuzzyHeuristic(String fclFile) {
        this.fis = FIS.load(fclFile, true);
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

        return score.getLatestDefuzzifiedValue();
    }//End process method
}//End class
