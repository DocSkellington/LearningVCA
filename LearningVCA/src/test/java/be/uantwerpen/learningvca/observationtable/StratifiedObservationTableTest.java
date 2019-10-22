package be.uantwerpen.learningvca.observationtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import be.uantwerpen.learningvca.vca.State;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class StratifiedObservationTableTest {
    private VPDAlphabet<Character> alphabet;
    private StratifiedObservationTable<Character, Boolean> table;
    private VCA<Character> vca;
    private MembershipOracle<Character, Boolean> oracle;

    @Before
    public void init() {
        // We have one useless internal symbol (as it is not used by the VCA)
        alphabet = new DefaultVPDAlphabet<>(Arrays.asList('c', 'd'), Arrays.asList('a'), Arrays.asList('b'));
        table = new StratifiedObservationTable<>(alphabet);

        vca = new VCA<>(alphabet, 1);
        State q0 = vca.addInitialState(false);
        State q1 = vca.addState();
        State q2 = vca.addState(true);

        vca.setCallSuccessor(q0, 0, 'a', q0);
        vca.setCallSuccessor(q0, 1, 'a', q0);
        vca.setInternalSuccessor(q0, 1, 'c', q1);
        vca.setInternalSuccessor(q1, 1, 'c', q1);
        vca.setReturnSuccessor(q1, 1, 'b', q2);
        vca.setReturnSuccessor(q2, 1, 'b', q2);

        oracle = new SimulatorOracle<>(vca);
    }

    @Test
    public void testInitialisation() {
        assertFalse(table.isInitialized());

        List<Word<Character>> prefixes = new ArrayStorage<>(Arrays.asList(Word.epsilon()));
        List<Word<Character>> suffixes = new ArrayStorage<>(Arrays.asList(Word.epsilon()));
        List<List<Row<Character>>> unclosed =  table.initialize(prefixes, suffixes, oracle);

        assertEquals(0, unclosed.size());

        assertEquals(0, table.getLevelLimit());
        assertEquals(1, table.numberOfShortPrefixRows());
        assertEquals(2, table.numberOfLongPrefixRows());
        assertEquals(3, table.numberOfRows());
        assertEquals(1, table.numberOfSuffixes());

        assertEquals(1, table.numberOfDistinctRows());

        StratifiedObservationRow<Character> rowEpsilon = table.getRow(Word.epsilon());
        StratifiedObservationRow<Character> rowC = table.getRow(Word.fromSymbols('c'));
        StratifiedObservationRow<Character> rowD = table.getRow(Word.fromSymbols('d'));

        assertNotNull(rowEpsilon);
        assertNotNull(rowC);
        assertNotNull(rowD);

        assertFalse(table.cellContents(rowEpsilon, 0));
        assertFalse(table.cellContents(rowC, 0));
        assertFalse(table.cellContents(rowD, 0));

        assertNull(table.getRow(Word.fromSymbols('a')));
        assertNull(table.getRow(Word.fromSymbols('b')));

        assertEquals(Word.epsilon(), table.getSuffix(0));

        assertTrue(table.isClosed());
        assertTrue(table.isConsistent());

        assertNull(table.findUnclosedRow());
        assertNull(table.findInconsistency());

        // TODO check that for every prefix and for suffix v, uv is in Sigma_{0, t}*?
    }

    @Test(expected = InvalidParameterException.class)
    public void testBadInitialisation() {
        List<Word<Character>> prefixes = Arrays.asList(Word.fromSymbols('a', 'b'));
        List<Word<Character>> suffixes = Arrays.asList(Word.epsilon());
        table.initialize(prefixes, suffixes, oracle);
    }

    @Test
    public void testAddOneShortPrefixAlreadyInTableAsShortPrefix() {
        table.initialize(Arrays.asList(Word.epsilon()), Arrays.asList(Word.epsilon()), oracle);

        List<List<Row<Character>>> unclosed = table.addShortPrefixes(Arrays.asList(Word.epsilon()), oracle);
        assertEquals(0, unclosed.size());

        assertEquals(0, table.getLevelLimit());
        assertEquals(1, table.numberOfShortPrefixRows());
        assertEquals(2, table.numberOfLongPrefixRows());
        assertEquals(3, table.numberOfRows());
        assertEquals(1, table.numberOfSuffixes());
        assertEquals(1, table.numberOfDistinctRows());

        assertTrue(table.isClosed());
        assertTrue(table.isConsistent());
    }

    @Test
    public void testAddOneNewShortPrefix() {
        table.initialize(Arrays.asList(Word.epsilon()), Arrays.asList(Word.epsilon()), oracle);

        List<List<Row<Character>>> unclosed = table.addShortPrefixes(Arrays.asList(Word.fromLetter('a')), oracle);
        assertEquals(0, unclosed.size());

        assertEquals(1, table.getLevelLimit());
        assertEquals(2, table.numberOfShortPrefixRows());
        assertEquals(5, table.numberOfLongPrefixRows());
        assertEquals(7, table.numberOfRows());
        // 2 distinct rows because the level 1 (for 'a') does not have any separators
        assertEquals(2, table.numberOfDistinctRows());

        StratifiedObservationRow<Character> rowA = table.getRow(Word.fromLetter('a'));
        assertNotNull(rowA);

        // The table is not closed since the new level limit is 1 and espilon a = a is not a representative
        assertFalse(table.isClosed());
        assertTrue(table.isConsistent());

        assertNotNull(table.findUnclosedRow());
    }

    @Test
    public void testLongPrefixToShortPrefix() {
        table.initialize(Arrays.asList(Word.epsilon()), Arrays.asList(Word.epsilon()), oracle);

        StratifiedObservationRow<Character> rowC = table.getRow(Word.fromLetter('c'));

        // toShortPrefixes directly close the rows
        List<List<Row<Character>>> unclosed = table.toShortPrefixes(Arrays.asList(rowC), oracle);
        assertEquals(0, unclosed.size());

        assertTrue(table.isClosed());
        assertTrue(table.isConsistent());
    }
}