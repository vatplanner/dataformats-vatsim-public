package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.function.Function;

/**
 * Provides helper methods to perform comparisons.
 */
public class Comparisons {

    private Comparisons() {
        // hide utility class constructor
    }

    /**
     * Checks if the results returned by both objects' accessor is either equal or
     * null. If only one accessor result is null, the evaluation result will always
     * be false.
     *
     * @param <T>      type of objects
     * @param a        first object, must not be null
     * @param b        second object, must not be null
     * @param accessor accessor to retrieve value to be compared; you will likely
     *                 want to provide a method reference to a getter
     * @return true if both accessor results are either null or equal (determined by {@link Object#equals(Object)})
     */
    public static <T> boolean equalsNullSafe(T a, T b, Function<T, Object> accessor) {
        Object objA = accessor.apply(a);
        Object objB = accessor.apply(b);

        if (objA == null) {
            return (objB == null);
        } else if (objB == null) {
            return false;
        }

        return objA.equals(objB);
    }
}
