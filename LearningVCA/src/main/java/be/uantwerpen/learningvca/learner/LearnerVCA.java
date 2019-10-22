package be.uantwerpen.learningvca.learner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.uantwerpen.learningvca.behaviorgraph.Description;
import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import be.uantwerpen.learningvca.observationtable.StratifiedObservationTable;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.util.ComputeCounterValue;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.OTLearner;
import de.learnlib.datastructure.observationtable.ObservationTable;
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
    private int positionInDescriptions;
    private final StratifiedObservationTable<I, Boolean> stratifiedObservationTable;

    public LearnerVCA(VPDAlphabet<I> alphabet, MembershipOracle<I, Boolean> membershipOracle,
            PartialEquivalenceOracle<I> partialEquivalenceOracle) {
        this.alphabet = alphabet;
        this.membershipOracle = membershipOracle;
        this.partialEquivalenceOracle = partialEquivalenceOracle;
        this.descriptions = new LinkedList<>();
        this.stratifiedObservationTable = new StratifiedObservationTable<>(alphabet);
    }

    @Override
    public void startLearning() {
        this.stratifiedObservationTable.initialize(Arrays.asList(Word.epsilon()), Arrays.asList(Word.epsilon()), membershipOracle);
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
        positionInDescriptions = 0;
        return false;
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
        if (descriptions.size() == 0 || positionInDescriptions >= descriptions.size()) {
            return null;
        }

        VCA<I> hypothesis = descriptions.get(positionInDescriptions++).toVCA(alphabet);

        return hypothesis;
    }

    /**
     * Gets the VCA built from the observation table
     * @return The t-VCA
     */
    public VCA<I> getObservationTableVCA() {
        return stratifiedObservationTable.toVCA();
    }

    /**
     * Learns the behavior graph up to the given threshold
     * @param t The threshold
     * @return The limited behavior graph learnt
     */
    private LimitedBehaviorGraph<I> learnBehaviorGraphUpTo(int t) {
        // TODO
        return null;
    }

    @Override
    public ObservationTable<I, Boolean> getObservationTable() {
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