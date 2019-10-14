package be.uantwerpen.learningvca.learner;

import java.util.LinkedList;
import java.util.List;

import be.uantwerpen.learningvca.behaviorgraph.Description;
import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import be.uantwerpen.learningvca.oracles.PartialEquivalenceOracle;
import be.uantwerpen.learningvca.vca.VCA;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;

/**
 * The learner for a m-VCA
 * @param <I>
 * @author Gaëtan Staquet
 */
public class LearnerVCA<I> implements LearningAlgorithm<VCA<I>, I, Boolean> {
    private final MembershipOracle<I, Boolean> membershipOracle;
    private final PartialEquivalenceOracle<I> partialEquivalenceOracle;
    private List<Description<I>> descriptions;
    private int t = 0;

    public LearnerVCA(MembershipOracle<I, Boolean> membershipOracle, PartialEquivalenceOracle<I> partialEquivalenceOracle) {
        this.membershipOracle = membershipOracle;
        this.partialEquivalenceOracle = partialEquivalenceOracle;
        this.descriptions = new LinkedList<>();
    }

    @Override
    public void startLearning() {
        // TODO Auto-generated method stub

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
     * That is, for every possible description of the behavior graph (learned up to a threshold t), an hypothesis is built.
     * Once every description has been used, the function returns null.
     * 
     * @return A VCA or null if there is no next hypothesis model
     */
    @Override
    public VCA<I> getHypothesisModel() {
        // TODO Auto-generated method stub

        if (descriptions.size() == 0) {
            return null;
        }

        for (Description<I> description : descriptions) {
            
        }
        return null;
    }

    private LimitedBehaviorGraph<I> learnBehaviorGraphUpTo(int t) {
        // TODO
        return null;
    }
}