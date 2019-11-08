package be.uantwerpen.learningvca.experiment;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import be.uantwerpen.learningvca.learner.LearnerVCA;
import be.uantwerpen.learningvca.oracles.EquivalenceVCAOracle;
import be.uantwerpen.learningvca.util.ComputeCounterValue;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * An experiment for a VCA.
 * 
 * Since, the {@code Experiment} class declares everything as {@code private}, we need to redefine everything ourself.
 * @param <I>
 * @author Gaëtan Staquet
 */
@ParametersAreNonnullByDefault
public class VCAExperiment<I extends Comparable<I>> {

    public static final String LEARNING_PROFILE_KEY = "Learning";
    public static final String COUNTEREXAMPLE_PROFILE_KEY = "Searching for counterexample";
    
    protected static final LearnLogger LOGGER = LearnLogger.getLogger(VCAExperiment.class);

    protected final VCAExperimentImpl impl;
    protected boolean log = false;
    protected boolean logModels = false;
    protected boolean profile = false;
    protected final Counter rounds = new Counter("learning rounds", "#");
    protected VCA<?, I> finalHypothesis = null;
    
    public VCAExperiment(LearnerVCA<I> learner, EquivalenceVCAOracle<I> equivalenceOracle, VPDAlphabet<I> alphabet) {
        this.impl = new VCAExperimentImpl(learner, equivalenceOracle, alphabet);
    }

    protected void profileStart(String taskname) {
        if (profile) {
            SimpleProfiler.start(taskname);
        }
    }

    protected void profileStop(String taskname) {
        if (profile) {
            SimpleProfiler.stop(taskname);
        }
    }

    protected void logCounterexample(Word<I> counterexample) {
        if (log) {
            LOGGER.logCounterexample(counterexample.toString());
        }
    }

    protected void logPhase(String phase) {
        if (log) {
            LOGGER.logPhase(phase);
        }
    }

    protected void logModel(VCA<?, I> vca) {
        if (log && logModels) {
            LOGGER.logModel(vca);
        }
    }

    /**
     * @param log flag whether logs should be done
     */
    public void setLog(boolean log) {
        this.log = log;
    }

    /**
     * @param logModels
     *         flag whether models should be logged
     */
    public void setLogModels(boolean logModels) {
        this.logModels = logModels;
    }

    /**
     * @param profile
     *         flag whether learning process should be profiled
     */
    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    @Nonnull
    public Counter getRounds() {
        return rounds;
    }

    @Nonnull
    public VCA<?, I> getFinalHypothesis() {
        if (finalHypothesis == null) {
            throw new IllegalStateException("Experiment has not yet been run");
        }
        return finalHypothesis;
    }

    @Nonnull
    public VCA<?, I> run() {
        if (this.finalHypothesis != null) {
            throw new IllegalStateException("Experiment has already been run");
        }

        finalHypothesis = impl.run();
        return finalHypothesis;
    }

    protected class VCAExperimentImpl {
        private final LearnerVCA<I> learner;
        private final EquivalenceVCAOracle<I> equivalenceVCAOracle;
        private final VPDAlphabet<I> alphabet;

        public VCAExperimentImpl(LearnerVCA<I> learner, EquivalenceVCAOracle<I> equivalenceOracle, VPDAlphabet<I> alphabet) {
            this.learner = learner;
            this.equivalenceVCAOracle = equivalenceOracle;
            this.alphabet = alphabet;
        }

        public VCA<?, I> run() {
            rounds.increment();
            logPhase("Starting round " + rounds.getCount());

            profileStart(LEARNING_PROFILE_KEY);
            learner.startLearning();
            profileStop(LEARNING_PROFILE_KEY);

            while (true) {
                DefaultQuery<I, Boolean> counterexample = null;

                VCA<?, I> hyp = null;
                while ((hyp = learner.getHypothesisModel()) != null) {
                    logModel(hyp);

                    logPhase("Searching for counterexample");

                    profileStart(COUNTEREXAMPLE_PROFILE_KEY);
                    DefaultQuery<I, Boolean> ce = equivalenceVCAOracle.findCounterExample(hyp, alphabet);
                    profileStop(COUNTEREXAMPLE_PROFILE_KEY);

                    if (ce == null) {
                        return hyp;
                    }

                    if (ComputeCounterValue.computeHeight(ce.getInput(), alphabet) > learner.getObservationTableLevelLimit()) {
                        counterexample = ce;
                    }
                }

                if (counterexample == null) {
                    // We didn't find a good counterexample nor an appropriate VCA
                    VCA<?, I> bg = learner.getObservationTable().toVCA();
                    logPhase("Using the limited behavior graph as a VCA");
                    logModel(bg);
                    counterexample = equivalenceVCAOracle.findCounterExample(bg, alphabet);
                }

                logCounterexample(counterexample.getInput());

                // next round ...
                rounds.increment();
                logPhase("Starting round " + rounds.getCount());

                profileStart(LEARNING_PROFILE_KEY);
                final boolean refined = learner.refineHypothesis(counterexample);
                profileStop(LEARNING_PROFILE_KEY);

                assert refined;
            }
        }
    }
}