package be.uantwerpen.learningvca.experiment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import be.uantwerpen.learningvca.behaviorgraph.BehaviorGraph;
import be.uantwerpen.learningvca.behaviorgraph.Description;
import be.uantwerpen.learningvca.behaviorgraph.TauMapping;
import be.uantwerpen.learningvca.learner.LearnerVCA;
import be.uantwerpen.learningvca.oracles.EquivalenceVCAOracle;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.VCA;
import be.uantwerpen.learningvca.vca.Location;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class VCAExperimentTest {
    @Test
    public void testWithoutInternals() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<Character>(Collections.emptyList(), Arrays.asList('a'), Arrays.asList('b'));
        DefaultVCA<Character> vca = new DefaultVCA<>(alphabet, 1);

        Location q0 = vca.addInitialLocation(true);
        Location q1 = vca.addLocation(true);

        vca.setCallSuccessor(q0, 0, 'a', q0);
        vca.setCallSuccessor(q0, 1, 'a', q0);
        vca.setReturnSuccessor(q0, 1, 'b', q1);
        vca.setReturnSuccessor(q1, 1, 'b', q1);

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
        
        BehaviorGraph<Character> bg = new BehaviorGraph<>(alphabet, description);

        MembershipOracle<Character, Boolean> membershipOracle = new SimulatorOracle<>(vca);
        PartialEquivalenceOracle<Character> partialEquivalenceOracle = new PartialEquivalenceOracle<>(bg);
        EquivalenceVCAOracle<Character> equivalenceVCAOracle = new EquivalenceVCAOracle<>(vca);

        LearnerVCA<Character> learner = new LearnerVCA<>(alphabet, membershipOracle, partialEquivalenceOracle);

        VCAExperiment<Character> experiment = new VCAExperiment<>(learner, equivalenceVCAOracle, alphabet);
        VCA<?, Character> answer = experiment.run();
        assertNotNull(answer);

        assertNull(equivalenceVCAOracle.findCounterExample(answer, alphabet));

        assertTrue(answer.accepts(Word.epsilon()));
    }
}