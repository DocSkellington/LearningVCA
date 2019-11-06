package be.uantwerpen.learningvca.behaviorgraph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

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
        class Data {
            public final StateBG stateInBG;
            public final int numberOfLoops;
            public final int counterValue;
            
            public Data(StateBG stateInBG, int numberOfLoops, int counterValue) {
                this.stateInBG = stateInBG;
                this.numberOfLoops = numberOfLoops;
                this.counterValue = counterValue;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != getClass()) {
                    return false;
                }
                
                Data other = (Data)obj;
                return other.stateInBG.equals(stateInBG) && other.numberOfLoops == numberOfLoops && other.counterValue == counterValue;
            }

            @Override
            public int hashCode() {
                return Objects.hash(stateInBG, numberOfLoops, counterValue);
            }
        }

        CompactDFA<I> dfa = new CompactDFA<>(alphabet);

        Map<Data, Integer> bgToState = new HashMap<>();
        StateBG initial = description.getInitialState();
        bgToState.put(new Data(initial, 0, 0), dfa.addInitialState(description.isAcceptingState(initial)));

        Queue<Data> queue = new LinkedList<>();
        queue.add(new Data(initial, 0, 0));

        while (!queue.isEmpty()) {
            Data current = queue.poll();
            StateBG currentState = current.stateInBG;
            Integer currentInDFA = bgToState.get(current);
            TauMapping<I> currentMapping = description.getTauMappings().get(currentState.getMapping());

            for (I symbol : alphabet) {
                int targetEquivalenceClass = currentMapping.getTransition(currentState.getEquivalenceClass(), symbol);
                if (targetEquivalenceClass == -1) {
                    // Undefined transition
                    continue;
                }
                int targetMapping = currentState.getMapping();
                int numberOfLoops = current.numberOfLoops;
                int counterValue = current.counterValue;

                // We find the correct tau mapping
                if (alphabet.isCallSymbol(symbol)) {
                    // That is, if we have a call symbol and if we are at the end of the loop, we must go back at the beginning
                    if (targetMapping == description.getOffset() + description.getPeriod() - 1) {
                        numberOfLoops++;
                        targetMapping = description.getOffset();
                    }
                    else {
                        targetMapping++;
                    }
                    counterValue++;
                }
                else if (alphabet.isReturnSymbol(symbol)) {
                    // And, if we have a return symbol and if we are at the beginning of the loop and if we still have to "unfold" the loops, we go back at the end
                    if (targetMapping == description.getOffset() && numberOfLoops > 0) {
                        numberOfLoops--;
                        targetMapping = description.getOffset() + description.getPeriod() - 1;
                    }
                    else {
                        targetMapping--;
                    }
                    counterValue--;
                }

                if (counterValue < 0 || threshold < counterValue) {
                    // We remain in the correct counter values range
                    continue;
                }
                
                StateBG targetState = new StateBG(targetMapping, targetEquivalenceClass);
                Data newData = new Data(targetState, numberOfLoops, counterValue);
                Integer stateInDFA = null;
                if ((stateInDFA = bgToState.getOrDefault(newData, null)) == null) {
                    stateInDFA = dfa.addState(description.isAcceptingState(targetState));
                    bgToState.put(newData, stateInDFA);
                    queue.add(newData);
                }

                dfa.setTransition(currentInDFA, symbol, stateInDFA);
            }
        }

        return dfa;
    }

}