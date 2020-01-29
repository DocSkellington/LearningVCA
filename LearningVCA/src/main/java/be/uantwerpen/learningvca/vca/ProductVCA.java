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

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.Pair;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.VPDAlphabet;

/**
 * The state product of two VCA. Acceptance semantics of the product automaton depends on the given AcceptanceCombiner.
 * @param <L1> The location type of the first VCA
 * @param <L2> The location type of the second VCA
 * @param <I> The input alphabet type
 */
public class ProductVCA<L1, L2, I> extends AbstractVCA<Pair<L1, L2>, I> {

    private final VCA<L1, I> vca1;
    private final VCA<L2, I> vca2;
    private final AcceptanceCombiner combiner;

    public ProductVCA(VPDAlphabet<I> alphabet, VCA<L1, I> vca1, VCA<L2, I> vca2, AcceptanceCombiner combiner) {
        super(alphabet);
        this.vca1 = vca1;
        this.vca2 = vca2;
        this.combiner = combiner;
    }

    @Override
    public Pair<L1, L2> getInitialLocation() {
        return Pair.of(vca1.getInitialLocation(), vca2.getInitialLocation());
    }

    @Override
    public List<Pair<L1, L2>> getLocations() {
        List<Pair<L1, L2>> locations = new ArrayList<>(size());

        for (L1 l1 : vca1.getLocations()) {
            for (L2 l2 : vca2.getLocations()) {
                locations.add(Pair.of(l1, l2));
            }
            locations.add(Pair.of(l1, null));
        }
        for (L2 l2 : vca2.getLocations()) {
            locations.add(Pair.of(null, l2));
        }
        locations.add(Pair.of(null,  null));

        return locations;
    }

    @Override
    public int getThreshold() {
        return Math.max(vca1.getThreshold(), vca2.getThreshold());
    }

    @Override
    public boolean isAcceptingLocation(Pair<L1, L2> loc) {
        return combiner.combine(vca1.isAcceptingLocation(loc.getFirst()), vca2.isAcceptingLocation(loc.getSecond()));
    }

    @Override
    public Pair<L1, L2> getCallSuccessor(Pair<L1, L2> loc, I symbol, int counterValue) {
        final L1 succ1 = vca1.getCallSuccessor(loc.getFirst(), symbol, counterValue);
        final L2 succ2 = vca2.getCallSuccessor(loc.getSecond(), symbol, counterValue);
        return Pair.of(succ1, succ2);
    }

    @Override
    public Pair<L1, L2> getReturnSuccessor(Pair<L1, L2> loc, I symbol, int counterValue) {
        final L1 succ1 = vca1.getReturnSuccessor(loc.getFirst(), symbol, counterValue);
        final L2 succ2 = vca2.getReturnSuccessor(loc.getSecond(), symbol, counterValue);
        return Pair.of(succ1, succ2);
    }

    @Override
    public Pair<L1, L2> getInternalSuccessor(Pair<L1, L2> loc, I symbol, int counterValue) {
        final L1 succ1 = vca1.getInternalSuccessor(loc.getFirst(), symbol, counterValue);
        final L2 succ2 = vca2.getInternalSuccessor(loc.getSecond(), symbol, counterValue);
        return Pair.of(succ1, succ2);
    }

    @Override
    public int getLocationId(Pair<L1, L2> loc) {
        return vca1.getLocationId(loc.getFirst()) * vca2.size() + vca2.getLocationId(loc.getSecond());
    }

    @Override
    public int size() {
        return vca1.size() * vca2.size() + vca1.size() + vca2.size() + 1;
    }

}