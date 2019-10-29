package be.uantwerpen.learningvca.behaviorgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.VPDAlphabet;

/**
 * A behavior graph limited to a threshold t
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class LimitedBehaviorGraph<I extends Comparable<I>> extends CompactDFA<I> {
    private static final long serialVersionUID = 5195494200755637801L;

    private final int threshold;
    private final Map<Integer, Integer> stateToLevel;
    private final List<List<Integer>> statesByLevel;

    /**
     * The constructor
     * @param alphabet The alphabet
     * @param threshold The threshold t
     */
    public LimitedBehaviorGraph(VPDAlphabet<I> alphabet, int threshold) {
        super(alphabet);
        this.threshold = threshold;
        this.stateToLevel = new HashMap<>();
        this.statesByLevel = new ArrayList<>(threshold + 1);
        for (int i = 0 ; i < threshold ; i++) {
            statesByLevel.add(new ArrayList<>());
        }
    }

    public void setStateLevel(int state, int level) {
        stateToLevel.put(state, level);
        statesByLevel.get(level).add(state);
    }

    public int getLevel(int state) {
        return stateToLevel.get(state);
    }

    public List<Integer> getStates(int level) {
        return statesByLevel.get(level);
    }

    @Override
    public VPDAlphabet<I> getInputAlphabet() {
        return (VPDAlphabet<I>) super.getInputAlphabet();
    }

    /**
     * Constructs every possible periodic description of this behavior graph
     * @return A list with every periodic description
     */
    public List<Description<I>> getPeriodicDescriptions() {
        List<Description<I>> descriptions = new ArrayList<>();
        for (int m = 0 ; m - 1 <= threshold ; m++) {
            for (int k = 0 ; m + k - 1 <= threshold ; k++) {
                // First, we create the nu mappings
                List<Map<Integer, Integer>> nu_mappings = new ArrayList<>(m + k);
                for (int i = 0 ; i < m + k ; i++) {
                    nu_mappings.add(new HashMap<>());
                }

                for (Integer state : getStates()) {
                    int level = getLevel(state);
                    Map<Integer, Integer> nu_i = nu_mappings.get(level);
                    nu_i.put(state, nu_i.size() + 1);
                }
            }
        }

        // TODO
        return descriptions;
    }
}