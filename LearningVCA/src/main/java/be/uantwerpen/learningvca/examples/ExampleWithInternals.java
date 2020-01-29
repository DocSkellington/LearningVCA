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
 * 1-VCA for L = {a^n c b^n | n > 0} where 'a' is a call symbol, 'b' is a return symbol and 'c' is an internal symbol
 */
public final class ExampleWithInternals {
    /**
     * Gets the pushdown alphabet with 'a' as call symbol, 'b' as return symbol and 'c' as internal symbol
     * @return The alphabet
     */
    public static VPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<Character>(Arrays.asList('c'), Arrays.asList('a'), Arrays.asList('b'));
    }

    /**
     * Constructs a simple 1-VCA for L = {a^n c b^n | n > 0}
     * @return A 1-VCA
     */
    public static VCA<?, Character> getVCA() {
        DefaultVCA<Character> vca = new DefaultVCA<>(getAlphabet(), 1);

        Location q0 = vca.addInitialLocation();
        Location q1 = vca.addLocation(true);

        vca.setSuccessor(q0, 0, 'a', q0);
        vca.setSuccessor(q0, 1, 'a', q0);
        vca.setSuccessor(q0, 1, 'c', q1);
        vca.setSuccessor(q1, 1, 'b', q1);

        return vca;
    }
}