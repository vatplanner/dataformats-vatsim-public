package org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ICAOTypeFormatExtractorTest {

    static Stream<Arguments> dataProviderICAOTypeFormatStringsAndExpectedExtraction() {
        return Stream.of(
            // input, type, wake category, equipment code
            Arguments.of("B763/H-L/LB1", "B763", "H", "L/LB1"),
            Arguments.of("A319/M-SDE3FGHIRWY/LB1", "A319", "M", "SDE3FGHIRWY/LB1"),
            Arguments.of("CONC/H-SDE2E3FGHIRYW/X", "CONC", "H", "SDE2E3FGHIRYW/X"),
            Arguments.of("B77W/H-SDE1E2E3GHIJ4J5M1P2RWXYZ/LB1D1", "B77W", "H", "SDE1E2E3GHIJ4J5M1P2RWXYZ/LB1D1"),
            Arguments.of("B732/M-SDFGHRWY/C", "B732", "M", "SDFGHRWY/C"),
            Arguments.of("B737/M-S", "B737", "M", "S"), /* this actually does not seem to be a real-world valid input but it has
                                                           been seen on VATSIM */
            Arguments.of("DH8D/M-SDE2E3FGRY/H", "DH8D", "M", "SDE2E3FGRY/H"),
            Arguments.of("B78X/H-SADE1E2E3FGHIJ1J3J4J5J6LM1M2OP2RWXYZ/LB1D1G1", "B78X", "H",
                         "SADE1E2E3FGHIJ1J3J4J5J6LM1M2OP2RWXYZ/LB1D1G1"
            ),
            Arguments.of("Boeing 737-800/M-L", "Boeing 737-800", "M", "L") /* actually not seen yet but free-text types instead
                                                                              of ICAO codes have been seen on FAA-based format,
                                                                              so expect it here as well */
        );
    }

    @ParameterizedTest
    @MethodSource("org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype.FAADomesticTypeFormatExtractorTest#dataProviderFAATypeFormatStringsAndExpectedExtraction")
    void testConstructor_faaValidInput_throwsIllegalArgumentException(String input, String type, String wakeCategory, String equipmentCode) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new ICAOTypeFormatExtractor(input);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype.FAADomesticTypeFormatExtractorTest#dataProviderInvalidInput")
    void testConstructor_faaInvalidInput_throwsIllegalArgumentException(String input) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new ICAOTypeFormatExtractor(input);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype.FAADomesticTypeFormatExtractorTest#dataProviderEmptyInput")
    void testConstructor_faaEmptyInput_throwsIllegalArgumentException(String input) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new ICAOTypeFormatExtractor(input);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("dataProviderICAOTypeFormatStringsAndExpectedExtraction")
    void testGetAircraftType_validInput_returnsExpectedType(String input, String expectedType, String wakeCategory, String equipmentCode) {
        // Arrange
        ICAOTypeFormatExtractor extractor = new ICAOTypeFormatExtractor(input);

        // Act
        String result = extractor.getAircraftType();

        // Assert
        assertThat(result).isEqualTo(expectedType);
    }

    @ParameterizedTest
    @MethodSource("dataProviderICAOTypeFormatStringsAndExpectedExtraction")
    void testGetEquipmentCode_validInput_returnsExpectedEquipmentCode(String input, String type, String wakeCategory, String expectedEquipmentCode) {
        // Arrange
        ICAOTypeFormatExtractor extractor = new ICAOTypeFormatExtractor(input);

        // Act
        String result = extractor.getEquipmentCode();

        // Assert
        assertThat(result).isEqualTo(expectedEquipmentCode);
    }

    @ParameterizedTest
    @MethodSource("dataProviderICAOTypeFormatStringsAndExpectedExtraction")
    void testGetWakeCategory_validInput_returnsExpectedWakeCategory(String input, String type, String expectedWakeCategory, String equipmentCode) {
        // Arrange
        ICAOTypeFormatExtractor extractor = new ICAOTypeFormatExtractor(input);

        // Act
        String result = extractor.getWakeCategory();

        // Assert
        assertThat(result).isEqualTo(expectedWakeCategory);
    }

    // TODO: test trimming
}
