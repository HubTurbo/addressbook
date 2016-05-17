package address.util;

import address.model.DataType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for enforcing data constraints
 */
public class DataConstraints {

    /**
     * Checks that the argument collection fulfills the set property.
     *
     * @param items group of items to be tested
     * @return true if no duplicates found in items
     */
    public static <D extends DataType> boolean itemsAreUnique(Collection<D> items) {
        final Set<D> test = new HashSet<>();
        for (D item : items) {
            if (!test.add(item)) return false;
        }
        return true;
    }

    public static <D extends DataType> boolean canCombineWithoutDuplicates(Collection<D>... itemCollections) {
        return areUniqueAndDisjoint(itemCollections);
    }

    /**
     * Checks that the argument collections fulfill the Set property and are disjoint relative to each other.
     *
     * @param itemCollections
     * @return true if every collection in itemCollections contains no duplicates and are disjoint
     */
    public static <D extends DataType> boolean areUniqueAndDisjoint(Collection<D>... itemCollections) {
        final Set<D> test = new HashSet<>();
        for (Collection<D> items : itemCollections) {
            for (D item : items) {
                if (!test.add(item)) return false;
            }
        }
        return true;
    }
}
