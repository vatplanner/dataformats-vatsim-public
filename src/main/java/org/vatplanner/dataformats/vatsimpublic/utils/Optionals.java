package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.Optional;

/**
 * Helper methods to work with {@link Optional}s.
 */
public class Optionals {
    private Optionals() {
        // utility class, hide constructor
    }

    /**
     * Tests if all given {@link Optional}s are present.
     * Returns false if no {@link Optional}s are provided or at least one is not present.
     *
     * @param optionals {@link Optional}s to check
     * @return {@code true} if all {@link Optional}s are present; {@code false} if none have been provided or at least one is empty
     */
    public static boolean allPresent(Optional<?>... optionals) {
        if (optionals.length == 0) {
            return false;
        }

        for (Optional<?> optional : optionals) {
            if (!optional.isPresent()) {
                return false;
            }
        }

        return true;
    }
}
