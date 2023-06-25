package org.vatplanner.dataformats.vatsimpublic.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TimeHelpersTest {

    static Stream<Arguments> dataProviderIsLessOrEqualThan() {
        return Stream.of(
            Arguments.of(Duration.ofSeconds(1), Duration.ofSeconds(2), true),
            Arguments.of(Duration.ofSeconds(2), Duration.ofSeconds(2), true),
            Arguments.of(Duration.ofSeconds(3), Duration.ofSeconds(2), false),

            Arguments.of(Duration.ofSeconds(3), Duration.ofMinutes(2), true),
            Arguments.of(Duration.ofMinutes(3), Duration.ofMinutes(2), false)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderIsLessOrEqualThan")
    void testIsLessOrEqualThan_validInput_returnsExpectedResult(Duration a, Duration b, boolean expectedResult) {
        // Arrange (nothing to do)

        // Act
        boolean result = TimeHelpers.isLessOrEqualThan(a, b);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    static Stream<Arguments> dataProviderIsLessThan() {
        return Stream.of(
            Arguments.of(Duration.ofSeconds(1), Duration.ofSeconds(2), true),
            Arguments.of(Duration.ofSeconds(2), Duration.ofSeconds(2), false),
            Arguments.of(Duration.ofSeconds(3), Duration.ofSeconds(2), false),

            Arguments.of(Duration.ofSeconds(2), Duration.ofMinutes(2), true),
            Arguments.of(Duration.ofMinutes(2), Duration.ofMinutes(2), false),

            Arguments.of(Duration.ofSeconds(3), Duration.ofMinutes(2), true),
            Arguments.of(Duration.ofMinutes(3), Duration.ofMinutes(2), false)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderIsLessThan")
    void testIsLessThan_validInput_returnsExpectedResult(Duration a, Duration b, boolean expectedResult) {
        // Arrange (nothing to do)

        // Act
        boolean result = TimeHelpers.isLessThan(a, b);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }
}
