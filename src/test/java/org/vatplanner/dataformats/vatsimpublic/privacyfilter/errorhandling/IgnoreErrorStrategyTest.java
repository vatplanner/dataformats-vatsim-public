package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IgnoreErrorStrategyTest {

    @ParameterizedTest
    @CsvSource({
        "unwanted, ''",
        "abc:def, de:abc:f",
        "original, Z",
    })
    void testHandleError_always_returnsFilteredLine(String rawLine, String expectedOutput) {
        // Arrange
        IgnoreErrorStrategy strategy = new IgnoreErrorStrategy();

        // Act
        String output = strategy.handleError(rawLine, expectedOutput, Collections.emptyList());

        // Assert
        assertThat(output).isEqualTo(expectedOutput);
    }
}
