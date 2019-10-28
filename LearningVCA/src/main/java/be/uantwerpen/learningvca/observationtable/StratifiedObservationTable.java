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
    public int numberOfSuffixes(int level);

    /**
     * @param level The level
     * @return The suffixes in the level
     */
    public List<Word<I>> getSuffixes(int level);

    @Override
    default public List<List<Row<I>>> addSuffixes(Collection<? extends Word<I>> newSuffixes, MembershipOracle<I, D> oracle) {
        throw new UnsupportedOperationException("StratifiedObservationTable: you must specify the levels of the new suffixes");
    }

    /**
     * Adds a suffix in the given level.
     * @param suffix The suffix
     * @param suffixLevel The level of the suffix
     * @param oracle The membership oracle
     * @return A list of equivalence classes of unclosed rows
     */
    default public List<List<Row<I>>> addSuffix(Word<I> suffix, int suffixLevel, MembershipOracle<I, D> oracle) {
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
    public List<List<Row<I>>> addSuffixes(List<? extends Word<I>> newSuffixes, List<Integer> newSuffixesLevels, MembershipOracle<I, D> oracle);

    /**
     * @return The current level limit (t)
     */
    public int getLevelLimit();

    /**
     * Constructs a t-VCA from this table.
     * 
     * If the table is not closed and consistent, the VCA might be ill-formed
     * @return The t-VCA
     */
    public VCA<I> toVCA();

    /**
     * Constructs a behavior graph limited up to the maximum level of this table.
     * @return The limited behavior graph this table defines
     */
    public LimitedBehaviorGraph<I> toLimitedBehaviorGraph();

    @Override
    public StratifiedObservationRow<I> getRow(Word<I> word);
}