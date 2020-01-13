package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

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

    /**
     * Searches a {@link Collection} for given needle and returns the element
     * just before, if any exists. To make any sense, Collection iterator should
     * return elements in a consistent order (such as for a {@link List} or
     * {@link SortedSet}).
     *
     * <p>
     * Comparison is performed by object reference match.
     * </p>
     *
     * @param <T> type of elements
     * @param haystack collection of elements to search through
     * @param needle element to search
     * @return element just before needle; empty if not found, never null
     */
    public static <T> Optional<T> findPrevious(Collection<T> haystack, T needle) {
        Iterator<T> it = haystack.iterator();
        if (!it.hasNext()) {
            return Optional.empty();
        }

        T previous = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (next == needle) {
                return Optional.of(previous);
            }
            previous = next;
        }

        return Optional.empty();
    }
}
