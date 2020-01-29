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
package be.uantwerpen.learningvca.oracles;

import java.util.Collection;

import javax.annotation.Nullable;

import be.uantwerpen.learningvca.vca.ProductVCA;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.Word;

/**
 * Equivalence query between two VCAs
 * 
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class EquivalenceVCAOracle<I> implements EquivalenceOracle<VCA<?, I>, I, Boolean> {
    private final VCA<?, I> sul;

    public EquivalenceVCAOracle(VCA<?, I> sul) {
        this.sul = sul;
    }

    @Override
    @Nullable
    public DefaultQuery<I, Boolean> findCounterExample(VCA<?, I> hypothesis, Collection<? extends I> inputs) {
        VCA<?, I> productVCA = new ProductVCA<>(sul.getAlphabet(), sul, hypothesis, AcceptanceCombiner.XOR);
        Word<I> counterexample = productVCA.getAcceptedWord();
        if (counterexample == null) {
            return null;
        }
        return new DefaultQuery<>(counterexample);
    }

}