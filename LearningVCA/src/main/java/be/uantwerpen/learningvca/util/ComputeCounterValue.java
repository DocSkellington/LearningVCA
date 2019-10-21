package be.uantwerpen.learningvca.util;

import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * Helper class to compute the counter value of a word
 * @author Gaëtan Staquet
 */
public class ComputeCounterValue {
    private ComputeCounterValue() {

    }

    /**
     * Computes the counter value of a word.
     * 
     * If the word can not be processed by a VCA (that is, if the counter value reaches a value below zero), then -1 is returned.
     * @param <I> The alphabet type
     * @param word The word
     * @param alphabet The alphabet
     * @return The counter value or -1.
     */
    public static <I> int computeCounterValue(Word<I> word, VPDAlphabet<I> alphabet) {
        int counterValue = 0;
        for (I symbol : word) {
            switch(alphabet.getSymbolType(symbol)) {
                case CALL:
                    counterValue++;
                    break;
                case RETURN:
                    if (counterValue == 0) {
                        return -1;
                    }
                    else {
                        counterValue--;
                    }
                    break;
                case INTERNAL:
                    break;
                default:
                    break;
            }
        }
        return counterValue;
    }
}