package be.uantwerpen.learningvca.observationtable;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import be.uantwerpen.learningvca.util.ComputeCounterValue;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.Inconsistency;
import de.learnlib.datastructure.observationtable.MutableObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * A stratified observation table.
 * 
 * The implementation is inspired from the implementation of GenericObservationTable and AbstractObservationTable (from LearnLib).
 * @author Gaëtan Staquet
 */
public class StratifiedObservationTable<I, D> implements MutableObservationTable<I, D> {
    private final VPDAlphabet<I> alphabet;
    // A prefix (or representative) is short if it is in the upper half of the observation table.
    // That is, it is short if it one of the row used for the creation of the automaton
    private final List<List<StratifiedObservationRow<I>>> shortPrefixRows;
    private final List<StratifiedObservationRow<I>> allLongPrefixRows;
    private final List<StratifiedObservationRow<I>> allPrefixRows;

    private final Map<Word<I>, StratifiedObservationRow<I>> rowMap;

    // A suffix is a separator
    private final List<List<Word<I>>> suffixes;

    private final List<List<D>> allRowContents;
    private final Map<List<D>, Integer> rowContentsIdsMap;
    
    // t
    private int maxLevel;

    private boolean initialConsistencyCheckRequired;

    public StratifiedObservationTable(VPDAlphabet<I> alphabet) {
        this.alphabet = alphabet;
        this.shortPrefixRows = new LinkedList<>();
        this.allLongPrefixRows = new LinkedList<>();
        this.allPrefixRows = new LinkedList<>();

        this.rowMap = new HashMap<>();

        this.suffixes = new LinkedList<>();

        this.allRowContents = new LinkedList<>();

        this.rowContentsIdsMap = new HashMap<>();

        this.maxLevel = -1;

        this.initialConsistencyCheckRequired = false;
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAccessSequence(Word<I> word) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
    }

    @Override
    public Collection<Row<I>> getShortPrefixRows() {
        List<Row<I>> list = new LinkedList<>();
        for (List<StratifiedObservationRow<I>> l : shortPrefixRows) {
            list.addAll(l);
        }
        return list;
    }

    @Override
    public Collection<Row<I>> getLongPrefixRows() {
        List<Row<I>> list = new ArrayList<>(allLongPrefixRows.size());
        list.addAll(allLongPrefixRows);
        return list;
    }

    @Override
    public StratifiedObservationRow<I> getRow(int idx) {
        return allPrefixRows.get(idx);
    }

    @Override
    public StratifiedObservationRow<I> getRow(Word<I> prefix) {
        return rowMap.get(prefix);
    }

    @Override
    public int numberOfDistinctRows() {
        return allRowContents.size();
    }

    @Override
    public List<Word<I>> getSuffixes() {
        List<Word<I>> list = new LinkedList<>();
        for (List<Word<I>> l : suffixes) {
            list.addAll(l);
        }
        return list;
    }

    @Override
    public List<D> rowContents(Row<I> row) {
        return allRowContents.get(row.getRowContentId());
    }

    @Override
    public List<List<Row<I>>> initialize(List<Word<I>> initialShortPrefixes, List<Word<I>> initialSuffixes,
            MembershipOracle<I, D> oracle) {
        // At the very beginning, we have that t = 0
        increaseLevelLimit(0);
        // So, the suffixes are all in S_0
        for (Word<I> suffix : initialSuffixes) {
            if (!this.suffixes.get(0).contains(suffix)) {
                this.suffixes.get(0).add(suffix);
            }
        }

        int numSuffixes = initialSuffixes.size();

        List<DefaultQuery<I, D>> queries = new ArrayList<>(); // TODO estimate number of queries

        // We add the short prefixes
        for (Word<I> shortPrefix : initialShortPrefixes) {
            if (createShortPrefixRow(shortPrefix) == null) {
                throw new InvalidParameterException("StratifiedObservationTable: invalid prefix: the prefix " + shortPrefix + " has a height exceeding " + maxLevel);
            }
            createQueries(queries, shortPrefix, initialSuffixes);
        }

        // We add the missing long prefixes, if needed
        // Since t = 0, we know that every short prefix has a counter value of 0
        shortPrefixRows.get(0).stream().forEach(row -> this.createLongPrefixesRows(row, queries));

        oracle.processQueries(queries);

        Iterator<DefaultQuery<I, D>> queryIt = queries.iterator();

        // We process the queries for short prefixes
        // That is, we set the contents to each short prefix row
        for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(0)) {
            List<D> rowContents = new ArrayList<>(numSuffixes);
            fetchQueriesResults(queryIt, rowContents, numSuffixes);
            if (!processContents(shortPrefixRow, rowContents)) {
                initialConsistencyCheckRequired = true;
            }
        }

        int numberOfDistinctShortPrefixRows = numberOfDistinctRows();

        List<List<Row<I>>> unclosed = new ArrayList<>();

        // We seek the unclosed rows
        // We also process the queries for long prefixes
        for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(0)) {
            for (int i = 0 ; i < alphabet.size() ; i++) {
                StratifiedObservationRow<I> successorRow = shortPrefixRow.getSuccessor(i);
                if (successorRow == null || successorRow.isShortPrefixRow()) {
                    // We ignore this successor if it is invalid or a short prefix
                    continue;
                }
                List<D> rowContents = new ArrayList<>(numSuffixes);
                fetchQueriesResults(queryIt, rowContents, numSuffixes);
                if (processContents(successorRow, rowContents)) {
                    unclosed.add(new ArrayList<>());
                }

                int id = successorRow.getRowContentId();
                if (id >= numberOfDistinctShortPrefixRows) {
                    unclosed.get(id - numberOfDistinctShortPrefixRows).add(successorRow);
                }
            }
        }

        return unclosed;
    }
    
    /**
     * Creates queries for the prefix and the suffixes and adds them to the given list
     * @param queries The list to fill
     * @param prefix The prefix
     * @param suffixes The list of suffixes
     */
    private void createQueries(List<DefaultQuery<I, D>> queries, Word<I> prefix, List<Word<I>> suffixes) {
        for (Word<I> suffix : suffixes) {
            queries.add(new DefaultQuery<>(prefix, suffix));
        }
    }

    /**
     * Fetchs the given number of results from queries and adds them to the given list of outputs.
     * 
     * The iterator is advanced accordingly.
     * @param queryIt The query iterator
     * @param output The output list
     * @param numSuffixes The number of suffixes
     */
    private void fetchQueriesResults(Iterator<DefaultQuery<I, D>> queryIt, List<D> output, int numSuffixes) {
        System.out.println("Fetching " + numSuffixes + " queries");
        for (int j = 0 ; j < numSuffixes ; j++) {
            DefaultQuery<I, D> query = queryIt.next();
            output.add(query.getOutput());
        }
    }

    /**
     * Associates the contents to the the row.
     * 
     * If the row contents is not yet registered, it gets registered.
     * @param row The row
     * @param rowContents The contents
     * @return True iff the row contents were added in allRowContents
     */
    private boolean processContents(StratifiedObservationRow<I> row, List<D> rowContents) {
        boolean added = false;
        Integer contentID = rowContentsIdsMap.get(rowContents);
        // If the row is not yet registered, we register it
        if (contentID == null) {
            added = true;
            contentID = numberOfDistinctRows();
            rowContentsIdsMap.put(rowContents, contentID);
            allRowContents.add(rowContents);
        }
        row.setRowContentId(contentID);
        return added;
    }

    /**
     * Creates a short prefix row
     * @param shortPrefix The short prefix
     * @return A row for the short prefix
     */
    private StratifiedObservationRow<I> createShortPrefixRow(Word<I> shortPrefix) {
        StratifiedObservationRow<I> row = new StratifiedObservationRow<>(shortPrefix, allPrefixRows.size(), alphabet.size());
        int counterValue = ComputeCounterValue.computeCounterValue(shortPrefix, alphabet, maxLevel);

        if (counterValue == -1 || counterValue > maxLevel) {
            return null;
        }

        rowMap.put(shortPrefix, row);
        shortPrefixRows.get(counterValue).add(row);

        allPrefixRows.add(row);
        return row;
    }

    /**
     * Creates every possible long prefix based on the short prefix and the queries needed to fill this rows.
     * @param shortPrefixRow The row of the short prefix to use
     * @param queries The list of queries to fill
     */
    private void createLongPrefixesRows(StratifiedObservationRow<I> shortPrefixRow, List<DefaultQuery<I, D>> queries) {
        Word<I> shortPrefix = shortPrefixRow.getLabel();
        for (int i = 0 ; i < alphabet.size() ; i++) {
            I symbol = alphabet.getSymbol(i);
            if ((i == 0 && alphabet.isReturnSymbol(symbol)) || (i == maxLevel && alphabet.isCallSymbol(symbol))) {
                continue;
            }
            int counterValue = ComputeCounterValue.computeCounterValue(shortPrefix, alphabet);
            Word<I> longPrefix = shortPrefix.append(symbol);

            StratifiedObservationRow<I> successorRow = getRow(longPrefix);

            if (successorRow == null) {
                // We create the long prefix row
                successorRow = createLongPrefixRow(longPrefix);
                if (successorRow != null) {
                    createQueries(queries, longPrefix, suffixes.get(counterValue + ComputeCounterValue.signOf(symbol, alphabet)));
                }
            }

            shortPrefixRow.setSuccessor(i, successorRow);
        }
    }

    /**
     * Creates a row for a long prefix.
     * @param longPrefix The long prefix
     * @return A row for the long prefix or null if the long prefix is invalid
     */
    private StratifiedObservationRow<I> createLongPrefixRow(Word<I> longPrefix) {
        StratifiedObservationRow<I> row = new StratifiedObservationRow<>(longPrefix, allPrefixRows.size());
        int counterValue = ComputeCounterValue.computeCounterValue(longPrefix, alphabet, maxLevel);
        if (counterValue == -1 || counterValue > maxLevel) {
            return null;
        }
        rowMap.put(longPrefix, row);
        allPrefixRows.add(row);
        allLongPrefixRows.add(row);
        return row;
    }

    @Override
    public boolean isInitialized() {
        return allRowContents.size() != 0;
    }

    @Override
    public boolean isInitialConsistencyCheckRequired() {
        return initialConsistencyCheckRequired;
    }

    @Override
    public List<List<Row<I>>> addSuffixes(Collection<? extends Word<I>> newSuffixes, MembershipOracle<I, D> oracle) {
        throw new UnsupportedOperationException("StratifiedObservationTable: you must specify the levels of the new suffixes");
    }

    /**
     * Adds a suffix in the given level.
     * @param suffix The suffix
     * @param suffixLevel The level of the suffix
     * @param oracle The membership oracle
     * @return A list of equivalence classes of unclosed rows
     */
    public List<List<Row<I>>> addSuffix(Word<I> suffix, int suffixLevel, MembershipOracle<I, D> oracle) {
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
    public List<List<Row<I>>> addSuffixes(List<? extends Word<I>> newSuffixes, List<Integer> newSuffixesLevels, MembershipOracle<I, D> oracle) {
        if (newSuffixes.size() != newSuffixesLevels.size()) {
            throw new InvalidParameterException("StratifiedObservationTable: addSuffixes: there must be the same number of new suffixes and levels");
        }

        List<List<Word<I>>> newSuffixesList = new ArrayList<>(maxLevel + 1);
        // We keep only the really new suffixes and we store them by level
        for (int level = 0 ; level <= maxLevel ; level++) {
            List<Word<I>> l = new ArrayList<>(newSuffixes.size());
            for (int j = 0 ; j < newSuffixes.size() ; j++) {
                if (newSuffixesLevels.get(j) == level && !suffixes.get(level).contains(newSuffixes.get(j))) {
                    l.add(newSuffixes.get(j));
                }
            }
            newSuffixesList.add(l);
        }

        System.out.println("New suffixes: ");
        for (int level = 0 ; level <= maxLevel ; level++) {
            System.out.println(level + " : " + newSuffixesList.get(level));
        }

        // We create the queries needed to close every row
        List<DefaultQuery<I, D>> queries = new ArrayList<>();

        for (int level = 0 ; level <= maxLevel ; level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(level)) {
                System.out.println("Creating queries to close " + shortPrefixRow.getLabel() + " with " + newSuffixesList.get(level));
                createQueries(queries, shortPrefixRow.getLabel(), newSuffixesList.get(level));
            }
        }

        for (StratifiedObservationRow<I> longPrefixRow : allLongPrefixRows) {
            Word<I> longPrefix = longPrefixRow.getLabel();
            System.out.println("Creating queries to close " + longPrefix + " with " + newSuffixesList.get(ComputeCounterValue.computeCounterValue(longPrefix, alphabet)));
            createQueries(queries, longPrefix, newSuffixesList.get(ComputeCounterValue.computeCounterValue(longPrefix, alphabet)));
        }

        oracle.processQueries(queries);

        Iterator<DefaultQuery<I, D>> queryIt = queries.iterator();

        // We start by updating the short prefixes
        for (int level = 0 ; level <= maxLevel ; level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(level)) {
                List<D> rowContents = allRowContents.get(shortPrefixRow.getRowContentId());
                System.out.println("Updating short prefix " + shortPrefixRow.getLabel());

                if (rowContents.size() == suffixes.get(level).size()) {
                    // This row contents have not yet been modified
                    // So, we just update the row contents
                    System.out.println("Old size");
                    rowContentsIdsMap.remove(rowContents);
                    System.out.println("Before : " + rowContents);
                    fetchQueriesResults(queryIt, rowContents, newSuffixesList.get(level).size());
                    System.out.println("After : " + rowContents);
                    rowContentsIdsMap.put(rowContents, shortPrefixRow.getRowContentId());
                }
                else {
                    // This row contents have already been modified
                    // We need to check if this row must still use this row contents
                    System.out.println("New size");
                    System.out.println("Before : " + rowContents);
                    List<D> newRowContents = new ArrayList<>(suffixes.get(level).size() + newSuffixesList.size());
                    newRowContents.addAll(rowContents.subList(0, suffixes.get(level).size()));
                    fetchQueriesResults(queryIt, newRowContents, newSuffixesList.get(level).size());
                    System.out.println("After : " + newRowContents);
                    processContents(shortPrefixRow, newRowContents);
                }
            }
        }

        List<List<Row<I>>> unclosed = new ArrayList<>();
        int numberOfDistinctShortPrefixRows = numberOfDistinctRows();

        // Then, the long prefix rows
        for (StratifiedObservationRow<I> longPrefixRow : allLongPrefixRows) {
            int rowContentId = longPrefixRow.getRowContentId();
            List<D> rowContents = allRowContents.get(rowContentId);
            int counterValue = ComputeCounterValue.computeCounterValue(longPrefixRow.getLabel(), alphabet);
            if (rowContents.size() == suffixes.get(counterValue).size()) {
                // This row contents have not yet been modified
                rowContentsIdsMap.remove(rowContents);
                fetchQueriesResults(queryIt, rowContents, newSuffixesList.get(counterValue).size());
                rowContentsIdsMap.put(rowContents, rowContentId);
            }
            else {
                // This row contents have already been modified
                List<D> newRowContents = new ArrayList<>(suffixes.get(counterValue).size() + newSuffixesList.size());
                newRowContents.addAll(rowContents.subList(0, suffixes.get(counterValue).size()));
                fetchQueriesResults(queryIt, newRowContents, newSuffixesList.get(counterValue).size());
                if (processContents(longPrefixRow, newRowContents)) {
                    unclosed.add(new ArrayList<>());
                }
                
                if (rowContentId >= numberOfDistinctShortPrefixRows) {
                    unclosed.get(rowContentId - numberOfDistinctShortPrefixRows).add(longPrefixRow);
                }
            }
        }

        suffixes.addAll(newSuffixesList);

        return unclosed;
    }

    @Override
    public List<List<Row<I>>> addShortPrefixes(List<? extends Word<I>> shortPrefixes, MembershipOracle<I, D> oracle) {
        List<Row<I>> toShortPrefixRows = new ArrayList<>(shortPrefixes.size());

        for (Word<I> shortPrefix : shortPrefixes) {
            int counterValue = ComputeCounterValue.computeCounterValue(shortPrefix, alphabet);
            increaseLevelLimit(counterValue);
            StratifiedObservationRow<I> shortPrefixRow = rowMap.get(shortPrefix);
            if (shortPrefixRow == null) {
                // If the short prefix is not yet in the table, we create a new row and close it
                List<DefaultQuery<I, D>> queries = new ArrayList<>();
                shortPrefixRow = createShortPrefixRow(shortPrefix);
                createQueries(queries, shortPrefix, suffixes.get(counterValue));
                createLongPrefixesRows(shortPrefixRow, queries);

                oracle.processQueries(queries);

                Iterator<DefaultQuery<I, D>> queryIt = queries.iterator();
                // Short prefix
                List<D> rowContents = new ArrayList<>(suffixes.get(counterValue).size());
                fetchQueriesResults(queryIt, rowContents, suffixes.get(counterValue).size());
                processContents(shortPrefixRow, rowContents);

                // Long prefixes
                for (int i = 0 ; i < alphabet.size() ; i++) {
                    StratifiedObservationRow<I> successorRow = shortPrefixRow.getSuccessor(i);
                    if (successorRow == null || successorRow.isShortPrefixRow()) {
                        // We ignore this successor if it is invalid or a short prefix
                        continue;
                    }
                    int numSuffixes = suffixes.get(counterValue + ComputeCounterValue.signOf(alphabet.getSymbol(i), alphabet)).size();
                    rowContents = new ArrayList<>(numSuffixes);
                    fetchQueriesResults(queryIt, rowContents, numSuffixes);
                    processContents(successorRow, rowContents);
                }

                // We must set the successor of the longest prefix of shortPrefix
                getRow(shortPrefix.subWord(0, shortPrefix.size() - 1)).setSuccessor(alphabet.getSymbolIndex(shortPrefix.lastSymbol()), shortPrefixRow);
            }
            else if (!shortPrefixRows.get(counterValue).contains(shortPrefixRow)) {
                toShortPrefixRows.add(shortPrefixRow);
            }
        }

        // TODO am I supposed to find every unclosed row?
        return toShortPrefixes(toShortPrefixRows, oracle);
    }

    /**
     * Transforms a long prefix row into a short prefix row.
     * 
     * It does not create the new long prefix rows.
     * 
     * If the row is already a short prefix, nothing happens
     * @param row The row
     */
    private void makeShort(StratifiedObservationRow<I> row) {
        if (row.isShortPrefixRow()) {
            return;
        }

        Word<I> longPrefix = row.getLabel();
        allLongPrefixRows.remove(row);

        int counterValue = ComputeCounterValue.computeCounterValue(longPrefix, alphabet);
        shortPrefixRows.get(counterValue).add(row);
        row.makeShort(alphabet.size());
    }

    @Override
    public List<List<Row<I>>> toShortPrefixes(List<Row<I>> longPrefixRows, MembershipOracle<I, D> oracle) {
        List<StratifiedObservationRow<I>> freshShortPrefixRows = new ArrayList<>(); // The short prefix rows without contents

        // We keep only the rows with missing contents
        for (Row<I> r : longPrefixRows) {
            final StratifiedObservationRow<I> row = getRow(r.getRowId());
            if (row.isShortPrefixRow()) {
                if (row.hasContents()) {
                    continue;
                }
                freshShortPrefixRows.add(row);
            }
            else {
                makeShort(row);
                freshShortPrefixRows.add(row);
            }
        }

        // We fill these rows
        for (StratifiedObservationRow<I> shortPrefixRow : freshShortPrefixRows) {
            Word<I> shortPrefix = shortPrefixRow.getLabel();
            int counterValue = ComputeCounterValue.computeCounterValue(shortPrefix, alphabet);

            List<DefaultQuery<I, D>> queries = new ArrayList<>();
            
            // We create the long prefixe rows and the queries
            createQueries(queries, shortPrefix, suffixes.get(counterValue));
            createLongPrefixesRows(shortPrefixRow, queries);

            oracle.processQueries(queries);

            Iterator<DefaultQuery<I, D>> queryIt = queries.iterator();
            // Short prefix
            List<D> rowContents = new ArrayList<>(suffixes.get(counterValue).size());
            fetchQueriesResults(queryIt, rowContents, suffixes.get(counterValue).size());
            processContents(shortPrefixRow, rowContents);

            // Long prefixes
            for (int i = 0 ; i < alphabet.size() ; i++) {
                StratifiedObservationRow<I> successorRow = shortPrefixRow.getSuccessor(i);
                if (successorRow == null || successorRow.isShortPrefixRow()) {
                    // We ignore this successor if it is invalid or a short prefix
                    continue;
                }
                int numSuffixes = suffixes.get(counterValue + ComputeCounterValue.signOf(alphabet.getSymbol(i), alphabet)).size();
                rowContents = new ArrayList<>(numSuffixes);
                fetchQueriesResults(queryIt, rowContents, numSuffixes);
                processContents(successorRow, rowContents);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<List<Row<I>>> addAlphabetSymbol(I symbol, MembershipOracle<I, D> oracle) {
        throw new UnsupportedOperationException("StratifiedObservationTable: impossible to add an alphabet symbol");
    }

    /**
     * Increases the max level of this table.
     * 
     * That is, words are allowed to have a counter value up to the new limit.
     * The empty word is automatically added to the new suffixes.
     * 
     * If the new limit is lower than the current limit, nothing happens
     * @param newLimit The new limit
     */
    private void increaseLevelLimit(int newLimit) {
        if (newLimit > maxLevel) {
            maxLevel = newLimit;
            while (shortPrefixRows.size() <= newLimit) {
                shortPrefixRows.add(new LinkedList<>());
            }
            while (suffixes.size() <= newLimit) {
                suffixes.add(new LinkedList<>());
                suffixes.get(suffixes.size() - 1).add(Word.epsilon());
            }
        }
    }

    /**
     * @return The current level limit (t)
     */
    public int getLevelLimit() {
        return maxLevel;
    }

    /**
     * Constructs a t-VCA from this table
     * @return The t-VCA
     */
    public VCA<I> toVCA() {
        // TODO use BG_O to construct a t-VCA
        return null;
    }

    @Override
    public Row<I> findUnclosedRow() {
        for (int i = 0 ; i <= maxLevel ; i++) {
            for (I symbol : alphabet) {
                if ((i == 0 && alphabet.isReturnSymbol(symbol)) || (i == maxLevel && alphabet.isCallSymbol(symbol))) {
                    // We skip
                    continue;
                }

                for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(i)) {
                    StratifiedObservationRow<I> longPrefixRow = shortPrefixRow.getSuccessor(alphabet.getSymbolIndex(symbol));
                    
                    if (longPrefixRow == null) {
                        return shortPrefixRow;
                    }
                    
                    // Same row content id => same information in the row => same equivalence class
                    boolean hasClass = shortPrefixRows.get(i + ComputeCounterValue.signOf(symbol, alphabet)).
                        stream().
                        anyMatch(row -> row.getRowContentId() == longPrefixRow.getRowContentId());

                    if (!hasClass) {
                        return shortPrefixRow;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Inconsistency<I> findInconsistency() {
        for (int i = 0; i <= maxLevel ; i++) {
            for (I symbol : alphabet) {
                if ((i == 0 && alphabet.isReturnSymbol(symbol)) || (i == maxLevel && alphabet.isCallSymbol(symbol))) {
                    // We skip
                    continue;
                }

                for (StratifiedObservationRow<I> uRow : shortPrefixRows.get(i)) {
                    for (StratifiedObservationRow<I> vRow : shortPrefixRows.get(i)) {
                        if (uRow.getRowContentId() == vRow.getRowContentId()) {
                            StratifiedObservationRow<I> uaRow = uRow.getSuccessor(alphabet.getSymbolIndex(symbol));
                            StratifiedObservationRow<I> vaRow = vRow.getSuccessor(alphabet.getSymbolIndex(symbol));

                            if (uaRow == null && vaRow == null) {
                                // Actually, the table is not closed (since ua and va are not known)
                                // So, we just skip this case and say that the table is consistent
                                // It's fine as the user is supposed to make the table close and consistent (and we can't really say that the classes are different)
                                continue;
                            }

                            if (uaRow == null || vaRow == null) {
                                return new Inconsistency<>(uRow, vRow, symbol);
                            }
                            
                            if (uaRow.getRowContentId() != vaRow.getRowContentId()) {
                                return new Inconsistency<>(uRow, vRow, symbol);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}