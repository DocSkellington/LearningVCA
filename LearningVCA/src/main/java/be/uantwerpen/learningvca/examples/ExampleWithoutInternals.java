package be.uantwerpen.learningvca.examples;

import java.util.Arrays;
import java.util.Collections;

import be.uantwerpen.learningvca.behaviorgraph.BehaviorGraph;
import be.uantwerpen.learningvca.behaviorgraph.Description;
import be.uantwerpen.learningvca.behaviorgraph.TauMapping;
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
     * Constructs a simple 1-VCA for L = {a^n b^n | n is a natural and n is not zero}
     * @return A 1-VCA
     */
    public static VCA<?, Character> getVCA() {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), 1);

        Location q0 = vca.addInitialLocation(true);
        Location q1 = vca.addLocation(true);

        vca.setCallSuccessor(q0, 0, 'a', q0);
        vca.setCallSuccessor(q0, 1, 'a', q0);
        vca.setReturnSuccessor(q0, 1, 'b', q1);
        vca.setReturnSuccessor(q1, 1, 'b', q1);

        return vca;
    }

    /**
     * Constructs the behavior graph for L = {a^n b^n  | n is a natural and n is not zero}
     * @return The behavior graph
     */
    public static BehaviorGraph<Character> getBehaviorGraph() {
        TauMapping<Character> tau0 = new TauMapping<>(3);
        tau0.addTransition(1, 'a', 1);
        tau0.addTransition(2, 'a', 3);
        tau0.addTransition(3, 'a', 3);

        TauMapping<Character> tau1 = new TauMapping<>(3);
        tau1.addTransition(1, 'a', 1);
        tau1.addTransition(1, 'b', 2);
        tau1.addTransition(2, 'a', 3);
        tau1.addTransition(2, 'b', 2);
        tau1.addTransition(3, 'a', 3);
        tau1.addTransition(3, 'b', 3);

        Description<Character> description = new Description<>(1, 1, 3);
        description.addTauMappings(Arrays.asList(tau0, tau1));
        description.setInitialState(0, 1);
        description.addAcceptingState(0, 1);
        description.addAcceptingState(0, 2);
        
        BehaviorGraph<Character> bg = new BehaviorGraph<>(getAlphabet(), description);
        return bg;
    }
}