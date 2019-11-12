package be.uantwerpen.learningvca.vca;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.VPDAlphabet;

/**
 * An abstract implementation of a visibly one-counter automaton
 * @param <L> The type of the locations
 * @param <I> The input alphabet type
 * @author Gaëtan Staquet
 */
public abstract class AbstractVCA<L, I> implements VCA<L, I> {

    private final VPDAlphabet<I> alphabet;

    public AbstractVCA(VPDAlphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public String toString() {
        return getThreshold() + "-VCA with " + size() + " states";
    }

    @Override
    public VPDAlphabet<I> getAlphabet() {
        return alphabet;
    }
    
    // Both interfaces define size
    @Override
    public abstract int size();

    @Override
    public DFA<?, I> toLimitedBehaviorGraph(int threshold) {
        CompactDFA<I> behaviorGraph = new CompactDFA<>(getAlphabet());
        Integer initialBG = behaviorGraph.addInitialState(isAcceptingLocation(getInitialLocation()));
        Map<State<L>, Integer> representatives = new HashMap<>();
        representatives.put(getInitialState(), initialBG);
        toLimitedBehaviorGraphDFS(threshold, behaviorGraph, getInitialState(), initialBG, representatives, new EquivalentStates<>(this, threshold));
        return behaviorGraph;
    }

    private void toLimitedBehaviorGraphDFS(
        int threshold,
        CompactDFA<I> behaviorGraph,
        State<L> stateVCA,
        Integer locationBG,
        Map<State<L>, Integer> representatives,
        EquivalentStates<L, I> equivalentStates) {
        for (I symbol : getAlphabet()) {
            State<L> newState = getTransition(stateVCA, symbol);
            if (!newState.getCounterValue().isBetween0AndT(threshold)) {
                continue;
            }

            Pair<State<L>, Integer> equivalent = null;
            for (Map.Entry<State<L>, Integer> representative : representatives.entrySet()) {
                if (equivalentStates.areEquivalent(representative.getKey(), newState)) {
                    equivalent = Pair.of(representative.getKey(), representative.getValue());
                }
            }

            boolean recusion = (equivalent == null);
            if (equivalent == null) {
                Integer newInBG = behaviorGraph.addState(isAccepting(newState));
                representatives.put(newState, newInBG);
                equivalent = Pair.of(newState, newInBG);
            }

            behaviorGraph.addTransition(locationBG, symbol, equivalent.getSecond());
            if (recusion) {
                this.toLimitedBehaviorGraphDFS(threshold, behaviorGraph, equivalent.getFirst(), equivalent.getSecond(), representatives, equivalentStates);
            }
        }
    }
}