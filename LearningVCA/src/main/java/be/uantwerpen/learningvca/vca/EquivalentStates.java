package be.uantwerpen.learningvca.vca;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;

/**
 * Computes and stores the equivalence between states in a {@link VCA}.
 * 
 * The algorithm is an adaptation of the algorithm used to find equivalent states in a DFA (see HOPCROFT and ULLMAN, Introduction to Automata Theory).
 * @param <L> The location type of the VCA
 * @param <I> The input alphabet type 
 */
final class EquivalentStates<L, I> {

    private final Table<State<L>, State<L>, Boolean> equivalentStates;
    private final Table<State<L>, State<L>, List<Pair<State<L>, State<L>>>> lists;
    private final List<State<L>> states;

    public EquivalentStates(VCA<L, I> vca, int threshold) {
        this.equivalentStates = HashBasedTable.create();
        this.lists = HashBasedTable.create();
        this.states = vca.getStates(threshold);

        // We initialize the table
        for (State<L> s1 : states) {
            for (State<L> s2 : states) {
                equivalentStates.put(s1, s2, true);
                equivalentStates.put(s2, s1, true);
                if (vca.isAccepting(s1) ^ vca.isAccepting(s2)) {
                    markAsDistinct(s1, s2);
                }
                if (!s1.getCounterValue().equals(s2.getCounterValue())) {
                    markAsDistinct(s1, s2);
                }
            }
        }

        Alphabet<I> alphabet = vca.getAlphabet();

        // We seek the distinct states
        for (State<L> s1 : states) {
            for (State<L> s2 : states) {
                if (vca.isAccepting(s1) == vca.isAccepting(s2)) {
                    boolean witness = false;
                    for (I symbol : alphabet) {
                        State<L> newS1 = vca.getTransition(s1, symbol);
                        State<L> newS2 = vca.getTransition(s2, symbol);

                        if (newS1.getCounterValue().isBetween0AndT(threshold) && newS2.getCounterValue().isBetween0AndT(threshold)) {
                            if (!equivalentStates.get(newS1, newS2)) {
                                markAsDistinct(s1, s2);
                                witness = true;
                                break;
                            }
                        }
                    }

                    if (!witness) {
                        for (I symbol : alphabet) {
                            State<L> newS1 = vca.getTransition(s1, symbol);
                            State<L> newS2 = vca.getTransition(s2, symbol);

                            if (!Objects.equals(newS1, newS2) && newS1.getCounterValue().isBetween0AndT(threshold) && newS2.getCounterValue().isBetween0AndT(threshold)) {
                                if (!lists.contains(newS1, newS2)) {
                                    lists.put(newS1, newS2, new ArrayList<>());
                                }
                                lists.get(newS1, newS2).add(Pair.of(s1, s2));
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void markAsDistinct(State<L> s1, State<L> s2) {
        equivalentStates.put(s1, s2, false);
        equivalentStates.put(s2, s1, false);
        if (lists.contains(s1, s2)) {
            lists.get(s1, s2).forEach(pair -> markAsDistinct(pair.getFirst(), pair.getSecond()));
            lists.remove(s1, s2);
        }
        if (lists.contains(s2, s1)) {
            lists.get(s2, s1).forEach(pair -> markAsDistinct(pair.getFirst(), pair.getSecond()));
            lists.remove(s2, s1);
        }
    }

    public boolean areEquivalent(State<L> s1, State<L> s2) {
        return equivalentStates.get(s1, s2);
    }
}