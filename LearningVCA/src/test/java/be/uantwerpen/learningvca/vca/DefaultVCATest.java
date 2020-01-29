package be.uantwerpen.learningvca.vca;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.testng.annotations.Test;

import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class DefaultVCATest {

    private VCA<?, Character> getVCAAcceptingEverything() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('a'), Collections.emptyList(), Collections.emptyList());
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 0);
        assertEquals(0, vca.getThreshold());
        Location q0 = vca.addInitialLocation(true);
        assertTrue(q0.isAccepting());
        vca.setSuccessor(q0, 0, 'a', q0);
        return vca;
    }

    private VCA<?, Character> getVCARejectingEverything() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('a'), Collections.emptyList(), Collections.emptyList());
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 0);
        assertEquals(0, vca.getThreshold());
        Location q0 = vca.addInitialLocation(false);
        assertFalse(q0.isAccepting());
        vca.setSuccessor(q0, 0, 'a', q0);
        return vca;
    }

    private VCA<?, Character> getVCAExample() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList(), Arrays.asList('a'), Arrays.asList('b'));
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 1);
        Location q0 = vca.addInitialLocation(false);
        Location q1 = vca.addLocation(true);
        vca.setSuccessor(q0, 0, 'a', q0);
        vca.setSuccessor(q0, 1, 'a', q1);
        vca.setSuccessor(q0, 1, 'b', q1);
        vca.setSuccessor(q1, 1, 'b', q1);
        return vca;
    }

    @Test
    public void testAcceptsEverything() {
        VCA<?, Character> vca = getVCAAcceptingEverything();

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
        VCA<?, Character> vca = getVCARejectingEverything();

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

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNotInAlphabet() {
        VCA<?, Character> vca = getVCAAcceptingEverything();
        vca.accepts(Word.fromString("b"));
    }

    @Test
    public void testExample() {
        VCA<?, Character> vca = getVCAExample();
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

    @Test
    public void testGetAcceptingLocations() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList(), Arrays.asList('a'), Arrays.asList('b'));
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 1);
        Location q0 = vca.addInitialLocation(false);
        Location q1 = vca.addLocation(true);
        vca.setSuccessor(q0, 0, 'a', q0);
        vca.setSuccessor(q0, 1, 'a', q1);
        vca.setSuccessor(q0, 1, 'b', q1);
        vca.setSuccessor(q1, 1, 'b', q1);

        List<Location> locations = vca.getAcceptingLocations();
        assertTrue(locations.get(0).equals(q1));
        assertTrue(locations.contains(q1));
    }

    @Test
    public void testGetAcceptedWordExample() {
        VCA<?, Character> vca = getVCAExample();
        Word<Character> word = vca.getAcceptedWord();
        assertNotNull(word);
        assertTrue(vca.accepts(word));
    }

    @Test
    public void testGetAcceptedWordEverything() {
        VCA<?, Character> vca = getVCAAcceptingEverything();
        Word<Character> word = vca.getAcceptedWord();
        assertNotNull(word);
        assertTrue(vca.accepts(word));
    }

    @Test
    public void testGetAcceptedWordNothing() {
        VCA<?, Character> vca = getVCARejectingEverything();
        Word<Character> word = vca.getAcceptedWord();
        assertNull(word);
    }
}