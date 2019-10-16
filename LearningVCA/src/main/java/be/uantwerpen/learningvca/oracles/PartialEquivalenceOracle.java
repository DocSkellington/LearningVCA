package be.uantwerpen.learningvca.oracles;

import java.util.Collection;

import be.uantwerpen.learningvca.behaviorgraph.BehaviorGraph;
import de.learnlib.api.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

/**
 * Performs a partial equivalence query.
 * 
 * This oracle checks that the behavior graph is correctly learned up to a treshold t
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class PartialEquivalenceOracle<I extends Comparable<I>> {
    private BehaviorGraph<I> behaviorGraph;

    public PartialEquivalenceOracle(BehaviorGraph<I> behaviorGraph) {
        this.behaviorGraph = behaviorGraph;
    }

    public DefaultQuery<I, Boolean> findCounterExample(CompactDFA<I> hypothesis, int threshold, Collection<? extends I> inputs) {
        CompactDFA<I> sul = behaviorGraph.toDFA(threshold);

        DFAEquivalenceOracle<I> oracle = new SimulatorEQOracle.DFASimulatorEQOracle<>(sul);

        return oracle.findCounterExample(hypothesis, inputs);
    }

}