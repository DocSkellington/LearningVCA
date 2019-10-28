package be.uantwerpen.learningvca.observationtable;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import be.uantwerpen.learningvca.util.ComputeCounterValue;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.Inconsistency;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * An abstract implementation of a stratified observation table.
 * 
 * The implementation is inspired from the implementation of GenericObservationTable and AbstractObservationTable (from LearnLib).
 * @param <I> The input alphabet type
 * @param <D> The type of the information to store in the table
 * @author Gaëtan Staquet
 */
public abstract class AbstractStratifiedObservationTable<I extends Comparable<I>, D> implements StratifiedObservationTable<I, D> {
    protected final VPDAlphabet<I> alphabet;
    // A prefix (or representative) is short if it is in the upper half of the observation table.
    // That is, it is short if it one of the row used for the creation of the automaton
    protected final List<List<StratifiedObservationRow<I>>> shortPrefixRows;
    protected final List<StratifiedObservationRow<I>> allLongPrefixRows;
    protected final List<StratifiedObservationRow<I>> allPrefixRows;

    protected final Map<Word<I>, StratifiedObservationRow<I>> rowMap;

    // A suffix is a separator
    protected final List<List<Word<I>>> suffixes;

    protected final List<List<D>> allRowContents;
    protected final Map<List<D>, Integer> rowContentsIdsMap;
    
    // t
    protected int maxLevel;

    protected boolean initialConsistencyCheckRequired;

    /**
     * Constructs the observation table.
     * 
     * The table is NOT initialized!
     * @param alphabet The pushdown alphabet
     */
    public AbstractStratifiedObservationTable(VPDAlphabet<I> alphabet) {
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
    public int numberOfSuffixes(int level) {
        return getSuffixes(level).size();
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
    public List<Word<I>> getSuffixes(int level) {
        return suffixes.get(level);
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

        List<DefaultQuery<I, D>> queries = new ArrayList<>();

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
    protected void createQueries(List<DefaultQuery<I, D>> queries, Word<I> prefix, List<Word<I>> suffixes) {
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
    protected void fetchQueriesResults(Iterator<DefaultQuery<I, D>> queryIt, List<D> output, int numSuffixes) {
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
    protected boolean processContents(StratifiedObservationRow<I> row, List<D> rowContents) {
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
    protected StratifiedObservationRow<I> createShortPrefixRow(Word<I> shortPrefix) {
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
     * 
     * If a long prefix is already a short prefix, the row is not changed and is not returned.
     * @param shortPrefixRow The row of the short prefix to use
     * @param queries The list of queries to fill
     * @return The long prefix rows
     */
    protected List<StratifiedObservationRow<I>> createLongPrefixesRows(StratifiedObservationRow<I> shortPrefixRow, List<DefaultQuery<I, D>> queries) {
        List<StratifiedObservationRow<I>> longRows = new ArrayList<>();
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
            else if (successorRow.isShortPrefixRow()) {
                continue;
            }

            shortPrefixRow.setSuccessor(i, successorRow);
            if (successorRow != null) {
                longRows.add(successorRow);
            }
        }
        return longRows;
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

        // We create the queries needed to fill every row
        List<DefaultQuery<I, D>> queries = new ArrayList<>();

        for (int level = 0 ; level <= maxLevel ; level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(level)) {
                createQueries(queries, shortPrefixRow.getLabel(), newSuffixesList.get(level));
            }
        }

        for (StratifiedObservationRow<I> longPrefixRow : allLongPrefixRows) {
            Word<I> longPrefix = longPrefixRow.getLabel();
            createQueries(queries, longPrefix, newSuffixesList.get(ComputeCounterValue.computeCounterValue(longPrefix, alphabet)));
        }

        oracle.processQueries(queries);

        Iterator<DefaultQuery<I, D>> queryIt = queries.iterator();
        // We need to know if we have already seen a row content in an other level
        // Indeed, if two rows on different levels use the same row contents, it's possible that adding a new suffix makes them use different row contents
        Map<List<D>, List<Integer>> rowContentsToLevels = new HashMap<>();

        // We start by updating the short prefixes
        for (int level = 0 ; level <= maxLevel ; level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(level)) {
                List<D> rowContents = allRowContents.get(shortPrefixRow.getRowContentId());
                List<Word<I>> newSuffixesForThisLevel = newSuffixesList.get(level);
                int oldNumberOfPrefixesForThisLevel = suffixes.get(level).size();

                if (rowContents.size() == newSuffixesForThisLevel.size()) {
                    // The row contents have a length equal to the old number of prefixes
                    final int l = level;
                    List<Integer> rowContentOnLevels = rowContentsToLevels.get(rowContents);
                    if (rowContentOnLevels == null) {
                        rowContentOnLevels = new ArrayList<>();
                    }

                    if (rowContentOnLevels.stream().anyMatch(i -> i != l)) {
                        // The row contents is used on a different already seen level
                        // Therefore, if we actually modify the row contents, we must change the id
                        if (newSuffixesForThisLevel.size() != 0) {
                            List<D> newRowContents = new ArrayList<>(oldNumberOfPrefixesForThisLevel + newSuffixesForThisLevel.size());
                            newRowContents.addAll(rowContents.subList(0, oldNumberOfPrefixesForThisLevel));
                            fetchQueriesResults(queryIt, newRowContents, newSuffixesForThisLevel.size());
                            processContents(shortPrefixRow, newRowContents);
                            List<Integer> levels = rowContentsToLevels.get(newRowContents);
                            if (levels == null) {
                                levels = new ArrayList<>();
                            }
                            levels.add(level);
                            rowContentsToLevels.put(newRowContents, levels);
                        }
                        else {
                            rowContentOnLevels.add(level);
                            rowContentsToLevels.put(rowContents, rowContentOnLevels);
                        }
                    }
                    else {
                        // This is the first time we see this row contents
                        // So, we just update the row contents
                        rowContentsIdsMap.remove(rowContents);
                        fetchQueriesResults(queryIt, rowContents, newSuffixesForThisLevel.size());
                        rowContentsIdsMap.put(rowContents, shortPrefixRow.getRowContentId());
                        List<Integer> levels = rowContentsToLevels.get(rowContents);
                        if (levels == null) {
                            levels = new ArrayList<>();
                        }
                        levels.add(level);
                        rowContentsToLevels.put(rowContents, levels);
                    }
                }
                else {
                    // This row contents have already been modified
                    // We need to check if this row must still use this row contents
                    List<D> newRowContents = new ArrayList<>(oldNumberOfPrefixesForThisLevel + newSuffixesList.size());
                    newRowContents.addAll(rowContents.subList(0, oldNumberOfPrefixesForThisLevel));
                    fetchQueriesResults(queryIt, newRowContents, newSuffixesForThisLevel.size());
                    processContents(shortPrefixRow, newRowContents);
                    List<Integer> levels = rowContentsToLevels.get(newRowContents);
                    if (levels == null) {
                        levels = new ArrayList<>();
                    }
                    levels.add(level);
                    rowContentsToLevels.put(newRowContents, levels);
                }
            }
        }

        List<List<Row<I>>> unclosed = new ArrayList<>();
        int numberOfDistinctShortPrefixRows = numberOfDistinctRows();

        // Then, the long prefix rows
        for (StratifiedObservationRow<I> longPrefixRow : allLongPrefixRows) {
            int rowContentId = longPrefixRow.getRowContentId();
            List<D> rowContents = allRowContents.get(rowContentId);
            int level = ComputeCounterValue.computeCounterValue(longPrefixRow.getLabel(), alphabet);

            List<Word<I>> newSuffixesForThisLevel = newSuffixesList.get(level);
            int oldNumberOfPrefixesForThisLevel = suffixes.get(level).size();

            if (rowContents.size() == oldNumberOfPrefixesForThisLevel) {
                // The row contents have a length equal to the old number of prefixes
                final int l = level;
                List<Integer> rowContentOnLevels = rowContentsToLevels.get(rowContents);
                if (rowContentOnLevels == null) {
                    rowContentOnLevels = new ArrayList<>();
                }

                if (rowContentOnLevels.stream().anyMatch(i -> i != l)) {
                    // The row contents is used on a different already seen level
                    // Therefore, if we actually modify the row contents, we must change the id
                    if (newSuffixesForThisLevel.size() != 0) {
                        List<D> newRowContents = new ArrayList<>(oldNumberOfPrefixesForThisLevel + newSuffixesForThisLevel.size());
                        newRowContents.addAll(rowContents.subList(0, oldNumberOfPrefixesForThisLevel));
                        fetchQueriesResults(queryIt, newRowContents, newSuffixesForThisLevel.size());
                        if (processContents(longPrefixRow, newRowContents)) {
                            unclosed.add(new ArrayList<>());
                        }
                
                        if (longPrefixRow.getRowContentId() >= numberOfDistinctShortPrefixRows) {
                            unclosed.get(longPrefixRow.getRowContentId() - numberOfDistinctShortPrefixRows).add(longPrefixRow);
                        }
                        List<Integer> levels = rowContentsToLevels.get(newRowContents);
                        if (levels == null) {
                            levels = new ArrayList<>();
                        }
                        levels.add(level);
                        rowContentsToLevels.put(newRowContents, levels);
                    }
                    else {
                        rowContentOnLevels.add(level);
                        rowContentsToLevels.put(rowContents, rowContentOnLevels);
                    }
                }
                else {
                    // This is the first time we see this row contents
                    // So, we just update the row contents
                    rowContentsIdsMap.remove(rowContents);
                    fetchQueriesResults(queryIt, rowContents, newSuffixesForThisLevel.size());
                    rowContentsIdsMap.put(rowContents, longPrefixRow.getRowContentId());
                    List<Integer> levels = rowContentsToLevels.get(rowContents);
                    if (levels == null) {
                        levels = new ArrayList<>();
                    }
                    levels.add(level);
                    rowContentsToLevels.put(rowContents, levels);
                }
            }
            else {
                // This row contents have already been modified
                List<D> newRowContents = new ArrayList<>(suffixes.get(level).size() + newSuffixesList.size());
                newRowContents.addAll(rowContents.subList(0, suffixes.get(level).size()));
                fetchQueriesResults(queryIt, newRowContents, newSuffixesList.get(level).size());
                if (processContents(longPrefixRow, newRowContents)) {
                    unclosed.add(new ArrayList<>());
                }
                
                if (longPrefixRow.getRowContentId() >= numberOfDistinctShortPrefixRows) {
                    unclosed.get(longPrefixRow.getRowContentId() - numberOfDistinctShortPrefixRows).add(longPrefixRow);
                }

                List<Integer> levels = rowContentsToLevels.get(newRowContents);
                if (levels == null) {
                    levels = new ArrayList<>();
                }
                levels.add(level);
                rowContentsToLevels.put(newRowContents, levels);
            }
        }

        for (int i = 0 ; i < newSuffixesList.size() ; i++) {
            suffixes.get(i).addAll(newSuffixesList.get(i));
        }

        return unclosed;
    }

    @Override
    public List<List<Row<I>>> addShortPrefixes(List<? extends Word<I>> shortPrefixes, MembershipOracle<I, D> oracle) {
        List<Row<I>> toShortPrefixRows = new ArrayList<>(shortPrefixes.size());
        for (Word<I> shortPrefix : shortPrefixes) {
            int counterValue = ComputeCounterValue.computeCounterValue(shortPrefix, alphabet);
            increaseLevelLimit(counterValue);
            Row<I> row = getRow(shortPrefix);
            if (row == null) {
                row = createShortPrefixRow(shortPrefix);
            }
            else if (row.isShortPrefixRow()) {
                // We skip
                continue;
            }
            toShortPrefixRows.add(row);
        }
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
    protected void makeShort(StratifiedObservationRow<I> row) {
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
        List<StratifiedObservationRow<I>> freshShortPrefixRows = new ArrayList<>(); // The short prefix rows with missing contents
        List<StratifiedObservationRow<I>> freshLongPrefixRows = new ArrayList<>(); // The long prefix rows with missing contents

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

        List<DefaultQuery<I, D>> queries = new ArrayList<>();

        // First, we generate the queries
        // Starting with short prefixes
        for (StratifiedObservationRow<I> shortPrefixRow : freshShortPrefixRows) {
            Word<I> shortPrefix = shortPrefixRow.getLabel();
            int counterValue = ComputeCounterValue.computeCounterValue(shortPrefix, alphabet);
            increaseLevelLimit(counterValue);
            createQueries(queries, shortPrefix, suffixes.get(counterValue));

            // We must set the successor of the longest prefix of shortPrefix
            getRow(shortPrefix.subWord(0, shortPrefix.size() - 1)).setSuccessor(alphabet.getSymbolIndex(shortPrefix.lastSymbol()), shortPrefixRow);
        }

        // Then, the long prefixes of the short prefixes
        for (StratifiedObservationRow<I> shortPrefixRow : freshShortPrefixRows) {
            List<StratifiedObservationRow<I>> longPrefixes = createLongPrefixesRows(shortPrefixRow, queries);
            // It might happen that a long prefix is already in the fresh short prefix
            // We ignore this long prefix
            List<StratifiedObservationRow<I>> newLongPrefix = longPrefixes.stream().
                filter(longPrefixRow -> !freshShortPrefixRows.contains(longPrefixRow)).
                collect(Collectors.toList());
            freshLongPrefixRows.addAll(newLongPrefix);
        }

        oracle.processQueries(queries);

        List<List<Row<I>>> unclosed = new ArrayList<>();
        Iterator<DefaultQuery<I, D>> queryIt = queries.iterator();

        // We now fill the rows
        // Again, we start with the short prefixes
        for (StratifiedObservationRow<I> shortPrefixRow : freshShortPrefixRows) {
            int counterValue = ComputeCounterValue.computeCounterValue(shortPrefixRow.getLabel(), alphabet);
            int numberOfSuffixes = suffixes.get(counterValue).size();
            List<D> rowContents = new ArrayList<>(numberOfSuffixes);
            fetchQueriesResults(queryIt, rowContents, numberOfSuffixes);
            processContents(shortPrefixRow, rowContents);
        }

        int numberOfDistinctRows = numberOfDistinctRows();

        // Then, the long prefixes
        for (StratifiedObservationRow<I> longPrefixRow : freshLongPrefixRows) {
            int counterValue = ComputeCounterValue.computeCounterValue(longPrefixRow.getLabel(), alphabet);
            int numSuffixes = suffixes.get(counterValue).size();
            List<D> rowContents = new ArrayList<>(numSuffixes);

            fetchQueriesResults(queryIt, rowContents, numSuffixes);
            if (processContents(longPrefixRow, rowContents)) {
                // We have a new equivalence class
                unclosed.add(new ArrayList<>());
            }

            int id = longPrefixRow.getRowContentId();
            if (id >= numberOfDistinctRows) {
                // This row uses a row contents that did not exist before
                unclosed.get(id - numberOfDistinctRows).add(longPrefixRow);
            }
        }

        return unclosed;
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
    protected void increaseLevelLimit(int newLimit) {
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

    @Override
    public int getLevelLimit() {
        return maxLevel;
    }

    @Override
    public StratifiedObservationRow<I> findUnclosedRow() {
        for (int i = 0 ; i <= maxLevel ; i++) {
            for (I symbol : alphabet) {
                if ((i == 0 && alphabet.isReturnSymbol(symbol)) || (i == maxLevel && alphabet.isCallSymbol(symbol))) {
                    // We skip
                    continue;
                }

                for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(i)) {
                    StratifiedObservationRow<I> longPrefixRow = shortPrefixRow.getSuccessor(alphabet.getSymbolIndex(symbol));
                    
                    if (longPrefixRow == null) {
                        return longPrefixRow;
                    }
                    
                    // Same row content id => same information in the row => same equivalence class
                    boolean hasClass = shortPrefixRows.get(i + ComputeCounterValue.signOf(symbol, alphabet)).
                        stream().
                        anyMatch(row -> row.getRowContentId() == longPrefixRow.getRowContentId());

                    if (!hasClass) {
                        return longPrefixRow;
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

    /**
     * Gets a representative for the given row.
     * 
     * Since the rows are stored in lists, the same row contents id implies the same returned representative.
     * @param row The row we want a representative of
     * @return A representative
     */
    protected StratifiedObservationRow<I> getRepresentativeRow(StratifiedObservationRow<I> row) {
        for (StratifiedObservationRow<I> spRow : shortPrefixRows.get(ComputeCounterValue.computeCounterValue(row.getLabel(), alphabet))) {
            if (spRow.getRowContentId() == row.getRowContentId()) {
                return spRow;
            }
        }
        return null;
    }
}