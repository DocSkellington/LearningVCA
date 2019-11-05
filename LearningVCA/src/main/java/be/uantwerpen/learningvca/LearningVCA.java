package be.uantwerpen.learningvca;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import be.uantwerpen.learningvca.behaviorgraph.BehaviorGraph;
import be.uantwerpen.learningvca.behaviorgraph.Description;
import be.uantwerpen.learningvca.behaviorgraph.TauMapping;
import be.uantwerpen.learningvca.learner.LearnerVCA;
import be.uantwerpen.learningvca.oracles.EquivalenceVCAOracle;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.util.ComputeCounterValue;
import be.uantwerpen.learningvca.vca.AbstractVCA;
import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

/**
 * The main class
 * 
 * @author Gaëtan Staquet
 */
public class LearningVCA {
    private LearningVCA() {

    }

    public static void main(String[] args) {
        VCA<?, Character> sul = getSUL();
        VPDAlphabet<Character> alphabet = sul.getAlphabet();
        BehaviorGraph<Character> behaviorGraph = getBG(alphabet);

        MembershipOracle<Character, Boolean> membershipOracle = new SimulatorOracle<>(sul);
        PartialEquivalenceOracle<Character> partialEquivalenceOracle = new PartialEquivalenceOracle<>(behaviorGraph);
        EquivalenceVCAOracle<Character> equivalenceVCAOracle = new EquivalenceVCAOracle<>(sul);

        LearnerVCA<Character> learner = new LearnerVCA<>(alphabet, membershipOracle, partialEquivalenceOracle);

        DefaultQuery<Character, Boolean> counterexample = null;
        VCA<?, Character> answer = null;

        do {
            System.out.println("Counterexample: " + counterexample);
            if (counterexample == null) {
                learner.startLearning();
            } else {
                System.out.println("REFINING");
                learner.refineHypothesis(counterexample);
                System.out.println("REFINING DONE");
            }
            counterexample = null;

            VCA<?, Character> hypothesis;
            // We test every possible description of BG_O
            while ((hypothesis = learner.getHypothesisModel()) != null) {
                System.out.println("Hypothesis: " + hypothesis);
                DefaultQuery<Character, Boolean> word = equivalenceVCAOracle.findCounterExample(hypothesis, alphabet);
                System.out.println("FindCounterExample done");

                if (word == null) {
                    // We have found a correct VCA
                    answer = hypothesis;
                    break;
                }

                if (ComputeCounterValue.computeHeight(word.getInput(), alphabet) > learner
                        .getObservationTableLevelLimit()) {
                    counterexample = word;
                }
            }

            if (counterexample == null && answer == null) {
                System.out.println("Testing with the behavior graph");
                // We didn't find a counter example nor an appropriate VCA
                VCA<?, Character> vca = learner.getObservationTable().toVCA();
                System.out.println(vca);
                // GraphDOT.write((AbstractVCA<?, Character>) vca, new FileWriter("bg.dot"));
                counterexample = equivalenceVCAOracle.findCounterExample(vca, alphabet);
                System.out.println(counterexample);
            }
        } while (counterexample != null);

        System.out.println("Answer: " + answer);
        try {
            GraphDOT.write((AbstractVCA<?, Character>)answer, new FileWriter("output.dot"));
        } catch (IOException e) {
            System.out.println("Impossible to open the file 'output.dot' to write the DOT format of the VCA");
        }
    }

    /**
     * Constructs a simple 1-VCA for L = {a^n b^n | n is a natural and n is not zero}
     * @return A 1-VCA
     */
    private static VCA<?, Character> getSUL() {
        List<Character> internalSymbols = Collections.emptyList();
        List<Character> callSymbols = Arrays.asList('a');
        List<Character> returnSymbols = Arrays.asList('b');
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<Character>(internalSymbols, callSymbols, returnSymbols);
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 1);

        Location q0 = vca.addInitialLocation(true);
        Location q1 = vca.addLocation(true);

        vca.setCallSuccessor(q0, 0, 'a', q0);
        vca.setCallSuccessor(q0, 1, 'a', q0);
        vca.setReturnSuccessor(q0, 1, 'b', q1);
        vca.setReturnSuccessor(q1, 1, 'b', q1);

        return vca;
    }

    /**
     * Constructs the behavior graph for L = {a^n b^n  | n is a natural and n is not zero}
     * @return The behavior graph
     */
    private static BehaviorGraph<Character> getBG(VPDAlphabet<Character> alphabet) {
        TauMapping<Character> tau0 = new TauMapping<>(3);
        tau0.addTransition(1, 'a', 1);
        tau0.addTransition(2, 'a', 3);
        tau0.addTransition(3, 'a', 3);

        TauMapping<Character> tau1 = new TauMapping<>(3);
        tau1.addTransition(1, 'a', 1);
        tau1.addTransition(1, 'b', 2);
        tau1.addTransition(2, 'a', 3);
        tau1.addTransition(2, 'b', 2);
        tau1.addTransition(3, 'a', 3);
        tau1.addTransition(3, 'b', 3);

        Description<Character> description = new Description<>(1, 1, 3);
        description.addTauMappings(Arrays.asList(tau0, tau1));
        description.setInitialState(0, 1);
        description.addAcceptingState(0, 1);
        description.addAcceptingState(0, 2);
        
        BehaviorGraph<Character> bg = new BehaviorGraph<>(alphabet, description);
        return bg;
    }
}
