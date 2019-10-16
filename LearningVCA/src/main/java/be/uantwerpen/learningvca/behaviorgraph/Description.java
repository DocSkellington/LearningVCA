package be.uantwerpen.learningvca.behaviorgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.uantwerpen.learningvca.vca.VCA;

/**
 * The description of a behavior graph.
 * 
 * The description consists of:
 *  - The offset m
 *  - The period k
 *  - A list of TauMapping
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class Description<I extends Comparable<I>> {
    private final int m;
    private final int k;
    private final List<TauMapping<I>> tauMappings;
    
    /**
     * The constructor
     * @param offset m
     * @param period k
     */
    public Description(int offset, int period) {
        this.m = offset;
        this.k = period;
        this.tauMappings = new ArrayList<>(offset + period);
    }

    public void addTauMappings(Collection<TauMapping<I>> mappings) {
        this.tauMappings.addAll(mappings);
    }

    /**
     * Gets the mapping tau
     * @return The mapping
     */
    public List<TauMapping<I>> getTauMappings() {
        return tauMappings;
    }

    /**
     * Gets the offset m
     * @return The offset
     */
    public int getOffset() {
        return m;
    }

    /**
     * Gets the period k
     * @return The period
     */
    public int getPeriod() {
        return k;
    }

    /**
     * Constructs a m-VCA accepting the same language as the behavior graph described.
     * @return A m-VCA
     */
    public VCA<I> toVCA() {
        // TODO
        return null;
    }
}