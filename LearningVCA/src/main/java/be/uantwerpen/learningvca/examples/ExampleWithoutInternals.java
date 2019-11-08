package be.uantwerpen.learningvca.examples;

import java.util.Arrays;
import java.util.Collections;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class ExampleWithoutInternals {
    /**
     * Gets the pushdown alphabet with 'a' as call symbol and 'b' as return symbol
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<Character>(Collections.emptyList(), Arrays.asList('a'), Arrays.asList('b'));
    }

    /**
     * Constructs a simple 1-VCA for L = {a^n b^n | n >= 0}
     * @return A 1-VCA
     */
    public static VCA<?, Character> getVCA() {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), 1);

        Location q0 = vca.addInitialLocation(true);
        Location q1 = vca.addLocation(true);

        vca.setSuccessor(q0, 0, 'a', q0);
        vca.setSuccessor(q0, 1, 'a', q0);
        vca.setSuccessor(q0, 1, 'b', q1);
        vca.setSuccessor(q1, 1, 'b', q1);

        return vca;
    }
}