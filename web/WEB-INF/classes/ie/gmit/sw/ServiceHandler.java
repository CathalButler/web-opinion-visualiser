package ie.gmit.sw;


import ie.gmit.sw.ai.services.ServiceManager;
import ie.gmit.sw.ai.services.WordService;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Base64;

/*
 * Service handler class which handles request made on the ngram application when served.
 */

public class ServiceHandler extends HttpServlet {
    // === M e m b e r V a r i a b l e s ============================
    private String ignoreWords = null;
    private String fuzzyFile = null;
    private File ignoreFile;
    private File fuzzy;
    private BufferedImage cloud;
    private int cloudSize;

    /**
     * Method which is used to init() the servlet
     */
    public void init() throws ServletException {
        ServletContext ctx = getServletContext(); //Get a handle on the application context
        //Reads the value from the <context-param> in web.xml
        ignoreWords = getServletContext().getRealPath(File.separator) + ctx.getInitParameter("IGNORE_WORDS_FILE_LOCATION");
        fuzzyFile = getServletContext().getRealPath(File.separator) + ctx.getInitParameter("FUZZY_FILE");
        ignoreFile = new File(ignoreWords); //A file wrapper around the ignore words...
        fuzzy = new File(fuzzyFile);
    }//End method

    /**
     * Method for GET Requests
     *
     * @param req  HTTP Request
     * @param resp HTTP Response
     * @throws IOException
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html"); //Output the MIME type
        PrintWriter out = resp.getWriter(); //Write out text. We can write out binary too and change the MIME type...

        //Initialise some request varuables with the submitted form info. These are local to this method and thread safe...
        String option = req.getParameter("sizeOptions"); //Change options to whatever you think adds value to your assignment...
        String searchTerm = req.getParameter("query");

        switch (option) {
            case "32":
                cloudSize = 32;
                break;
            case "64":
                cloudSize = 64;
                break;
            case "128":
                cloudSize = 128;
                break;
            case "256":
                cloudSize = 256;
                break;

        }

        // Create new instance of ServiceManager, this handles the threaded search
        ServiceManager serviceManager = new ServiceManager(searchTerm, 40, fuzzyFile, cloudSize);

        // For now have it start once the application start
        try {
            // Start search, passing it the search engine and inputted search term.
            serviceManager.go(new URL("https://duckduckgo.com/html?q=" + searchTerm), ignoreFile.toString());

            //Create cloud
            cloud = serviceManager.createCloud();

        } catch (IOException | InterruptedException e) {
            //LOGGER.info((Supplier<String>) e);
            e.printStackTrace();
        }

        out.print("<html><head><title>Artificial Intelligence Assignment</title>");
        out.print("<link rel=\"stylesheet\" href=\"includes/style.css\">");

        out.print("</head>");
        out.print("<body>");
        out.print("<div style=\"font-size:48pt; font-family:arial; color:#990000; font-weight:bold\">Web Opinion Visualiser</div>");

        out.print("<p><h2>Files</h2>");
        out.print("<p>The &quot;ignore words&quot; file is located at <font color=red><b>" + ignoreFile.getAbsolutePath() + "</b></font> and is <b><u>" + ignoreFile.length() + "</u></b> bytes in size.");
        out.print("<p>The &quot;fuzzy fcl&quot; file is located at <font color=red><b>" + fuzzy.getAbsolutePath() + "</b></font> and is <b><u>" + fuzzy.length() + "</u></b> bytes in size.");

        out.print("<p><fieldset><legend><h3>Result</h3></legend>");

        out.print("<img src=\"data:image/png;base64," + encodeToString(cloud) + "\" alt=\"Word Cloud\">");


        out.print("</fieldset>");
        out.print("<p><h2>Results</h2>");
        out.print("<p>" + serviceManager.getVisitedList().size() + " URLs parsed across 5 threads ");


        out.print("<a href=\"./\">Return to Start Page</a>");
        out.print("</body>");
        out.print("</html>");
    }

    /**
     * Method for POST Request
     *
     * @param req  HTTP Request
     * @param resp HTTP Response
     * @throws IOException
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }


    private String encodeToString(BufferedImage image) {
        String s = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", bos);
            byte[] bytes = bos.toByteArray();

            Base64.Encoder encoder = Base64.getEncoder();
            s = encoder.encodeToString(bytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    private BufferedImage decodeToImage(String imageString) {
        BufferedImage image = null;
        byte[] bytes;
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            bytes = decoder.decode(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }
}//End class