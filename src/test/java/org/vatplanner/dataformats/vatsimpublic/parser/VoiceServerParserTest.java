package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

@RunWith(DataProviderRunner.class)
public class VoiceServerParserTest {

    private VoiceServerParser parser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        parser = new VoiceServerParser();
    }

    @Test
    @DataProvider({ "", "hostname:location:name:1:Abc123", "hostname:location:name:1:Abc123:1:" })
    public void testParse_genericFormatViolation_throwsIllegalArgumentException(String erroneousLine) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(erroneousLine);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({ "hostname", "some.other.host.name", "123.45.67.89" })
    public void testParse_validHostnameOrIp_returnsObjectWithExpectedAddress(String expectedAddress) {
        // Arrange
        String line = String.format("%s:location:name:1:Abc123:", expectedAddress);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result.getAddress(), is(equalTo(expectedAddress)));
    }

    @Test
    public void testParse_withoutHostnameOrIp_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":location:name:1:Abc123:";
        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(erroneousLine);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({ "Location", "My Location", "My Location, Somewhere" })
    public void testParse_validLocation_returnsObjectWithExpectedLocation(String expectedLocation) {
        // Arrange
        String line = String.format("hostname:%s:name:1:Abc123:", expectedLocation);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result.getLocation(), is(equalTo(expectedLocation)));
    }

    @Test
    public void testParse_withoutLocation_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "hostname::name:1:Abc123:";
        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(erroneousLine);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({ "Simple Name", "special chars -.,%$!\" and numbers 0123456789 are ok too" })
    public void testParse_validName_returnsObjectWithExpectedName(String expectedName) {
        // Arrange
        String line = String.format("hostname:location:%s:1:Abc123:", expectedName);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result.getName(), is(equalTo(expectedName)));
    }

    @Test
    public void testParse_withoutName_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "hostname:location::1:Abc123:";
        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(erroneousLine);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({ "0, false", "1, true" })
    public void testParse_validFlagForClientsConnectionAllowed_returnsObjectWithExpectedConnectionFlag(String inputFlag, boolean expectedOutputFlag) {
        // Arrange
        String line = String.format("hostname:location:name:%s:Abc123:", inputFlag);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result.isClientConnectionAllowed(), is(equalTo(expectedOutputFlag)));
    }

    @Test
    @DataProvider({ "", "-1", "2", "a", "10", "01" })
    public void testParse_invalidFlagsForClientsConnectionAllowed_throwsIllegalArgumentException(String invalidFlag) {
        // Arrange
        String erroneousLine = String.format("hostname:location:name:%s:Abc123:", invalidFlag);
        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(erroneousLine);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({ "", "A", "1", "abc123", "-.,#!\"§$%&" })
    public void testParse_withTypeOfVoiceServer_returnsObjectWithExpectedRawType(String rawType) {
        // Arrange
        String line = String.format("hostname:location:name:1:%s:", rawType);

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result.getRawServerType(), is(equalTo(rawType)));
    }

    @Test
    public void testParse_withoutTypeOfVoiceServer_returnsObjectWithNullForRawType() {
        // Arrange
        String line = "hostname:location:name:1:";

        // Act
        VoiceServer result = parser.parse(line);

        // Assert
        assertThat(result.getRawServerType(), is(nullValue()));
    }

}
