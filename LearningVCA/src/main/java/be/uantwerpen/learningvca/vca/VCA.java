package be.uantwerpen.learningvca.vca;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

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
    default L getSuccessor(L loc, I symbol, int counterValue) {
        switch (getAlphabet().getSymbolType(symbol)) {
            case CALL:
                return getCallSuccessor(loc, symbol, counterValue);
            case RETURN:
                return getReturnSuccessor(loc, symbol, counterValue);
            case INTERNAL:
                return getInternalSuccessor(loc, symbol, counterValue);
            default:
                throw new InvalidParameterException("Unknown input symbol type. Received: "  + symbol + " but it is not in the alphabet.");
        }
    }

    /**
     * Gets the call successor of the location when reading the given input symbol and with the given counter value.
     * @param loc The starting location
     * @param symbol The input symbol
     * @param counterValue The counter value
     * @return The call successor, or null if there is no successor.
     */
    @Nullable
    L getCallSuccessor(@Nullable L loc, I symbol, int counterValue);

    /**
     * Gets the return successor of the location when reading the given input symbol and with the given counter value.
     * @param loc The starting location
     * @param symbol The input symbol
     * @param counterValue The counter value
     * @return The return successor, or null if there is no successor.
     */
    @Nullable
    L getReturnSuccessor(@Nullable L loc, I symbol, int counterValue);

    /**
     * Gets the internal successor of the location when reading the given input symbol and with the given counter value.
     * @param loc The starting location
     * @param symbol The input symbol
     * @param counterValue The counter value
     * @return The internal successor, or null if there is no successor.
     */
    @Nullable
    L getInternalSuccessor(@Nullable L loc, I symbol, int counterValue);

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

    /**
     * Gives the list of accepting locations
     * @return The list of accepting locations
     */
    default List<L> getAcceptingLocations() {
        List<L> acceptingLocations = new ArrayList<>();
        for (L loc : getLocations()) {
            if (isAcceptingLocation(loc)) {
                acceptingLocations.add(loc);
            }
        }
        return acceptingLocations;
    }

    default Set<Pair<State<L>, I>> getPredecessors(State<L> state) {
        Set<Pair<State<L>, I>> predecessors = new HashSet<>();

        for (L l : getLocations()) {
            for (I symbol : getAlphabet()) {
                CounterValue cv = state.getCounterValue();
                if (getAlphabet().isCallSymbol(symbol)) {
                    cv = cv.decrement();
                    if (!cv.isValid()) {
                        continue;
                    }
                }
                else if (getAlphabet().isReturnSymbol(symbol)) {
                    cv = cv.increment();
                }
                L successor = getSuccessor(l, symbol, cv.toInt());
                if (successor == null) {
                    if (state.getLocation() == null) {
                        predecessors.add(Pair.of(new State<>(l, cv), symbol));
                    }
                }
                else if (successor.equals(state.getLocation())) {
                    predecessors.add(Pair.of(new State<L>(l, cv), symbol));
                }
            }
        }

        return predecessors;
    }

    /**
     * Computes an accepted word by this VCA.
     * @return An accepted word, or null
     */
    @Nullable
    default Word<I> getAcceptedWord() {
        List<Pair<State<L>, Word<I>>> states = new ArrayList<>();

        // We initialize at the accepting configurations
        for (L l : getAcceptingLocations()) {
            states.add(Pair.of(new State<>(l, new CounterValue(0)), Word.epsilon()));
        }

        // We compute the set of the predecessors of the configurations until:
        //      - we reach a fix point; or
        //      - we reach the initial configuration
        boolean changement = true;
        while (changement) {
            changement = false;
            List<Pair<State<L>, Word<I>>> newStates = new ArrayList<>(states.size());
            newStates.addAll(states);

            for (Pair<State<L>, Word<I>> pair : states) {
                State<L> state = pair.getFirst();
                Word<I> word = pair.getSecond();
                for (Pair<State<L>, I> p : getPredecessors(state)) {
                    State<L> predecessor = p.getFirst();
                    I symbol = p.getSecond();
                    if (getInitialState().equals(predecessor)) {
                        return word.prepend(symbol);
                    }
                    if (predecessor.getCounterValue().toInt() > size()) {
                        continue;
                    }

                    Word<I> newWord = word.prepend(symbol);
                    if (!newStates.contains(Pair.of(predecessor, newWord))) {
                        changement = true;
                        newStates.add(Pair.of(predecessor, newWord));
                    }
                }
            }
        
            states = newStates;
        }

        return null;
    }
}