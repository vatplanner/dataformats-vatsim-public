package org.vatplanner.dataformats.vatsimpublic.extraction;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class AltitudeParserTest {

    @DataProvider
    public static Object[][] dataProviderValidInput() {
        return new Object[][]{
            // minimum threshold = 200ft
            new Object[]{"200ft", 200, true, 200}, //
            new Object[]{"1000", 1000, true, 1000}, //

            // different representations for 4000 ft
            new Object[]{"A040", 4000, true, 4000}, // ICAO format
            new Object[]{"F040", 4000, true, 4000}, // ICAO format
            new Object[]{"4000", 4000, true, 4000}, //
            new Object[]{"4000ft", 4000, true, 4000}, //
            new Object[]{"FL040", 4000, true, 4000}, //
            new Object[]{"4000F", 4000, true, 4000}, //
            new Object[]{"4000T", 4000, true, 4000}, //
            new Object[]{"040", 4000, true, 4000}, //
            new Object[]{"040F", 4000, true, 4000}, //
            new Object[]{"A4000", 4000, true, 4000}, //
            new Object[]{" 4,000. ", 4000, true, 4000}, //
            new Object[]{"04000", 4000, true, 4000}, //

            // different representations for 25000 ft
            new Object[]{"A250", 25000, true, 25000}, // ICAO format
            new Object[]{"F250", 25000, true, 25000}, // ICAO format
            new Object[]{"25000", 25000, true, 25000}, //
            new Object[]{"25000ft", 25000, true, 25000}, //
            new Object[]{"FL250", 25000, true, 25000}, //
            new Object[]{"25000F", 25000, true, 25000}, //
            new Object[]{"25000T", 25000, true, 25000}, //
            new Object[]{"250", 25000, true, 25000}, //
            new Object[]{"250F", 25000, true, 25000}, //
            new Object[]{"A25000", 25000, true, 25000}, //
            new Object[]{" 25,000. ", 25000, true, 25000}, //
            new Object[]{"025000", 25000, true, 25000}, //

            // different representations for 36500 ft
            new Object[]{"A365", 36500, true, 36500}, // ICAO format
            new Object[]{"F365", 36500, true, 36500}, // ICAO format
            new Object[]{"36500", 36500, true, 36500}, //
            new Object[]{"36500ft", 36500, true, 36500}, //
            new Object[]{"FL365", 36500, true, 36500}, //
            new Object[]{"36500F", 36500, true, 36500}, //
            new Object[]{"36500T", 36500, true, 36500}, //
            new Object[]{"365", 36500, true, 36500}, //
            new Object[]{"365F", 36500, true, 36500}, //
            new Object[]{"A36500", 36500, true, 36500}, //
            new Object[]{" 36,500. ", 36500, true, 36500}, //
            new Object[]{"036500", 36500, true, 36500}, //

            // general clean up of input errors
            new Object[]{"<120", 12000, true, 12000}, //
            new Object[]{"<1200", 1200, true, 1200}, //
            new Object[]{"1234\\", 1234, true, 1234}, //
            new Object[]{"-100", 10000, true, 10000}, //
            new Object[]{"-1000", 1000, true, 1000}, //
            new Object[]{"5000,", 5000, true, 5000}, //
            new Object[]{" - 050,0 0. ", 5000, true, 5000}, //
            new Object[]{"FL300-320", 30000, true, 30000}, //
            new Object[]{"300-320", 30000, true, 30000}, //
            new Object[]{" 250", 25000, true, 25000}, //

            // metric altitudes
            new Object[]{"S0150", 1500, false, 4921}, // ICAO format
            new Object[]{"M0150", 1500, false, 4921}, // ICAO format
            new Object[]{"S0610", 6100, false, 20013}, // ICAO format
            new Object[]{"M0610", 6100, false, 20013}, // ICAO format
            new Object[]{"61m", 61, false, 200}, // minimum threshold
            new Object[]{" - 30.0, 0 M", 3000, false, 9843}, //
        };
    }

    @DataProvider
    public static Object[][] dataProviderInvalidInput() {
        return new Object[][]{
            // below threshold (200ft ~ 60.96m)
            new Object[]{"0"}, //
            new Object[]{"199ft"}, //
            new Object[]{"F001"}, //
            new Object[]{"FL001"}, //
            new Object[]{"A0001"}, //
            new Object[]{"A0199"}, //
            new Object[]{"60m"}, // metric
            new Object[]{"M0006"}, // metric
            new Object[]{"S0006"}, // metric

            // above threshold (70000ft ~ 21336m)
            new Object[]{"70001"}, //
            new Object[]{"F701"}, //
            new Object[]{"A70001"}, //
            new Object[]{"A0199"}, //
            new Object[]{"21337m"}, //

            // general non-sense
            new Object[]{"2147483648"}, // exceeding unsigned Java integer
            new Object[]{"9223372036854775809"}, // exceeding unsigned Java long
            new Object[]{""}, //
            new Object[]{"nonsense"}, //
            new Object[]{"F"}, //
            new Object[]{"A"}, //
            new Object[]{"M"}, //
            new Object[]{"S"}, //
        };
    }

    @Test
    @UseDataProvider("dataProviderValidInput")
    public void testGetValue_validInput_returnsExpectedOutput(String input, int expectedValue, boolean unusedIsUnitFeet, int unusedFeet) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        int result = parser.getValue();

        // Assert
        assertThat(result, is(equalTo(expectedValue)));
    }

    @Test
    @UseDataProvider("dataProviderValidInput")
    public void testIsUnitFeet_validInput_returnsExpectedOutput(String input, int unusedValue, boolean expectedIsUnitFeet, int unusedFeet) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        boolean result = parser.isUnitFeet();

        // Assert
        assertThat(result, is(expectedIsUnitFeet));
    }

    @Test
    @UseDataProvider("dataProviderValidInput")
    public void testGetFeet_validInput_returnsExpectedOutput(String input, int unusedValue, boolean unusedIsUnitFeet, int expectedFeet) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        int result = parser.getFeet();

        // Assert
        assertThat(result, is(equalTo(expectedFeet)));
    }

    @Test
    @UseDataProvider("dataProviderInvalidInput")
    public void testGetValue_invalidInput_returnsNegativeOutput(String input) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        int result = parser.getValue();

        // Assert
        assertThat(result, is(lessThan(0)));
    }

    @Test
    @UseDataProvider("dataProviderInvalidInput")
    public void testGetFeet_invalidInput_returnsNegativeOutput(String input) {
        // Arrange
        AltitudeParser parser = new AltitudeParser(input);

        // Act
        int result = parser.getFeet();

        // Assert
        assertThat(result, is(lessThan(0)));
    }
}
