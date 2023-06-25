package org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FAADomesticTypeFormatExtractorTest {

    static Stream<Arguments> dataProviderFAATypeFormatStringsAndExpectedExtraction() {
        return Stream.of(
            // input, type, wake category, equipment code
            Arguments.of("B738/L", "B738", null, "L"),
            Arguments.of("C550/G", "C550", null, "G"),
            Arguments.of("H/A359/L", "A359", "H", "L"),
            Arguments.of("B78X", "B78X", null, null),
            Arguments.of("H/A333", "A333", "H", null),
            Arguments.of("Boeing 737-800", "Boeing 737-800", null, null),
            Arguments.of("Boeing 737-800/L", "Boeing 737-800", null, "L"),
            Arguments.of("M/Boeing 737-800/L", "Boeing 737-800", "M", "L"),
            Arguments.of("M/Boeing 737-800", "Boeing 737-800", "M", null)
        );
    }

    static Stream<Arguments> dataProviderInvalidInput() {
        return Stream.of(
            "a",
            "M/B738/L-S",
            "M/B738/L/S",
            "M-S/B738/L",
            "M/S/B738/L",
            "/",
            "//",
            "///"
        ).map(Arguments::of);
    }

    static Stream<Arguments> dataProviderEmptyInput() {
        return Stream.of(
            null,
            ""
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("dataProviderFAATypeFormatStringsAndExpectedExtraction")
    void testGetAircraftType_validInput_returnsExpectedType(String input, String expectedType, String wakeCategory, String equipmentCode) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getAircraftType();

        // Assert
        assertThat(result).isEqualTo(expectedType);
    }

    @ParameterizedTest
    @MethodSource("dataProviderInvalidInput")
    void testGetAircraftType_invalidInput_returnsInput(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getAircraftType();

        // Assert
        assertThat(result).isEqualTo(input);
    }

    @ParameterizedTest
    @MethodSource("dataProviderEmptyInput")
    void testGetAircraftType_emptyInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getAircraftType();

        // Assert
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderFAATypeFormatStringsAndExpectedExtraction")
    void testGetEquipmentCode_validInput_returnsExpectedEquipmentCode(String input, String type, String wakeCategory, String expectedEquipmentCode) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getEquipmentCode();

        // Assert
        assertThat(result).isEqualTo(expectedEquipmentCode);
    }

    @ParameterizedTest
    @MethodSource("dataProviderInvalidInput")
    void testGetEquipmentCode_invalidInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getEquipmentCode();

        // Assert
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderEmptyInput")
    void testGetEquipmentCode_emptyInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getEquipmentCode();

        // Assert
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderFAATypeFormatStringsAndExpectedExtraction")
    void testGetWakeCategory_validInput_returnsExpectedWakeCategory(String input, String type, String expectedWakeCategory, String equipmentCode) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getWakeCategory();

        // Assert
        assertThat(result).isEqualTo(expectedWakeCategory);
    }

    @ParameterizedTest
    @MethodSource("dataProviderInvalidInput")
    void testGetWakeCategory_invalidInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getWakeCategory();

        // Assert
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderEmptyInput")
    void testGetWakeCategory_emptyInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getWakeCategory();

        // Assert
        assertThat(result).isNull();
    }

    // TODO: test trimming
}
