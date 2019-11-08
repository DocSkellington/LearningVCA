package be.uantwerpen.learningvca.experiment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import be.uantwerpen.learningvca.examples.ExampleWithInternals;
import be.uantwerpen.learningvca.examples.ExampleWithoutInternals;
import be.uantwerpen.learningvca.learner.LearnerVCA;
import be.uantwerpen.learningvca.oracles.EquivalenceVCAOracle;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

public class VCAExperimentTest {
    @Test
    public void testWithoutInternals() {
        VPDAlphabet<Character> alphabet = ExampleWithoutInternals.getAlphabet();
        VCA<?, Character> vca = ExampleWithoutInternals.getVCA();

        MembershipOracle<Character, Boolean> membershipOracle = new SimulatorOracle<>(vca);
        PartialEquivalenceOracle<Character> partialEquivalenceOracle = new PartialEquivalenceOracle<>(vca);
        EquivalenceVCAOracle<Character> equivalenceVCAOracle = new EquivalenceVCAOracle<>(vca);

        LearnerVCA<Character> learner = new LearnerVCA<>(alphabet, membershipOracle, partialEquivalenceOracle);

        VCAExperiment<Character> experiment = new VCAExperiment<>(learner, equivalenceVCAOracle, alphabet);
        VCA<?, Character> answer = experiment.run();
        assertNotNull(answer);

        assertNull(equivalenceVCAOracle.findCounterExample(answer, alphabet));

        assertTrue(vca.accepts(Word.epsilon()));
        for (int i = 1 ; i <= 100 ; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0 ; j < i ; j++) {
                builder.append('a');
            }
            for (int j = 0 ; j < i ; j++) {
                builder.append('b');
            }
            assertTrue(vca.accepts(Word.fromString(builder.toString())));
        }

        // Testing a lot of words to be rejected
        for (int i = 1 ; i <= 50 ; i++) {
            for (int j = 1 ; j <= i ; j++) {
                for (int k = 1 ; k <= i ; k++) {
                    StringBuilder builder = new StringBuilder();
                    
                    for (int l = 1 ; l <= j ; l++) {
                        builder.append('a');
                    }
                    if (k == j) {
                        continue;
                    }
                    for (int l = 1 ; l <= k ; l++) {
                        builder.append('b');
                    }
                    assertFalse(vca.accepts(Word.fromString(builder.toString())));
                }
            }
        }
    }

    @Test
    public void testWithInternals() {
        VPDAlphabet<Character> alphabet = ExampleWithInternals.getAlphabet();
        VCA<?, Character> vca = ExampleWithInternals.getVCA();

        MembershipOracle<Character, Boolean> membershipOracle = new SimulatorOracle<>(vca);
        PartialEquivalenceOracle<Character> partialEquivalenceOracle = new PartialEquivalenceOracle<>(vca);
        EquivalenceVCAOracle<Character> equivalenceVCAOracle = new EquivalenceVCAOracle<>(vca);

        LearnerVCA<Character> learner = new LearnerVCA<>(alphabet, membershipOracle, partialEquivalenceOracle);

        VCAExperiment<Character> experiment = new VCAExperiment<>(learner, equivalenceVCAOracle, alphabet);
        VCA<?, Character> answer = experiment.run();
        assertNotNull(answer);

        assertNull(equivalenceVCAOracle.findCounterExample(answer, alphabet));

        assertTrue(answer.accepts(Word.epsilon()));
        for (int i = 1 ; i <= 100 ; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0 ; j < i ; j++) {
                builder.append('a');
            }
            builder.append('c');
            for (int j = 0 ; j < i ; j++) {
                builder.append('b');
            }
            assertTrue(vca.accepts(Word.fromString(builder.toString())));
        }

        // Testing a lot of words to be rejected
        for (int i = 1 ; i <= 50 ; i++) {
            for (int j = 1 ; j <= i ; j++) {
                for (int k = 1 ; k <= i ; k++) {
                    StringBuilder builder = new StringBuilder();
                    
                    for (int l = 1 ; l <= j ; l++) {
                        builder.append('a');
                    }
                    for (int l = 1 ; l <= k ; l++) {
                        builder.append('b');
                    }
                    assertFalse(vca.accepts(Word.fromString(builder.toString())));
                }

                for (int k = 1 ; k <= i ; k++) {
                    StringBuilder builder = new StringBuilder();
                    
                    builder.append('c');
                    for (int l = 1 ; l <= j ; l++) {
                        builder.append('a');
                    }
                    for (int l = 1 ; l <= k ; l++) {
                        builder.append('b');
                    }
                    assertFalse(vca.accepts(Word.fromString(builder.toString())));
                }

                for (int k = 1 ; k <= i ; k++) {
                    StringBuilder builder = new StringBuilder();
                    
                    for (int l = 1 ; l <= j ; l++) {
                        builder.append('a');
                    }
                    for (int l = 1 ; l <= k ; l++) {
                        builder.append('b');
                    }
                    builder.append('c');
                    assertFalse(vca.accepts(Word.fromString(builder.toString())));
                }

                for (int k = 1 ; k <= i ; k++) {
                    StringBuilder builder = new StringBuilder();
                    
                    for (int l = 1 ; l <= j ; l++) {
                        builder.append('a');
                    }
                    for (int l = 1 ; l <= Math.max(2, j + k) ; l++) {
                        builder.append('c');
                    }
                    for (int l = 1 ; l <= k ; l++) {
                        builder.append('b');
                    }
                    assertFalse(vca.accepts(Word.fromString(builder.toString())));
                }
            }
        }
    }
}