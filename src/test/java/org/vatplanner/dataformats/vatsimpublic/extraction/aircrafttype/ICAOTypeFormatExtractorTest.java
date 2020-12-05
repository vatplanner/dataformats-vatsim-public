package org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class ICAOTypeFormatExtractorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @DataProvider
    public static Object[][] dataProviderICAOTypeFormatStringsAndExpectedExtraction() {
        return new Object[][] {
            // input, type, wake category, equipment code
            { "B763/H-L/LB1", "B763", "H", "L/LB1" },
            { "A319/M-SDE3FGHIRWY/LB1", "A319", "M", "SDE3FGHIRWY/LB1" },
            { "CONC/H-SDE2E3FGHIRYW/X", "CONC", "H", "SDE2E3FGHIRYW/X" },
            { "B77W/H-SDE1E2E3GHIJ4J5M1P2RWXYZ/LB1D1", "B77W", "H", "SDE1E2E3GHIJ4J5M1P2RWXYZ/LB1D1" },
            { "B732/M-SDFGHRWY/C", "B732", "M", "SDFGHRWY/C" },
            { "B737/M-S", "B737", "M", "S" }, // this actually does not seem to be a real-world valid input but it has
                                              // been seen on VATSIM
            { "DH8D/M-SDE2E3FGRY/H", "DH8D", "M", "SDE2E3FGRY/H" },

            { "B78X/H-SADE1E2E3FGHIJ1J3J4J5J6LM1M2OP2RWXYZ/LB1D1G1", "B78X", "H",
                "SADE1E2E3FGHIJ1J3J4J5J6LM1M2OP2RWXYZ/LB1D1G1" }, //

            { "Boeing 737-800/M-L", "Boeing 737-800", "M", "L" }, // actually not seen yet but free-text types instead
                                                                  // of ICAO codes have been seen on FAA-based format,
                                                                  // so expect it here as well
        };
    }

    @Test
    @UseDataProvider(location = FAADomesticTypeFormatExtractorTest.class, value = "dataProviderFAATypeFormatStringsAndExpectedExtraction")
    public void testConstructor_faaValidInput_throwsIllegalArgumentException(String input, String type, String wakeCategory, String equipmentCode) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        new ICAOTypeFormatExtractor(input);

        // Assert (nothing to do)
    }

    @Test
    @UseDataProvider(location = FAADomesticTypeFormatExtractorTest.class, value = "dataProviderInvalidInput")
    public void testConstructor_faaInvalidInput_throwsIllegalArgumentException(String input) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        new ICAOTypeFormatExtractor(input);

        // Assert (nothing to do)
    }

    @Test
    @UseDataProvider(location = FAADomesticTypeFormatExtractorTest.class, value = "dataProviderEmptyInput")
    public void testConstructor_faaEmptyInput_throwsIllegalArgumentException(String input) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        new ICAOTypeFormatExtractor(input);

        // Assert (nothing to do)
    }

    @Test
    @UseDataProvider("dataProviderICAOTypeFormatStringsAndExpectedExtraction")
    public void testGetAircraftType_validInput_returnsExpectedType(String input, String expectedType, String wakeCategory, String equipmentCode) {
        // Arrange
        ICAOTypeFormatExtractor extractor = new ICAOTypeFormatExtractor(input);

        // Act
        String result = extractor.getAircraftType();

        // Assert
        assertThat(result, is(equalTo(expectedType)));
    }

    @Test
    @UseDataProvider("dataProviderICAOTypeFormatStringsAndExpectedExtraction")
    public void testGetEquipmentCode_validInput_returnsExpectedEquipmentCode(String input, String type, String wakeCategory, String expectedEquipmentCode) {
        // Arrange
        ICAOTypeFormatExtractor extractor = new ICAOTypeFormatExtractor(input);

        // Act
        String result = extractor.getEquipmentCode();

        // Assert
        assertThat(result, is(equalTo(expectedEquipmentCode)));
    }

    @Test
    @UseDataProvider("dataProviderICAOTypeFormatStringsAndExpectedExtraction")
    public void testGetWakeCategory_validInput_returnsExpectedWakeCategory(String input, String type, String expectedWakeCategory, String equipmentCode) {
        // Arrange
        ICAOTypeFormatExtractor extractor = new ICAOTypeFormatExtractor(input);

        // Act
        String result = extractor.getWakeCategory();

        // Assert
        assertThat(result, is(equalTo(expectedWakeCategory)));
    }

    // TODO: test trimming
}
