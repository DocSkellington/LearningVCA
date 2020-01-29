/*
 * LearningVCA - An implementation of an active learning algorithm for Visibly One-Counter Automata
 * Copyright (C) 2020 University of Mons and University of Antwerp
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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