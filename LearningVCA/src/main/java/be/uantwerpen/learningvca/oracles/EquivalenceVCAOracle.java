package be.uantwerpen.learningvca.oracles;

import java.util.Collection;

import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.equivalence.vpda.SimulatorEQOracle;

/**
 * Equivalence query between two VCAs
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class EquivalenceVCAOracle<I> implements EquivalenceOracle<VCA<I>, I, Boolean> {
    private final SimulatorEQOracle<I> vpdaOracle;

    public EquivalenceVCAOracle(VCA<I> sul) {
        this.vpdaOracle = new SimulatorEQOracle<>(sul.toVPDA(), sul.getAlphabet());
    }

    @Override
    public DefaultQuery<I, Boolean> findCounterExample(VCA<I> hypothesis, Collection<? extends I> inputs) {
        return vpdaOracle.findCounterExample(hypothesis.toVPDA(), inputs);
    }

}