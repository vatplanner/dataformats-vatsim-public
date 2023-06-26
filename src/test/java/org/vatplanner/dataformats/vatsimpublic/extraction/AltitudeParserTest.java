package org.vatplanner.dataformats.vatsimpublic.extraction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AltitudeParserTest {

    static Stream<Arguments> dataProviderValidInput() {
        return Stream.of(
            // minimum threshold = 200ft
            Arguments.of("200ft", 200, true, 200),
            Arguments.of("1000", 1000, true, 1000),

            // different representations for 4000 ft
            Arguments.of("A040", 4000, true, 4000), // ICAO format
            Arguments.of("F040", 4000, true, 4000), // ICAO format
            Arguments.of("4000", 4000, true, 4000),
            Arguments.of("4000ft", 4000, true, 4000),
            Arguments.of("FL040", 4000, true, 4000),
            Arguments.of("4000F", 4000, true, 4000),
            Arguments.of("4000T", 4000, true, 4000),
            Arguments.of("040", 4000, true, 4000),
            Arguments.of("040F", 4000, true, 4000),
            Arguments.of("A4000", 4000, true, 4000),
            Arguments.of(" 4,000. ", 4000, true, 4000),
            Arguments.of("04000", 4000, true, 4000),

            // different representations for 25000 ft
            Arguments.of("A250", 25000, true, 25000), // ICAO format
            Arguments.of("F250", 25000, true, 25000), // ICAO format
            Arguments.of("25000", 25000, true, 25000),
            Arguments.of("25000ft", 25000, true, 25000),
            Arguments.of("FL250", 25000, true, 25000),
            Arguments.of("25000F", 25000, true, 25000),
            Arguments.of("25000T", 25000, true, 25000),
            Arguments.of("250", 25000, true, 25000),
            Arguments.of("250F", 25000, true, 25000),
            Arguments.of("A25000", 25000, true, 25000),
            Arguments.of(" 25,000. ", 25000, true, 25000),
            Arguments.of("025000", 25000, true, 25000),

            // different representations for 36500 ft
            Arguments.of("A365", 36500, true, 36500), // ICAO format
            Arguments.of("F365", 36500, true, 36500), // ICAO format
            Arguments.of("36500", 36500, true, 36500),
            Arguments.of("36500ft", 36500, true, 36500),
            Arguments.of("FL365", 36500, true, 36500),
            Arguments.of("36500F", 36500, true, 36500),
            Arguments.of("36500T", 36500, true, 36500),
            Arguments.of("365", 36500, true, 36500),
            Arguments.of("365F", 36500, true, 36500),
            Arguments.of("A36500", 36500, true, 36500),
            Arguments.of(" 36,500. ", 36500, true, 36500),
            Arguments.of("036500", 36500, true, 36500),

            // general clean up of input errors
            Arguments.of("<120", 12000, true, 12000),
            Arguments.of("<1200", 1200, true, 1200),
            Arguments.of("1234\\", 1234, true, 1234),
            Arguments.of("-100", 10000, true, 10000),
            Arguments.of("-1000", 1000, true, 1000),
            Arguments.of("5000,", 5000, true, 5000),
            Arguments.of(" - 050,0 0. ", 5000, true, 5000),
            Arguments.of("FL300-320", 30000, true, 30000),
            Arguments.of("300-320", 30000, true, 30000),
            Arguments.of(" 250", 25000, true, 25000),

            // metric altitudes
            Arguments.of("S0150", 1500, false, 4921), // ICAO format
            Arguments.of("M0150", 1500, false, 4921), // ICAO format
            Arguments.of("S0610", 6100, false, 20013), // ICAO format
            Arguments.of("M0610", 6100, false, 20013), // ICAO format
            Arguments.of("61m", 61, false, 200), // minimum threshold
            Arguments.of(" - 30.0, 0 M", 3000, false, 9843)
        );
    }

    static Stream<Arguments> dataProviderInvalidInput() {
        return Stream.of(
            // below threshold (200ft ~ 60.96m)
            "0",
            "199ft",
            "F001",
            "FL001",
            "A0001",
            "A0199",
            "60m", // metric
            "M0006", // metric
            "S0006", // metric

            // above threshold (70000ft ~ 21336m)
            "70001",
            "F701",
            "A70001",
            "A0199",
            "21337m",

            // general non-sense
            "2147483648", // exceeding unsigned Java integer
            "9223372036854775809", // exceeding unsigned Java long
            "",
            "nonsense",
            "F",
            "A",
            "M",
            "S"
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("dataProviderValidInput")
    void testGetValue_validInput_returnsExpectedOutput(String input, int expectedValue, boolean unusedIsUnitFeet, int unusedFeet) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        int result = parser.getValue();

        // Assert
        assertThat(result).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("dataProviderValidInput")
    void testIsUnitFeet_validInput_returnsExpectedOutput(String input, int unusedValue, boolean expectedIsUnitFeet, int unusedFeet) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        boolean result = parser.isUnitFeet();

        // Assert
        assertThat(result).isEqualTo(expectedIsUnitFeet);
    }

    @ParameterizedTest
    @MethodSource("dataProviderValidInput")
    void testGetFeet_validInput_returnsExpectedOutput(String input, int unusedValue, boolean unusedIsUnitFeet, int expectedFeet) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        int result = parser.getFeet();

        // Assert
        assertThat(result).isEqualTo(expectedFeet);
    }

    @ParameterizedTest
    @MethodSource("dataProviderInvalidInput")
    void testGetValue_invalidInput_returnsNegativeOutput(String input) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        int result = parser.getValue();

        // Assert
        assertThat(result).isNegative();
    }

    @ParameterizedTest
    @MethodSource("dataProviderInvalidInput")
    void testGetFeet_invalidInput_returnsNegativeOutput(String input) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        int result = parser.getFeet();

        // Assert
        assertThat(result).isNegative();
    }
}
