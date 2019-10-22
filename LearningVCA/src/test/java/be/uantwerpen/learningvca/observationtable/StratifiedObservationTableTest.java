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
}