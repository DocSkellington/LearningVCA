package be.uantwerpen.learningvca.behaviorgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.Word;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class DescriptionToVCATest {

    @Test
    public void descriptionToVCA() {
        Description<Character> description = ConstructBG.constructDescription();

        VCA<?, Character> vca = description.toVCA(ConstructBG.getAlphabet());

        assertEquals(2, vca.size());

        assertTrue(vca.accepts(Arrays.asList()));
        assertTrue(vca.accepts(Arrays.asList('a', 'b')));
        assertTrue(vca.accepts(Arrays.asList('a', 'a', 'b', 'b')));
        assertTrue(vca.accepts(Arrays.asList('a', 'a', 'a', 'b', 'b', 'b')));

        assertFalse(vca.accepts(Arrays.asList('a')));
        assertFalse(vca.accepts(Arrays.asList('a', 'a')));
        assertFalse(vca.accepts(Arrays.asList('a', 'b', 'a')));
        assertFalse(vca.accepts(Arrays.asList('a', 'b', 'b')));
        assertFalse(vca.accepts(Arrays.asList('b')));
    }

    // This test comes from a description from the execution
    @Test
    public void exampleFromExecution() {
        TauMapping<Character> t0 = new TauMapping<>(3);
        t0.addTransition(1, 'a', 1);
        t0.addTransition(2, 'a', 2);
        t0.addTransition(3, 'a', 2);

        TauMapping<Character> t1 = new TauMapping<>(3);
        t1.addTransition(1, 'a', 1);
        t1.addTransition(1, 'b', 2);
        t1.addTransition(2, 'a', 2);
        t1.addTransition(2, 'b', 3);
        t1.addTransition(3, 'a', 2);
        t1.addTransition(3, 'b', 2);

        TauMapping<Character> t2 = new TauMapping<>(3);
        t2.addTransition(1, 'a', 1);
        t2.addTransition(1, 'b', 3);
        t2.addTransition(2, 'a', 2);
        t2.addTransition(2, 'b', 2);
        t2.addTransition(3, 'a', 2);
        t2.addTransition(3, 'b', 3);

        Description<Character> description = new Description<>(2, 1, 3);
        description.addTauMappings(Arrays.asList(t0, t1, t2));
        description.setInitialState(0, 1);
        description.addAcceptingState(0, 1);
        description.addAcceptingState(0, 2);

        VCA<?, Character> vca = description.toVCA(new DefaultVPDAlphabet<>(Collections.emptyList(), Arrays.asList('a'), Arrays.asList('b')));

        assertTrue(vca.accepts(Word.epsilon()));
        for (int i = 1 ; i <= 100 ; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0 ; j < i ; j++) {
                builder.append('a');
            }
            for (int j = 0 ; j < i ; j++) {
                builder.append('b');
            }
            assertTrue(vca.accepts(Word.fromString(builder.toString())));
        }

        // Testing a lot of words to be rejected
        for (int i = 1 ; i <= 50 ; i++) {
            for (int j = 1 ; j <= i ; j++) {
                for (int k = 1 ; k <= i ; k++) {
                    StringBuilder builder = new StringBuilder();
                    
                    for (int l = 1 ; l <= j ; l++) {
                        builder.append('a');
                    }
                    if (k == j) {
                        continue;
                    }
                    for (int l = 1 ; l <= k ; l++) {
                        builder.append('b');
                    }
                    assertFalse(vca.accepts(Word.fromString(builder.toString())));
                }
            }
        }
    }

    @Test
    public void descriptionOfRegular() {
        // A regular language implies that offset = 1 and period = 0
        TauMapping<Character> t0 = new TauMapping<>(4);
        t0.addTransition(1, (Character)'a', 2);
        t0.addTransition(1, (Character)'b', 4);
        t0.addTransition(2, (Character)'a', 2);
        t0.addTransition(2, (Character)'b', 3);
        t0.addTransition(3, (Character)'a', 4);
        t0.addTransition(3, (Character)'b', 3);
        t0.addTransition(4, (Character)'a', 4);
        t0.addTransition(4, (Character)'b', 4);

        Description<Character> description = new Description<>(1, 0, 4);
        description.addTauMappings(Arrays.asList(t0));
        description.addAcceptingState(0, 3);
        description.setInitialState(0, 1);
        VCA<?, Character> vca = description.toVCA(new DefaultVPDAlphabet<>(Arrays.asList('a', 'b'), Collections.emptyList(), Collections.emptyList()));
        assertTrue(vca.accepts(Word.fromString("aabbb")));
        assertTrue(vca.accepts(Word.fromString("ab")));
        assertFalse(vca.accepts(Word.fromString("aba")));
        assertFalse(vca.accepts(Word.fromString("b")));
        assertFalse(vca.accepts(Word.fromString("a")));
        assertFalse(vca.accepts(Word.fromString("ba")));
        assertFalse(vca.accepts(Word.epsilon()));
    }
}