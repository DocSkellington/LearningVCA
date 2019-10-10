package be.uantwerpen.learningvca.vca;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.words.VPDAlphabet;

/**
 * A state (or location in LearnLib nomenclature) in a VCA.
 * 
 * It stores whether the state is final and the transition functions for this state
 */
public class State {
    private boolean isAccepting;
    /**
     * Stores the transition functions for call symbols for this state.
     * The first list maps the counter value to the transition function to use, while the second list maps the input symbol to the target state.
     * That is, we have N x Sigma -> Q
     */
    private final List<ArrayStorage<State>> callTransitions;
    /**
     * Same but for return symbols
     */
    private final List<ArrayStorage<State>> returnTransitions;
    /**
     * Same but for internal symbols
     */
    private final List<ArrayStorage<State>> internalTransitions;
    /**
     * The maximal counter value that can be used
     */
    private final int m;

    /**
     * Constructs the state
     * @param alphabet The pushdown alphabet
     * @param m The m in m-VCA
     * @param isAccepting Whether this state is accepting
     */
    public State(VPDAlphabet<?> alphabet, int m, boolean isAccepting) {
        this.m = m;
        this.callTransitions = new ArrayList<>(m + 1);
        this.returnTransitions = new ArrayList<>(m + 1);
        this.internalTransitions = new ArrayList<>(m + 1);

        // By default, everything leads to the sink state (that is, everything is null)
        for (int i = 0 ; i <= m ; i++) {
            callTransitions.add(new ArrayStorage<>(alphabet.getNumCalls()));
            returnTransitions.add(new ArrayStorage<>(alphabet.getNumReturns()));
            internalTransitions.add(new ArrayStorage<>(alphabet.getNumInternals()));
        }
    }

    /**
     * Is this state accepting?
     * @return True iff the state is accepting
     */
    public boolean isAccepting() {
        return isAccepting;
    }

    /**
     * Gets the successor of this state when reading the given call symbol with the given counter value.
     * @param callSymbolId The id of the call symbol in the alphabet
     * @param counterValue The counter value
     * @return The successor
     */
    public State getCallSuccessor(int callSymbolId, int counterValue) {
        int whichFunction = counterValue < m ? counterValue : m;
        return callTransitions.get(whichFunction).get(callSymbolId);
    }

    /**
     * Defines the successor of this state when reading the given call symbol with the given counter value
     * @param callSymbolId The id of the call symbol in the alphabet
     * @param functionIndex The index of the function to set. That is, the counter value that must be read for this transition to be used
     * @param successor The successor
     */
    public void setCallSuccessor(int callSymbolId, int functionIndex, State successor) {
        if (callSymbolId > m) {
            throw new IllegalArgumentException("Invalid function index for a call successor for a state. Received " + functionIndex + " but the limit is " + m);
        }
        callTransitions.get(functionIndex).set(callSymbolId, successor);
    }

    /**
     * Gets the successor of this state when reading the given return symbol with the given counter value.
     * @param returnSymbolId The id of the return symbol in the alphabet
     * @param counterValue The counter value
     * @return The successor
     */
    public State getReturnSuccessor(int returnSymbolId, int counterValue) {
        int whichFunction = counterValue < m ? counterValue : m;
        return returnTransitions.get(whichFunction).get(returnSymbolId);
    }

    /**
     * Defines the successor of this state when reading the given call symbol with the given counter value
     * @param returnSymbolId The id of the return symbol in the alphabet
     * @param functionIndex The index of the function to set. That is, the counter value that must be read for this transition to be used
     * @param successor The successor
     */
    public void setReturnSuccessor(int returnSymbolId, int functionIndex, State successor) {
        if (returnSymbolId > m) {
            throw new IllegalArgumentException("Invalid function index for a return successor for a state. Received " + functionIndex + " but the limit is " + m);
        }
        returnTransitions.get(functionIndex).set(returnSymbolId, successor);
    }

    /**
     * Gets the successor of this state when reading the given internal symbol with the given counter value.
     * @param internalSymbolId The id of the internal symbol in the alphabet
     * @param counterValue The counter value
     * @return The successor
     */
    public State getInternalSuccessor(int internalSymbolId, int counterValue) {
        int whichFunction = counterValue < m ? counterValue : m;
        return internalTransitions.get(whichFunction).get(internalSymbolId);
    }

    /**
     * Defines the successor of this state when reading the given call symbol with the given counter value
     * @param internalSymbolId The id of the internal symbol in the alphabet
     * @param functionIndex The index of the function to set. That is, the counter value that must be read for this transition to be used
     * @param successor The successor
     */
    public void setInternalSuccessor(int internalSymbolId, int functionIndex, State successor) {
        if (internalSymbolId > m) {
            throw new IllegalArgumentException("Invalid function index for a internal successor for a state. Received " + functionIndex + " but the limit is " + m);
        }
        returnTransitions.get(functionIndex).set(internalSymbolId, successor);
    }
}