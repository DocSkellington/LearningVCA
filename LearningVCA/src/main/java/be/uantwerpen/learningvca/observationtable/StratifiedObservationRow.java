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
package be.uantwerpen.learningvca.observationtable;

import java.util.List;

import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.words.Word;

/**
 * A row in a stratified observation table
 * @author Gaëtan Staquet
 */
public class StratifiedObservationRow<I> implements Row<I> {
    private final Word<I> label;
    private final int rowID;
    private int rowContentId;
    private List<StratifiedObservationRow<I>> successors;

    /**
     * Constructs a long prefix row
     * @param label The long prefix
     * @param rowID The ID of this row
     */
    public StratifiedObservationRow(Word<I> label, int rowID) {
        this.label = label;
        this.rowID = rowID;
        this.rowContentId = -1;
        successors = null;
    }

    /**
     * Constructs a short prefix row
     * @param label The short prefix
     * @param rowID The ID of this row
     * @param alphabetSize The size of the alphabet
     */
    public StratifiedObservationRow(Word<I> label, int rowID, int alphabetSize) {
        this.label = label;
        this.rowID = rowID;
        this.rowContentId = -1;
        makeShort(alphabetSize);
    }

    @Override
    public int getRowId() {
        return rowID;
    }

    @Override
    public int getRowContentId() {
        return rowContentId;
    }

    public void setRowContentId(int rowContentId) {
        this.rowContentId = rowContentId;
    }

    @Override
    public Word<I> getLabel() {
        return label;
    }

    @Override
    public boolean isShortPrefixRow() {
        // A row is short iff it has successors
        return successors != null;
    }

    @Override
    public StratifiedObservationRow<I> getSuccessor(int pos) {
        return successors.get(pos);
    }

    public void setSuccessor(int pos, StratifiedObservationRow<I> successor) {
        this.successors.set(pos, successor);
    }

    /**
     * Converts this row to a short prefix row.
     * 
     * If the row is already short, nothing happens
     * @param alphabetSize The size of the alphabet
     */
    protected void makeShort(int alphabetSize) {
        if (isShortPrefixRow()) {
            return;
        }
        this.successors = new ArrayStorage<>(alphabetSize);
    }

    /**
     * @return True iff this row has contents
     */
    public boolean hasContents() {
        return (rowContentId != -1);
    }

    @Override
    public String toString() {
        return rowID + " " + label + " " + rowContentId;
    }
}