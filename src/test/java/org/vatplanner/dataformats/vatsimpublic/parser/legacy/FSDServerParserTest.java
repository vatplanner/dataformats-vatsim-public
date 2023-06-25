package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vatplanner.dataformats.vatsimpublic.parser.FSDServer;

class FSDServerParserTest {

    private FSDServerParser parser;

    @BeforeEach
    void setUp() {
        parser = new FSDServerParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ident:hostname:location:name:1", "ident:hostname:location:name:1:1:"})
    void testParse_genericFormatViolation_throwsIllegalArgumentException(String erroneousLine) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id1", "another ID"})
    void testParse_validIdent_returnsObjectWithExpectedId(String expectedId) {
        // Arrange
        String line = String.format("%s:hostname:location:name:1:", expectedId);

        // Act
        FSDServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(FSDServer::getId)
                          .isEqualTo(expectedId);
    }

    @Test
    void testParse_withoutIdent_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":hostname:location:name:1:";

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hostname", "some.other.host.name", "123.45.67.89"})
    void testParse_validHostnameOrIp_returnsObjectWithExpectedAddress(String expectedAddress) {
        // Arrange
        String line = String.format("someId:%s:location:name:1:", expectedAddress);

        // Act
        FSDServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(FSDServer::getAddress)
                          .isEqualTo(expectedAddress);
    }

    @Test
    void testParse_withoutHostnameOrIp_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "someId::location:name:1:";

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Location", "My Location", "My Location, Somewhere"})
    void testParse_validLocation_returnsObjectWithExpectedLocation(String expectedLocation) {
        // Arrange
        String line = String.format("someId:hostname:%s:name:1:", expectedLocation);

        // Act
        FSDServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(FSDServer::getLocation)
                          .isEqualTo(expectedLocation);
    }

    @Test
    void testParse_withoutLocation_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "someId:hostname::name:1:";

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Simple Name", "special chars -.,%$!\" and numbers 0123456789 are ok too"})
    void testParse_validName_returnsObjectWithExpectedName(String expectedName) {
        // Arrange
        String line = String.format("someId:hostname:location:%s:1:", expectedName);

        // Act
        FSDServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(FSDServer::getName)
                          .isEqualTo(expectedName);
    }

    @Test
    void testParse_withoutName_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "someId:hostname:location::1:";

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
        String line = String.format("someId:hostname:location:name:%s:", inputFlag);

        // Act
        FSDServer result = parser.parse(line);

        // Assert
        assertThat(result).extracting(FSDServer::isClientConnectionAllowed)
                          .isEqualTo(expectedOutputFlag);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "-1", "2", "a", "10", "01"})
    void testParse_invalidFlagsForClientsConnectionAllowed_throwsIllegalArgumentException(String invalidFlag) {
        // Arrange
        String erroneousLine = String.format("someId:hostname:location:name:%s:", invalidFlag);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
}
