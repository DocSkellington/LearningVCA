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
package be.uantwerpen.learningvca;

import java.io.FileWriter;
import java.io.IOException;

import be.uantwerpen.learningvca.examples.ExampleWithInternals;
import be.uantwerpen.learningvca.experiment.VCAExperiment;
import be.uantwerpen.learningvca.learner.LearnerVCA;
import be.uantwerpen.learningvca.observationtable.writer.StratifiedObservationTableASCIIWriter;
import be.uantwerpen.learningvca.observationtable.writer.StratifiedObservationTableHTMLWriter;
import be.uantwerpen.learningvca.oracles.EquivalenceVCAOracle;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.VPDAlphabet;

/**
 * The main class
 * 
 * @author Gaëtan Staquet
 */
public class LearningVCA {
    private LearningVCA() {

    }

    public static void main(String[] args) throws IOException {
        VCA<?, Character> sul = ExampleWithInternals.getVCA();
        VPDAlphabet<Character> alphabet = sul.getAlphabet();

        MembershipOracle<Character, Boolean> membershipOracle = new SimulatorOracle<>(sul);
        CounterOracle<Character, Boolean> membershipOracleCounter = new CounterOracle<>(membershipOracle, "membership queries");
        PartialEquivalenceOracle<Character> partialEquivalenceOracle = new PartialEquivalenceOracle<>(sul);
        EquivalenceVCAOracle<Character> equivalenceVCAOracle = new EquivalenceVCAOracle<>(sul);

        LearnerVCA<Character> learner = new LearnerVCA<>(alphabet, membershipOracleCounter, partialEquivalenceOracle);

        VCAExperiment<Character> experiment = new VCAExperiment<>(learner, equivalenceVCAOracle, alphabet);
        experiment.setLog(true);
        experiment.setLogModels(true);
        experiment.setProfile(true);
        VCA<?, Character> answer = experiment.run();

        System.out.println("-------------------------------------------------------");

        System.out.println(SimpleProfiler.getResults());
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(membershipOracleCounter.getStatisticalData().getSummary());

        System.out.println("States: " + answer.size());
        System.out.println("Sigma: " + alphabet.size());

        GraphDOT.write(answer, new FileWriter("output.dot"));
        System.out.println();
        System.out.println("Model: ");
        GraphDOT.write(answer, System.out);

        System.out.println("-------------------------------------------------------");
        
        System.out.println("Final observation table: ");
        new StratifiedObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);

        StratifiedObservationTableHTMLWriter.displayHTMLInBrowser(learner.getObservationTable());
    }
}
