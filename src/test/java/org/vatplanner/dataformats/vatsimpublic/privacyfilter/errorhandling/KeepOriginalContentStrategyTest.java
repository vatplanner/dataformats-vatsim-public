package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KeepOriginalContentStrategyTest {

    @ParameterizedTest
    @CsvSource({
        "wanted, ''",
        "abc:def, de:abc:f",
        "original, Z",
    })
    void testHandleError_always_returnsRawLine(String expectedOutput, String filteredLine) {
        // Arrange
        KeepOriginalContentStrategy strategy = new KeepOriginalContentStrategy();

        // Act
        String output = strategy.handleError(expectedOutput, filteredLine, Collections.emptyList());

        // Assert
        assertThat(output).isEqualTo(expectedOutput);
    }
}
