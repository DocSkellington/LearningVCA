package be.uantwerpen.learningvca;

import java.io.IOException;

import de.learnlib.algorithms.dhc.mealy.MealyDHC;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFABuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.OTUtils;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.filter.cache.mealy.MealyCaches;
import de.learnlib.filter.statistic.oracle.CounterOracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle.DFAWMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.util.Experiment.DFAExperiment;
import de.learnlib.util.statistics.SimpleProfiler;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleCoffeeMachine.Input;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * Hello world!
 *
 */
public class LearningVCA 
{
    private static final int EXPLORATION_DEPTH = 4;

    private LearningVCA() {

    }

    public static void main( String[] args )
    {
        CompactMealy<Input, String> fm = ExampleCoffeeMachine.constructMachine();
        Alphabet<Input> alphabet = fm.getInputAlphabet();

        SimulatorOracle<Input, Word<String>> simoracle = new SimulatorOracle<>(fm);
        SimulatorEQOracle<Input, Word<String>> eqoracle = new SimulatorEQOracle<>(fm);

        MembershipOracle<Input, Word<String>> cache = MealyCaches.createCache(alphabet, simoracle);

        MealyDHC<Input, String> learner = new MealyDHC<>(alphabet, cache);

        DefaultQuery<Input, Word<String>> counterexample = null;
        do {
            if (counterexample == null) {
                learner.startLearning();
            } else {
                boolean refined = learner.refineHypothesis(counterexample);
                if (!refined) {
                    System.err.println("No refinement effected by counterexample!");
                }
            }

            counterexample = eqoracle.findCounterExample(learner.getHypothesisModel(), alphabet);

        } while (counterexample != null);

        CompactMealy<Input, String> result = learner.getHypothesisModel();

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        // System.out.println(experiment.getRounds().getSummary());
        // System.out.println(mqOracle.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        // System.out.println("Sigma: " + inputs.size());

        // show model
        System.out.println();
        System.out.println("Model: ");
        // try {
        //     GraphDOT.write(result, inputs, System.out); // may throw IOException!
        // }
        // catch (IOException exception) {
        //     exception.printStackTrace();
        // }

        // Visualization.visualize(result, inputs);

        System.out.println("-------------------------------------------------------");

        System.out.println("Final observation table:");
        // new ObservationTableASCIIWriter<>().write(lstar.getObservationTable(), System.out);

        // try {
        //     OTUtils.displayHTMLInBrowser(lstar.getObservationTable());
        // }
        // catch (IOException exception) {
        //     exception.printStackTrace();
        // }
    }

    private static CompactDFA<Character> constructSUL() {
        // input alphabet contains characters 'a'..'b'
        Alphabet<Character> sigma = Alphabets.characters('a', 'b');

        // @formatter:off
        // create automaton
        return AutomatonBuilders.newDFA(sigma)
                .withInitial("q0")
                .from("q0")
                    .on('a').to("q1")
                    .on('b').to("q2")
                .from("q1")
                    .on('a').to("q0")
                    .on('b').to("q3")
                .from("q2")
                    .on('a').to("q3")
                    .on('b').to("q0")
                .from("q3")
                    .on('a').to("q2")
                    .on('b').to("q1")
                .withAccepting("q0")
                .create();
        // @formatter:on
    }
}
