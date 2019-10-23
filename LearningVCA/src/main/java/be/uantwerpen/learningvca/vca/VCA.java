package be.uantwerpen.learningvca.vca;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Iterables;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.VPDAlphabet;

/**
 * A visibly one-counter automaton.
 * 
 * Inspired from the implementation of one single-entry visibly pushdown automaton
 * (oneSEVPA) from LearnLib and AutomataLib
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class VCA<I> implements DeterministicAcceptorTS<Configuration<State>, I>, SuffixOutput<I, Boolean> {
    protected final VPDAlphabet<I> alphabet;
    private final List<State> states;
    private State initialState;
    private int m;

    /**
     * The constructor
     * @param alphabet The pushdown alphabet
     */
    public VCA(final VPDAlphabet<I> alphabet, int m) {
        this.alphabet = alphabet;
        this.states = new LinkedList<>();
        this.m = m;
    }

    /**
     * Gets the initial state (location, in LearnLib nomenclature)
     * @return The initial state
     */
    public State getInitialLocation() {
        return initialState;
    }

    /**
     * Gets the alphabet
     * @return The alphabet
     */
    public VPDAlphabet<I> getAlphabet() {
        return alphabet;
    }

    /**
     * @return The size (number of states)
     */
    public int size() {
        return states.size();
    }

    /**
     * @return The list of states
     */
    public List<State> getStates() {
        return states;
    }

    /**
     * Gets the threshold m of this m-VCA.
     * @return m
     */
    public int getThreshold() {
        return m;
    }

    /**
     * Adds an initial state.
     * 
     * It overrides any previously defined initial state
     * @param accepting Whether the state is accepting
     * @return The initial state
     */
    public State addInitialState(boolean accepting) {
        initialState = addState(accepting);
        return initialState;
    }

    /**
     * Adds a state that is not accepting.
     * @return The state
     */
    public State addState() {
        return addState(false);
    }

    /**
     * Adds a state.
     * @param accepting Whether the state is accepting
     * @return The state
     */
    public State addState(boolean accepting) {
        State state = new State(getAlphabet(), m, accepting);
        states.add(state);
        return state;
    }

    /**
     * Sets the initial state
     * @param initialState The new initial state
     */
    public void setInitialState(State initialState) {
        this.initialState = initialState;
    }

    /**
     * Defines the transition from start to successor when reading an internal input and when the counter value is equal to the given value
     * @param start The starting state
     * @param counterValue The value the counter must be equal to for this transition to be active
     * @param input The input
     * @param successor The successor
     */
    public void setInternalSuccessor(State start, int counterValue, I input, State successor) {
        start.setInternalSuccessor(alphabet.getInternalSymbolIndex(input), counterValue, successor);
    }

    /**
     * Defines the transition from start to successor when reading a call input and when the counter value is equal to the given value
     * @param start The starting state
     * @param counterValue The value the counter must be equal to for this transition to be active
     * @param input The input
     * @param successor The successor
     */
    public void setCallSuccessor(State start, int counterValue, I input, State successor) {
        start.setCallSuccessor(alphabet.getCallSymbolIndex(input), counterValue, successor);
    }

    /**
     * Defines the transition from start to successor when reading a return input and when the counter value is equal to the given value
     * @param start The starting state
     * @param counterValue The value the counter must be equal to for this transition to be active
     * @param input The input
     * @param successor The successor
     */
    public void setReturnSuccessor(State start, int counterValue, I input, State successor) {
        start.setReturnSuccessor(alphabet.getReturnSymbolIndex(input), counterValue, successor);
    }

    /**
     * Constructs a 1-single entry visibly pushdown automaton accepting the same language as this VCA
     * @return The VPDA
     */
    public DefaultOneSEVPA<I> toVPDA() {
        DefaultOneSEVPA<I> vpda = new DefaultOneSEVPA<>(alphabet);
        // TODO
        return null;
    }

    // Since DeterministicAcceptorTS and SuffixOutput both defines computeOutput, we need to explicitly define our function
    @Override
    public Boolean computeOutput(Iterable<? extends I> input) {
        return accepts(input);
    }

    // For DeterministicAcceptorTS
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
            successor = state.getLocation().getCallSuccessor(alphabet.getCallSymbolIndex(input),
                    state.getCounterValue().toInt());
            successorValue = state.getCounterValue().increment();
            break;
        case RETURN:
            successor = state.getLocation().getReturnSuccessor(alphabet.getReturnSymbolIndex(input),
                    state.getCounterValue().toInt());
            successorValue = state.getCounterValue().decrement();
            break;
        case INTERNAL:
            successor = state.getLocation().getInternalSuccessor(alphabet.getInternalSymbolIndex(input),
                    state.getCounterValue().toInt());
            successorValue = state.getCounterValue();
            break;
        default:
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

    // For SuffixOutput
    @Override
    public Collection<Configuration<State>> getTransitions(Configuration<State> state, I input) {
        ArrayList<Configuration<State>> transitions = new ArrayList<>(1);
        transitions.add(getTransition(state, input));
        return transitions;
    }

    @Override
    public Set<Configuration<State>> getInitialStates() {
        TreeSet<Configuration<State>> states = new TreeSet<>();
        states.add(getInitialState());
        return states;
    }

    @Override
    public Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        Configuration<State> state =  getState(Iterables.concat(prefix, suffix));
        return state != null && isAccepting(state);
    }
}