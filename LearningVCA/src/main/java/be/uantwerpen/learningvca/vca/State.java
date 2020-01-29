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

import java.util.Objects;

/**
 * A configuration (or state in LearnLib nomenclature) is a pair (location, counter value).
 * @param <L> The type of the location
 * @author Gaëtan Staquet
 */
public class State<L> {
    private final static State<?> SINK = new State<>(null, null);
    private final L location;
    private final CounterValue counter;

    /**
     * Constructs a configuration from a state and a counter value
     * @param location The location
     * @param counter The counter value
     */
    public State(L location, CounterValue counter) {
        this.location = location;
        this.counter = counter;
    }

    /**
     * Gets the location 
     * @return The location
     */
    public L getLocation() {
        return location;
    }

    /**
     * Gets the counter value
     * @return The counter value
     */
    public CounterValue getCounterValue() {
        return counter;
    }

    /**
     * Gets the sink configuration
     * @return The sink configuration
     */
    @SuppressWarnings("unchecked")
    public State<L> getSink() {
        return (State<L>) SINK;
    }

    /**
     * Is the current configuration a sink configuration
     * @return True iff the current configuration is a sink
     */
    public boolean isSink() {
        return location == null;
    }

    @Override
    public String toString() {
        return "(" + getLocation() + ", " + counter + ")";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        State<L> other = (State<L>)obj;
        if (isSink() && other.isSink()) {
            return true;
        }

        return Objects.equals(other.location, this.location) && Objects.equals(other.counter, this.counter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, counter);
    }
}