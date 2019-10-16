package be.uantwerpen.learningvca.behaviorgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class BehaviorGraphTest {
    // @Test
    public void toDFAOneStateNoTransitions() {
        TauMapping<Character> tau0 = new TauMapping<>(1);
        Description<Character> description = new Description<>(0, 1);
        description.addTauMappings(Arrays.asList(tau0));
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('a'), Arrays.asList(), Arrays.asList());

        BehaviorGraph<Character> bg = new BehaviorGraph<>(alphabet, description);
        bg.setInitialState(0, 1);

        CompactDFA<Character> dfa = bg.toDFA(0);

        assertEquals(dfa.size(), 1);
        assertFalse(dfa.isAccepting(0));
        assertNull(dfa.getTransition(0, alphabet.getSymbolIndex('a')));
    }

    @Test
    public void toDFAExample() throws IOException {
        TauMapping<Character> tau0 = new TauMapping<>(2);
        tau0.addTransition(1, 'a', 1);
        // tau0.addTransition(2, 'a', 3);
        // tau0.addTransition(3, 'a', 3);

        TauMapping<Character> tau1 = new TauMapping<>(2);
        tau1.addTransition(1, 'a', 1);
        tau1.addTransition(1, 'b', 2);
        // tau1.addTransition(2, 'a', 3);
        tau1.addTransition(2, 'b', 2);
        // tau1.addTransition(3, 'a', 3);
        // tau1.addTransition(3, 'b', 3);

        Description<Character> description = new Description<>(1, 1);
        description.addTauMappings(Arrays.asList(tau0, tau1));

        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList(), Arrays.asList('a'), Arrays.asList('b'));
        
        BehaviorGraph<Character> bg = new BehaviorGraph<>(alphabet, description);
        bg.setInitialState(0, 1);
        CompactDFA<Character> dfa = bg.toDFA(2);

        System.out.println(dfa.size());

        FileWriter file = new FileWriter(new File("test.dot"));
        GraphDOT.write(dfa, alphabet, file);
    }
}