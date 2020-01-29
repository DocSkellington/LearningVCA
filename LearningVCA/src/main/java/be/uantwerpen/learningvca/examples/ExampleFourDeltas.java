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
package be.uantwerpen.learningvca.examples;

import java.util.Arrays;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

/**
 * 3-VCA for L = {a^n b^m c b^2 | n, m, l > 0 such that n - m = 2} UNION {a^n b^n | n > 0} where 'a' is a call symbol, 'b' a return symbol and 'c' an internal symbol
 */
public final class ExampleFourDeltas {
    /**
     * Gets the pushdown alphabet with 'a' as call, 'b' as return and 'c' as internal
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<>(Arrays.asList('c'), Arrays.asList('a'), Arrays.asList('b'));
    }

    /**
     * Constructs a 3-VCA for L = {a^n b^m c b^2 | n, m, l > 0 such that n - m = 2} UNION {a^n b^n | n > 0}
     * @return A 3-VCA
     */
    public static VCA<?, Character> getVCA() {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), 3);

        Location q0 = vca.addInitialLocation();
        Location q1 = vca.addLocation(true);
        Location q2 = vca.addLocation();
        Location q3 = vca.addLocation();
        Location q4 = vca.addLocation(true);
        Location q5 = vca.addLocation();
        Location q6 = vca.addLocation(true);

        vca.setSuccessor(q0, 0, 'a', q0);
        vca.setSuccessor(q0, 1, 'a', q0);
        vca.setSuccessor(q0, 2, 'a', q2);
        vca.setSuccessor(q0, 1, 'b', q1);
        vca.setSuccessor(q0, 2, 'b', q1);
        vca.setSuccessor(q1, 1, 'b', q1);

        vca.setSuccessor(q2, 3, 'a', q2);
        vca.setSuccessor(q2, 3, 'b', q3);
        vca.setSuccessor(q3, 2, 'b', q4);
        vca.setSuccessor(q3, 3, 'b', q3);
        vca.setSuccessor(q3, 2, 'c', q5);
        vca.setSuccessor(q4, 1, 'b', q4);
        vca.setSuccessor(q5, 2, 'b', q6);
        vca.setSuccessor(q5, 2, 'c', q5);
        vca.setSuccessor(q6, 1, 'b', q6);

        return vca;
    }
}