package be.uantwerpen.learningvca.behaviorgraph;

import java.util.Objects;

/**
 * A state is designated by the pair (index of the nu enumeration, number associated with the equivalence class).
 * @author Gaëtan Staquet
 */
public class StateBG {
    private final int mapping;
    private final int equivalenceClass;

    public StateBG(int mapping, int equivalenceClass) {
        this.mapping = mapping;
        this.equivalenceClass = equivalenceClass;
    }

    /**
     * @return the mapping
     */
    public int getMapping() {
        return mapping;
    }

    /**
     * @return the equivalenceClass
     */
    public int getEquivalenceClass() {
        return equivalenceClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }
        StateBG o = (StateBG)obj;
        return o.equivalenceClass == this.equivalenceClass && o.mapping == this.mapping;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapping, equivalenceClass);
    }
};