package be.uantwerpen.learningvca.oracles;

import java.util.Collection;

import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.equivalence.NearLinearEquivalenceTest;
import net.automatalib.words.Word;

/**
 * Performs a partial equivalence query.
 * 
 * This oracle checks that the behavior graph is correctly learned up to a treshold t
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class PartialEquivalenceOracle<I extends Comparable<I>> {
    private VCA<?, I> vca;

    public PartialEquivalenceOracle(VCA<?, I> vca) {
        this.vca = vca;
    }

    public DefaultQuery<I, Boolean> findCounterExample(LimitedBehaviorGraph<I> hypothesis, int threshold, Collection<? extends I> inputs) {
        DFA<?, I> sul = vca.toLimitedBehaviorGraph(threshold);
        Word<I> counterexample = NearLinearEquivalenceTest.findSeparatingWord(sul, hypothesis, vca.getAlphabet());

        if (counterexample == null) {
            return null;
        }

        return new DefaultQuery<>(counterexample);
    }

}