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
 * m-VCA for L = {a^n b^n | n >= m} where 'a' is a call symbol and 'b' a return symbol.
 * 
 * The m is fixed when the VCA is constructed.
 */
public final class ExampleVariableThreshold {
    /**
     * Gets the pushdown alphabet with 'a' as call and 'b' as return
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<>(Collections.emptyList(), Arrays.asList('a'), Arrays.asList('b'));
    }

    /**
     * Constructs a m-VCA for L = {a^n b^n | n >= m}
     * @return A m-VCA
     */
    public static VCA<?, Character> getVCA(int threshold) {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), threshold);

        Location q0 = vca.addInitialLocation();
        Location q1 = vca.addLocation(true);

        for (int i = 0 ; i <= threshold ; i++) {
            vca.setSuccessor(q0, i, 'a', q0);
        }
        vca.setSuccessor(q0, threshold, 'b', q1);
        for (int i = 0 ; i <= threshold ; i++) {
            vca.setSuccessor(q1, i, 'b', q1);
        }

        return vca;
    }
}