package be.uantwerpen.learningvca.learner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.uantwerpen.learningvca.behaviorgraph.Description;
import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import be.uantwerpen.learningvca.observationtable.StratifiedObservationTable;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.OTLearner;
import de.learnlib.datastructure.observationtable.ObservationTable;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.WordBuilder;

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
    private final StratifiedObservationTable<I> stratifiedObservationTable;
    private int t = 0;
    private VCA<I> lastHypothesis;

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
        WordBuilder<I> builder = new WordBuilder<>();
        this.stratifiedObservationTable.initialize(Arrays.asList(builder.toWord()), Arrays.asList(builder.toWord()), membershipOracle);
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        // TODO Auto-generated method stub

        // Learning the behavior graph up to t
        LimitedBehaviorGraph<I> behaviorGraphUpToT = learnBehaviorGraphUpTo(t);
        descriptions = behaviorGraphUpToT.getPeriodicDescriptions();
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
        // TODO Auto-generated method stub

        if (descriptions.size() == 0) {
            return null;
        }

        VCA<I> hypothesis = null;

        for (Description<I> description : descriptions) {

        }

        lastHypothesis = hypothesis;
        return hypothesis;
    }

    /**
     * @return the lastHypothesis
     */
    public VCA<I> getLastHypothesis() {
        return lastHypothesis;
    }

    private LimitedBehaviorGraph<I> learnBehaviorGraphUpTo(int t) {
        // TODO
        return null;
    }

    @Override
    public ObservationTable<I, Boolean> getObservationTable() {
        // TODO Auto-generated method stub
        return null;
    }
}