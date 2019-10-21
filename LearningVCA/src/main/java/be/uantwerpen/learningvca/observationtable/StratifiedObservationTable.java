package be.uantwerpen.learningvca.observationtable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import be.uantwerpen.learningvca.util.ComputeCounterValue;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
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
    private final List<StratifiedObservationRow<I>> internalPrefixRows;
    private final List<StratifiedObservationRow<I>> callPrefixRows;
    private final List<StratifiedObservationRow<I>> returnPrefixRows;
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
        this.internalPrefixRows = new LinkedList<>();
        this.callPrefixRows = new LinkedList<>();
        this.returnPrefixRows = new LinkedList<>();
        this.allPrefixRows = new LinkedList<>();

        this.rowMap = new HashMap<>();

        this.suffixes = new LinkedList<>();

        this.allRowContents = new LinkedList<>();

        this.rowContentsIdsMap = new HashMap<>();

        this.maxLevel = 0;

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
        List<Row<I>> list = new LinkedList<>();
        list.addAll(internalPrefixRows);
        list.addAll(callPrefixRows);
        list.addAll(returnPrefixRows);
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
            createShortPrefixRow(shortPrefix);
            createQueries(queries, shortPrefix, initialSuffixes);
        }

        // We add the missing long prefixes, if needed
        // Since t = 0, we know that every short prefix has a counter value of 0
        for (StratifiedObservationRow<I> row : shortPrefixRows.get(0)) {
            Word<I> shortPrefix = row.getLabel();
            for (int i = 0 ; i < alphabet.size() ; i++) {
                I symbol = alphabet.getSymbol(i);
                Word<I> longPrefix = shortPrefix.append(symbol);
                StratifiedObservationRow<I> successorRow = getRow(longPrefix);
                if (successorRow == null) {
                    // We create the long prefix row
                    switch (alphabet.getSymbolType(symbol)) {
                        case CALL:
                            successorRow = createCallPrefixRow(longPrefix);
                            break;
                        case RETURN:
                            successorRow = createReturnPrefixRow(longPrefix);
                            break;
                        case INTERNAL:
                            successorRow = createInternalPrefixRow(longPrefix);
                            break;
                    }

                    if (successorRow != null) {
                        // We do not create queries for words that can not be in T
                        createQueries(queries, longPrefix, suffixes.get(0));
                    }
                }

                row.setSuccessor(i, successorRow);
            }
        }

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
        int counterValue = ComputeCounterValue.computeCounterValue(shortPrefix, alphabet);

        if (counterValue > maxLevel) {
            return null;
        }

        rowMap.put(shortPrefix, row);
        shortPrefixRows.get(counterValue).add(row);

        allPrefixRows.add(row);
        return row;
    }

    /**
     * Creates a row for a long prefix built with an internal symbol
     * @param longPrefix The long prefix
     * @return A row for the long prefix
     */
    private StratifiedObservationRow<I> createInternalPrefixRow(Word<I> longPrefix) {
        StratifiedObservationRow<I> row = new StratifiedObservationRow<>(longPrefix, allPrefixRows.size());
        int counterValue = ComputeCounterValue.computeCounterValue(longPrefix, alphabet);
        if (counterValue > maxLevel) {
            return null;
        }
        rowMap.put(longPrefix, row);
        internalPrefixRows.add(row);
        allPrefixRows.add(row);
        return row;
    }

    /**
     * Creates a row for a long prefix built with a call symbol
     * @param longPrefix The long prefix
     * @return A row for the long prefix
     */
    private StratifiedObservationRow<I> createCallPrefixRow(Word<I> longPrefix) {
        StratifiedObservationRow<I> row = new StratifiedObservationRow<>(longPrefix, allPrefixRows.size());
        int counterValue = ComputeCounterValue.computeCounterValue(longPrefix, alphabet);
        if (counterValue > maxLevel) {
            return null;
        }
        rowMap.put(longPrefix, row);
        callPrefixRows.add(row);
        allPrefixRows.add(row);
        return row;
    }

    /**
     * Creates a row for a long prefix built with a return symbol
     * @param longPrefix The long prefix
     * @return A row for the long prefix
     */
    private StratifiedObservationRow<I> createReturnPrefixRow(Word<I> longPrefix) {
        StratifiedObservationRow<I> row = new StratifiedObservationRow<>(longPrefix, allPrefixRows.size());
        int counterValue = ComputeCounterValue.computeCounterValue(longPrefix, alphabet);
        if (counterValue == -1 || counterValue > maxLevel) {
            return null;
        }
        rowMap.put(longPrefix, row);
        returnPrefixRows.add(row);
        allPrefixRows.add(row);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Row<I>>> addShortPrefixes(List<? extends Word<I>> shortPrefixes, MembershipOracle<I, D> oracle) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Row<I>>> toShortPrefixes(List<Row<I>> lpRows, MembershipOracle<I, D> oracle) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Row<I>>> addAlphabetSymbol(I symbol, MembershipOracle<I, D> oracle) {
        throw new UnsupportedOperationException("StratifiedObservationTable: impossible to add an alphabet symbol");
    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return MutableObservationTable.super.isClosed();
    }

    @Override
    public boolean isConsistent() {
        // TODO Auto-generated method stub
        return MutableObservationTable.super.isConsistent();
    }

    /**
     * Increases the max level of this table.
     * 
     * That is, words are allowed to have a counter value up to the new limit
     * @param newLimit The new limit
     */
    private void increaseLevelLimit(int newLimit) {
        this.maxLevel = newLimit;
        while (shortPrefixRows.size() <= newLimit) {
            shortPrefixRows.add(new LinkedList<>());
        }
        while (suffixes.size() <= newLimit) {
            suffixes.add(new LinkedList<>());
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
}