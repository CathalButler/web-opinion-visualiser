## web-opinion-visualiser
### Cathal Butler | G00346889 | Final Year Software Development
 A multi-threaded AI search application that can generate a word cloud from the top 20 words associated with an internet search term.

### Environment Setups
Environment setup can be done in two ways, ether install [Tomcat](https://tomcat.apache.org/download-80.cgi) or use a [Docker](https://www.docker.com/) container with tomcat.


### How to run with Docker
 
`docker run -p 8888:8080`

* To mount the volume with the wcloud.war amend the command with the the path were you downloaded the .war file too
`-v [download location]/ngrams.war:/usr/local/tomcat/webapps/wcloud.war`
* Container name:
`tomcat:9.0.30-jdk8-openjdk`

Navigate to `localhost:8888/wcloud`

### Development & Testing
This project was developed on my own personal laptop running
* OS: [Manjaro Linux](https://manjaro.org/download/official/kde/)
* Kernel: 5.5.15
* Java 8
* [IntelliJ IDEA 2019.3.4 (Ultimate Edition)](https://www.jetbrains.com/idea/)
  - Build #IU-193.6911.18, built on March 17, 2020
* [Docker](https://www.docker.com/): 19.03.5


### Ref:
 * https://www.youtube.com/watch?v=ySN5Wnu88nE
 * https://www.baeldung.com/java-runnable-callable

