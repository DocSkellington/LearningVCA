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
     * Computes the height of a word.
     * 
     * The height of a word is the maximal counter value among the prefixes of the word
     * @param <I> The input alphabet type
     * @param word The word
     * @param alphabet The alphabet
     * @return The height of the word
     */
    public static <I> int computeHeight(Word<I> word, VPDAlphabet<I> alphabet) {
        int counterValue = 0;
        int height = 0;
        for (I symbol : word) {
            switch(alphabet.getSymbolType(symbol)) {
                case CALL:
                    counterValue++;
                    height = Math.max(counterValue, height);
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
        return height;
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

    /**
     * Computes the counter value of a word.
     * 
     * If the counter value reaches a value below zero or exceeds the maximum height, then -1 is returned.
     * @param <I> The alphabet type
     * @param word The word
     * @param alphabet The alphabet
     * @param maxHeight The maximum height of the word
     * @return The counter value or -1.
     */
    public static <I> int computeCounterValue(Word<I> word, VPDAlphabet<I> alphabet, int maxHeight) {
        int counterValue = 0;
        for (I symbol : word) {
            switch(alphabet.getSymbolType(symbol)) {
                case CALL:
                    counterValue++;
                    if (counterValue > maxHeight) {
                        return -1;
                    }
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

    /**
     * Gets the sign of a symbol.
     * 
     * That is, the sign of a call symbol is +1, the sign of a return symbol is -1 and the sign of an internal symbol is 0.
     * @param <I> The input alphabet type
     * @param symbol The symbol
     * @param alphabet The alphabet
     * @return The sign of the symbol
     */
    public static <I> int signOf(I symbol, VPDAlphabet<I> alphabet) {
        switch(alphabet.getSymbolType(symbol)) {
            case CALL:
                return 1;
            case RETURN:
                return -1;
            case INTERNAL:
                return 0;
            default:
                throw new IllegalArgumentException("Unknown symbol type in a pushdown alphabet. Symbol: " + symbol);
        }
    }
}