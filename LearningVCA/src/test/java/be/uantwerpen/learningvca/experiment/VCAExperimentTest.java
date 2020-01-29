/*
 * LearningVCA - An implementation of an active learning algorithm for Visibly One-Counter Automata
 * Copyright (C) 2020 University of Mons and University of Antwerp
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.uantwerpen.learningvca.experiment;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import be.uantwerpen.learningvca.examples.ExampleVariableThreshold;
import be.uantwerpen.learningvca.examples.ExampleFourDeltas;
import be.uantwerpen.learningvca.examples.ExampleInternalLoop;
import be.uantwerpen.learningvca.examples.ExampleRegular;
import be.uantwerpen.learningvca.examples.ExampleTwoCalls;
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
    private <I extends Comparable<I>> VCA<?, I> execute(VCA<?, I> sul) {
        VPDAlphabet<I> alphabet = sul.getAlphabet();

        MembershipOracle<I, Boolean> membershipOracle = new SimulatorOracle<>(sul);
        PartialEquivalenceOracle<I> partialEquivalenceOracle = new PartialEquivalenceOracle<>(sul);
        EquivalenceVCAOracle<I> equivalenceVCAOracle = new EquivalenceVCAOracle<>(sul);

        LearnerVCA<I> learner = new LearnerVCA<>(alphabet, membershipOracle, partialEquivalenceOracle);

        VCAExperiment<I> experiment = new VCAExperiment<>(learner, equivalenceVCAOracle, alphabet);
        VCA<?, I> answer = experiment.run();
        assertNotNull(answer);

        assertNull(equivalenceVCAOracle.findCounterExample(answer, alphabet));
        return answer;
    }

    @Test
    public void testWithoutInternals() {
        VCA<?, Character> vca = ExampleWithoutInternals.getVCA();
        VCA<?, Character> answer = execute(vca);

        assertTrue(answer.accepts(Word.epsilon()));
        for (int i = 1 ; i <= 100 ; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0 ; j < i ; j++) {
                builder.append('a');
            }
            for (int j = 0 ; j < i ; j++) {
                builder.append('b');
            }
            assertTrue(answer.accepts(Word.fromString(builder.toString())));
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
                    assertFalse(answer.accepts(Word.fromString(builder.toString())));
                }
            }
        }
    }

    @Test
    public void testWithInternals() {
        VCA<?, Character> vca = ExampleWithInternals.getVCA();
        VCA<?, Character> answer = execute(vca);

        assertFalse(answer.accepts(Word.epsilon()));
        for (int i = 1 ; i <= 100 ; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0 ; j < i ; j++) {
                builder.append('a');
            }
            builder.append('c');
            for (int j = 0 ; j < i ; j++) {
                builder.append('b');
            }
            assertTrue(answer.accepts(Word.fromString(builder.toString())));
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
                    assertFalse(answer.accepts(Word.fromString(builder.toString())));
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
                    assertFalse(answer.accepts(Word.fromString(builder.toString())));
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
                    assertFalse(answer.accepts(Word.fromString(builder.toString())));
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
                    assertFalse(answer.accepts(Word.fromString(builder.toString())));
                }
            }
        }
    }

    @Test
    public void testTwoCalls() {
        // Warning: this test takes a lot of time
        VCA<?, Character> vca = ExampleTwoCalls.getVCA();
        execute(vca);
    }

    @Test
    public void testRegular() {
        VCA<?, Character> vca = ExampleRegular.getVCA();
        execute(vca);
    }

    @Test
    public void testCallLoop() {
        VCA<?, Character> vca = ExampleInternalLoop.getVCA();
        execute(vca);
    }

    @Test
    public void testFourDeltas()  {
        execute(ExampleFourDeltas.getVCA());
    }

    @Test
    public void testVariableThreshold() {
        execute(ExampleVariableThreshold.getVCA(20));
    }
}