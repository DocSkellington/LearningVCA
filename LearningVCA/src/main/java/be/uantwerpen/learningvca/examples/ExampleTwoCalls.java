package be.uantwerpen.learningvca.examples;

import java.util.Arrays;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class ExampleTwoCalls {
    /**
     * Gets the pushdown alphabet with 'b' and 'd' as call symbols, 'f' as return symbol, and 'a', 'c' and 'e' as internal symbols
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<>(Arrays.asList('a', 'c', 'e'), Arrays.asList('b', 'd'), Arrays.asList('f'));
    }

    /**
     * Constructs a simple 1-VCA for L = {a b^n c d^m e f^{n+m} | n, m > 0}
     * @return A 1-VCA
     */
    public static VCA<?, Character> getVCA() {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), 1);

        var q0 = vca.addInitialLocation();
        var q1 = vca.addLocation();
        var q2 = vca.addLocation();
        var q3 = vca.addLocation();
        var q4 = vca.addLocation(true);

        vca.setSuccessor(q0, 0, 'a', q1);
        vca.setSuccessor(q1, 0, 'b', q1);
        vca.setSuccessor(q1, 1, 'b', q1);
        vca.setSuccessor(q1, 1, 'c', q2);
        vca.setSuccessor(q2, 1, 'd', q3);
        vca.setSuccessor(q3, 1, 'd', q3);
        vca.setSuccessor(q3, 1, 'e', q4);
        vca.setSuccessor(q4, 1, 'f', q4);

        return vca;
    }
}