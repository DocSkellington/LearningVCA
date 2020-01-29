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
 * The counter value.
 * @author Gaëtan Staquet
 */
public class CounterValue {
    private final int counter;

    /**
     * Constructs a counter value from an integer.
     * @param counterValue
     */
    public CounterValue(int counterValue) {
        this.counter = counterValue;
    }

    /**
     * Gets the counter value as an integer.
     * @return The counter value 
     */
    public int toInt() {
        return counter;
    }

    /**
     * Constructs a new counter value whose value is the incrementation of the current value by one.
     * 
     * This does not change the counter of this instance.
     * @return A counter value of value (current value + 1)
     */
    public CounterValue increment() {
        return new CounterValue(counter + 1);
    }

    /**
     * Constructs a new counter value whose value is the decrementation of the current value by one.
     * 
     * This does not change the counter of this instance.
     * @return A counter value of value (current value - 1)
     */
    public CounterValue decrement() {
        return new CounterValue(counter - 1);
    }

    /**
     * Checks if the counter value is zero.
     * @return If the counter value is zero
     */
    public boolean isZero() {
        return counter == 0;
    }

    public boolean isValid() {
        return counter >= 0;
    }

    public boolean isBetween0AndT(int t) {
        return 0 <= counter && counter <= t;
    }

    @Override
    public String toString() {
        return "" + toInt();
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

        CounterValue other = (CounterValue)obj;
        return other.counter == this.counter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(counter);
    }
}