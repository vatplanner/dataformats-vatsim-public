package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class holding commonly used helper methods to work with
 * {@link Collection} objects.
 */
public class CollectionHelpers {

    private CollectionHelpers() {
        // utility class; hide constructor
    }

    /**
     * Creates an unmodifiable {@link Set} holding the given values.
     *
     * @param <T> value type
     * @param arr values to be added
     * @return unmodifiable {@link Set} holding given values
     */
    public static <T> Set<T> asUnmodifiableSet(T... arr) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(arr)));
    }
}
