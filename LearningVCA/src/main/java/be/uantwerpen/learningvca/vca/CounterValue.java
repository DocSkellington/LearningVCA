package be.uantwerpen.learningvca.vca;

/**
 * The counter value.
 * @author Gaëtan Staquet
 */
public class CounterValue {
    private final int counter;

    /**
     * Constructs a counter value from an integer.
     * @param counterValue
     */
    public CounterValue(int counterValue) {
        this.counter = counterValue;
    }

    /**
     * Gets the counter value as an integer.
     * @return The counter value 
     */
    public int toInt() {
        return counter;
    }

    /**
     * Constructs a new counter value whose value is the incrementation of the current value by one.
     * 
     * This does not change the counter of this instance.
     * @return A counter value of value (current value + 1)
     */
    public CounterValue increment() {
        return new CounterValue(counter + 1);
    }

    /**
     * Constructs a new counter value whose value is the decrementation of the current value by one.
     * 
     * This does not change the counter of this instance.
     * @return A counter value of value (current value - 1)
     */
    public CounterValue decrement() {
        return new CounterValue(counter - 1);
    }

    /**
     * Checks if the counter value is zero.
     * @return If the counter value is zero
     */
    public boolean isZero() {
        return counter == 0;
    }
}