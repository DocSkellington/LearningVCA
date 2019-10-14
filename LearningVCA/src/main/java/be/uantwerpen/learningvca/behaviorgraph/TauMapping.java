package be.uantwerpen.learningvca.behaviorgraph;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * A mapping tau maps (state, input) to state, in a behavior graph.
 * 
 * States are stored according to a certain mapping nu.
 * That is, states are stored in [1, K], with K the width of the behavior graph.
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class TauMapping<I> {
    /**
     * A key in the mapping.
     * 
     * A key is a pair (state id, input)
     */
    protected class MappingKey {
        public final int index;
        public final I input;

        public MappingKey(int index, I input) {
            this.index = index;
            this.input = input;
        }
    }

    private final Map<MappingKey, Integer> mapping;
    private final int K;

    /**
     * The constructor
     * @param width K
     */
    public TauMapping(int width) {
        this.mapping = new HashMap<>();
        this.K = width;
    }

    /**
     * Adds a transition from start to target when reading the input
     * @param start The starting state
     * @param input The input
     * @param target The target state
     */
    public void addTransition(int start, I input, int target) {
        if (!(1 <= start && start <= K)) {
            throw new InvalidParameterException("Description of a behavior graph: start must be in [" + 1 + ", " + K + "]. Received: " + start);
        }
        if (!(1 <= target && target <= K)) {
            throw new InvalidParameterException("Description of a behavior graph: target must be in [" + 1 + ", " + K + "]. Received: " + target);
        }

        mapping.put(new MappingKey(start, input), target);
    }
}