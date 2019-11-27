package be.uantwerpen.learningvca.examples;

import java.util.Arrays;
import java.util.Collections;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

/**
 * m-VCA for L = {a^n b^n | n >= m} where 'a' is a call symbol and 'b' a return symbol.
 * 
 * The m is fixed when the VCA is constructed.
 */
public final class ExampleVariableThreshold {
    /**
     * Gets the pushdown alphabet with 'a' as call and 'b' as return
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<>(Collections.emptyList(), Arrays.asList('a'), Arrays.asList('b'));
    }

    /**
     * Constructs a m-VCA for L = {a^n b^n | n >= m}
     * @return A m-VCA
     */
    public static VCA<?, Character> getVCA(int threshold) {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), threshold);

        Location q0 = vca.addInitialLocation();
        Location q1 = vca.addLocation(true);

        for (int i = 0 ; i <= threshold ; i++) {
            vca.setSuccessor(q0, i, 'a', q0);
        }
        vca.setSuccessor(q0, threshold, 'b', q1);
        for (int i = 0 ; i <= threshold ; i++) {
            vca.setSuccessor(q1, i, 'b', q1);
        }

        return vca;
    }
}