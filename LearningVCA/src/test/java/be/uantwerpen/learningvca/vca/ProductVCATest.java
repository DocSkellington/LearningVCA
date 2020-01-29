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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class ProductVCATest {
    @Test
    public void testProduct() {
        VPDAlphabet<Character> alphabet = new DefaultVPDAlphabet<>(Arrays.asList('c'), Arrays.asList('a'), Arrays.asList('b'));
        DefaultVCA<Character> vca1 = new DefaultVCA<>(alphabet, 0);
        vca1.addInitialLocation(true);

        DefaultVCA<Character> vca2 = new DefaultVCA<>(alphabet, 1);
        Location q0 = vca2.addInitialLocation(false);
        Location q1 = vca2.addLocation(true);
        vca2.setSuccessor(q0, 0, 'a', q0);
        vca2.setSuccessor(q0, 1, 'a', q0);
        vca2.setSuccessor(q0, 1, 'b', q1);
        vca2.setSuccessor(q1, 1, 'b', q1);

        VCA<?, Character> vca = new ProductVCA<>(alphabet, vca1, vca2, AcceptanceCombiner.XOR);
        Word<Character> w = vca.getAcceptedWord();
        assertNotNull(w);
        assertTrue(vca.accepts(w));
    }
}