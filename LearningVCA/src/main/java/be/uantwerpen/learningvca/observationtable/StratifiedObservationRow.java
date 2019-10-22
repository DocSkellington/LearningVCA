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
}