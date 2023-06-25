package org.vatplanner.dataformats.vatsimpublic.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OptionalsTest {
    @Test
    void testAllPresent_withoutArguments_returnsFalse() {
        // Arrange (nothing to do)

        // Act
        boolean result = Optionals.allPresent();

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "empty,   present, present",
            "present, empty,   present",
            "empty,   empty,   present",
            "present, present, empty",
            "empty,   present, empty",
            "present, empty,   empty",
            "empty,   empty,   empty",
        },
        nullValues = {"empty"}
    )
    void testAllPresent_atLeastOneEmpty_returnsFalse(String a, String b, String c) {
        // Arrange
        Optional<String> optionalA = Optional.ofNullable(a);
        Optional<String> optionalB = Optional.ofNullable(b);
        Optional<String> optionalC = Optional.ofNullable(c);

        // Act
        boolean result = Optionals.allPresent(optionalA, optionalB, optionalC);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testAllPresent_allPresent_returnsTrue() {
        // Arrange
        Optional<String> optionalA = Optional.of("a");
        Optional<String> optionalB = Optional.of("b");
        Optional<String> optionalC = Optional.of("c");

        // Act
        boolean result = Optionals.allPresent(optionalA, optionalB, optionalC);

        // Assert
        assertThat(result).isTrue();
    }
}