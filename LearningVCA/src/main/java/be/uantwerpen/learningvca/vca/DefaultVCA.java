/*
 * LearningVCA - An implementation of an active learning algorithm for Visibly One-Counter Automata
 * Copyright (C) 2020 University of Mons and University of Antwerp
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.uantwerpen.learningvca.vca;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

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
    /**
     * The threshold
     */
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
        if (loc == null) {
            return false;
        }
        return loc.isAccepting();
    }

    /**
     * Adds a non-accepting initial location.
     * 
     * It overrides any previously defined initial location
     * @return The initial location
     */
    public Location addInitialLocation() {
        return addInitialLocation(false);
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
     * Defines the transition from start to successor when reading an input and when the counter value is equal to the given value
     * @param start The starting state
     * @param counterValue The value the counter must be equal to for this transition to be active
     * @param input The input
     * @param successor The successor
     */
    public void setSuccessor(Location start, int counterValue, I input, Location successor) {
        switch (getAlphabet().getSymbolType(input)) {
            case CALL:
                setCallSuccessor(start, counterValue, input, successor);
                break;
            case RETURN:
                setReturnSuccessor(start, counterValue, input, successor);
                break;
            case INTERNAL:
                setInternalSuccessor(start, counterValue, input, successor);
                break;
            default:
                break;
        }
    }

    /**
     * Defines the transition from start to successor when reading an internal input and when the counter value is equal to the given value
     * @param start The starting state
     * @param counterValue The value the counter must be equal to for this transition to be active
     * @param input The input
     * @param successor The successor
     */
    public void setInternalSuccessor(Location start, int counterValue, I input, Location successor) {
        start.setInternalSuccessor(getAlphabet().getInternalSymbolIndex(input), counterValue, successor);
    }

    @Override
    public Location getInternalSuccessor(@Nullable Location loc, I symbol, int counterValue) {
        if (loc == null) {
            return null;
        }
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
        start.setCallSuccessor(getAlphabet().getCallSymbolIndex(input), counterValue, successor);
    }

    @Override
    public Location getCallSuccessor(@Nullable Location loc, I symbol, int counterValue) {
        if (loc == null) {
            return null;
        }
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
        start.setReturnSuccessor(getAlphabet().getReturnSymbolIndex(input), counterValue, successor);
    }

    @Override
    public Location getReturnSuccessor(@Nullable Location loc, I symbol, int counterValue) {
        if (loc == null) {
            return null;
        }
        return loc.getReturnSuccessor(getAlphabet().getReturnSymbolIndex(symbol), counterValue);
    }

    @Override
    public int getLocationId(Location loc) {
        if (loc == null) {
            return -1;
        }
        return loc.getId();
    }
}