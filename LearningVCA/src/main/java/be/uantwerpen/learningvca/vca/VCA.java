package be.uantwerpen.learningvca.vca;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.VPDAlphabet;

/**
 * Interface for a visibly one-counter automaton.
 * @param <L> The type of the locations
 * @param <I> The input alphabet type
 * @author Gaëtan Staquet
 */
public interface VCA<L, I> extends DeterministicAcceptorTS<State<L>, I>, SuffixOutput<I, Boolean> {
    // Since DeterministicAcceptorTS and SuffixOutput both defines computeOutput, we need to explicitly define our function
    @Override
    default Boolean computeOutput(Iterable<? extends I> input) {
        return accepts(input);
    }

    /**
     * Gets the initial state (location, in LearnLib nomenclature)
     * @return The initial state
     */
    L getInitialLocation();

    /**
     * Gets the alphabet
     * @return The alphabet
     */
    VPDAlphabet<I> getAlphabet();

    /**
     * @return The size (number of states)
     */
    int size();

    /**
     * @return The list of locations
     */
    List<L> getLocations();

    /**
     * Gets the threshold m of this m-VCA.
     * @return m
     */
    int getThreshold();

    @Override
    default Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        State<L> state =  getState(Iterables.concat(prefix, suffix));
        return state != null && isAccepting(state);
    }

    @Override
    default boolean isAccepting(State<L> state) {
        return !state.isSink() && isAcceptingLocation(state.getLocation()) && state.getCounterValue().isZero();
    }

    /**
     * Is the location accepting?
     * @param loc The location
     * @return True iff the location is accepting
     */
    boolean isAcceptingLocation(L loc);

    /**
     * Gets the successor of the location when reading the given input symbol and with the given counter value.
     * @param loc The starting location
     * @param symbol The input symbol
     * @param counterValue The counter value
     * @return The successor, or null if there is no successor.
     */
    @Nullable
    L getSuccessor(L loc, I symbol, int counterValue);

    /**
     * Gets the call successor of the location when reading the given input symbol and with the given counter value.
     * @param loc The starting location
     * @param symbol The input symbol
     * @param counterValue The counter value
     * @return The call successor, or null if there is no successor.
     */
    @Nullable
    L getCallSuccessor(L loc, I symbol, int counterValue);

    /**
     * Gets the return successor of the location when reading the given input symbol and with the given counter value.
     * @param loc The starting location
     * @param symbol The input symbol
     * @param counterValue The counter value
     * @return The return successor, or null if there is no successor.
     */
    @Nullable
    L getReturnSuccessor(L loc, I symbol, int counterValue);

    /**
     * Gets the internal successor of the location when reading the given input symbol and with the given counter value.
     * @param loc The starting location
     * @param symbol The input symbol
     * @param counterValue The counter value
     * @return The internal successor, or null if there is no successor.
     */
    @Nullable
    L getInternalSuccessor(L loc, I symbol, int counterValue);

    /**
     * Gets the location id
     * @param loc
     * @return
     */
    int getLocationId(L loc);

    @Override
    default State<L> getInitialState() {
        return new State<L>(getInitialLocation(), new CounterValue(0));
    }
}