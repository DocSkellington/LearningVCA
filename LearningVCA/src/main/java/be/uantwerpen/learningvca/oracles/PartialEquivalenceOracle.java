package be.uantwerpen.learningvca.oracles;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import be.uantwerpen.learningvca.behaviorgraph.BehaviorGraph;
import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * Performs a partial equivalence query.
 * 
 * This oracle checks that the behavior graph is correctly learned up to a treshold t
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class PartialEquivalenceOracle<I extends Comparable<I>> {
    private BehaviorGraph<I> behaviorGraph;

    public PartialEquivalenceOracle(BehaviorGraph<I> behaviorGraph) {
        this.behaviorGraph = behaviorGraph;
    }

    private <S> Word<I> findCounterExample(DFA<S, I> sul, LimitedBehaviorGraph<I> hypothesis, int threshold, VPDAlphabet<I> alphabet) {
        class InQueue {
            public final S stateInSul;
            public final int stateInHypo;
            public final int counterValue;
            public final Word<I> word;

            public InQueue(S stateInSul, int stateInHypo, int counterValue, Word<I> word) {
                this.stateInSul = stateInSul;
                this.stateInHypo = stateInHypo;
                this.counterValue = counterValue;
                this.word = word;
            }

        }

        // W use the fact that BG is unique for a language
        // That is, we use a BFS to check if the behavior graphs are identical (up to t)
        // We ignore the states that can not be reached in the learned behavior graph (due to redondant representatives)
        Queue<InQueue> queue = new LinkedList<>();
        Map<S, Integer> sulToInt = new HashMap<>();
        Map<Integer, Integer> hypoToInt = new HashMap<>();
        // ^ is XOR
        if (sul.isAccepting(sul.getInitialState()) ^ hypothesis.isAccepting(hypothesis.getInitialState())) {
            return Word.epsilon();
        }

        queue.add(new InQueue(sul.getInitialState(), hypothesis.getInitialState(), 0, Word.epsilon()));
        sulToInt.put(sul.getInitialState(), 0);
        hypoToInt.put(hypothesis.getInitialState(), 0);

        while (!queue.isEmpty()) {
            InQueue elem = queue.poll();
            S inSul = elem.stateInSul;
            int inHypo = elem.stateInHypo;
            int countervalue = elem.counterValue;

            for (I symbol : alphabet) {
                int newCounterValue = countervalue;
                if (alphabet.isCallSymbol(symbol)) {
                    newCounterValue++;
                    if (newCounterValue > threshold) {
                        continue;
                    }
                }
                else if (alphabet.isReturnSymbol(symbol)) {
                    newCounterValue--;
                    if (newCounterValue < 0) {
                        continue;
                    }
                }

                Word<I> word = elem.word.append(symbol);
                S transitionInSul = sul.getTransition(inSul, symbol);
                Integer transitionInHypo = hypothesis.getTransition(inHypo, symbol);

                if (transitionInHypo == null && transitionInSul == null) {
                    continue;
                }
                if (transitionInSul == null || transitionInSul == null) {
                    return word;
                }

                if (sul.isAccepting(transitionInSul) ^ hypothesis.isAccepting(transitionInHypo)) {
                    return word;
                }

                if (sulToInt.containsKey(transitionInSul)) {
                    if (hypoToInt.containsKey(transitionInHypo)) {
                        if (!sulToInt.get(transitionInSul).equals(hypoToInt.get(transitionInHypo))) {
                            return word;
                        }
                        else {
                            // This node has already been processed
                            continue;
                        }
                    }
                    else {
                        return word;
                    }
                }
                else if (hypoToInt.containsKey(transitionInHypo)) {
                    return word;
                }
                sulToInt.put(transitionInSul, sulToInt.size());
                hypoToInt.put(transitionInHypo, hypoToInt.size());

                queue.add(new InQueue(transitionInSul, transitionInHypo, newCounterValue, word));
            }
        }

        return null;
    }

    public DefaultQuery<I, Boolean> findCounterExample(LimitedBehaviorGraph<I> hypothesis, int threshold, Collection<? extends I> inputs) {
        DFA<?, I> sul = behaviorGraph.toDFA(threshold);
        Word<I> counterexample = findCounterExample(sul, hypothesis, threshold, hypothesis.getInputAlphabet());

        if (counterexample == null) {
            return null;
        }

        return new DefaultQuery<>(counterexample);
    }

}