## Web-opinion-visualiser
### Cathal Butler | G00346889 | Final Year Software Development
 A multithreaded AI search application that can generate a word cloud from the top 20 words associated with an internet search term.

### Environment Setups
Environment setup can be done in two ways, either install [Tomcat](https://tomcat.apache.org/download-80.cgi) or use a [Docker](https://www.docker.com/) container with tomcat.

### Building Jar
**Must use Java 8**<br>
Compile Java classes with needed Jar inside`./web/WEB-INF/lib` set in the classpath:<br>
* First cd into the classes folder: `cd web/WEB-INF/classes`
* Then run compile the classes and use find and grep to location `.java` files: `javac -cp ".:../lib/*" $(find ./* | grep .java)`
* Finally, create the jar by running: `jar -cf ../wcloud.war *`

### How to run with Docker
 
`docker run -p 8888:8080`

* To mount the volume with the wcloud.war amend the command with the path were you downloaded the .war file too
`-v [download location]/wcloud.war:/usr/local/tomcat/webapps/wcloud.war`
* Container name:
`tomcat:9.0.30-jdk8-openjdk`

Navigate to `localhost:8888/wcloud`

### Development & Testing
This project was developed on my own personal laptop running
* OS: [Manjaro Linux](https://manjaro.org/download/official/kde/)
* Kernel: 5.5.15
* Java 8
* Tomcat 9
* [IntelliJ IDEA 2019.3.4 (Ultimate Edition)](https://www.jetbrains.com/idea/)
  - Build #IU-193.6911.18, built on March 17, 2020
* [Docker](https://www.docker.com/): 19.03.5


### References:
 * John Healy - Lecture of the module. Online tutorial videos and lecture content.
 * https://www.cs.tau.ac.il/~shanir/concurrent-data-structures.pdf
 * https://www.youtube.com/watch?v=ySN5Wnu88nE
 * https://www.baeldung.com/java-runnable-callable
 * https://stackoverflow.com/questions/21771566/calculating-frequency-of-each-word-in-a-sentence-in-java
 * https://www.logicbig.com/how-to/code-snippets/jcode-java-8-streams-collectors-groupingby.html
 * https://www.geeksforgeeks.org/stream-in-java/

