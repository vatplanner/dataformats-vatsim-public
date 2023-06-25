package org.vatplanner.dataformats.vatsimpublic.extraction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RealNameHomeBaseExtractorTest {

    static Stream<Arguments> dataProviderValidInputWithExpectedRealNameAndHomeBase() {
        return Stream.of(
            Arguments.of("just a name", "just a name", null),
            Arguments.of("with a base EDDT", "with a base", "EDDT"),
            Arguments.of("J. Doe", "J. Doe", null),
            Arguments.of("me R2D2", "me R2D2", null),
            Arguments.of("ALL CAPS KJFK", "ALL CAPS", "KJFK"),
            Arguments.of(
                "  whitespace  only stripped   before and after  LIRF  ",
                "whitespace  only stripped   before and after",
                "LIRF"
            ),
            Arguments.of("", null, null),
            Arguments.of("123456", null, null),
            Arguments.of("123456 ESSA", null, "ESSA")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderValidInputWithExpectedRealNameAndHomeBase")
    void testGetRealName_validInput_returnsExpectedValue(String input, String expectedRealName, String unusedHomeBase) {
        // Arrange
        RealNameHomeBaseExtractor extractor = new RealNameHomeBaseExtractor(input);

        // Act
        String result = extractor.getRealName();

        // Assert
        assertThat(result).isEqualTo(expectedRealName);
    }

    @ParameterizedTest
    @MethodSource("dataProviderValidInputWithExpectedRealNameAndHomeBase")
    void testGetHomeBase_validInput_returnsExpectedValue(String input, String unusedRealName, String expectedHomeBase) {
        // Arrange
        RealNameHomeBaseExtractor extractor = new RealNameHomeBaseExtractor(input);

        // Act
        String result = extractor.getHomeBase();

        // Assert
        assertThat(result).isEqualTo(expectedHomeBase);
    }
}
