package be.uantwerpen.learningvca.behaviorgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ArrayAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class LimitedBehaviorGraphTest {
    @Test
    public void testRegularLanguage() {
        // TODO
    }

    @Test
    public void testWithoutInternals() {
        // L = {a^n b^n | n > 0}
        // Shortest offset and period: (1, 1)
        // So, the threshold sould be at least 1 + 2*1 = 3
    }

    public LimitedBehaviorGraph<Character> getWithInternals(int threshold) {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('c'), Arrays.asList('a'),
                Arrays.asList('b'));

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
                        behaviorGraph.setStateLevel(aaaa, 3);
                        behaviorGraph.setStateLevel(aaaac, 3);
                        behaviorGraph.setStateLevel(aaaacb, 2);
                        behaviorGraph.setStateLevel(caaaa, 3);

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
                            behaviorGraph.setStateLevel(aaaaa, 3);
                            behaviorGraph.setStateLevel(aaaaac, 3);
                            behaviorGraph.setStateLevel(aaaaacb, 2);
                            behaviorGraph.setStateLevel(caaaaa, 3);

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
                    } else if (threshold > 4) {
                        throw new InvalidParameterException("Can only construct up to level 4");
                    }
                }
            }
        }

        return behaviorGraph;
    }

    @Test
    public void testWithInternals() throws IOException {
        // L = {a^n b^m c^n | n, m > 0}
        // Smallest offset and period possible: (2, 1)
        // Therefore, we need at least a threshold of 2 + 2*1 = 4

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
        assertEquals(0, descriptions.size());

        behaviorGraph = getWithInternals(5);
        descriptions = behaviorGraph.getPeriodicDescriptions();
        System.out.println(descriptions);
    }
}