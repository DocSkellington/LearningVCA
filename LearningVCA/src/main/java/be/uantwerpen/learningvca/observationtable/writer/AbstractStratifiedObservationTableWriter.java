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