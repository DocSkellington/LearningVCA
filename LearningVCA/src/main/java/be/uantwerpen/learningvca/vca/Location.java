package be.uantwerpen.learningvca.vca;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.words.VPDAlphabet;

/**
 * A state (or location in LearnLib nomenclature) in a VCA.
 * 
 * It stores whether the state is final and the transition functions for this state
 * @author Gaëtan Staquet
 */
public class Location {
    private boolean isAccepting;
    /**
     * Stores the transition functions for call symbols for this state.
     * The first list maps the counter value to the transition function to use, while the second list maps the input symbol to the target state.
     * That is, we have N x Sigma -> Q
     */
    private final List<ArrayStorage<Location>> callTransitions;
    /**
     * Stores the transition functions for return symbols for this location.
     * The first list maps the counter value to the transition function to use, while the second list maps the input symbol to the target state.
     * That is, we have N x Sigma -> Q
     */
    private final List<ArrayStorage<Location>> returnTransitions;
    /**
     * Stores the transition functions for internal symbols for this location.
     * The first list maps the counter value to the transition function to use, while the second list maps the input symbol to the target state.
     * That is, we have N x Sigma -> Q
     */
    private final List<ArrayStorage<Location>> internalTransitions;
    /**
     * The maximal counter value that can be used
     */
    private final int m;

    private final int id;

    /**
     * Constructs the state
     * @param alphabet The pushdown alphabet
     * @param m The m in m-VCA
     * @param isAccepting Whether this state is accepting
     */
    public Location(VPDAlphabet<?> alphabet, int m, boolean isAccepting, int id) {
        this.m = m;
        this.callTransitions = new ArrayList<>(m + 1);
        this.returnTransitions = new ArrayList<>(m + 1);
        this.internalTransitions = new ArrayList<>(m + 1);
        this.isAccepting = isAccepting;
        this.id = id;

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
     * @param isAccepting True iff the state is accepting
     */
    public void setIsAccepting(boolean isAccepting) {
        this.isAccepting = isAccepting;
    }

    /**
     * Gets the successor of this state when reading the given call symbol with the given counter value.
     * @param callSymbolId The id of the call symbol in the alphabet
     * @param counterValue The counter value
     * @return The successor, or null
     */
    public Location getCallSuccessor(int callSymbolId, int counterValue) {
        int whichFunction = counterValue < m ? counterValue : m;
        return callTransitions.get(whichFunction).get(callSymbolId);
    }

    /**
     * Defines the successor of this state when reading the given call symbol with the given counter value
     * @param callSymbolId The id of the call symbol in the alphabet
     * @param functionIndex The index of the function to set. That is, the counter value that must be read for this transition to be used
     * @param successor The successor
     */
    public void setCallSuccessor(int callSymbolId, int functionIndex, Location successor) {
        if (functionIndex > m) {
            throw new IllegalArgumentException("Invalid function index for a call successor for a state. Received " + functionIndex + " but the limit is " + m);
        }
        callTransitions.get(functionIndex).set(callSymbolId, successor);
    }

    /**
     * Gets the successor of this state when reading the given return symbol with the given counter value.
     * @param returnSymbolId The id of the return symbol in the alphabet
     * @param counterValue The counter value
     * @return The successor, or null
     */
    public Location getReturnSuccessor(int returnSymbolId, int counterValue) {
        int whichFunction = counterValue < m ? counterValue : m;
        return returnTransitions.get(whichFunction).get(returnSymbolId);
    }

    /**
     * Defines the successor of this state when reading the given return symbol with the given counter value
     * @param returnSymbolId The id of the return symbol in the alphabet
     * @param functionIndex The index of the function to set. That is, the counter value that must be read for this transition to be used
     * @param successor The successor
     */
    public void setReturnSuccessor(int returnSymbolId, int functionIndex, Location successor) {
        if (functionIndex > m) {
            throw new IllegalArgumentException("Invalid function index for a return successor for a state. Received " + functionIndex + " but the limit is " + m);
        }
        returnTransitions.get(functionIndex).set(returnSymbolId, successor);
    }

    /**
     * Gets the successor of this state when reading the given internal symbol with the given counter value.
     * @param internalSymbolId The id of the internal symbol in the alphabet
     * @param counterValue The counter value
     * @return The successor, or null
     */
    public Location getInternalSuccessor(int internalSymbolId, int counterValue) {
        int whichFunction = counterValue < m ? counterValue : m;
        ArrayStorage<Location> delta = internalTransitions.get(whichFunction);
        return delta.get(internalSymbolId);
    }

    /**
     * Defines the successor of this state when reading the given internal symbol with the given counter value
     * @param internalSymbolId The id of the internal symbol in the alphabet
     * @param functionIndex The index of the function to set. That is, the counter value that must be read for this transition to be used
     * @param successor The successor
     */
    public void setInternalSuccessor(int internalSymbolId, int functionIndex, Location successor) {
        if (functionIndex > m) {
            throw new IllegalArgumentException("Invalid function index for a internal successor for a state. Received " + functionIndex + " but the limit is " + m);
        }
        internalTransitions.get(functionIndex).set(internalSymbolId, successor);
    }

    /**
     * @return The id
     */
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "q" + getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }
        Location other = (Location)obj;
        return
            this.isAccepting == other.isAccepting &&
            this.id == other.id &&
            this.m == other.m &&
            this.callTransitions.equals(other.callTransitions) &&
            this.returnTransitions.equals(other.returnTransitions) &&
            this.internalTransitions.equals(other.internalTransitions)
        ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m, id);
    }
}