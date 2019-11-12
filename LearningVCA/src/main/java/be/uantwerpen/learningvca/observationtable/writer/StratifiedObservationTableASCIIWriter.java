package be.uantwerpen.learningvca.observationtable.writer;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import be.uantwerpen.learningvca.observationtable.StratifiedObservationRow;
import be.uantwerpen.learningvca.observationtable.StratifiedObservationTable;
import net.automatalib.words.Word;

/**
 * ASCII writer.
 * @param <I> Alphabet input type
 * @param <D> Data type
 * @author Gaëtan Staquet
 */
public class StratifiedObservationTableASCIIWriter<I, D> extends AbstractStratifiedObservationTableWriter<I, D> {

    private boolean rowSeparators;

    public StratifiedObservationTableASCIIWriter(Function<? super Word<? extends I>, ? extends String> wordToString,
                                       Function<? super D, ? extends String> outputToString,
                                       boolean rowSeparators) {
        super(wordToString, outputToString);
        this.rowSeparators = rowSeparators;
    }

    public StratifiedObservationTableASCIIWriter() {
        this(true);
    }

    public StratifiedObservationTableASCIIWriter(boolean rowSeparators) {
        this.rowSeparators = rowSeparators;
    }

    public void setRowSeparators(boolean rowSeparators) {
        this.rowSeparators = rowSeparators;
    }

    @Override
    protected <J extends Comparable<J>, E> void writeInternal(StratifiedObservationTable<J, E> table,
                                      Function<? super Word<? extends J>, ? extends String> wordToString,
                                      Function<? super E, ? extends String> outputToString,
                                      Appendable out) throws IOException {
        for (int level = 0 ; level <= table.getLevelLimit() ; level++) {
            List<Word<J>> suffixes = table.getSuffixes(level);
            int numSuffixes = suffixes.size();

            int[] colWidth = new int[numSuffixes + 1];

            int i = 1;
            for (Word<J> suffix : suffixes) {
                colWidth[i++] = wordToString.apply(suffix).length();
            }

            for (StratifiedObservationRow<J> row : table.getAllRows(level)) {
                int thisWidth = wordToString.apply(row.getLabel()).length();
                if (thisWidth > colWidth[0]) {
                    colWidth[0] = thisWidth;
                }

                i = 1;
                for (E value : table.rowContents(row)) {
                    thisWidth = outputToString.apply(value).length();
                    if (thisWidth > colWidth[i]) {
                        colWidth[i] = thisWidth;
                    }
                    i++;
                }
            }

            appendLevel(out, level);
            appendSeparatorRow(out, '=', colWidth);
            String[] content = new String[numSuffixes + 1];

            // Header
            content[0] = "";
            i = 1;
            for (Word<J> suffix : suffixes) {
                content[i++] = wordToString.apply(suffix);
            }
            appendContentRow(out, content, colWidth);
            appendSeparatorRow(out, '=', colWidth);

            boolean first = true;
            for (StratifiedObservationRow<J> spRow : table.getShortPrefixRows(level)) {
                if (first) {
                    first = false;
                } else if (rowSeparators) {
                    appendSeparatorRow(out, '-', colWidth);
                }
                content[0] = wordToString.apply(spRow.getLabel());
                i = 1;
                for (E value : table.rowContents(spRow)) {
                    content[i++] = outputToString.apply(value);
                }
                appendContentRow(out, content, colWidth);
            }

            appendSeparatorRow(out, '=', colWidth);

            first = true;
            for (StratifiedObservationRow<J> lpRow : table.getLongPrefixRows(level)) {
                if (first) {
                    first = false;
                } else if (rowSeparators) {
                    appendSeparatorRow(out, '-', colWidth);
                }
                content[0] = wordToString.apply(lpRow.getLabel());
                i = 1;
                for (E value : table.rowContents(lpRow)) {
                    content[i++] = outputToString.apply(value);
                }
                appendContentRow(out, content, colWidth);
            }

            appendSeparatorRow(out, '=', colWidth);
        }
    }

    private static void appendLevel(Appendable a, int level) throws IOException {
        a.append("Level " + level).append(System.lineSeparator());
    }

    private static void appendSeparatorRow(Appendable a, char sepChar, int[] colWidth) throws IOException {
        a.append('+').append(sepChar);
        appendRepeated(a, sepChar, colWidth[0]);
        for (int i = 1; i < colWidth.length; i++) {
            a.append(sepChar).append('+').append(sepChar);
            appendRepeated(a, sepChar, colWidth[i]);
        }
        a.append(sepChar).append("+").append(System.lineSeparator());
    }

    private static void appendContentRow(Appendable a, String[] content, int[] colWidth) throws IOException {
        a.append("| ");
        appendRightPadded(a, content[0], colWidth[0]);
        for (int i = 1; i < content.length; i++) {
            a.append(" | ");
            appendRightPadded(a, content[i], colWidth[i]);
        }
        a.append(" |").append(System.lineSeparator());
    }

    private static void appendRepeated(Appendable a, char c, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            a.append(c);
        }
    }

    private static void appendRightPadded(Appendable a, String string, int width) throws IOException {
        a.append(string);
        appendRepeated(a, ' ', width - string.length());
    }

}