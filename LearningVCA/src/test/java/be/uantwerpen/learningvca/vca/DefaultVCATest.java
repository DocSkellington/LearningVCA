package be.uantwerpen.learningvca.vca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

import org.junit.Test;

import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class DefaultVCATest {
    @Test
    public void testAcceptsEverything() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('a'), Collections.emptyList(), Collections.emptyList());
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 0);
        assertEquals(0, vca.getThreshold());
        Location q0 = vca.addInitialLocation(true);
        assertTrue(q0.isAccepting());
        vca.setInternalSuccessor(q0, 0, 'a', q0);

        assertTrue(vca.accepts(Word.epsilon()));
        assertTrue(vca.computeOutput(Word.epsilon()));
        assertTrue(vca.computeSuffixOutput(Word.epsilon(), Word.epsilon()));

        Word<Character> word = Word.fromString("aaaaaa");
        IntStream.range(0, word.size()).forEach(i -> {
            Word<Character> prefix = word.subWord(0, i);
            Word<Character> suffix = word.subWord(i);
            assertTrue(vca.accepts(prefix));
            assertTrue(vca.accepts(suffix));
            assertTrue(vca.computeOutput(prefix));
            assertTrue(vca.computeOutput(suffix));
            assertTrue(vca.computeSuffixOutput(prefix, suffix));
            assertTrue(vca.computeSuffixOutput(prefix, prefix));
            assertTrue(vca.computeSuffixOutput(suffix, suffix));
        });
    }

    @Test
    public void testRejectEverything() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('a'), Collections.emptyList(), Collections.emptyList());
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 0);
        assertEquals(0, vca.getThreshold());
        Location q0 = vca.addInitialLocation(false);
        assertFalse(q0.isAccepting());
        vca.setInternalSuccessor(q0, 0, 'a', q0);

        Word<Character> word = Word.fromString("aaaaaa");
        IntStream.range(0, word.size()).forEach(i -> {
            Word<Character> prefix = word.subWord(0, i);
            Word<Character> suffix = word.subWord(i);
            assertFalse(vca.accepts(prefix));
            assertFalse(vca.accepts(suffix));
            assertFalse(vca.computeOutput(prefix));
            assertFalse(vca.computeOutput(suffix));
            assertFalse(vca.computeSuffixOutput(prefix, suffix));
            assertFalse(vca.computeSuffixOutput(prefix, prefix));
            assertFalse(vca.computeSuffixOutput(suffix, suffix));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotInAlphabet() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('a'), Collections.emptyList(), Collections.emptyList());
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 0);
        assertEquals(0, vca.getThreshold());
        Location q0 = vca.addInitialLocation(true);
        assertTrue(q0.isAccepting());
        vca.setInternalSuccessor(q0, 0, 'a', q0);

        vca.accepts(Word.fromString("b"));
    }

    @Test
    public void testExample() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList(), Arrays.asList('a'), Arrays.asList('b'));
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 1);
        Location q0 = vca.addInitialLocation(false);
        Location q1 = vca.addLocation(true);
        vca.setCallSuccessor(q0, 0, 'a', q0);
        vca.setCallSuccessor(q0, 1, 'a', q1);
        vca.setReturnSuccessor(q0, 1, 'b', q1);
        vca.setReturnSuccessor(q1, 1, 'b', q1);

        Word<Character> word = Word.fromString("aabb");
        assertTrue(vca.accepts(word));
        IntStream.range(0, word.size() + 1).forEach(i -> {
            Word<Character> prefix = word.subWord(0, i);
            Word<Character> suffix = word.subWord(i);
            
            if (i == 0) {
                // prefix == epsilon
                assertTrue(vca.computeOutput(suffix));
                assertFalse(vca.computeOutput(prefix));
            }
            else if (i == word.size()) {
                // suffix = epsilon
                assertTrue(vca.computeOutput(prefix));
                assertFalse(vca.computeOutput(suffix));
            }
            else {
                assertFalse(vca.computeOutput(prefix));
                assertFalse(vca.computeOutput(suffix));
            }
            assertFalse(vca.computeSuffixOutput(prefix, prefix));
            assertFalse(vca.computeSuffixOutput(suffix, suffix));
            assertTrue(vca.computeSuffixOutput(prefix, suffix));
        });
    }
}