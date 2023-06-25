package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RemoveLineStrategyTest {

    @ParameterizedTest
    @CsvSource({ //
        "unwanted, ''", //
        "abc:def, de:abc:f", //
        "original, Z", //
    })
    void testHandleError_always_returnsNull(String rawLine, String filteredLine) {
        // Arrange
        RemoveLineStrategy strategy = new RemoveLineStrategy();

        // Act
        String output = strategy.handleError(rawLine, filteredLine, Collections.emptyList());

        // Assert
        assertThat(output).isNull();
    }
}
