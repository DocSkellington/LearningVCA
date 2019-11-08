package be.uantwerpen.learningvca;

import java.io.FileWriter;
import java.io.IOException;

import be.uantwerpen.learningvca.examples.ExampleTwoCalls;
import be.uantwerpen.learningvca.experiment.VCAExperiment;
import be.uantwerpen.learningvca.learner.LearnerVCA;
import be.uantwerpen.learningvca.oracles.EquivalenceVCAOracle;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.VPDAlphabet;

/**
 * The main class
 * 
 * @author Gaëtan Staquet
 */
public class LearningVCA {
    private LearningVCA() {

    }

    public static void main(String[] args) throws IOException {
        VCA<?, Character> sul = ExampleTwoCalls.getVCA();
        VPDAlphabet<Character> alphabet = sul.getAlphabet();

        MembershipOracle<Character, Boolean> membershipOracle = new SimulatorOracle<>(sul);
        CounterOracle<Character, Boolean> membershipOracleCounter = new CounterOracle<>(membershipOracle, "membership queries");
        PartialEquivalenceOracle<Character> partialEquivalenceOracle = new PartialEquivalenceOracle<>(sul);
        EquivalenceVCAOracle<Character> equivalenceVCAOracle = new EquivalenceVCAOracle<>(sul);

        LearnerVCA<Character> learner = new LearnerVCA<>(alphabet, membershipOracleCounter, partialEquivalenceOracle);

        VCAExperiment<Character> experiment = new VCAExperiment<>(learner, equivalenceVCAOracle, alphabet);
        experiment.setLog(true);
        experiment.setLogModels(true);
        experiment.setProfile(true);
        VCA<?, Character> answer = experiment.run();

        System.out.println("-------------------------------------------------------");

        System.out.println(SimpleProfiler.getResults());
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(membershipOracleCounter.getStatisticalData().getSummary());

        System.out.println("States: " + answer.size());
        System.out.println("Sigma: " + alphabet.size());

        GraphDOT.write(answer, new FileWriter("output.dot"));
        System.out.println();
        System.out.println("Model: ");
        GraphDOT.write(answer, System.out);

        System.out.println("-------------------------------------------------------");
        
        // TODO we should redo the functions that print the observation table to properly show the different levels
        // System.out.println("Final observation table: ");
        // new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);

        // OTUtils.displayHTMLInBrowser(learner.getObservationTable());
    }
}
