package be.uantwerpen.learningvca.behaviorgraph;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;

/**
 * The behavior graph of a visibly one-counter language
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class BehaviorGraph<I> {
    private final Description<I> description;

    /**
     * The constructor
     * @param description The description of the behavior graph
     */
    public BehaviorGraph(Description<I> description) {
        this.description = description;
    }

    /**
     * Gets the description
     * @return The description
     */
    public Description<I> getDescription() {
        return description;
    }

    /**
     * Constructs a DFA that accepts the same language that the behavior graph up to the given threshold
     * @param threshold The threshold
     * @return The DFA
     */
    public CompactDFA<I> toDFA(int threshold) {
        // TODO
        return null;
    }
}