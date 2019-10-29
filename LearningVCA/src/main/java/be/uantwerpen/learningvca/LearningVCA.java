package be.uantwerpen.learningvca;

import java.util.Arrays;
import java.util.List;

import be.uantwerpen.learningvca.behaviorgraph.BehaviorGraph;
import be.uantwerpen.learningvca.behaviorgraph.Description;
import be.uantwerpen.learningvca.behaviorgraph.TauMapping;
import be.uantwerpen.learningvca.learner.LearnerVCA;
import be.uantwerpen.learningvca.oracles.EquivalenceVCAOracle;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.util.ComputeCounterValue;
import be.uantwerpen.learningvca.vca.State;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

/**
 * The main class
 * @author Gaëtan Staquet
 */
public class LearningVCA {
    private LearningVCA() {

    }

    public static void main( String[] args ) {
        VCA<Character> sul = getSUL();
        VPDAlphabet<Character> alphabet = sul.getAlphabet();
        BehaviorGraph<Character> behaviorGraph = getBG(alphabet);

        MembershipOracle<Character, Boolean> membershipOracle = new SimulatorOracle<>(sul);
        PartialEquivalenceOracle<Character> partialEquivalenceOracle = new PartialEquivalenceOracle<>(behaviorGraph);
        EquivalenceVCAOracle<Character>     equivalenceVCAOracle = new EquivalenceVCAOracle<>(sul);

        LearnerVCA<Character> learner = new LearnerVCA<>(alphabet, membershipOracle, partialEquivalenceOracle);

        DefaultQuery<Character, Boolean> counterexample = null;
        VCA<Character> answer = null;

        do {
            if (counterexample == null) {
                learner.startLearning();
            }
            else {
                learner.refineHypothesis(counterexample);
            }

            VCA<Character> hypothesis;
            // We test every possible description of BG_O
            while ((hypothesis = learner.getHypothesisModel()) != null) {
                DefaultQuery<Character, Boolean> word = equivalenceVCAOracle.findCounterExample(hypothesis, alphabet);

                if (word == null) {
                    // We have found a correct VCA
                    answer = hypothesis;
                    break;
                }

                if (ComputeCounterValue.computeHeight(word.getInput(), alphabet) > learner.getObservationTableLevelLimit()) {
                    counterexample = word;
                }
            }

            if (counterexample == null && answer == null) {
                // We didn't find a counter example nor an appropriate VCA
                counterexample = equivalenceVCAOracle.findCounterExample(learner.getObservationTable().toVCA(), alphabet);
            }
        } while (counterexample != null);

        // TODO write the final VCA in DOT format
    }

    /**
     * Constructs a simple 1-VCA for L = {a^n b^n | n is a natural and n is not zero}
     * @return A 1-VCA
     */
    private static VCA<Character> getSUL() {
        List<Character> internalSymbols = Arrays.asList();
        List<Character> callSymbols = Arrays.asList('a');
        List<Character> returnSymbols = Arrays.asList('b');

        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<Character>(internalSymbols, callSymbols, returnSymbols);
        VCA<Character> vca = new VCA<>(alphabet, 1);

        State q0 = vca.addInitialState(true);
        State q1 = vca.addState(true);

        vca.setCallSuccessor(q0, 0, 'a', q0);
        vca.setCallSuccessor(q0, 1, 'a', q1);
        vca.setReturnSuccessor(q0, 1, 'b', q1);
        vca.setReturnSuccessor(q1, 1, 'b', q1);

        return vca;
    }

    /**
     * Constructs the behavior graph for L = {a^n b^n  | n is a natural and n is not zero}
     * @return The behavior graph
     */
    private static BehaviorGraph<Character> getBG(VPDAlphabet<Character> alphabet) {
        TauMapping<Character> tau0 = new TauMapping<>(2);
        tau0.addTransition(1, 'a', 1);
        // tau0.addTransition(2, 'a', 3);
        // tau0.addTransition(3, 'a', 3);

        TauMapping<Character> tau1 = new TauMapping<>(2);
        tau1.addTransition(1, 'a', 1);
        tau1.addTransition(1, 'b', 2);
        // tau1.addTransition(2, 'a', 3);
        tau1.addTransition(2, 'b', 2);
        // tau1.addTransition(3, 'a', 3);
        // tau1.addTransition(3, 'b', 3);

        Description<Character> description = new Description<>(1, 1, 2);
        description.addTauMappings(Arrays.asList(tau0, tau1));
        description.setInitialState(0, 1);
        description.addAcceptingState(0, 1);
        description.addAcceptingState(0, 2);
        
        BehaviorGraph<Character> bg = new BehaviorGraph<>(alphabet, description);
        return bg;
    }
}
