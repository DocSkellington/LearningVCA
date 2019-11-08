package be.uantwerpen.learningvca.examples;

import java.util.Arrays;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class ExampleWithInternals {
    /**
     * Gets the pushdown alphabet with 'a' as call symbol, 'b' as return symbol and 'c' as internal symbol
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<Character>(Arrays.asList('c'), Arrays.asList('a'), Arrays.asList('b'));
    }

    /**
     * Constructs a simple 1-VCA for L = {a^n c b^n | n is a natural and n is not zero}
     * @return A 1-VCA
     */
    public static VCA<?, Character> getVCA() {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), 1);

        Location q0 = vca.addInitialLocation(true);
        Location q1 = vca.addLocation(true);

        vca.setCallSuccessor(q0, 0, 'a', q0);
        vca.setCallSuccessor(q0, 1, 'a', q0);
        vca.setInternalSuccessor(q0, 1, 'c', q1);
        vca.setReturnSuccessor(q1, 1, 'b', q1);

        return vca;
    }
}