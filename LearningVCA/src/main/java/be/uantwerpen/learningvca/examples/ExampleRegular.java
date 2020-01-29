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
import java.util.Collections;

import be.uantwerpen.learningvca.vca.DefaultVCA;
import be.uantwerpen.learningvca.vca.Location;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

/**
 * 0-VCA for L = {(a b)^n (b a)^m | n, m > 0} where a and b are internal symbols
 */
public final class ExampleRegular {
    /**
     * Gets the pushdown alphabet with 'a' and 'b' as internal symbols
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<>(Arrays.asList('a', 'b'), Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Constructs a 0-VCA for L = {(a b)^n (b a)^m | n, m > 0}
     * @return A 0-VCA
     */
    public static VCA<?, Character> getVCA() {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), 0);
        Location q0 = vca.addInitialLocation();
        Location q1 = vca.addLocation();
        Location q2 = vca.addLocation();
        Location q3 = vca.addLocation();
        Location q4 = vca.addLocation(true);
        vca.setSuccessor(q0, 0, 'a', q1);
        vca.setSuccessor(q1, 0, 'b', q2);
        vca.setSuccessor(q2, 0, 'a', q1);
        vca.setSuccessor(q2, 0, 'b', q3);
        vca.setSuccessor(q3, 0, 'a', q4);
        vca.setSuccessor(q4, 0, 'b', q3);
        return vca;
    }
}