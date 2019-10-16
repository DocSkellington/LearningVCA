package be.uantwerpen.learningvca.behaviorgraph;

import java.util.List;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;

/**
 * A behavior graph limited to a threshold t
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class LimitedBehaviorGraph<I extends Comparable<I>> extends CompactDFA<I> {
    private static final long serialVersionUID = 5195494200755637801L;

    private final int threshold;

    /**
     * The constructor
     * @param alphabet The alphabet
     * @param threshold The threshold t
     */
    public LimitedBehaviorGraph(Alphabet<I> alphabet, int threshold) {
        super(alphabet);
        this.threshold = threshold;
    }

    /**
     * Constructs every possible periodic description of this behavior graph
     * @return A list with every periodic description
     */
    public List<Description<I>> getPeriodicDescriptions() {
        // TODO
        return null;
    }
}