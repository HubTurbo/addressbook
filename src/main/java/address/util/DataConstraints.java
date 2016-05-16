package address.util;

import address.model.DataType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for enforcing data constraints
 */
public class DataConstraints {

    public static <D extends DataType> boolean itemsAreUnique(Collection<D> items) {
        final Set<D> set = new HashSet<>();
        for (D item : items) {
            if (!set.add(item)) return false;
        }
        return true;
    }
}
