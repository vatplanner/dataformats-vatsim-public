package org.vatplanner.dataformats.vatsimpublic.utils;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CollectionHelpersTest {

    @Test
    void testFindPrevious_emptyCollection_returnsEmpty() {
        // Arrange
        List<Integer> empty = Collections.emptyList();

        // Act
        Optional<Integer> result = CollectionHelpers.findPrevious(empty, 0);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testFindPrevious_noPreviousItem_returnsEmpty() {
        // Arrange
        Integer needle = 0;
        List<Integer> empty = Collections.singletonList(needle);

        // Act
        Optional<Integer> result = CollectionHelpers.findPrevious(empty, needle);

        // Assert
        assertThat(result).isEmpty();
    }

    static Stream<Arguments> dataProviderFindPrevious() {
        return Stream.of(
            Arguments.of(asList(0, 1), 1, 0),
            Arguments.of(asList(0, 1, 2), 2, 1),
            Arguments.of(asList(5, 23, 42, 73), 23, 5),
            Arguments.of(asList(5, 23, 42, 73), 42, 23),
            Arguments.of(asList(5, 23, 42, 73), 73, 42)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderFindPrevious")
    void testFindPrevious_inCollection_returnsExpectedElement(List<Integer> haystack, Integer needle, Integer expectedResult) {
        // Arrange (nothing to do)

        // Act
        Optional<Integer> result = CollectionHelpers.findPrevious(haystack, needle);

        // Assert
        assertThat(result).contains(expectedResult);
    }
}
