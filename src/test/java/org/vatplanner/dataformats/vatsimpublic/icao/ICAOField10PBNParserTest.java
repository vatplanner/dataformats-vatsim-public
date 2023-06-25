package org.vatplanner.dataformats.vatsimpublic.icao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ICAOField10PBNParserTest {

    @Test
    void testGetCommunicationCapabilities_null_returnsEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser(null);

        // Act
        Set<CommunicationCapability> result = parser.getCommunicationCapabilities();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetNavigationApproachCapabilities_null_returnsEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser(null);

        // Act
        Set<NavigationApproachCapability> result = parser.getNavigationApproachCapabilities();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testHasTransponder_null_returnsTrue() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser(null);

        // Act
        boolean result = parser.hasTransponder();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testGetCommunicationCapabilities_empty_returnsEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("");

        // Act
        Set<CommunicationCapability> result = parser.getCommunicationCapabilities();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetNavigationApproachCapabilities_empty_returnsEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("");

        // Act
        Set<NavigationApproachCapability> result = parser.getNavigationApproachCapabilities();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testHasTransponder_empty_returnsTrue() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("");

        // Act
        boolean result = parser.hasTransponder();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testGetCommunicationCapabilities_empty10a_returnsEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("/S");

        // Act
        Set<CommunicationCapability> result = parser.getCommunicationCapabilities();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetNavigationApproachCapabilities_empty10a_returnsEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("/S");

        // Act
        Set<NavigationApproachCapability> result = parser.getNavigationApproachCapabilities();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testHasTransponder_empty10a_returnsTrue() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("/S");

        // Act
        boolean result = parser.hasTransponder();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testGetCommunicationCapabilities_missing10b_returnsNonEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("S");

        // Act
        Set<CommunicationCapability> result = parser.getCommunicationCapabilities();

        // Assert
        assertThat(result).isNotEmpty();
    }

    @Test
    void testGetNavigationApproachCapabilities_missing10b_returnsNonEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("S");

        // Act
        Set<NavigationApproachCapability> result = parser.getNavigationApproachCapabilities();

        // Assert
        assertThat(result).isNotEmpty();
    }

    @Test
    void testHasTransponder_missing10b_returnsTrue() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("S");

        // Act
        boolean result = parser.hasTransponder();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testGetCommunicationCapabilities_empty10b_returnsNonEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("S/");

        // Act
        Set<CommunicationCapability> result = parser.getCommunicationCapabilities();

        // Assert
        assertThat(result).isNotEmpty();
    }

    @Test
    void testGetNavigationApproachCapabilities_empty10b_returnsNonEmpty() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("S/");

        // Act
        Set<NavigationApproachCapability> result = parser.getNavigationApproachCapabilities();

        // Assert
        assertThat(result).isNotEmpty();
    }

    @Test
    void testHasTransponder_empty10b_returnsTrue() {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser("S/");

        // Act
        boolean result = parser.hasTransponder();

        // Assert
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "/N, false", // N = nil / no capability for surveillance
        "/n, false", // lower-case should work too
        "/S, true", // S = Mode S

        // seen on VATSIM data files
        "/C, true",
        "/H, true",
        "/LB1D1, true",
        "/X, true", //

        // ambiguous information should assume transponder supported
        "/NC, true", //
        "/CN, true", //
    })
    void testHasTransponder_validData_returnsExpectedResult(String combinedField10, boolean expectedResult) {
        // Arrange
        ICAOField10PBNParser parser = new ICAOField10PBNParser(combinedField10);

        // Act
        boolean result = parser.hasTransponder();

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    /**
     * {"B763/H-L/LB1", "B763", "H", "L/LB1"}, {"A319/M-SDE3FGHIRWY/LB1", "A319",
     * "M", "SDE3FGHIRWY/LB1"}, {"CONC/H-SDE2E3FGHIRYW/X", "CONC", "H",
     * "SDE2E3FGHIRYW/X"}, {"B77W/H-SDE1E2E3GHIJ4J5M1P2RWXYZ/LB1D1", "B77W", "H",
     * "SDE1E2E3GHIJ4J5M1P2RWXYZ/LB1D1"}, {"B732/M-SDFGHRWY/C", "B732", "M",
     * "SDFGHRWY/C"}, {"B737/M-S", "B737", "M", "S"}, // this actually does not seem
     * to be a real-world valid input but it has been seen on VATSIM
     * {"DH8D/M-SDE2E3FGRY/H", "DH8D", "M", "SDE2E3FGRY/H"},
     * {"B78X/H-SADE1E2E3FGHIJ1J3J4J5J6LM1M2OP2RWXYZ/LB1D1G1", "B78X", "H",
     * "SADE1E2E3FGHIJ1J3J4J5J6LM1M2OP2RWXYZ/LB1D1G1"}, // {"Boeing 737-800/M-L",
     * "Boeing 737-800", "M", "L"}, // actually not seen yet but free-text types
     * instead of ICAO codes have been seen on FAA-based format, so expect it here
     * as well
     */
    // TODO: check lower-case in field 10a
}
