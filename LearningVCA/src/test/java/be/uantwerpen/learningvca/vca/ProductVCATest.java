package be.uantwerpen.learningvca.vca;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class ProductVCATest {
    @Test
    public void testProduct() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('c'), Arrays.asList('a'), Arrays.asList('b'));
        DefaultVCA<Character> vca1 = new DefaultVCA<>(alphabet, 0);
        vca1.addInitialLocation(true);

        DefaultVCA<Character> vca2 = new DefaultVCA<>(alphabet, 1);
        Location q0 = vca2.addInitialLocation(false);
        Location q1 = vca2.addLocation(true);
        vca2.setSuccessor(q0, 0, 'a', q0);
        vca2.setSuccessor(q0, 1, 'a', q0);
        vca2.setSuccessor(q0, 1, 'b', q1);
        vca2.setSuccessor(q1, 1, 'b', q1);

        VCA<?, Character> vca = new ProductVCA<>(alphabet, vca1, vca2, AcceptanceCombiner.XOR);
        Word<Character> w = vca.getAcceptedWord();
        assertNotNull(w);
        assertTrue(vca.accepts(w));
    }
}