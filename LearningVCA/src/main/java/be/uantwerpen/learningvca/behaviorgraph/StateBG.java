/*
 * LearningVCA - An implementation of an active learning algorithm for Visibly One-Counter Automata
 * Copyright (C) 2020 University of Mons and University of Antwerp
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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