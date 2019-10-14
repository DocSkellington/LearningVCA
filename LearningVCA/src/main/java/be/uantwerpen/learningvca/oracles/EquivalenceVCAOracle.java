package be.uantwerpen.learningvca.oracles;

import java.util.Collection;

import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;

/**
 * Equivalence query between two VCAs
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class EquivalenceVCAOracle<I> implements EquivalenceOracle<VCA<I>, I, Boolean> {
    private VCA<I> sul;

    public EquivalenceVCAOracle(VCA<I> sul) {
        this.sul = sul;
    }

    @Override
    public DefaultQuery<I, Boolean> findCounterExample(VCA<I> hypothesis, Collection<? extends I> inputs) {
        // TODO Auto-generated method stub
        return null;
    }

}