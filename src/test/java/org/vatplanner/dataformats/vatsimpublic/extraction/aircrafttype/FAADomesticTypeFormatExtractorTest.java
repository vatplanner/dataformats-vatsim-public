package org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class FAADomesticTypeFormatExtractorTest {

    @DataProvider
    public static Object[][] dataProviderFAATypeFormatStringsAndExpectedExtraction() {
        return new Object[][]{
            // input, type, wake category, equipment code
            {"B738/L", "B738", null, "L"},
            {"C550/G", "C550", null, "G"},
            {"H/A359/L", "A359", "H", "L"},
            {"B78X", "B78X", null, null},
            {"H/A333", "A333", "H", null},
            {"Boeing 737-800", "Boeing 737-800", null, null},
            {"Boeing 737-800/L", "Boeing 737-800", null, "L"},
            {"M/Boeing 737-800/L", "Boeing 737-800", "M", "L"},
            {"M/Boeing 737-800", "Boeing 737-800", "M", null}, //
        };
    }

    @DataProvider
    public static Object[][] dataProviderInvalidInput() {
        return new Object[][]{
            {"a"},
            {"M/B738/L-S"},
            {"M/B738/L/S"},
            {"M-S/B738/L"},
            {"M/S/B738/L"},
            {"/"},
            {"//"},
            {"///"}, //
        };
    }

    @DataProvider
    public static Object[][] dataProviderEmptyInput() {
        return new Object[][]{
            {null},
            {""}, //
        };
    }

    @Test
    @UseDataProvider("dataProviderFAATypeFormatStringsAndExpectedExtraction")
    public void testGetAircraftType_validInput_returnsExpectedType(String input, String expectedType, String wakeCategory, String equipmentCode) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getAircraftType();

        // Assert
        assertThat(result, is(equalTo(expectedType)));
    }

    @Test
    @UseDataProvider("dataProviderInvalidInput")
    public void testGetAircraftType_invalidInput_returnsInput(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getAircraftType();

        // Assert
        assertThat(result, is(equalTo(input)));
    }

    @Test
    @UseDataProvider("dataProviderEmptyInput")
    public void testGetAircraftType_emptyInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getAircraftType();

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderFAATypeFormatStringsAndExpectedExtraction")
    public void testGetEquipmentCode_validInput_returnsExpectedEquipmentCode(String input, String type, String wakeCategory, String expectedEquipmentCode) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getEquipmentCode();

        // Assert
        assertThat(result, is(equalTo(expectedEquipmentCode)));
    }

    @Test
    @UseDataProvider("dataProviderInvalidInput")
    public void testGetEquipmentCode_invalidInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getEquipmentCode();

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderEmptyInput")
    public void testGetEquipmentCode_emptyInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getEquipmentCode();

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderFAATypeFormatStringsAndExpectedExtraction")
    public void testGetWakeCategory_validInput_returnsExpectedWakeCategory(String input, String type, String expectedWakeCategory, String equipmentCode) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getWakeCategory();

        // Assert
        assertThat(result, is(equalTo(expectedWakeCategory)));
    }

    @Test
    @UseDataProvider("dataProviderInvalidInput")
    public void testGetWakeCategory_invalidInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getWakeCategory();

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderEmptyInput")
    public void testGetWakeCategory_emptyInput_returnsNull(String input) {
        // Arrange
        FAADomesticTypeFormatExtractor extractor = new FAADomesticTypeFormatExtractor(input);

        // Act
        String result = extractor.getWakeCategory();

        // Assert
        assertThat(result, is(nullValue()));
    }

    // TODO: test trimming
}
