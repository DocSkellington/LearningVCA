package be.uantwerpen.learningvca.observationtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

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
    @Test
    public void testInitialisation() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('c', 'd'), Arrays.asList('a'), Arrays.asList('b'));
        StratifiedObservationTable<Character, Boolean> table = new StratifiedObservationTable<>(alphabet);

        assertFalse(table.isInitialized());

        VCA<Character> vca = new VCA<>(alphabet, 1);
        State q0 = vca.addInitialState(false);
        State q1 = vca.addState();
        State q2 = vca.addState(true);

        vca.setCallSuccessor(q0, 0, 'a', q0);
        vca.setCallSuccessor(q0, 1, 'a', q0);
        vca.setInternalSuccessor(q0, 1, 'c', q1);
        vca.setInternalSuccessor(q1, 1, 'c', q1);
        vca.setReturnSuccessor(q1, 1, 'b', q2);
        vca.setReturnSuccessor(q2, 1, 'b', q2);

        List<Word<Character>> prefixes = new ArrayStorage<>(Arrays.asList(Word.epsilon()));
        List<Word<Character>> suffixes = new ArrayStorage<>(Arrays.asList(Word.epsilon()));
        MembershipOracle<Character, Boolean> oracle = new SimulatorOracle<>(vca);
        List<List<Row<Character>>> unclosed =  table.initialize(prefixes, suffixes, oracle);

        assertEquals(0, unclosed.size());

        assertEquals(0, table.getLevelLimit());
        assertEquals(1, table.numberOfShortPrefixRows());
        assertEquals(2, table.numberOfLongPrefixRows());

        assertEquals(1, table.numberOfDistinctRows());

        StratifiedObservationRow<Character> rowEpsilon = table.getRow(Word.epsilon());
        StratifiedObservationRow<Character> rowC = table.getRow(Word.fromSymbols('c'));
        StratifiedObservationRow<Character> rowD = table.getRow(Word.fromSymbols('d'));

        // TODO once VCA.computeSuffixOutput is done
        // assertFalse(table.cellContents(rowEpsilon, 0));
    }
}