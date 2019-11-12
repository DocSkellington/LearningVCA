package be.uantwerpen.learningvca.observationtable.writer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Function;

import java.awt.Desktop;

import be.uantwerpen.learningvca.observationtable.StratifiedObservationRow;
import be.uantwerpen.learningvca.observationtable.StratifiedObservationTable;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.words.Word;

/**
 * HTML writer.
 * @param <I> Alphabet input type
 * @param <D> Data type
 * @author Gaëtan Staquet
 */
public class StratifiedObservationTableHTMLWriter<I, D> extends AbstractStratifiedObservationTableWriter<I, D> {

    private static final String HTML_FILE_HEADER =
            "<!doctype html>" + System.lineSeparator()
                    + "<html><head>" + System.lineSeparator()
                    + "<meta charset=\"UTF-8\">" + System.lineSeparator()
                    + "<style type=\"text/css\">" + System.lineSeparator()
                    + "table.learnlib-observationtable { border-width: 1px; border: solid; }" + System.lineSeparator()
                    + "table.learnlib-observationtable th.suffixes-header { text-align: center; }" + System.lineSeparator()
                    + "table.learnlib-observationtable th.prefix { vertical-align: top; }" + System.lineSeparator()
                    + "table.learnlib-observationtable .suffix-column { text-align: left; }" + System.lineSeparator()
                    + "table.learnlib-observationtable tr { border-width: 1px; border: solid; }" + System.lineSeparator()
                    + "table.learnlib-observationtable tr.long-prefix { background-color: #dfdfdf; }" + System.lineSeparator()
                    + "</style></head>" + System.lineSeparator()
                    + "<body>" + System.lineSeparator();

    private static final String HTML_FILE_FOOTER = "</body></html>" + System.lineSeparator();

    private static <I extends Comparable<I>, D> void writeHTMLToFile(StratifiedObservationTable<I, D> table,
                                              File file,
                                              Function<? super Word<? extends I>, ? extends String> wordToString,
                                              Function<? super D, ? extends String> outputToString) throws IOException {

        try (Writer w = IOUtil.asBufferedUTF8Writer(file)) {
            w.write(HTML_FILE_HEADER);
            StratifiedObservationTableHTMLWriter<I, D> otWriter = new StratifiedObservationTableHTMLWriter<>();
            otWriter.write(table, w);
            w.write(HTML_FILE_FOOTER);
        }
    }

    /**
     * Writes the table in an HTML file and open this file in the default browser
     * @param <I> Input alphabet type
     * @param <E> Data type
     * @param table The table
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public static <I, D> void displayHTMLInBrowser(StratifiedObservationTable<? extends I, ? extends D> table) throws IOException, UnsupportedOperationException {
        File tempFile = File.createTempFile("learnlib-ot", ".html");

        // Doing this might cause problems if the startup delay of the browser
        // causes it to start only after the JVM has exited.
        // Temp directory should be wiped regularly anyway.
        // tempFile.deleteOnExit();
        writeHTMLToFile(table, tempFile, Object::toString, Object::toString);

        Desktop desktop = Desktop.getDesktop();
        // We use browse() instead of open() because, e.g., web developers may have
        // an HTML editor set up as their default application to open HTML files
        desktop.browse(tempFile.toURI());
    }

	@Override
	protected <J extends Comparable<J>, E> void writeInternal(StratifiedObservationTable<J, E> table,
			Function<? super Word<? extends J>, ? extends String> wordToString,
			Function<? super E, ? extends String> outputToString, Appendable out) throws IOException {
        for (int level = 0 ; level <= table.getLevelLimit() ; level++) {
            List<Word<J>> suffixes = table.getSuffixes(level);

            out.append("<h2>Level " + level + "</h2>");
            out.append("<table class=\"learnlib-observationtable\">").append(System.lineSeparator());
            out.append("\t<thead>").append(System.lineSeparator());
            out.append("\t\t<tr><th rowspan=\"2\" class=\"prefix\">Prefix</th><th colspan=\"")
            .append(Integer.toString(suffixes.size()))
            .append("\" class=\"suffixes-header\">Suffixes</th></tr>").append(System.lineSeparator());
            out.append("\t\t<tr>");
            for (Word<J> suffix : suffixes) {
                out.append("<td>").append(wordToString.apply(suffix)).append("</td>");
            }
            out.append("</tr>").append(System.lineSeparator());
            out.append("\t</thead>").append(System.lineSeparator());
            out.append("\t<tbody>").append(System.lineSeparator());

            for (StratifiedObservationRow<J> row : table.getShortPrefixRows(level)) {
                out.append("\t\t<tr class=\"short-prefix\"><td class=\"prefix\">")
                .append(wordToString.apply(row.getLabel()))
                .append("</td>");
                for (E value : table.rowContents(row)) {
                    out.append("<td class=\"suffix-column\">").append(outputToString.apply(value)).append("</td>");
                }
                out.append("</tr>").append(System.lineSeparator());
            }

            out.append("\t\t<tr><td colspan=\"").append(Integer.toString(suffixes.size() + 1)).append("\"></td></tr>").append(System.lineSeparator());

            for (StratifiedObservationRow<J> row : table.getLongPrefixRows(level)) {
                out.append("\t\t<tr class=\"long-prefix\"><td>").append(wordToString.apply(row.getLabel())).append("</td>");
                for (E value : table.rowContents(row)) {
                    out.append("<td class=\"suffix-column\">").append(outputToString.apply(value)).append("</td>");
                }
                out.append("</tr>").append(System.lineSeparator());
            }

            out.append("</table>").append(System.lineSeparator());
        }
	}

}