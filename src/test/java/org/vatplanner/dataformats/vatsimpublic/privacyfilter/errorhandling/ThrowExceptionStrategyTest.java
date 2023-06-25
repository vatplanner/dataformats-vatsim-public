package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling.ErrorHandlingStrategy.FailWithException;

class ThrowExceptionStrategyTest {

    @ParameterizedTest
    @CsvSource({
        "unwanted, ''",
        "abc:def, de:abc:f",
        "original, Z",
    })
    void testHandleError_always_throwsFailWithException(String rawLine, String filteredLine) {
        // Arrange
        ThrowExceptionStrategy strategy = new ThrowExceptionStrategy();

        // Act
        ThrowingCallable action = () -> strategy.handleError(rawLine, filteredLine, Collections.emptyList());

        // Assert
        assertThatThrownBy(action).isInstanceOf(FailWithException.class);
    }
}
