package be.uantwerpen.learningvca.learner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import be.uantwerpen.learningvca.behaviorgraph.Description;
import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import be.uantwerpen.learningvca.observationtable.StratifiedObservationTable;
import be.uantwerpen.learningvca.observationtable.StratifiedObservationTableBoolean;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.util.ComputeCounterValue;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.Inconsistency;
import de.learnlib.datastructure.observationtable.OTLearner;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * The learner for a m-VCA
 * 
 * @param <I>
 * @author Gaëtan Staquet
 */
public class LearnerVCA<I extends Comparable<I>> implements OTLearner<VCA<I>, I, Boolean> {
    private final VPDAlphabet<I> alphabet;
    private final MembershipOracle<I, Boolean> membershipOracle;
    private final PartialEquivalenceOracle<I> partialEquivalenceOracle;
    private List<Description<I>> descriptions;
    private Iterator<Description<I>> descriptionIterator;
    private final StratifiedObservationTable<I, Boolean> stratifiedObservationTable;

    public LearnerVCA(VPDAlphabet<I> alphabet, MembershipOracle<I, Boolean> membershipOracle,
            PartialEquivalenceOracle<I> partialEquivalenceOracle) {
        this.alphabet = alphabet;
        this.membershipOracle = membershipOracle;
        this.partialEquivalenceOracle = partialEquivalenceOracle;
        this.descriptions = new LinkedList<>();
        this.stratifiedObservationTable = new StratifiedObservationTableBoolean<>(alphabet);
    }

    @Override
    public void startLearning() {
        this.stratifiedObservationTable.initialize(Arrays.asList(Word.epsilon()), Arrays.asList(Word.epsilon()), membershipOracle);
        LimitedBehaviorGraph<I> behaviorGraphUpInitial = learnBehaviorGraphUpTo(stratifiedObservationTable.getLevelLimit());
        descriptions = behaviorGraphUpInitial.getPeriodicDescriptions();
        descriptionIterator = descriptions.iterator();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        // Let w be the counter example
        // For every decomposition w = uv, we add u as representative and v as separator
        Word<I> counterexample = ceQuery.getInput();
        List<Word<I>> prefixes = new ArrayList<>(counterexample.size());
        List<Word<I>> suffixes = new ArrayList<>(counterexample.size());
        List<Integer> suffixesLevels = new ArrayList<>(counterexample.size());
        // We skip epsilon since it is already in the representatives and the separators
        for (int i = 1 ; i < counterexample.size() ; i++) {
            prefixes.add(counterexample.subWord(0, i + 1)); // upper bound is exclusive
            Word<I> suffix = counterexample.subWord(i + 1);
            suffixes.add(suffix);
            suffixesLevels.add(ComputeCounterValue.computeCounterValue(suffix, alphabet));
        }
        stratifiedObservationTable.addShortPrefixes(prefixes, membershipOracle);
        stratifiedObservationTable.addSuffixes(suffixes, suffixesLevels, membershipOracle);

        // Learning the behavior graph up to t
        LimitedBehaviorGraph<I> behaviorGraphUpToT = learnBehaviorGraphUpTo(stratifiedObservationTable.getLevelLimit());
        descriptions = behaviorGraphUpToT.getPeriodicDescriptions();
        descriptionIterator = descriptions.iterator();
        return prefixes.size() != 0 || suffixes.size() != 0;
    }

    /**
     * Gets the next hypothesis model.
     * 
     * That is, for every possible description of the behavior graph (learned up to
     * a threshold t), an hypothesis is built. Once every description has been used,
     * the function returns null.
     * 
     * @return A VCA or null if there is no next hypothesis model
     */
    @Override
    public VCA<I> getHypothesisModel() {
        if (descriptions.size() == 0 || !descriptionIterator.hasNext()) {
            return null;
        }

        Description<I> description = descriptionIterator.next();
        VCA<I> hypothesis = description.toVCA(alphabet);

        return hypothesis;
    }

    /**
     * Learns the behavior graph up to the given threshold
     * @param t The threshold
     * @return The limited behavior graph learnt
     */
    private LimitedBehaviorGraph<I> learnBehaviorGraphUpTo(int t) {
        DefaultQuery<I, Boolean> counterexample = null;
        LimitedBehaviorGraph<I> limitedBehaviorGraph = null;
        do {
            // We process the counterexample
            if (counterexample != null) {
                Word<I> counterexampleWord = counterexample.getInput();
                List<Word<I>> prefixes = new ArrayList<>(counterexampleWord.size());
                List<Word<I>> suffixes = new ArrayList<>(counterexampleWord.size());
                List<Integer> suffixesLevels = new ArrayList<>(counterexampleWord.size());
                for (int i = 0 ; i < counterexampleWord.size() ; i++) {
                    Word<I> prefix = counterexampleWord.subWord(0, i);
                    Word<I> suffix = counterexampleWord.subWord(i);
                    prefixes.add(prefix);
                    suffixes.add(suffix);
                    suffixesLevels.add(ComputeCounterValue.computeCounterValue(suffix, alphabet));
                }
                stratifiedObservationTable.addShortPrefixes(prefixes, membershipOracle);
                stratifiedObservationTable.addSuffixes(suffixes, suffixesLevels, membershipOracle);
            }

            // We make the table closed and consistent
            boolean closedAndConsistent = true;
            do {
                closedAndConsistent = true;
                Row<I> unclosedRow = null;
                while ((unclosedRow = stratifiedObservationTable.findUnclosedRow()) != null) {
                    // unclosedRow is directly the long prefix to add
                    stratifiedObservationTable.addShortPrefixes(Arrays.asList(unclosedRow.getLabel()), membershipOracle);
                    closedAndConsistent = false;
                }

                Inconsistency<I> inconsistency = null;
                while ((inconsistency = stratifiedObservationTable.findInconsistency()) != null) {
                    Word<I> w = stratifiedObservationTable.findDistinguishingSuffix(inconsistency);
                    Word<I> aw = w.prepend(inconsistency.getSymbol());
                    int counterValue = ComputeCounterValue.computeCounterValue(inconsistency.getFirstRow().getLabel(), alphabet);
                    stratifiedObservationTable.addSuffix(aw, counterValue, membershipOracle);
                    closedAndConsistent = false;
                }
            } while (!closedAndConsistent);

            // We compute the new limited behavior graph and check if there exists a counterexample
            limitedBehaviorGraph = stratifiedObservationTable.toLimitedBehaviorGraph();
            counterexample = partialEquivalenceOracle.findCounterExample(limitedBehaviorGraph, stratifiedObservationTable.getLevelLimit(), alphabet);
        } while (counterexample != null);

        return limitedBehaviorGraph;
    }

    @Override
    public StratifiedObservationTable<I, Boolean> getObservationTable() {
        return stratifiedObservationTable;
    }

    /**
     * Gets the current level limit in the observation table
     * @return t
     */
    public int getObservationTableLevelLimit() {
        return stratifiedObservationTable.getLevelLimit();
    }
}