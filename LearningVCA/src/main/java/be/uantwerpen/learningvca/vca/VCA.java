package be.uantwerpen.learningvca.vca;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.VPDAlphabet;

/**
 * A visibly one-counter automaton
 * 
 * Inspired from the implementation of single-entry visibly pushdown automaton (SEVPA) from LearnLib and AutomataLib
 */
public class VCA<I> implements DeterministicAcceptorTS<Configuration<State>, I> {
    protected final VPDAlphabet<I> alphabet;
    private final List<State> locations;
    private State initialState;

    public VCA(final VPDAlphabet<I> alphabet) {
        this.alphabet = alphabet;
        this.locations = new ArrayList<>();
    }

    @Override
    public Configuration<State> getTransition(Configuration<State> state, I input) {
        // If we are in a sink, we stay in the sink
        if (state.isSink()) {
            return state;
        }

        State successor = null;
        CounterValue successorValue = null;
        switch (alphabet.getSymbolType(input)) {
            case CALL:
                successor = state.getLocation().getCallSuccessor(alphabet.getCallSymbolIndex(input), state.getCounterValue().toInt());
                successorValue = state.getCounterValue().increment();
                break;
            case RETURN:
                successor = state.getLocation().getReturnSuccessor(alphabet.getReturnSymbolIndex(input), state.getCounterValue().toInt());
                successorValue = state.getCounterValue().decrement();
                break;
            case INTERNAL:
                successor = state.getLocation().getInternalSuccessor(alphabet.getInternalSymbolIndex(input), state.getCounterValue().toInt());
                successorValue = state.getCounterValue();
                break;
        }
        return new Configuration<State>(successor, successorValue);
    }

    @Override
    public boolean isAccepting(Configuration<State> state) {
        return !state.isSink() && state.getLocation().isAccepting() && state.getCounterValue().isZero();
    }

    @Override
    public Configuration<State> getInitialState() {
        return new Configuration<State>(getInitialLocation(), new CounterValue(0));
    }

    public State getInitialLocation() {
        return initialState;
    }
}