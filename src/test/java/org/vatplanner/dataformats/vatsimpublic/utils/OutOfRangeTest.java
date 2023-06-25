package org.vatplanner.dataformats.vatsimpublic.utils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OutOfRangeTest {
    @ParameterizedTest
    @CsvSource({
        "-1.0,   0.0,   1.0",
        " 1.1,   0.0,   1.0",
        " 2.0,   0.0,   1.0",
        " 2.00,  2.01,  2.02",
        " 2.03,  2.01,  2.02",
        "-2.00, -2.02, -2.01",
        "-2.03, -2.02, -2.01",
    })
    void testThrowIfNotWithinIncluding_excessive_throwsOutOfRange(double actual, double expectedMin, double expectedMax) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> OutOfRange.throwIfNotWithinIncluding("something", actual, expectedMin, expectedMax);

        // Assert
        assertThatThrownBy(action).isInstanceOf(OutOfRange.class);
    }

    @ParameterizedTest
    @CsvSource({
        " 0.0,    0.0,   1.0",
        " 0.5,    0.0,   1.0",
        " 1.0,    0.0,   1.0",
        " 2.01,   2.01,  2.02",
        "-2.015, -2.02, -2.01",
    })
    void testThrowIfNotWithinIncluding_valid_doesNotThrowAnyException(double actual, double expectedMin, double expectedMax) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> OutOfRange.throwIfNotWithinIncluding("something", actual, expectedMin, expectedMax);

        // Assert
        assertThatCode(action).doesNotThrowAnyException();
    }
}