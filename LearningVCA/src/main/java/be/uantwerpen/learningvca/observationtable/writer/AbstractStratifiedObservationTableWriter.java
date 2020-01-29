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
package be.uantwerpen.learningvca.observationtable.writer;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import be.uantwerpen.learningvca.observationtable.StratifiedObservationTable;
import net.automatalib.words.Word;

/**
 * Abstract implementation of a writer for a stratified observation table
 * @param <I> Input alphabet type
 * @param <D> Type of the data stored in the table
 * @author Gaëtan Staquet
 */
public abstract class AbstractStratifiedObservationTableWriter<I, D> implements StratifiedObservationTableWriter<I, D> {

    protected Function<? super Word<? extends I>, ? extends String> wordToString;
    protected Function<? super D, ? extends String> outputToString;

    public AbstractStratifiedObservationTableWriter() {
        this(Objects::toString, Objects::toString);
    }

    public AbstractStratifiedObservationTableWriter(Function<? super Word<? extends I>, ? extends String> wordToString,
                                          Function<? super D, ? extends String> outputToString) {
        this.wordToString = safeToStringFunction(wordToString);
        this.outputToString = safeToStringFunction(outputToString);
    }

    protected static <T> Function<? super T, ? extends String> safeToStringFunction(Function<? super T, ? extends String> toStringFunction) {
        if (toStringFunction != null) {
            return toStringFunction;
        }
        return Objects::toString;
    }

    public void setWordToString(Function<? super Word<? extends I>, ? extends String> wordToString) {
        this.wordToString = safeToStringFunction(wordToString);
    }

    public void setOutputToString(Function<? super D, ? extends String> outputToString) {
        this.outputToString = safeToStringFunction(outputToString);
    }

    protected String wordToString(Word<? extends I> word) {
        return wordToString.apply(word);
    }

    protected String outputToString(D output) {
        return outputToString.apply(output);
    }

    @Override
    public void write(StratifiedObservationTable<? extends I, ? extends D> table, Appendable out) throws IOException {
        writeInternal(table, wordToString, outputToString, out);
    }

    /**
     * Effectively writes the table
     * @param <J> The true input type
     * @param <E> The true data type
     * @param table The table
     * @param wordToString The function to use to convert word to string
     * @param outputToString The function to use to convert data to string
     * @param out Where to write
     * @throws IOException
     */
    protected abstract <J extends Comparable<J>, E> void writeInternal(
        StratifiedObservationTable<J, E> table,
        Function<? super Word<? extends J>, ? extends String> wordToString,
        Function<? super E, ? extends String> outputToString,
        Appendable out) throws IOException;

}