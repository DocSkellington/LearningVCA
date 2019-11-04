package be.uantwerpen.learningvca.vca;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.Graph;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.VPDAlphabet;

/**
 * An abstract implementation of a visibly one-counter automaton
 * @param <L> The type of the locations
 * @param <I> The input alphabet type
 * @author Gaëtan Staquet
 */
public abstract class AbstractVCA<L, I> implements VCA<L, I>, Graph<L, AbstractVCA.VCAViewEdge<L, I>> {
    /**
     * Represents an edge.
     * 
     * It is used for the conversion to DOT format.
     * @param <S> The state type
     * @param <I> The input alphabet type
     */
    static class VCAViewEdge<S, I> {
        private final I input;
        private final int counterValue;
        private final S target;

        VCAViewEdge(I input, int counterValue, S target) {
            this.input = input;
            this.counterValue = counterValue;
            this.target = target;
        }
    }

    protected final VPDAlphabet<I> alphabet;

    public AbstractVCA(VPDAlphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public VPDAlphabet<I> getAlphabet() {
        return alphabet;
    }
    
    // Both interfaces define size
    @Override
    public abstract int size();

    @Override
    public Collection<L> getNodes() {
        return Collections.unmodifiableCollection(getLocations());
    }

    @Override
    public State<L> getTransition(State<L> state, I input) {
        // If we are in a sink, we stay in the sink
        if (state.isSink()) {
            return state;
        }

        L successor = null;
        CounterValue successorValue = null;
        switch (alphabet.getSymbolType(input)) {
        case CALL:
            successor = getCallSuccessor(state.getLocation(), input, state.getCounterValue().toInt());
            successorValue = state.getCounterValue().increment();
            break;
        case RETURN:
            successor = getReturnSuccessor(state.getLocation(), input, state.getCounterValue().toInt());
            successorValue = state.getCounterValue().decrement();
            break;
        case INTERNAL:
            successor = getInternalSuccessor(state.getLocation(), input, state.getCounterValue().toInt());
            successorValue = state.getCounterValue();
            break;
        default:
            break;
        }
        return new State<L>(successor, successorValue);
    }

    @Override
    public Collection<VCAViewEdge<L, I>> getOutgoingEdges(L startingLoc) {
        List<VCAViewEdge<L, I>> result = new ArrayList<>();

        for (int counterValue = 0 ; counterValue <= getThreshold() ; counterValue++) {
            for (I symbol : alphabet) {
                L successor = getSuccessor(startingLoc, symbol, counterValue);

                if (successor != null) {
                    result.add(new VCAViewEdge<>(symbol, counterValue, successor));
                }
            }
        }

        return result;
    }

    @Override
    public L getTarget(VCAViewEdge<L, I> edge) {
        return edge.target;
    }

    @Override
    public VisualizationHelper<L, VCAViewEdge<L, I>> getVisualizationHelper() {
        return new DefaultVisualizationHelper<L, VCAViewEdge<L, I>>() {
            @Override
            protected Collection<L> initialNodes() {
                return Collections.singleton(getInitialLocation());
            }

            @Override
            public boolean getNodeProperties(L node, Map<String, String> properties) {
                super.getNodeProperties(node, properties);

                properties.put(NodeAttrs.SHAPE, isAcceptingLocation(node) ? NodeShapes.DOUBLECIRCLE : NodeShapes.CIRCLE);
                properties.put(NodeAttrs.LABEL, "q" + getLocationId(node));

                return true;
            }

            @Override
            public boolean getEdgeProperties(L src, VCAViewEdge<L, I> edge, L tgt,
                    Map<String, String> properties) {
                final I input = edge.input;
                final int counterValue = edge.counterValue;

                properties.put(EdgeAttrs.LABEL, input + ", " + counterValue);
                return true;
            }
        };
    }
}