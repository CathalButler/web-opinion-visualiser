package ie.gmit.sw;


import ie.gmit.sw.ai.parser.NodeManager;
import ie.gmit.sw.ai.parser.ParserInterface;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * -------------------------------------------------------------------------------------------------------------------
 * PLEASE READ THE FOLLOWING CAREFULLY. MOST OF THE "ISSUES" STUDENTS HAVE WITH DEPLOYMENT ARISE FROM NOT READING
 * AND FOLLOWING THE INSTRUCTIONS BELOW.
 * -------------------------------------------------------------------------------------------------------------------
 *
 * To compile this servlet, open a command prompt in the web application directory and execute the following commands:
 *
 * Linux/Mac													Windows
 * ---------													---------
 * cd WEB-INF/classes/											cd WEB-INF\classes\
 * javac -cp .:$TOMCAT_HOME/lib/* ie/gmit/sw/*.java				javac -cp .:%TOMCAT_HOME%/lib/* ie/gmit/sw/*.java
 * cd ../../													cd ..\..\
 * jar -cf wcloud.war *											jar -cf wcloud.war *
 *
 * Drag and drop the file ngrams.war into the webapps directory of Tomcat to deploy the application. It will then be
 * accessible from http://localhost:8080. The ignore words file at res/ignorewords.txt will be located using the
 * IGNORE_WORDS_FILE_LOCATION mapping in web.xml. This works perfectly, so don't change it unless you know what
 * you are doing...
 *
 */

public class ServiceHandler extends HttpServlet {
    // === M e m b e r V a r i a b l e s ============================
    private String ignoreWords = null;
    private File f;
    private ParserInterface parser;

    public void init() throws ServletException {
        ServletContext ctx = getServletContext(); //Get a handle on the application context

        //Reads the value from the <context-param> in web.xml
        ignoreWords = getServletContext().getRealPath(File.separator) + ctx.getInitParameter("IGNORE_WORDS_FILE_LOCATION");
        f = new File(ignoreWords); //A file wrapper around the ignore words...

        // For now have it start once the application start
        try {
            new NodeManager("test", 100).go("https://jsoup.org/cookbook/input/parse-document-from-string");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }//End method

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html"); //Output the MIME type
        PrintWriter out = resp.getWriter(); //Write out text. We can write out binary too and change the MIME type...

        //Initialise some request varuables with the submitted form info. These are local to this method and thread safe...
        String option = req.getParameter("cmbOptions"); //Change options to whatever you think adds value to your assignment...
        String s = req.getParameter("query");

        out.print("<html><head><title>Artificial Intelligence Assignment</title>");
        out.print("<link rel=\"stylesheet\" href=\"includes/style.css\">");

        out.print("</head>");
        out.print("<body>");
        out.print("<div style=\"font-size:48pt; font-family:arial; color:#990000; font-weight:bold\">Web Opinion Visualiser</div>");

        out.print("<p><h2>Please read the following carefully</h2>");
        out.print("<p>The &quot;ignore words&quot; file is located at <font color=red><b>" + f.getAbsolutePath() + "</b></font> and is <b><u>" + f.length() + "</u></b> bytes in size.");
        out.print("You must place any additional files in the <b>res</b> directory and access them in the same way as the set of ignore words.");
        out.print("<p>Place any additional JAR archives in the WEB-INF/lib directory. This will result in Tomcat adding the library of classes ");
        out.print("to the CLASSPATH for the web application context. Please note that the JAR archives <b>jFuzzyLogic.jar</b>, <b>encog-core-3.4.jar</b> and ");
        out.print("<b>jsoup-1.12.1.jar</b> have already been added to the project.");

        out.print("<p><fieldset><legend><h3>Result</h3></legend>");


        out.print("</fieldset>");
        out.print("<P>Maybe output some search stats here, e.g. max search depth, effective branching factor.....<p>");
        out.print("<a href=\"./\">Return to Start Page</a>");
        out.print("</body>");
        out.print("</html>");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}//End class