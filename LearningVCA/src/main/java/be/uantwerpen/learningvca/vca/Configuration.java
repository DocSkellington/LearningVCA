package be.uantwerpen.learningvca.vca;

/**
 * A configuration (or state in LearnLib nomenclature) is a pair (location, counter value).
 * @param <L> The type of the location
 * @author Gaëtan Staquet
 */
public class Configuration<L> {
    private final static Configuration<?> SINK = new Configuration<>(null, null);
    private final L location;
    private final CounterValue counter;

    /**
     * Constructs a configuration from a state and a counter value
     * @param location The location
     * @param counter The counter value
     */
    public Configuration(L location, CounterValue counter) {
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
    public Configuration<L> getSink() {
        return (Configuration<L>) SINK;
    }

    /**
     * Is the current configuration a sink configuration
     * @return True iff the current configuration is a sink
     */
    public boolean isSink() {
        return location == null;
    }
}