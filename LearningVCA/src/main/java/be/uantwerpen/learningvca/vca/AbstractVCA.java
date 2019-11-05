package be.uantwerpen.learningvca.vca;

import net.automatalib.words.VPDAlphabet;

/**
 * An abstract implementation of a visibly one-counter automaton
 * @param <L> The type of the locations
 * @param <I> The input alphabet type
 * @author Gaëtan Staquet
 */
public abstract class AbstractVCA<L, I> implements VCA<L, I> {

    protected final VPDAlphabet<I> alphabet;

    public AbstractVCA(VPDAlphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public VPDAlphabet<I> getAlphabet() {
        return alphabet;
    }
    
    // Both interfaces define size
    @Override
    public abstract int size();
}