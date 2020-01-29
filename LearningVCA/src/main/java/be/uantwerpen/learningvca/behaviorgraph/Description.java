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
package be.uantwerpen.learningvca.behaviorgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;

/**
 * The description of a behavior graph.
 * 
 * The description consists of:
 *  - The offset m
 *  - The period k
 *  - A list of TauMapping
 * 
 * In this implementation, we also store:
 *  - The width of the behavior graph
 *  - The initial state
 *  - The set of accepting states
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class Description<I extends Comparable<I>> {
    private final int m;
    private final int k;
    private final int K;
    private final List<TauMapping<I>> tauMappings;
    private StateBG initialState;
    private Collection<StateBG> acceptingStates;
    
    /**
     * The constructor
     * @param offset m
     * @param period k
     * @param width K
     */
    public Description(int offset, int period, int width) {
        this.m = offset;
        this.k = period;
        this.K = width;
        this.tauMappings = new ArrayList<>(offset + period);
        this.acceptingStates = new HashSet<>();
    }

    public void addTauMappings(Collection<TauMapping<I>> mappings) {
        this.tauMappings.addAll(mappings);
    }

    /**
     * Gets the mapping tau
     * @return The mapping
     */
    public List<TauMapping<I>> getTauMappings() {
        return tauMappings;
    }

    /**
     * Gets the offset m
     * @return The offset
     */
    public int getOffset() {
        return m;
    }

    /**
     * Gets the period k
     * @return The period
     */
    public int getPeriod() {
        return k;
    }
    
    /**
     * Sets the initial state of the behavior graph.
     * 
     * The state is designated by the index of the nu mapping and the number associated with the equivalence class
     * @param mapping The index of the mapping
     * @param equivalenceClass The number associated with the equivalence class
     */
    public void setInitialState(int mapping, int equivalenceClass) {
        this.initialState = new StateBG(mapping, equivalenceClass);
    }

    /**
     * Adds an accepting state of the behavior graph.
     * 
     * The state is designated by the index of the nu mapping and the number associated with the equivalence class
     * @param mapping The index of the mapping
     * @param equivalenceClass The number associated with the equivalence class
     */
    public void addAcceptingState(int mapping, int equivalenceClass) {
        this.acceptingStates.add(new StateBG(mapping, equivalenceClass));
    }

    /**
     * Checks if a state is accepting.
     * 
     * The state is designated by the index of the nu mapping and the number associated with the equivalence class
     * @param mapping The index of the mapping
     * @param equivalenceClass The number associated with the equivalence class
     * @return True iff the state is accepting
     */
    public boolean isAcceptingState(int mapping, int equivalenceClass) {
        return isAcceptingState(new StateBG(mapping, equivalenceClass));
    }

    /**
     * Checks if a state is accepting.
     * @param state The state
     * @return True iff the state is accepting
     */
    public boolean isAcceptingState(StateBG state) {
        return this.acceptingStates.contains(state);
    }

    /**
     * @return The initial state
     */
    public StateBG getInitialState() {
        return initialState;
    }

    /**
     * Constructs a m-VCA from a description that has no period.
     * @param alphabet The input alphabet of the VCA
     * @return
     */
    private VCA<?, I> toVCANoPeriod(VPDAlphabet<I> alphabet) {
        DefaultVCA<I> vca = new DefaultVCA<>(alphabet, m);
        // We create Q
        ArrayList<Location> states = new ArrayList<>(K);
        for (int i = 1 ; i <= K ; i++) {
            states.add(vca.addLocation());
        }

        // q_0
        vca.setInitialLocation(states.get(initialState.getEquivalenceClass() - 1));

        // F
        acceptingStates.stream().
            map(state -> states.get(state.getEquivalenceClass() - 1)).
            forEach(state -> state.setIsAccepting(true))
        ;

        // delta
        for (int i = 1 ; i <= K ; i++) {
            for (I symbol : alphabet) {
                Location start = states.get(i - 1);
                // For every transition function
                for (int j = 0 ; j < m ; j++) {
                    int tau = tauMappings.get(j).getTransition(i, symbol);
                    if (tau == -1) {
                        continue;
                    }
                    vca.setSuccessor(start, j, symbol, states.get(tau - 1));
                }
            }
        }

        return vca;
    }

    /**
     * Constructs a m-VCA accepting the same language as the behavior graph described.
     * @param alphabet The input alphabet of the VCA
     * @return A m-VCA
     */
    public VCA<?, I> toVCA(VPDAlphabet<I> alphabet) {
        if (getPeriod() == 0) {
            return toVCANoPeriod(alphabet);
        }

        // See definition 2.1
        DefaultVCA<I> vca = new DefaultVCA<>(alphabet, m);

        // We create Q
        ArrayList<ArrayList<Location>> states = new ArrayList<>(K);
        for (int i = 1 ; i <= K ; i++) {
            ArrayList<Location> s = new ArrayList<>(k);
            for (int j = 0 ; j <= k - 1 ; j++) {
                s.add(vca.addLocation());
            }
            states.add(s);
        }

        // q_0
        vca.setInitialLocation(states.get(initialState.getEquivalenceClass() - 1).get(k - 1));

        // We set F
        for (StateBG stateBG : acceptingStates) {
            for (int j = 0 ; j <= k - 1 ; j++) {
                states.get(stateBG.getEquivalenceClass() - 1).get(j).setIsAccepting(true);
            }
        }

        // delta
        for (int i = 1 ; i <= K ; i++) {
            for (int r = 0 ; r <= k - 1 ; r++) {
                for (I a : alphabet) {
                    Location start = states.get(i - 1).get(r);
                    // Every transition function except delta_m
                    for (int j = 0 ; j <= m - 1 ; j++) {
                        int tau = tauMappings.get(j).getTransition(i, a);
                        if (tau == -1) {
                            continue;
                        }
                        if (alphabet.isCallSymbol(a)) {
                            if (j == m - 1) {
                                vca.setCallSuccessor(start, j, a, states.get(tau - 1).get(0));
                            }
                            else {
                                vca.setCallSuccessor(start, j, a, states.get(tau - 1).get(k - 1));
                            }
                        }
                        else if (alphabet.isReturnSymbol(a)) {
                            vca.setReturnSuccessor(start, j, a, states.get(tau - 1).get(k - 1));
                        }
                        else {
                            vca.setInternalSuccessor(start, j, a, states.get(tau - 1).get(k - 1));
                        }
                    }

                    // delta_m
                    int tau = tauMappings.get(m + r).getTransition(i, a);
                    if (tau == -1) {
                        continue;
                    }
                    // Math.floorMod returns the modulus (so, it is always positive)
                    if (alphabet.isCallSymbol(a)) {
                        vca.setCallSuccessor(start, m, a, states.get(tau - 1).get(Math.floorMod(r + 1, k)));
                    }
                    else if (alphabet.isReturnSymbol(a)) {
                        vca.setReturnSuccessor(start, m, a, states.get(tau - 1).get(Math.floorMod(r - 1, k)));
                    }
                    else {
                        vca.setInternalSuccessor(start, m, a, states.get(tau - 1).get(r));
                    }
                }
            }
        }

        return vca;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Offset: " + getOffset() + "\n");
        builder.append("Period: " + getPeriod() + "\n");
        if (tauMappings.size() == 0) {
            builder.append("Empty description");
        }
        else {
            for (int i = 0 ; i < tauMappings.size() ; i++) {
                builder.append("Tau mapping number " + i + ":\n");
                builder.append(tauMappings.get(i));
            }
        }
        return builder.toString();
    }
}