package be.uantwerpen.learningvca.observationtable;

import java.util.HashMap;
import java.util.Map;

import be.uantwerpen.learningvca.behaviorgraph.LimitedBehaviorGraph;
import be.uantwerpen.learningvca.vca.State;
import be.uantwerpen.learningvca.vca.VCA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * A stratified observation table that stores booleans.
 * 
 * @param <I> The input alphabet type
 * @author Gaëtan Staquet
 */
public class StratifiedObservationTableBoolean<I extends Comparable<I>>
        extends AbstractStratifiedObservationTable<I, Boolean> {
    public StratifiedObservationTableBoolean(VPDAlphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public VCA<I> toVCA() {
        VCA<I> vca = new VCA<>(alphabet, maxLevel);
        Map<StratifiedObservationRow<I>, State> rowToState = new HashMap<>();

        for (int level = 0; level <= maxLevel; level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(level)) {
                State qi = null;
                if (level == 0 && shortPrefixRow.getLabel() == Word.<I>epsilon()) {
                    qi = vca.addInitialState(cellContents(shortPrefixRow, 0));
                } else {
                    qi = vca.addState(cellContents(shortPrefixRow, 0));
                }
                rowToState.put(shortPrefixRow, qi);
            }
        }

        // We create the transitions
        for (int level = 0; level <= maxLevel; level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(level)) {
                State startingState = rowToState.get(shortPrefixRow);
                for (int i = 0; i < alphabet.size(); i++) {
                    I symbol = alphabet.getSymbol(i);
                    StratifiedObservationRow<I> successor = shortPrefixRow.getSuccessor(i);
                    if (successor == null) {
                        continue;
                    }

                    StratifiedObservationRow<I> equivalenceClass = getRepresentativeRow(successor);
                    if (equivalenceClass == null) {
                        continue;
                    }

                    State targetState = rowToState.get(equivalenceClass);
                    switch (alphabet.getSymbolType(symbol)) {
                    case CALL:
                        vca.setCallSuccessor(startingState, level, symbol, targetState);
                        break;
                    case RETURN:
                        vca.setReturnSuccessor(startingState, level, symbol, targetState);
                        break;
                    case INTERNAL:
                        vca.setInternalSuccessor(startingState, level, symbol, targetState);
                        break;
                    default:
                        break;
                    }
                }
            }
        }

        return vca;
    }

    @Override
    public LimitedBehaviorGraph<I> toLimitedBehaviorGraph() {
        LimitedBehaviorGraph<I> limitedBehaviorGraph = new LimitedBehaviorGraph<>(alphabet, maxLevel);
        Map<StratifiedObservationRow<I>, Integer> rowToState = new HashMap<>();

        for (int level = 0; level <= maxLevel; level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(level)) {
                Integer qi = null;
                if (level == 0 && shortPrefixRow.getLabel() == Word.<I>epsilon()) {
                    qi = limitedBehaviorGraph.addInitialState(cellContents(shortPrefixRow, 0));
                } else {
                    qi = limitedBehaviorGraph.addState(cellContents(shortPrefixRow, 0));
                }
                limitedBehaviorGraph.setStateLevel(qi, level);
                rowToState.put(shortPrefixRow, qi);
            }
        }

        // We create the transitions
        for (int level = 0; level <= maxLevel; level++) {
            for (StratifiedObservationRow<I> shortPrefixRow : shortPrefixRows.get(level)) {
                Integer startingState = rowToState.get(shortPrefixRow);
                for (int i = 0; i < alphabet.size(); i++) {
                    I symbol = alphabet.getSymbol(i);
                    StratifiedObservationRow<I> successor = shortPrefixRow.getSuccessor(i);
                    if (successor == null) {
                        continue;
                    }

                    StratifiedObservationRow<I> equivalenceClass = getRepresentativeRow(successor);
                    if (equivalenceClass == null) {
                        continue;
                    }

                    Integer targetState = rowToState.get(equivalenceClass);
                    limitedBehaviorGraph.setTransition(startingState, symbol, targetState);
                }
            }
        }

        return limitedBehaviorGraph;
    }

}