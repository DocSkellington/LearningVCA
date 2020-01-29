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
package be.uantwerpen.learningvca.behaviorgraph;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A mapping tau maps (state, input) to state, in a behavior graph.
 * 
 * States are stored according to a certain mapping nu.
 * That is, states are stored in [1, K], with K the width of the behavior graph.
 * @param <I> Input alphabet type
 * @author Gaëtan Staquet
 */
public class TauMapping<I extends Comparable<I>> {
    /**
     * A key in the mapping.
     * 
     * A key is a pair (state id, input)
     */
    protected class KeyMapping implements Comparable<KeyMapping> {
        public final int index;
        public final I input;

        public KeyMapping(int index, I input) {
            this.index = index;
            this.input = input;
        }

        @SuppressWarnings("unchecked")
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

            KeyMapping o = (KeyMapping)obj;
            return o.index == index && o.input.equals(input);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, input);
        }

        @Override
        public int compareTo(TauMapping<I>.KeyMapping o) {
            if (o.index == this.index) {
                return this.input.compareTo(o.input);
            }
            else if (o.index < this.index) {
                return -1;
            }
            else {
                return 1;
            }
        }
    }

    private final Map<KeyMapping, Integer> mapping;
    private final int K;

    /**
     * The constructor
     * @param width K
     */
    public TauMapping(int width) {
        this.mapping = new TreeMap<>();
        this.K = width;
    }

    /**
     * Adds a transition from start to target when reading the input
     * @param start The starting state
     * @param input The input
     * @param target The target state
     */
    public void addTransition(int start, I input, int target) {
        if (!(1 <= start && start <= K)) {
            throw new InvalidParameterException("Description of a behavior graph: start must be in [" + 1 + ", " + K + "]. Received: " + start);
        }
        if (!(1 <= target && target <= K)) {
            throw new InvalidParameterException("Description of a behavior graph: target must be in [" + 1 + ", " + K + "]. Received: " + target);
        }

        mapping.put(new KeyMapping(start, input), target);
    }

    /**
     * Gets the target equivalence class of the transition from start reading input, or -1 if there is no defined transition
     * @param start The starting equivalence class
     * @param input The input
     * @return -1 if there is no transition defined, or the target equivalence class
     */
    public int getTransition(int start, I input) {
        if (!(1 <= start && start <= K)) {
            throw new InvalidParameterException("Description of a behavior graph: start must be in [" + 1 + ", " + K + "]. Received: " + start);
        }

        return mapping.getOrDefault(new KeyMapping(start, input), -1);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (mapping.size() == 0) {
            builder.append("Empty mapping");
        }
        else {
            for (var entry : mapping.entrySet()) {
                builder.append(entry.getKey().index + " --" + entry.getKey().input + "--> " + entry.getValue() + "\n");
            }
        }
        return builder.toString();
    }
}