# LearningVCA

## Building
The following dependencies are needed:
  - JDK for Java 1.13
  - Maven
  - An Internet connection the first time the project is built

Simply run
```bash
mvn package
```
This generates a .jar file in `target/`. This file can be run with
```bash
java -jar LearningVCA-1.0-SNAPSHOT.jar
```

To build the documentation, the `JAVA_HOME` environment variable must be set.
The command
```bash
mvn site
```
generates the html files in `target/site`