package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vatplanner.dataformats.vatsimpublic.parser.VoiceServer;

class VoiceServerParserTest {

    private VoiceServerParser parser;

    @BeforeEach
    public void setUp() {
        parser = new VoiceServerParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "hostname:location:name:1:Abc123", "hostname:location:name:1:Abc123:1:"})
    void testParse_genericFormatViolation_throwsIllegalArgumentException(String erroneousLine) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hostname", "some.other.host.name", "123.45.67.89"})
    void testParse_validHostnameOrIp_returnsObjectWithExpectedAddress(String expectedAddress) {
        // Arrange
        String line = String.format("%s:location:name:1:Abc123:", expectedAddress);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(VoiceServer::getAddress)
                          .isEqualTo(expectedAddress);
    }

    @Test
    void testParse_withoutHostnameOrIp_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":location:name:1:Abc123:";

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Location", "My Location", "My Location, Somewhere"})
    void testParse_validLocation_returnsObjectWithExpectedLocation(String expectedLocation) {
        // Arrange
        String line = String.format("hostname:%s:name:1:Abc123:", expectedLocation);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(VoiceServer::getLocation)
                          .isEqualTo(expectedLocation);
    }

    @Test
    void testParse_withoutLocation_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "hostname::name:1:Abc123:";

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Simple Name", "special chars -.,%$!\" and numbers 0123456789 are ok too"})
    void testParse_validName_returnsObjectWithExpectedName(String expectedName) {
        // Arrange
        String line = String.format("hostname:location:%s:1:Abc123:", expectedName);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(VoiceServer::getName)
                          .isEqualTo(expectedName);
    }

    @Test
    void testParse_withoutName_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "hostname:location::1:Abc123:";

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "0, false",
        "1, true"
    })
    void testParse_validFlagForClientsConnectionAllowed_returnsObjectWithExpectedConnectionFlag(String inputFlag, boolean expectedOutputFlag) {
        // Arrange
        String line = String.format("hostname:location:name:%s:Abc123:", inputFlag);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(VoiceServer::isClientConnectionAllowed)
                          .isEqualTo(expectedOutputFlag);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "-1", "2", "a", "10", "01"})
    void testParse_invalidFlagsForClientsConnectionAllowed_throwsIllegalArgumentException(String invalidFlag) {
        // Arrange
        String erroneousLine = String.format("hostname:location:name:%s:Abc123:", invalidFlag);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "A", "1", "abc123", "-.,#!\"§$%&"})
    void testParse_withTypeOfVoiceServer_returnsObjectWithExpectedRawType(String rawType) {
        // Arrange
        String line = String.format("hostname:location:name:1:%s:", rawType);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(VoiceServer::getRawServerType)
                          .isEqualTo(rawType);
    }

    @Test
    void testParse_withoutTypeOfVoiceServer_returnsObjectWithNullForRawType() {
        // Arrange
        String line = "hostname:location:name:1:";

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(VoiceServer::getRawServerType)
                          .isNull();
    }
}
