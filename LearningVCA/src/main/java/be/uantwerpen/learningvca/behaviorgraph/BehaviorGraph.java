package be.uantwerpen.learningvca.behaviorgraph;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.VPDAlphabet;

/**
 * The behavior graph of a visibly one-counter language.
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class BehaviorGraph<I extends Comparable<I>> {
    private final Description<I> description;
    private final VPDAlphabet<I> alphabet;

    /**
     * The constructor.
     * @param description The description of the behavior graph
     */
    public BehaviorGraph(VPDAlphabet<I> alphabet, Description<I> description) {
        this.description = description;
        this.alphabet = alphabet;
    }

    /**
     * Gets the description
     * @return The description
     */
    public Description<I> getDescription() {
        return description;
    }

    /**
     * Constructs a DFA that accepts the same language that the behavior graph up to the given threshold
     * @param threshold The threshold
     * @return The DFA
     */
    public CompactDFA<I> toDFA(int threshold) {
        CompactDFA<I> dfa = new CompactDFA<>(alphabet);
        StateBG initial = description.getInitialState();
        toDFA(dfa, dfa.addInitialState(description.isAcceptingState(initial)), threshold, 0, initial.getMapping(), initial.getEquivalenceClass(), 0);
        return dfa;
    }
    
    /**
     * Effectively construct the DFA.
     * @param dfa The DFA to construct
     * @param state The state in the DFA to expand
     * @param threshold t
     * @param counterValue The current counter value
     * @param mapping The tau mapping to use
     * @param equivalenceClass The equivalence class to use
     * @param numberOfLoops The number of time the recurring part has been traversed
     */
    private void toDFA(CompactDFA<I> dfa, int state, int threshold, int counterValue, int mapping, int equivalenceClass, int numberOfLoops) {
        // TODO completely redo
        for (I a : alphabet) {
            // If there is no transition starting from equivalenceClass reading a, we skip a
            if (!description.getTauMappings().get(mapping).hasTransition(equivalenceClass, a)) {
                continue;
            }

            // We compute the counter value after reading a
            int newCounterValue = counterValue;
            if (alphabet.isCallSymbol(a)) {
                newCounterValue++;
            }
            else if (alphabet.isReturnSymbol(a)) {
                newCounterValue--;
            }

            // If the counter value exceeds the threshold, we do not follow the transition
            if (newCounterValue > threshold) {
                continue;
            }

            // We compute the next tau mapping to use
            int nextMapping = mapping;
            int nextNumberOfLoops = numberOfLoops;
            if (alphabet.isCallSymbol(a)) {
                // If a is a call symbol, we must increase the tau index
                nextMapping = mapping + 1;
                // However, if we are at the end of the recurring part, we must go back at the beginning
                if (nextMapping >= description.getOffset() + description.getPeriod()) {
                    nextMapping = description.getOffset();
                    nextNumberOfLoops++;
                }
            }
            else if (alphabet.isReturnSymbol(a)) {
                // If a is a return, we must decrease the tau index
                nextMapping = mapping - 1;
                // However, if we are at the beginning of the recurring part and if we have gone through at least one loop, we must go back at the end of the recurring part
                if (numberOfLoops > 0 && nextMapping < description.getOffset()) {
                    nextMapping = description.getOffset() + description.getPeriod() - 1;
                    nextNumberOfLoops--;
                }
            }
            // If a is an internal, the tau index does not change

            // We follow tau_i(q, a)
            int nextEquivalenceClass = description.getTauMappings().get(mapping).getTransition(equivalenceClass, a);

            // We add a new state in the DFA
            int newState = dfa.addState(description.isAcceptingState(nextMapping, nextEquivalenceClass));
            // And we add the transition from state to newState
            dfa.addTransition(state, a, newState);

            // Finally, we perform a recursive call
            toDFA(dfa, newState, threshold, newCounterValue, nextMapping, nextEquivalenceClass, nextNumberOfLoops);
        }
    }
}