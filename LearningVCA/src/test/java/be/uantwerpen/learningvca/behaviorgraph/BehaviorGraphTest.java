package be.uantwerpen.learningvca.behaviorgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class BehaviorGraphTest {
    @Test
    public void toDFAOneStateNoTransitions() {
        TauMapping<Character> tau0 = new TauMapping<>(1);
        Description<Character> description = new Description<>(0, 1, 1);
        description.addTauMappings(Arrays.asList(tau0));
        description.setInitialState(0, 1);
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('a'), Arrays.asList(), Arrays.asList());

        BehaviorGraph<Character> bg = new BehaviorGraph<>(alphabet, description);

        CompactDFA<Character> dfa = bg.toDFA(0);

        assertEquals(dfa.size(), 1);
        assertFalse(dfa.isAccepting(0));
        assertNull(dfa.getTransition(0, alphabet.getSymbolIndex('a')));
    }

    @Test
    public void toDFAExampleUpTo0() {
        BehaviorGraph<Character> bg = ConstructBG.constructBGExample();
        CompactDFA<Character> dfa = bg.toDFA(0);

        assertEquals(1, dfa.size());

        assertTrue(dfa.accepts(Arrays.asList()));
        // Not in L
        assertFalse(dfa.accepts(Arrays.asList('a')));
        assertFalse(dfa.accepts(Arrays.asList('a', 'a')));
        assertFalse(dfa.accepts(Arrays.asList('b')));
        assertFalse(dfa.accepts(Arrays.asList('b', 'b')));
        // Height exceeds t
        assertFalse(dfa.accepts(Arrays.asList('a', 'b')));
        assertFalse(dfa.accepts(Arrays.asList('a', 'a', 'b', 'b')));
        assertFalse(dfa.accepts(Arrays.asList('a', 'a', 'a', 'b', 'b', 'b')));
    }

    @Test
    public void toDFAExampleUpTo1() {
        BehaviorGraph<Character> bg = ConstructBG.constructBGExample();
        CompactDFA<Character> dfa = bg.toDFA(1);

        assertEquals(5, dfa.size());

        assertTrue(dfa.accepts(Arrays.asList()));
        assertTrue(dfa.accepts(Arrays.asList('a', 'b')));
        // Not in L
        assertFalse(dfa.accepts(Arrays.asList('a')));
        assertFalse(dfa.accepts(Arrays.asList('a', 'a')));
        assertFalse(dfa.accepts(Arrays.asList('b')));
        assertFalse(dfa.accepts(Arrays.asList('b', 'b')));
        // Height exceeds t
        assertFalse(dfa.accepts(Arrays.asList('a', 'a', 'b', 'b')));
        assertFalse(dfa.accepts(Arrays.asList('a', 'a', 'a', 'b', 'b', 'b')));
    }

    @Test
    public void toDFAExampleUpTo2() {
        BehaviorGraph<Character> bg = ConstructBG.constructBGExample();
        CompactDFA<Character> dfa = bg.toDFA(2);

        assertEquals(8, dfa.size());

        assertTrue(dfa.accepts(Arrays.asList()));
        assertTrue(dfa.accepts(Arrays.asList('a', 'b')));
        assertTrue(dfa.accepts(Arrays.asList('a', 'a', 'b', 'b')));
        // Not in L
        assertFalse(dfa.accepts(Arrays.asList('a')));
        assertFalse(dfa.accepts(Arrays.asList('a', 'a')));
        assertFalse(dfa.accepts(Arrays.asList('b')));
        assertFalse(dfa.accepts(Arrays.asList('b', 'b')));
        // Height exceeds t
        assertFalse(dfa.accepts(Arrays.asList('a', 'a', 'a', 'b', 'b', 'b')));
    }
}