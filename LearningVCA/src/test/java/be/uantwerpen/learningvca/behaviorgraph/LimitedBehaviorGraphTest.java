package be.uantwerpen.learningvca.behaviorgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class LimitedBehaviorGraphTest {
    private VPDAlphabet<Character> getRegularAlphabet() {
        return new DefaultVPDAlphabet<>(Arrays.asList('a', 'b'), Collections.emptyList(), Collections.emptyList());
    }

    private LimitedBehaviorGraph<Character> getRegular() {
        LimitedBehaviorGraph<Character> behaviorGraph = new LimitedBehaviorGraph<>(getRegularAlphabet(), 0);
        Integer eps = behaviorGraph.addInitialState();
        Integer a = behaviorGraph.addState();
        Integer ab = behaviorGraph.addState(true);
        Integer b = behaviorGraph.addState();
        behaviorGraph.setStateLevel(eps, 0);
        behaviorGraph.setStateLevel(a, 0);
        behaviorGraph.setStateLevel(ab, 0);
        behaviorGraph.setStateLevel(b, 0);

        behaviorGraph.setTransition(eps, (Character) 'a', a);
        behaviorGraph.setTransition(eps, (Character) 'b', b);
        behaviorGraph.setTransition(a, (Character) 'a', a);
        behaviorGraph.setTransition(a, (Character) 'b', ab);
        behaviorGraph.setTransition(ab, (Character) 'a', b);
        behaviorGraph.setTransition(ab, (Character) 'b', ab);
        behaviorGraph.setTransition(b, (Character) 'a', b);
        behaviorGraph.setTransition(b, (Character) 'b', b);

        return behaviorGraph;
    }

    @Test
    public void testRegularLanguage() {
        // L = {a^n b^m | n, m > 0}
        // There is no need for a period
        // So, the shortest offset and period: (1, 0)
        LimitedBehaviorGraph<Character> behaviorGraph = getRegular();
        List<Description<Character>> descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(1, descriptions.size());
        VCA<Character> vca = descriptions.get(0).toVCA(getRegularAlphabet());
        assertTrue(vca.accepts(Word.fromString("aaabb")));
        assertTrue(vca.accepts(Word.fromString("ab")));
        assertTrue(vca.accepts(Word.fromString("aaaaaaaaabbbbb")));
        assertFalse(vca.accepts(Word.epsilon()));
        assertFalse(vca.accepts(Word.fromString("a")));
        assertFalse(vca.accepts(Word.fromString("b")));
        assertFalse(vca.accepts(Word.fromString("ba")));
        assertFalse(vca.accepts(Word.fromString("aba")));
    }

    private VPDAlphabet<Character> getAlphabetWithoutInternals() {
        return new DefaultVPDAlphabet<>(Collections.emptyList(), Arrays.asList('a'), Arrays.asList('b'));
    }

    private LimitedBehaviorGraph<Character> getWithoutInternals(int threshold) {
        LimitedBehaviorGraph<Character> behaviorGraph = new LimitedBehaviorGraph<>(getAlphabetWithoutInternals(), threshold);
        Integer eps = behaviorGraph.addInitialState(true);
        behaviorGraph.setStateLevel(eps, 0);

        if (threshold >= 1) {
            Integer a = behaviorGraph.addState();
            Integer ab = behaviorGraph.addState(true);
            Integer aba = behaviorGraph.addState();
            Integer abab = behaviorGraph.addState();
            behaviorGraph.setStateLevel(a, 1);
            behaviorGraph.setStateLevel(ab, 0);
            behaviorGraph.setStateLevel(aba, 1);
            behaviorGraph.setStateLevel(abab, 0);

            behaviorGraph.setTransition(eps,    (Character)'a', a);
            behaviorGraph.setTransition(a,      (Character)'b', ab);
            behaviorGraph.setTransition(ab,     (Character)'a', aba);
            behaviorGraph.setTransition(aba,    (Character)'b', abab);
            behaviorGraph.setTransition(abab,   (Character)'a', aba);

            if (threshold >= 2) {
                Integer aa = behaviorGraph.addState();
                Integer aab = behaviorGraph.addState();
                Integer aaba = behaviorGraph.addState();
                behaviorGraph.setStateLevel(aa, 2);
                behaviorGraph.setStateLevel(aab, 1);
                behaviorGraph.setStateLevel(aaba, 2);

                behaviorGraph.setTransition(a,      (Character)'a', aa);
                behaviorGraph.setTransition(aa,     (Character)'b', aab);
                behaviorGraph.setTransition(aab,    (Character)'a', aaba);
                behaviorGraph.setTransition(aab,    (Character)'b', ab);
                behaviorGraph.setTransition(aba,    (Character)'a', aaba);
                behaviorGraph.setTransition(aaba,   (Character)'b', aba);

                if (threshold >= 3) {
                    Integer aaa = behaviorGraph.addState();
                    Integer aaab = behaviorGraph.addState();
                    Integer aaaba = behaviorGraph.addState();
                    behaviorGraph.setStateLevel(aaa, 3);
                    behaviorGraph.setStateLevel(aaab, 2);
                    behaviorGraph.setStateLevel(aaaba, 3);

                    behaviorGraph.setTransition(aa,     (Character)'a', aaa);
                    behaviorGraph.setTransition(aaa,    (Character)'b', aaab);
                    behaviorGraph.setTransition(aaab,   (Character)'a', aaaba);
                    behaviorGraph.setTransition(aaab,   (Character)'b', aab);
                    behaviorGraph.setTransition(aaba,   (Character)'a', aaaba);
                    behaviorGraph.setTransition(aaaba,  (Character)'b', aaba);

                    if (threshold >= 4) {
                        Integer aaaa = behaviorGraph.addState();
                        Integer aaaab = behaviorGraph.addState();
                        Integer aaaaba = behaviorGraph.addState();
                        behaviorGraph.setStateLevel(aaaa, 4);
                        behaviorGraph.setStateLevel(aaaab, 3);
                        behaviorGraph.setStateLevel(aaaaba, 4);

                        behaviorGraph.setTransition(aaa,    (Character)'a', aaaa);
                        behaviorGraph.setTransition(aaaa,   (Character)'b', aaaab);
                        behaviorGraph.setTransition(aaaab,  (Character)'a', aaaaba);
                        behaviorGraph.setTransition(aaaab,  (Character)'b', aaab);
                        behaviorGraph.setTransition(aaaba,  (Character)'a', aaaaba);
                        behaviorGraph.setTransition(aaaaba, (Character)'b', aaaba);
                    }
                }
            }
        }

        return behaviorGraph;
    }

    @Test
    public void testWithoutInternals() {
        // L = {a^n b^n | n > 0}
        // Shortest offset and period: (1, 1)
        // So, the behavior graph must be complete up to the level at least 1 + 2*1 = 3
        // So, we need a threshold of at least 4
        LimitedBehaviorGraph<Character> behaviorGraph = getWithoutInternals(0);
        List<Description<Character>> descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(0, descriptions.size());

        behaviorGraph = getWithoutInternals(1);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(0, descriptions.size());

        behaviorGraph = getWithoutInternals(2);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(1, descriptions.size());
        // We have a description but it is not a correct description of the behavior graph
        Description<Character> desc = descriptions.get(0);
        assertEquals(0, desc.getOffset());
        assertEquals(1, desc.getPeriod());
        assertEquals(1, desc.getTauMappings().size());
        VCA<Character> vca = desc.toVCA(getAlphabetWithoutInternals());
        assertFalse(vca.accepts(Word.fromString("ab")));

        behaviorGraph = getWithoutInternals(3);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(3, descriptions.size());
        // The three descriptions are still incorrect
        for (int i = 0 ; i < descriptions.size() ; i++) {
            vca = descriptions.get(i).toVCA(getAlphabetWithoutInternals());
            assertFalse(vca.accepts(Word.fromString("aabb")));
        }

        behaviorGraph = getWithoutInternals(4);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(5, descriptions.size());
        for (int i = 0 ; i < 3 ; i++) {
            vca = descriptions.get(i).toVCA(getAlphabetWithoutInternals());
            assertFalse(vca.accepts(Word.fromString("aabb")));
        }
        vca = descriptions.get(3).toVCA(getAlphabetWithoutInternals());
        assertFalse(vca.accepts(Word.fromString("aaaabbbb")));
        // The fifth description is correct
        vca = descriptions.get(4).toVCA(getAlphabetWithoutInternals());
        assertTrue(vca.accepts(Word.fromString("aabb")));
        assertTrue(vca.accepts(Word.fromString("aaabbb")));
    }

    private VPDAlphabet<Character> getAlphabetWithInternals() {
        return new DefaultVPDAlphabet<>(Arrays.asList('c'), Arrays.asList('a'), Arrays.asList('b'));
    }

    private LimitedBehaviorGraph<Character> getWithInternals(int threshold) {
        VPDAlphabet<Character> alphabet = getAlphabetWithInternals();

        LimitedBehaviorGraph<Character> behaviorGraph = new LimitedBehaviorGraph<>(alphabet, threshold);
        Integer eps = behaviorGraph.addInitialState();
        Integer c = behaviorGraph.addState();
        behaviorGraph.setStateLevel(eps, 0);
        behaviorGraph.setStateLevel(c, 0);

        behaviorGraph.setTransition(eps, (Character) 'c', c);
        behaviorGraph.setTransition(c, (Character) 'c', c);

        if (threshold >= 1) {
            Integer a = behaviorGraph.addState();
            Integer ac = behaviorGraph.addState();
            Integer acb = behaviorGraph.addState(true);
            Integer ca = behaviorGraph.addState();
            behaviorGraph.setStateLevel(a, 1);
            behaviorGraph.setStateLevel(ac, 1);
            behaviorGraph.setStateLevel(acb, 0);
            behaviorGraph.setStateLevel(ca, 1);

            behaviorGraph.setTransition(eps, (Character) 'a', a);
            behaviorGraph.setTransition(a, (Character) 'c', ac);
            behaviorGraph.setTransition(ac, (Character) 'b', acb);
            behaviorGraph.setTransition(ac, (Character) 'c', ac);
            behaviorGraph.setTransition(acb, (Character) 'a', ca);
            behaviorGraph.setTransition(acb, (Character) 'c', c);
            behaviorGraph.setTransition(c, (Character) 'a', ca);
            behaviorGraph.setTransition(ca, (Character) 'b', c);
            behaviorGraph.setTransition(ca, (Character) 'c', ca);

            if (threshold >= 2) {
                Integer aa = behaviorGraph.addState();
                Integer aacb = behaviorGraph.addState();
                Integer aac = behaviorGraph.addState();
                Integer caa = behaviorGraph.addState();
                behaviorGraph.setStateLevel(aa, 2);
                behaviorGraph.setStateLevel(aac, 2);
                behaviorGraph.setStateLevel(aacb, 1);
                behaviorGraph.setStateLevel(caa, 2);

                behaviorGraph.setTransition(a, (Character) 'a', aa);
                behaviorGraph.setTransition(aa, (Character) 'c', aac);
                behaviorGraph.setTransition(ac, (Character) 'a', caa);
                behaviorGraph.setTransition(aac, (Character) 'b', aacb);
                behaviorGraph.setTransition(aac, (Character) 'c', aac);
                behaviorGraph.setTransition(aacb, (Character) 'a', caa);
                behaviorGraph.setTransition(aacb, (Character) 'b', acb);
                behaviorGraph.setTransition(aacb, (Character) 'c', ca);
                behaviorGraph.setTransition(ca, (Character) 'a', caa);
                behaviorGraph.setTransition(caa, (Character) 'b', ca);
                behaviorGraph.setTransition(caa, (Character) 'c', caa);

                if (threshold >= 3) {
                    Integer aaa = behaviorGraph.addState();
                    Integer aaac = behaviorGraph.addState();
                    Integer aaacb = behaviorGraph.addState();
                    Integer caaa = behaviorGraph.addState();
                    behaviorGraph.setStateLevel(aaa, 3);
                    behaviorGraph.setStateLevel(aaac, 3);
                    behaviorGraph.setStateLevel(aaacb, 2);
                    behaviorGraph.setStateLevel(caaa, 3);

                    behaviorGraph.setTransition(aa, (Character) 'a', aaa);
                    behaviorGraph.setTransition(aaa, (Character) 'c', aaac);
                    behaviorGraph.setTransition(aac, (Character) 'a', caaa);
                    behaviorGraph.setTransition(aaac, (Character) 'b', aaacb);
                    behaviorGraph.setTransition(aaac, (Character) 'c', aaac);
                    behaviorGraph.setTransition(aaacb, (Character) 'a', caaa);
                    behaviorGraph.setTransition(aaacb, (Character) 'b', aacb);
                    behaviorGraph.setTransition(aaacb, (Character) 'c', caa);
                    behaviorGraph.setTransition(caa, (Character) 'a', caaa);
                    behaviorGraph.setTransition(caaa, (Character) 'b', caa);
                    behaviorGraph.setTransition(caaa, (Character) 'c', caaa);

                    if (threshold >= 4) {
                        Integer aaaa = behaviorGraph.addState();
                        Integer aaaac = behaviorGraph.addState();
                        Integer aaaacb = behaviorGraph.addState();
                        Integer caaaa = behaviorGraph.addState();
                        behaviorGraph.setStateLevel(aaaa, 4);
                        behaviorGraph.setStateLevel(aaaac, 4);
                        behaviorGraph.setStateLevel(aaaacb, 3);
                        behaviorGraph.setStateLevel(caaaa, 4);

                        behaviorGraph.setTransition(aaa, (Character) 'a', aaaa);
                        behaviorGraph.setTransition(aaaa, (Character) 'c', aaaac);
                        behaviorGraph.setTransition(aaac, (Character) 'a', caaaa);
                        behaviorGraph.setTransition(aaaac, (Character) 'b', aaaacb);
                        behaviorGraph.setTransition(aaaac, (Character) 'c', aaaac);
                        behaviorGraph.setTransition(aaaacb, (Character) 'a', caaaa);
                        behaviorGraph.setTransition(aaaacb, (Character) 'b', aaacb);
                        behaviorGraph.setTransition(aaaacb, (Character) 'c', caaa);
                        behaviorGraph.setTransition(caaa, (Character) 'a', caaaa);
                        behaviorGraph.setTransition(caaaa, (Character) 'b', caaa);
                        behaviorGraph.setTransition(caaaa, (Character) 'c', caaaa);

                        if (threshold >= 5) {
                            Integer aaaaa = behaviorGraph.addState();
                            Integer aaaaac = behaviorGraph.addState();
                            Integer aaaaacb = behaviorGraph.addState();
                            Integer caaaaa = behaviorGraph.addState();
                            behaviorGraph.setStateLevel(aaaaa, 4);
                            behaviorGraph.setStateLevel(aaaaac, 4);
                            behaviorGraph.setStateLevel(aaaaacb, 3);
                            behaviorGraph.setStateLevel(caaaaa, 4);

                            behaviorGraph.setTransition(aaaa, (Character) 'a', aaaaa);
                            behaviorGraph.setTransition(aaaaa, (Character) 'c', aaaaac);
                            behaviorGraph.setTransition(aaaac, (Character) 'a', caaaaa);
                            behaviorGraph.setTransition(aaaaac, (Character) 'b', aaaaacb);
                            behaviorGraph.setTransition(aaaaac, (Character) 'c', aaaaac);
                            behaviorGraph.setTransition(aaaaacb, (Character) 'a', caaaaa);
                            behaviorGraph.setTransition(aaaaacb, (Character) 'b', aaaacb);
                            behaviorGraph.setTransition(aaaaacb, (Character) 'c', caaaa);
                            behaviorGraph.setTransition(caaaa, (Character) 'a', caaaaa);
                            behaviorGraph.setTransition(caaaaa, (Character) 'b', caaaa);
                            behaviorGraph.setTransition(caaaaa, (Character) 'c', caaaaa);
                        }
                    }
                }
            }
        }

        return behaviorGraph;
    }

    @Test
    public void testWithInternals() {
        // L = {a^n b^m c^n | n, m > 0}
        // Smallest offset and period possible: (2, 1)
        // Therefore, the behavior graph must complete up to at least the level 3
        // So, we need a threshold of 4

        LimitedBehaviorGraph<Character> behaviorGraph = getWithInternals(0);
        List<Description<Character>> descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(0, descriptions.size());

        behaviorGraph = getWithInternals(1);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(0, descriptions.size());

        behaviorGraph = getWithInternals(2);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions);
        assertEquals(0, descriptions.size());

        behaviorGraph = getWithInternals(3);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        assertNotNull(descriptions.size());
        assertEquals(1, descriptions.size());
        // The description is correct up to level 1
        VCA<Character> vca = descriptions.get(0).toVCA(getAlphabetWithInternals());
        assertFalse(vca.accepts(Word.epsilon()));
        assertTrue(vca.accepts(Word.fromString("acb")));
        assertTrue(vca.accepts(Word.fromString("acccccb")));
        assertFalse(vca.accepts(Word.fromString("a")));
        assertFalse(vca.accepts(Word.fromString("c")));
        assertFalse(vca.accepts(Word.fromString("ab")));
        assertFalse(vca.accepts(Word.fromString("ac")));
        assertFalse(vca.accepts(Word.fromString("abc")));
        assertFalse(vca.accepts(Word.fromString("cab")));
        // But is incorrect from level 2
        assertFalse(vca.accepts(Word.fromString("aacbb"))); // Should be accepted

        behaviorGraph = getWithInternals(4);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        assertEquals(3, descriptions.size());
        // The first two descriptions are incorrect
        vca = descriptions.get(0).toVCA(getAlphabetWithInternals());
        assertFalse(vca.accepts(Word.fromString("aacbb"))); // Should be accepted
        vca = descriptions.get(1).toVCA(getAlphabetWithInternals());
        assertTrue(vca.accepts(Word.fromString("acb")));
        assertFalse(vca.accepts(Word.fromString("aaacbbb"))); // Should be accepted

        // The third description is correct
        vca = descriptions.get(2).toVCA(getAlphabetWithInternals());
        assertTrue(vca.accepts(Word.fromString("aacbb")));
        assertTrue(vca.accepts(Word.fromString("aaacbbb")));
        assertTrue(vca.accepts(Word.fromString("accccb")));
        assertFalse(vca.accepts(Word.fromString("ca")));
        assertFalse(vca.accepts(Word.fromString("ab")));
        assertFalse(vca.accepts(Word.fromString("abc")));
        assertFalse(vca.accepts(Word.fromString("cab")));
        assertFalse(vca.accepts(Word.fromString("aacbcb")));
        assertFalse(vca.accepts(Word.fromString("acabb")));
    }
}