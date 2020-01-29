# LearningVCA

_LearningVCA_ is an implementation of the Visibly One-Counter Automaton (VCA) learning algorithm of Neider and Löding.

A VCA is an automaton with a counter that can be incremented/decremented by one and whose value can be checked up to a threshold.
A VCA works over a tuple of three alphabets. When reading a symbol of the first alphabet, the counter value must always be incremented by one. When reading a symbol of the second alphabet, the counter must be decremented, and, when reading a symbol of the third alphabet, the counter is not modified.

Using a pushdown alphabet implies that the counter value of a word depends solely on the word (and not on the automaton).
The idea of the algorithm is to learn a representation of the language up to a certain level (that is, the counter value of the words can not exceed the level).
Once this is done, VCAs are constructed from this representation.

The algorithm automatically learns a VCA accepting a target language and is structured in two parts:
  - A teacher who knows the target language; and
  - A learner whose goal is to learn a VCA.

To achieve this goal, the learner can ask three types of queries to the teacher:
  - Membership query: does a given word belong to the target language?
  - Partial Equivalence query: it my knowledge of the language up to a certain level correct?
  - Equivalence Query: does this VCA accept the target language?

In this implementation, the teacher receives a VCA accepting the target language.
See `LearningVCA.java` file to change the target language.

This project was made during the internship in the second year of my master's degree under the supervision of Guillermo A. Pérez (University of Antwerp) and Véronique Bruyère (University of Mons).

## Dependencies
The following dependencies are needed:
  - JDK for Java 1.13;
  - Maven; and
  - An Internet connection the first time the project is built.

Maven automatically downloads the following dependencies:
  - _LearnLib_ (which implies downloading _AutomataLib_ and other dependencies);
  - _testng_; and
  - _logback-classic_ (for logging).

## Building
In the `LearningVCA` folder, run:
```bash
mvn package
```

This compiles, executes the unit test and generates a `.jar` file in `target/`. This file can be run with:
```bash
java -jar LearningVCA-jar-with-dependencies.jar
```

To build the documentation, the `JAVA_HOME` environment variable must be set.
The following command generates the html files in `target/site`:
```bash
mvn site
```

## References
  - Daniel Neider and Christof Löding. _Learning Visibly One-Counter Automata in Polynomial Time_. Tech. rep. Department of Computer Science, RWTH Aachen, 2010. url: http://sunsite.informatik.rwth-aachen.de/Publications/AIB/2010/2010-02.pdf.
  - _LearnLib_ and _AutomataLib_: https://learnlib.de/.
  - _LearnLib_'s implementation of 1-Single Entry Visibly Pushdown Automaton.