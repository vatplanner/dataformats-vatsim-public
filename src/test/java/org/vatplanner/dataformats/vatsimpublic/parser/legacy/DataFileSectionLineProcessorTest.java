package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DataFileSectionLineProcessorTest {

    static Stream<Arguments> dataProviderStrings() {
        return Stream.of(
            "",
            "just a one-liner",
            "just a one-liner terminated\n",
            "\n\r\n\r\r\n\r",
            ";some comment\r\n!GENERAL:\r\nABC = 1\r\nSECOND = 123\r\n!SOMETHINGELSE:\r\n!GENERAL:\r\nABC = 0\r\n"
        ).map(Arguments::of);
    }

    static Stream<Arguments> dataProviderApplicationToSection() {
        UnaryOperator<String> function1 = s -> "prefix " + s + " postfix";
        UnaryOperator<String> function2 = String::toLowerCase;

        return Stream.of(
            Arguments.of("", "SECTION", function1, ""),
            Arguments.of("\n", "SECTION", function1, "\n"),
            Arguments.of("A", "SECTION", function1, "A"),
            Arguments.of("!SECTION:\nA\n\n", "SECTION", function1, "!SECTION:\nprefix A postfix\n\n"),
            Arguments.of("!SECTION:\nA\n\n", "sEcTIOn", function1, "!SECTION:\nprefix A postfix\n\n"),
            Arguments.of(
                "!SECTION:\n;comments should not be processed\nA\n; trailing\n",
                "SECTION",
                function1,
                "!SECTION:\n;comments should not be processed\nprefix A postfix\n; trailing\n"
            ),
            Arguments.of(
                "!SECTION:\nA\nB\n\rC",
                "SECTION",
                function1,
                "!SECTION:\nprefix A postfix\nprefix B postfix\n\rprefix C postfix"
            ),
            Arguments.of(
                "!SECTION:\nA\n!ANOTHERSECTION:\nB\n!SECTION:\nC",
                "SECTION",
                function1,
                "!SECTION:\nprefix A postfix\n!ANOTHERSECTION:\nB\n!SECTION:\nprefix C postfix"
            ),
            Arguments.of(
                "!SECTION:\nA\n!ANOTHERSECTION:\nB\n!SECTION:\nC",
                "ANOTHERSECTION",
                function1,
                "!SECTION:\nA\n!ANOTHERSECTION:\nprefix B postfix\n!SECTION:\nC"
            ),
            Arguments.of(
                "!SECTION:\r\nA\r\n\nB\nC",
                "SECTION",
                function2,
                "!SECTION:\r\na\r\n\nb\nc"
            ),
            Arguments.of(
                ";WHATEVER\n!SECTION:\nSOMECONTENT = 1",
                "SECTION",
                function2,
                ";WHATEVER\n!SECTION:\nsomecontent = 1"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderStrings")
    void testGetResultAsString_notApplied_returnsInput(String input) {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor(input);

        // Act
        String result = processor.getResultAsString();

        // Assert
        assertThat(result).isEqualTo(input);
    }

    @ParameterizedTest
    @MethodSource("dataProviderApplicationToSection")
    void testGetResultAsString_applied_returnsAlteredOutput(String input, String sectionName, UnaryOperator<String> function, String expectedResult) {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor(input);
        processor.apply(sectionName, function);

        // Act
        String result = processor.getResultAsString();

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetResultAsString_appliedTwice_returnsResultCombinedInCorrectOrder() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("!TEST:\nA\nB");
        processor.apply("TEST", s -> s.toLowerCase());
        processor.apply("TEST", s -> s + s.toUpperCase());

        // Act
        String result = processor.getResultAsString();

        // Assert
        assertThat(result).isEqualTo("!TEST:\naA\nbB");
    }

    @Test
    void testApply_nullFunction_throwsIllegalArgumentException() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("");

        // Act
        ThrowableAssert.ThrowingCallable action = () -> processor.apply("A", null);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testApply_nullSectionName_throwsIllegalArgumentException() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("");

        // Act
        ThrowableAssert.ThrowingCallable action = () -> processor.apply(null, UnaryOperator.identity());

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testApply_emptySectionName_throwsIllegalArgumentException() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("");

        // Act
        ThrowableAssert.ThrowingCallable action = () -> processor.apply("", UnaryOperator.identity());

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testApply_valid_returnsSameInstance() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("");

        // Act
        DataFileSectionLineProcessor returned = processor.apply("a", UnaryOperator.identity());

        // Assert
        assertThat(returned).isSameAs(processor);
    }
}
