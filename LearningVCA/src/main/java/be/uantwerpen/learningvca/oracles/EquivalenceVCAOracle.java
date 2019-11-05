package be.uantwerpen.learningvca.oracles;

import java.util.Collection;

import javax.annotation.Nullable;

import be.uantwerpen.learningvca.vca.ProductVCA;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.Word;

/**
 * Equivalence query between two VCAs
 * 
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class EquivalenceVCAOracle<I> implements EquivalenceOracle<VCA<?, I>, I, Boolean> {
    private final VCA<?, I> sul;

    public EquivalenceVCAOracle(VCA<?, I> sul) {
        this.sul = sul;
    }

    @Override
    @Nullable
    public DefaultQuery<I, Boolean> findCounterExample(VCA<?, I> hypothesis, Collection<? extends I> inputs) {
        VCA<?, I> productVCA = new ProductVCA<>(sul.getAlphabet(), sul, hypothesis, AcceptanceCombiner.XOR);
        Word<I> counterexample = productVCA.getAcceptedWord();
        if (counterexample == null) {
            return null;
        }
        return new DefaultQuery<>(counterexample);
    }

}