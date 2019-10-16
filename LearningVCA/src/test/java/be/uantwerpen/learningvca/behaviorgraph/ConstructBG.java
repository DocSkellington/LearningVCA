package be.uantwerpen.learningvca.behaviorgraph;

import java.util.Arrays;

import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class ConstructBG {
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<>(Arrays.asList(), Arrays.asList('a'), Arrays.asList('b'));
    }

    public static Description<Character> constructDescription() {
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

        Description<Character> description = new Description<>(1, 1, 2);
        description.addTauMappings(Arrays.asList(tau0, tau1));
        description.setInitialState(0, 1);
        description.addAcceptingState(0, 1);
        description.addAcceptingState(0, 2);
        return description;
    }

    public static BehaviorGraph<Character> constructBGExample() {
        BehaviorGraph<Character> bg = new BehaviorGraph<>(getAlphabet(), constructDescription());
        return bg;
    }
}