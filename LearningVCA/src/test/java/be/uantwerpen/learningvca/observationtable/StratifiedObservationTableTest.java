package be.uantwerpen.learningvca.observationtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
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
    private DefaultVCA<Character> vca;
    private MembershipOracle<Character, Boolean> oracle;

    @Before
    public void init() {
        // We have one useless internal symbol (as it is not used by the VCA)
        alphabet = new DefaultVPDAlphabet<>(Arrays.asList('c', 'd'), Arrays.asList('a'), Arrays.asList('b'));
        table = new StratifiedObservationTableBoolean<>(alphabet);

        vca = new DefaultVCA<>(alphabet, 1);
        Location q0 = vca.addInitialLocation(false);
        Location q1 = vca.addLocation();
        Location q2 = vca.addLocation(true);

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
        assertEquals(1, table.numberOfDistinctRows());

        StratifiedObservationRow<Character> rowA = table.getRow(Word.fromLetter('a'));
        assertNotNull(rowA);

        assertTrue(table.isClosed());
        assertTrue(table.isConsistent());

        assertNull(table.findUnclosedRow());
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

    @Test
    public void testAddTwoNewPrefixes() {
        table.initialize(Arrays.asList(Word.epsilon()), Arrays.asList(Word.epsilon()), oracle);

        List<List<Row<Character>>> unclosed = table.addShortPrefixes(Arrays.asList(Word.fromLetter('a'), Word.fromString("ac")), oracle);
        assertEquals(1, unclosed.size());
        assertEquals(1, unclosed.get(0).size());
        assertEquals(Word.fromString("acb"), unclosed.get(0).get(0).getLabel());

        assertEquals(2, table.numberOfDistinctRows());
        assertEquals(3, table.numberOfShortPrefixRows());
        assertEquals(7, table.numberOfLongPrefixRows());
        assertEquals(10, table.numberOfRows());

        assertFalse(table.isClosed());
        assertFalse(table.isConsistent());
    }

    @Test
    public void testAddSuffix() {
        table.initialize(Arrays.asList(Word.epsilon()), Arrays.asList(Word.epsilon()), oracle);

        table.addShortPrefixes(Arrays.asList(Word.fromLetter('a'), Word.fromSymbols('a', 'c')), oracle);

        assertEquals(2, table.numberOfSuffixes());
        assertEquals(1, table.numberOfSuffixes(0));
        assertEquals(1, table.numberOfSuffixes(1));

        List<List<Row<Character>>> unclosed = table.addSuffix(Word.fromLetter('b'), 1, oracle);
        // The table is not closed BUT addSuffix with 'b' does not process the unclosed row
        assertEquals(0, unclosed.size());

        assertEquals(3, table.numberOfSuffixes());
        assertEquals(4, table.numberOfDistinctRows());
        assertEquals(1, table.numberOfSuffixes(0));
        assertEquals(2, table.numberOfSuffixes(1));

        assertFalse(table.isClosed()); // "acb" does not have an equivalence class
        assertTrue(table.isConsistent()); // Each equivalence class has only one representative
    }

    @Test
    public void testToVCA() {
        table.initialize(Collections.singletonList(Word.epsilon()), Collections.singletonList(Word.epsilon()), oracle);

        VCA<?, Character> simpleVCA = table.toVCA();
        assertEquals(1, simpleVCA.size());
        assertFalse(simpleVCA.accepts(Word.epsilon()));
        assertFalse(simpleVCA.accepts(Word.fromString("a")));
        assertFalse(simpleVCA.accepts(Word.fromString("c")));
        assertFalse(simpleVCA.accepts(Word.fromString("b")));
        assertFalse(simpleVCA.accepts(Word.fromString("ac")));
        assertFalse(simpleVCA.accepts(Word.fromString("acb")));

        table.addShortPrefixes(Arrays.asList(Word.fromString("a"), Word.fromString("ac"), Word.fromString("acb")), oracle);
        assertTrue(table.isClosed());
        table.addSuffix(Word.fromString("b"), 1, oracle);
        assertTrue(table.isConsistent());

        VCA<?, Character> biggerVCA = table.toVCA();
        assertEquals(4, biggerVCA.size());

        assertTrue(biggerVCA.accepts(Word.fromString("acb")));
        assertTrue(biggerVCA.accepts(Word.fromString("accb")));
        assertTrue(biggerVCA.accepts(Word.fromString("accccb")));
        assertFalse(biggerVCA.accepts(Word.epsilon()));
        assertFalse(biggerVCA.accepts(Word.fromString("a")));
        assertFalse(biggerVCA.accepts(Word.fromString("ac")));
        assertFalse(biggerVCA.accepts(Word.fromString("ab")));
        assertFalse(biggerVCA.accepts(Word.fromString("aacbb")));
    }
}