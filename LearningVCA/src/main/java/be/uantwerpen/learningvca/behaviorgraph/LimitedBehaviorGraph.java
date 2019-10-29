package be.uantwerpen.learningvca.behaviorgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
        for (int i = 0 ; i <= threshold ; i++) {
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

    public int getWidth() {
        return statesByLevel.stream().mapToInt(l -> l.size()).max().orElseThrow();
    }

    /**
     * Constructs every possible periodic description of this behavior graph
     * @return A list with every periodic description
     */
    public List<Description<I>> getPeriodicDescriptions() {
        int width = getWidth();
        List<Description<I>> descriptions = new ArrayList<>();
        for (int m = 0 ; m - 1 <= threshold ; m++) {
            // TODO what happens when there are just internal symbols?
            for (int k = 1 ; m + 2 * k - 1 <= threshold ; k++) {
                int limit = m + k - 1;
                // First, we create the nu mappings
                List<Map<Integer, Integer>> nu_mappings = new ArrayList<>(m + k);
                for (int i = 0 ; i <= limit ; i++) {
                    nu_mappings.add(new HashMap<>());
                }

                IntStream.range(0, limit + 1).
                    forEach(level -> {
                        Map<Integer, Integer> nu_i = nu_mappings.get(level);
                        getStates(level).stream().
                            forEach(state -> nu_i.put(state, nu_i.size() + 1)
                        );
                    }
                );

                List<TauMapping<I>> tauMappings = new ArrayList<>(m + k);
                IntStream.range(0, limit).
                    forEach(i -> tauMappings.add(new TauMapping<>(width))
                );

                // We create the tau mappings up to m + k - 2
                IntStream.range(0, limit).
                    forEach(level -> {
                        for (Integer startState : getStates(level)) {
                            TauMapping<I> tauMapping = tauMappings.get(level);
                            for (I symbol : getInputAlphabet()) {
                                Integer targetState = getTransition(startState, symbol);
                                if (targetState != null) {
                                    int targetLevel = getLevel(targetState);
                                    int startClass = nu_mappings.get(level).get(startState);
                                    int targetClass = nu_mappings.get(targetLevel).get(targetState);
                                    tauMapping.addTransition(startClass, symbol, targetClass);
                                }
                            }
                        }
                    }
                );
                // And we find the last tau mapping thanks to an isomorphism
                TauMapping<I> lastTauMapping = getEndOfPeriod(m, k, width, nu_mappings);
                if (lastTauMapping == null) {
                    // It was impossible to find an isomorphism. So, the description must be rejected
                    continue;
                }
                tauMappings.add(lastTauMapping);

                Description<I> description = new Description<>(m, k, width);
                description.addTauMappings(tauMappings);
                int initialLevel = getLevel(getInitialState());
                description.setInitialState(initialLevel, nu_mappings.get(initialLevel).get(getInitialState()));
                descriptions.add(description);
            }
        }

        return descriptions;
    }

    @Nullable
    private TauMapping<I> getEndOfPeriod(int m, int k, int width, List<Map<Integer, Integer>> nu_mappings) {
        // We create the isomorphism
        BiMap<Integer, Integer> isomorphism = HashBiMap.create();
        List<Integer> freeStates = new ArrayList<>();
        freeStates.addAll(getStates(m));

        while (!freeStates.isEmpty()) {
            Integer startingState = freeStates.get(0);
            BiMap<Integer, Integer> partial = findIsomorphism(m, k, startingState, isomorphism);
            if (partial == null) {
                // Impossible to find an isomorphism
                return null;
            }
            isomorphism.putAll(partial);
            freeStates.removeAll(partial.keySet());
        }

        // We create the tau mapping
        TauMapping<I> tauMapping = new TauMapping<>(width);
        VPDAlphabet<I> alphabet = getInputAlphabet();

        for (int startingState : getStates(m + k - 1)) {
            int startClass = nu_mappings.get(getLevel(startingState)).get(startingState);
            for (I symbol : alphabet) {
                Integer targetState = getTransition(startingState, symbol);
                if (targetState != null) {
                    if (alphabet.isCallSymbol(symbol)) {
                        // If we process a call symbol, we must follow the isomorphism
                        targetState = isomorphism.inverse().get(targetState);
                    }
                    int targetClass = nu_mappings.get(getLevel(targetState)).get(targetState);
                    tauMapping.addTransition(startClass, symbol, targetClass);
                }
            }
        }
        return tauMapping;
    }

    /**
     * Finds a isomorphism starting from [w]_O
     * @param m
     * @param k
     * @param startingState [w]_O. It must not yet be in an isomorphism
     * @return The isomorphism
     */
    @Nullable
    public BiMap<Integer, Integer> findIsomorphism(int m, int k, int startingState, BiMap<Integer, Integer> previousIsomorphism) {
        class InQueue {
            public final int stateInFirst;
            public final int stateInSecond;

            public InQueue(int stateInFirst, int stateInSecond) {
                this.stateInFirst = stateInFirst;
                this.stateInSecond = stateInSecond;
            }

            @Override
            public String toString() {
                return stateInFirst + " " + stateInSecond;
            }
        }

        int targetLevel = m + k;
        VPDAlphabet<I> alphabet = getInputAlphabet();

        for (int targetState : getStates(targetLevel)) {
            if (previousIsomorphism.containsValue(targetState)) {
                // We can not reuse an equivalence class
                continue;
            }
            BiMap<Integer, Integer> newIsomorphism = HashBiMap.create();
            // We suppose it's an isomorphism and we seek a counterexample
            boolean isIsomorphism = true;
            Queue<InQueue> queue = new LinkedList<>();
            queue.add(new InQueue(startingState, targetState));
            newIsomorphism.put(startingState, targetState);

            InQueue current;
            while ((current = queue.poll()) != null && isIsomorphism) {
                int currentStateFirst = current.stateInFirst;
                int currentLevelFirst = getLevel(currentStateFirst);
                int currentStateSecond = current.stateInSecond;
                int currentLevelSecond = getLevel(currentStateSecond);

                for (I symbol : alphabet) {
                    // We only keep the subgraph induced by the levels m to m + k - 1
                    if ((alphabet.isCallSymbol(symbol) && currentLevelFirst == m + k - 1) || (alphabet.isReturnSymbol(symbol) && currentLevelFirst == m)) {
                        continue;
                    }

                    Integer newStateFirst = getTransition(currentStateFirst, symbol);
                    Integer newStateSecond = getTransition(currentStateSecond, symbol);

                    if (newStateFirst == null && newStateSecond == null) {
                        // Both transitions are not defined.
                        // So, it's okay
                        continue;
                    }
                    else if (newStateFirst == null || newStateSecond == null) {
                        // Only one transition is not defined
                        // This is not okay
                        isIsomorphism = false;
                        break;
                    }

                    int newLevelFirst = getLevel(newStateFirst);
                    int newLevelSecond = getLevel(newStateSecond);

                    int movementInFirst = newLevelFirst - currentLevelFirst;
                    int movementInSecond = newLevelSecond - currentLevelSecond;

                    if (movementInFirst != movementInSecond) {
                        System.out.println("Different movements");
                        // The transitions do not lead to the "same" level
                        isIsomorphism = false;
                        break;
                    }

                    // If one of the states is already in an isomorphism
                    // And if the values do not coincide, then we don't have an isomorphism
                    if (
                        (newIsomorphism.containsKey(newStateFirst) && newIsomorphism.get(newStateFirst) != newStateSecond) ||
                        (newIsomorphism.inverse().containsKey(newStateSecond) && newIsomorphism.inverse().get(newStateSecond) != newStateFirst) ||
                        (previousIsomorphism.containsKey(newStateFirst) && previousIsomorphism.get(newStateFirst) != newStateSecond) ||
                        (previousIsomorphism.inverse().containsKey(newStateSecond) && newIsomorphism.inverse().get(newStateSecond) != newStateFirst)
                    ) {
                        isIsomorphism = false;
                        break;
                    }

                    if (!queue.contains(new InQueue(newStateFirst, newStateSecond)) && !newIsomorphism.containsKey(newStateFirst) && !previousIsomorphism.containsKey(newStateFirst)) {
                        // We don't add the new equivalence classes if they already have been explored or marked for exploration
                        newIsomorphism.put(newStateFirst, newStateSecond);
                        queue.add(new InQueue(newStateFirst, newStateSecond));
                    }
                }
            }

            if (isIsomorphism) {
                return newIsomorphism;
            }
        }

        return null;
    }
}