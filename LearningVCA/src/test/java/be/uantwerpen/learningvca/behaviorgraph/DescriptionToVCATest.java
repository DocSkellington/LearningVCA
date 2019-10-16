package be.uantwerpen.learningvca.behaviorgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import be.uantwerpen.learningvca.vca.State;
import be.uantwerpen.learningvca.vca.VCA;

public class DescriptionToVCATest {
    @Test
    public void descriptionToVCA() {
        Description<Character> description = ConstructBG.constructDescription();

        VCA<Character> vca = description.toVCA(ConstructBG.getAlphabet());

        assertEquals(2, vca.size());

        List<State> states = vca.getStates();
        assertTrue(states.get(0).isAccepting());
        assertTrue(states.get(1).isAccepting());

        assertTrue(vca.accepts(Arrays.asList()));
        assertTrue(vca.accepts(Arrays.asList('a', 'b')));
        assertTrue(vca.accepts(Arrays.asList('a', 'a', 'b', 'b')));
        assertTrue(vca.accepts(Arrays.asList('a', 'a', 'a', 'b', 'b', 'b')));

        assertFalse(vca.accepts(Arrays.asList('a')));
        assertFalse(vca.accepts(Arrays.asList('a', 'a')));
        assertFalse(vca.accepts(Arrays.asList('a', 'b', 'a')));
        assertFalse(vca.accepts(Arrays.asList('a', 'b', 'b')));
        assertFalse(vca.accepts(Arrays.asList('b')));
    }
}