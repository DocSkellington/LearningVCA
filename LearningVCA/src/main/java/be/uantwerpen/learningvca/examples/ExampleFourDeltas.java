package be.uantwerpen.learningvca.examples;

import java.util.Arrays;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

/**
 * 3-VCA for L = {a^n b^m c b^2 | n, m, l > 0 such that n - m = 2} UNION {a^n b^n | n > 0} where 'a' is a call symbol, 'b' a return symbol and 'c' an internal symbol
 */
public final class ExampleFourDeltas {
    /**
     * Gets the pushdown alphabet with 'a' as call, 'b' as return and 'c' as internal
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<>(Arrays.asList('c'), Arrays.asList('a'), Arrays.asList('b'));
    }

    /**
     * Constructs a 3-VCA for L = {a^n b^m c b^2 | n, m, l > 0 such that n - m = 2} UNION {a^n b^n | n > 0}
     * @return A 3-VCA
     */
    public static VCA<?, Character> getVCA() {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), 3);

        Location q0 = vca.addInitialLocation();
        Location q1 = vca.addLocation(true);
        Location q2 = vca.addLocation();
        Location q3 = vca.addLocation();
        Location q4 = vca.addLocation(true);
        Location q5 = vca.addLocation();
        Location q6 = vca.addLocation(true);

        vca.setSuccessor(q0, 0, 'a', q0);
        vca.setSuccessor(q0, 1, 'a', q0);
        vca.setSuccessor(q0, 2, 'a', q2);
        vca.setSuccessor(q0, 1, 'b', q1);
        vca.setSuccessor(q0, 2, 'b', q1);
        vca.setSuccessor(q1, 1, 'b', q1);

        vca.setSuccessor(q2, 3, 'a', q2);
        vca.setSuccessor(q2, 3, 'b', q3);
        vca.setSuccessor(q3, 2, 'b', q4);
        vca.setSuccessor(q3, 3, 'b', q3);
        vca.setSuccessor(q3, 2, 'c', q5);
        vca.setSuccessor(q4, 1, 'b', q4);
        vca.setSuccessor(q5, 2, 'b', q6);
        vca.setSuccessor(q5, 2, 'c', q5);
        vca.setSuccessor(q6, 1, 'b', q6);

        return vca;
    }
}