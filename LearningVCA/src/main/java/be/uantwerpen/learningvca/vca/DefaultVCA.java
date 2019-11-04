package be.uantwerpen.learningvca.vca;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import net.automatalib.words.VPDAlphabet;

/**
 * An implementation for a visibly one-counter automaton.
 * 
 * Inspired from the implementation of one single-entry visibly pushdown automaton
 * (oneSEVPA) from LearnLib and AutomataLib
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class DefaultVCA<I> extends AbstractVCA<Location, I>  {
    private final List<Location> locations;
    private Location initialLocation;
    private int m;

    /**
     * The constructor
     * @param alphabet The pushdown alphabet
     */
    public DefaultVCA(final VPDAlphabet<I> alphabet, int m) {
        super(alphabet);
        this.locations = new LinkedList<>();
        this.m = m;
    }

    @Override
    public Location getInitialLocation() {
        return initialLocation;
    }

    @Override
    public int size() {
        return locations.size();
    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }

    @Override
    public int getThreshold() {
        return m;
    }

    @Override
    public boolean isAcceptingLocation(Location loc) {
        return loc.isAccepting();
    }

    /**
     * Adds an initial location.
     * 
     * It overrides any previously defined initial location
     * @param accepting Whether the location is accepting
     * @return The initial location
     */
    public Location addInitialLocation(boolean accepting) {
        initialLocation = addLocation(accepting);
        return initialLocation;
    }

    /**
     * Adds a location that is not accepting.
     * @return The location
     */
    public Location addLocation() {
        return addLocation(false);
    }

    /**
     * Adds a location.
     * @param accepting Whether the location is accepting
     * @return The location
     */
    public Location addLocation(boolean accepting) {
        Location loc = new Location(getAlphabet(), m, accepting, locations.size());
        locations.add(loc);
        return loc;
    }

    /**
     * Sets the initial location
     * @param initialLocation The new initial location
     */
    public void setInitialLocation(Location initialLocation) {
        this.initialLocation = initialLocation;
    }

    /**
     * Defines the transition from start to successor when reading an internal input and when the counter value is equal to the given value
     * @param start The starting state
     * @param counterValue The value the counter must be equal to for this transition to be active
     * @param input The input
     * @param successor The successor
     */
    public void setInternalSuccessor(Location start, int counterValue, I input, Location successor) {
        start.setInternalSuccessor(alphabet.getInternalSymbolIndex(input), counterValue, successor);
    }

    @Override
    public Location getInternalSuccessor(Location loc, I symbol, int counterValue) {
        return loc.getInternalSuccessor(getAlphabet().getInternalSymbolIndex(symbol), counterValue);
    }

    /**
     * Defines the transition from start to successor when reading a call input and when the counter value is equal to the given value
     * @param start The starting state
     * @param counterValue The value the counter must be equal to for this transition to be active
     * @param input The input
     * @param successor The successor
     */
    public void setCallSuccessor(Location start, int counterValue, I input, Location successor) {
        start.setCallSuccessor(alphabet.getCallSymbolIndex(input), counterValue, successor);
    }

    @Override
    public Location getCallSuccessor(Location loc, I symbol, int counterValue) {
        return loc.getCallSuccessor(getAlphabet().getCallSymbolIndex(symbol), counterValue);
    }

    /**
     * Defines the transition from start to successor when reading a return input and when the counter value is equal to the given value
     * @param start The starting state
     * @param counterValue The value the counter must be equal to for this transition to be active
     * @param input The input
     * @param successor The successor
     */
    public void setReturnSuccessor(Location start, int counterValue, I input, Location successor) {
        start.setReturnSuccessor(alphabet.getReturnSymbolIndex(input), counterValue, successor);
    }

    @Override
    public Location getReturnSuccessor(Location loc, I symbol, int counterValue) {
        return loc.getReturnSuccessor(getAlphabet().getReturnSymbolIndex(symbol), counterValue);
    }

    @Override
    public Location getSuccessor(Location loc, I symbol, int counterValue) {
        switch (getAlphabet().getSymbolType(symbol)) {
            case CALL:
                return getCallSuccessor(loc, symbol, counterValue);
            case RETURN:
                return getReturnSuccessor(loc, symbol, counterValue);
            case INTERNAL:
                return getInternalSuccessor(loc, symbol, counterValue);
            default:
                throw new InvalidParameterException("Unknown input symbol type. Received: "  + symbol + " but it is not in the alphabet.");
        }
    }

    @Override
    public int getLocationId(Location loc) {
        return loc.getId();
    }
}