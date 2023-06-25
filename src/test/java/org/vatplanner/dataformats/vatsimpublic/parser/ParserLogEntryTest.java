package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ParserLogEntryTest {

    static Stream<Arguments> dataProviderThrowablesAndExpectedClassNames() {
        return Stream.of(
            Arguments.of(new Throwable(), "java.lang.Throwable"),
            Arguments.of(new IllegalArgumentException(), "java.lang.IllegalArgumentException")
        );
    }

    @Test
    void testConstructor_nullMessage_throwsIllegalArgumentException() {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new ParserLogEntry("abc", "xyz", false, null, null);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc"})
    void testConstructor_nonNullMessage_doesNotFail(String message) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new ParserLogEntry("abc", "xyz", false, message, null);

        // Assert
        assertThatCode(action).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SOME_SECTION", "somethingElse"})
    void testToString_nonNullSection_listsSection(String expectedSection) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry(expectedSection, "xyz", false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains("section: " + expectedSection);
    }

    @Test
    void testToString_nullSection_listsNullAsSection() {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry(null, "xyz", false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains("section: null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"This is some line content.", "1:2:3:4:some:more content"})
    void testToString_nonNullLineContent_listsLineContent(String expectedLineContent) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", expectedLineContent, false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains("line: \"" + expectedLineContent + "\"");
    }

    @Test
    void testToString_nullLineContent_listsNullAsLineContent() {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", null, false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains("line: null");
    }

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "false,false"
    })
    void testToString_anyLineContent_listsLineRejection(boolean isLineRejected, String expectedRejectionOutput) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", isLineRejected, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains("rejected: " + expectedRejectionOutput);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Expected message #1", "This is another message to be expected."})
    void testToString_anyMessage_containsMessage(String expectedMessage) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", false, expectedMessage, null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains(expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("dataProviderThrowablesAndExpectedClassNames")
    void testToString_nonNullThrowable_containsThrowableClassName(Throwable throwable, String expectedClassName) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", false, "some message", throwable);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains(expectedClassName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Message of Throwable", "Another exception detail."})
    void testToString_nonNullThrowable_containsThrowableMessage(String expectedThrowableMessage) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", false, "some message",
                                                  new Throwable(expectedThrowableMessage)
        );

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains(expectedThrowableMessage);
    }

    @Test
    void testToString_nullThrowable_listsNoneAsException() {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result).contains("exception: none");
    }
}
