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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.MutableObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.words.Word;

/**
 * A stratified observation table
 * @param <I> The input alphabet type
 * @param <D> The type of the information to store in the table
 * @author Gaëtan Staquet
 */
public interface StratifiedObservationTable<I extends Comparable<I>, D> extends MutableObservationTable<I, D> {
    /**
     * Gets the number of suffixes in the given level
     * @param level The level
     * @return The number of suffixes in the level
     */
    int numberOfSuffixes(int level);

    /**
     * @param level The level
     * @return The suffixes in the level
     */
    List<Word<I>> getSuffixes(int level);

    /**
     * @param level The level
     * @return The rows in the level
     */
    Collection<StratifiedObservationRow<I>> getAllRows(int level);

    /**
     * @param level The level
     * @return The short prefix rows in the level
     */
    Collection<StratifiedObservationRow<I>> getShortPrefixRows(int level);

    /**
     * @param level The level
     * @return The long prefix rows in the level
     */
    Collection<StratifiedObservationRow<I>> getLongPrefixRows(int level);

    @Override
    default List<List<Row<I>>> addSuffixes(Collection<? extends Word<I>> newSuffixes, MembershipOracle<I, D> oracle) {
        throw new UnsupportedOperationException("StratifiedObservationTable: you must specify the levels of the new suffixes");
    }

    /**
     * Adds a suffix in the given level.
     * @param suffix The suffix
     * @param suffixLevel The level of the suffix
     * @param oracle The membership oracle
     * @return A list of equivalence classes of unclosed rows
     */
    default List<List<Row<I>>> addSuffix(Word<I> suffix, int suffixLevel, MembershipOracle<I, D> oracle) {
        return addSuffixes(Collections.singletonList(suffix), Collections.singletonList(suffixLevel), oracle);
    }

    /**
     * Adds suffixes to the list of distinguishing suffixes on the given levels.
     * 
     * This does not increase the maximum level of the table.
     * @param newSuffixes The suffixes
     * @param newSuffixesLevels The level in which each suffix must be added
     * @param oracle The membership oracle
     * @return A list of equivalence classes of unclosed rows
     */
    List<List<Row<I>>> addSuffixes(List<? extends Word<I>> newSuffixes, List<Integer> newSuffixesLevels, MembershipOracle<I, D> oracle);

    /**
     * @return The current level limit (t)
     */
    int getLevelLimit();

    /**
     * Constructs a t-VCA from this table.
     * 
     * If the table is not closed and consistent, the VCA might be ill-formed
     * @return The t-VCA
     */
    VCA<?, I> toVCA();

    /**
     * Constructs a behavior graph limited up to the maximum level of this table.
     * @return The limited behavior graph this table defines
     */
    LimitedBehaviorGraph<I> toLimitedBehaviorGraph();

    @Override
    StratifiedObservationRow<I> getRow(Word<I> word);
}