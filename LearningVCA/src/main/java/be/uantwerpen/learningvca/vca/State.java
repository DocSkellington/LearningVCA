package be.uantwerpen.learningvca.vca;

/**
 * A configuration (or state in LearnLib nomenclature) is a pair (location, counter value).
 * @param <L> The type of the location
 * @author Gaëtan Staquet
 */
public class State<L> {
    private final static State<?> SINK = new State<>(null, null);
    private final L location;
    private final CounterValue counter;

    /**
     * Constructs a configuration from a state and a counter value
     * @param location The location
     * @param counter The counter value
     */
    public State(L location, CounterValue counter) {
        this.location = location;
        this.counter = counter;
    }

    /**
     * Gets the location 
     * @return The location
     */
    public L getLocation() {
        return location;
    }

    /**
     * Gets the counter value
     * @return The counter value
     */
    public CounterValue getCounterValue() {
        return counter;
    }

    /**
     * Gets the sink configuration
     * @return The sink configuration
     */
    @SuppressWarnings("unchecked")
    public State<L> getSink() {
        return (State<L>) SINK;
    }

    /**
     * Is the current configuration a sink configuration
     * @return True iff the current configuration is a sink
     */
    public boolean isSink() {
        return location == null;
    }

    @Override
    public String toString() {
        return "(" + getLocation().toString() + ", " + counter.toString() + ")";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        State<L> other = (State<L>)obj;
        if (isSink() && other.isSink()) {
            return true;
        }

        if (other.location == null && this.location == null) {
            return false;
        }
        else if (other.location == null || this.location == null) {
            return false;
        }

        if (other.counter == null && this.counter == null) {
            return false;
        }
        else if (other.counter == null || this.counter == null) {
            return false;
        }

        return this.location.equals(other.location) && this.counter.equals(other.counter);
    }
}