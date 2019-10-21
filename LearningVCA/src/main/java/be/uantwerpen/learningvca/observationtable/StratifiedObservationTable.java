package be.uantwerpen.learningvca.observationtable;

import java.util.Collection;
import java.util.List;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.MutableObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

public class StratifiedObservationTable<I> implements MutableObservationTable<I, Boolean> {
    private final VPDAlphabet<I> alphabet;

    public StratifiedObservationTable(VPDAlphabet<I> alphabet) {
        this.alphabet = alphabet;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Row<I>> getShortPrefixRows() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Row<I>> getLongPrefixRows() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Row<I> getRow(int idx) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int numberOfDistinctRows() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Word<I>> getSuffixes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Boolean> rowContents(Row<I> row) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Row<I>>> initialize(List<Word<I>> initialShortPrefixes, List<Word<I>> initialSuffixes,
            MembershipOracle<I, Boolean> oracle) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInitialized() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInitialConsistencyCheckRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<List<Row<I>>> addSuffixes(Collection<? extends Word<I>> newSuffixes, MembershipOracle<I, Boolean> oracle) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Row<I>>> addShortPrefixes(List<? extends Word<I>> shortPrefixes, MembershipOracle<I, Boolean> oracle) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Row<I>>> toShortPrefixes(List<Row<I>> lpRows, MembershipOracle<I, Boolean> oracle) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Row<I>>> addAlphabetSymbol(I symbol, MembershipOracle<I, Boolean> oracle) {
        // TODO Auto-generated method stub
        return null;
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

}