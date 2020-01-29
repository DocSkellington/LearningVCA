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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import be.uantwerpen.learningvca.observationtable.StratifiedObservationTable;
import net.automatalib.commons.util.IOUtil;;

/**
 * Interface for writers for a stratified observation table
 * @param <I> Input alphabet type
 * @param <D> The type of data stored in the table
 * @author Gaëtan Staquet
 */
public interface StratifiedObservationTableWriter<I, D> {

    /**
     * Writes the observation table to {@code out}
     * @param table The stratified observation table
     * @param out Where to write
     * @throws IOException
     */
    void write(StratifiedObservationTable<? extends I, ? extends D> table, Appendable out) throws IOException;

    /**
     * Writes the observation table to {@code out}
     * @param table The stratified observation table
     * @param out Where to write
     * @throws IOException
     */
    default void write(StratifiedObservationTable<? extends I, ? extends D> table, File out) throws IOException {
        try (Writer w = IOUtil.asBufferedUTF8Writer(out)) {
            write(table, w);
        }
    }

    /**
     * Writes the observation table to {@code out}
     * @param table The stratified observation table
     * @param out Where to write
     */
    default void write(StratifiedObservationTable<? extends I, ? extends D> table, PrintStream out) {
        try {
            write(table, (Appendable) out);
        } catch (IOException ex) {
            throw new AssertionError("Writing to PrintStream must not throw", ex);
        }
    }

    /**
     * Writes the observation table to {@code out}
     * @param table The stratified observation table
     * @param out Where to write
     */
    default void write(StratifiedObservationTable<? extends I, ? extends D> table, StringBuilder out) {
        try {
            write(table, (Appendable) out);
        } catch (IOException ex) {
            throw new AssertionError("Writing to StringBuilder must not throw", ex);
        }
    }
}