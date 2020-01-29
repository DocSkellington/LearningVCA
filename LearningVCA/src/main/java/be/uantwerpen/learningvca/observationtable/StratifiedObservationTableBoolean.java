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
package be.uantwerpen.learningvca.observationtable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * A stratified observation table that stores booleans.
 * 
 * @param <I> The input alphabet type
 * @author Gaëtan Staquet
 */
public class StratifiedObservationTableBoolean<I extends Comparable<I>>
        extends AbstractStratifiedObservationTable<I, Boolean> {
    public StratifiedObservationTableBoolean(VPDAlphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public VCA<?, I> toVCA() {
        DefaultVCA<I> vca = new DefaultVCA<>(alphabet, getLevelLimit());
        Map<StratifiedObservationRow<I>, Location> rowToState = new HashMap<>();
        List<List<StratifiedObservationRow<I>>> representatives = getUniqueRepresentatives();

        for (int level = 0; level <= getLevelLimit(); level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : representatives.get(level)) {
                Location qi = null;
                if (level == 0 && shortPrefixRow.getLabel() == Word.<I>epsilon()) {
                    qi = vca.addInitialLocation(cellContents(shortPrefixRow, 0));
                } else {
                    qi = vca.addLocation(cellContents(shortPrefixRow, 0));
                }
                rowToState.put(shortPrefixRow, qi);
            }
        }

        // We create the transitions
        for (int level = 0; level <= getLevelLimit(); level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : representatives.get(level)) {
                Location startingState = rowToState.get(shortPrefixRow);
                for (int i = 0; i < alphabet.size(); i++) {
                    I symbol = alphabet.getSymbol(i);
                    StratifiedObservationRow<I> successor = shortPrefixRow.getSuccessor(i);
                    if (successor == null) {
                        continue;
                    }

                    StratifiedObservationRow<I> equivalenceClass = getRepresentativeRow(successor);
                    if (equivalenceClass == null) {
                        continue;
                    }

                    Location targetState = rowToState.get(equivalenceClass);
                    vca.setSuccessor(startingState, level, symbol, targetState);
                }
            }
        }

        return vca;
    }

    @Override
    public LimitedBehaviorGraph<I> toLimitedBehaviorGraph() {
        LimitedBehaviorGraph<I> limitedBehaviorGraph = new LimitedBehaviorGraph<>(alphabet, getLevelLimit());
        Map<StratifiedObservationRow<I>, Integer> rowToState = new HashMap<>();
        List<List<StratifiedObservationRow<I>>> representatives = getUniqueRepresentatives();

        for (int level = 0; level <= getLevelLimit(); level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : representatives.get(level)) {
                Integer qi = null;
                if (level == 0 && shortPrefixRow.getLabel() == Word.<I>epsilon()) {
                    qi = limitedBehaviorGraph.addInitialState(cellContents(shortPrefixRow, 0));
                } else {
                    qi = limitedBehaviorGraph.addState(cellContents(shortPrefixRow, 0));
                }
                limitedBehaviorGraph.setStateLevel(qi, level);
                rowToState.put(shortPrefixRow, qi);
            }
        }

        // We create the transitions
        for (int level = 0; level <= getLevelLimit(); level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : representatives.get(level)) {
                Integer startingState = rowToState.get(shortPrefixRow);
                for (int i = 0; i < alphabet.size(); i++) {
                    I symbol = alphabet.getSymbol(i);
                    StratifiedObservationRow<I> successor = shortPrefixRow.getSuccessor(i);
                    if (successor == null) {
                        continue;
                    }

                    StratifiedObservationRow<I> equivalenceClass = getRepresentativeRow(successor);
                    if (equivalenceClass == null) {
                        continue;
                    }

                    Integer targetState = rowToState.get(equivalenceClass);
                    limitedBehaviorGraph.setTransition(startingState, symbol, targetState);
                }
            }
        }

        return limitedBehaviorGraph;
    }

}