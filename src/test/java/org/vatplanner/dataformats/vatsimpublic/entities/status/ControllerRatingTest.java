package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.ADM;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.C1;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.C2;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.C3;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.I;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.I2;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.I3;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.INAC;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.OBS;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.S1;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.S2;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.S3;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.SUP;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.SUS;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class ControllerRatingTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @DataProvider
    public static Object[][] dataProviderIdAndEnum() {
        return new Object[][] {
            new Object[] { -1, INAC },
            new Object[] { 0, SUS },
            new Object[] { 1, OBS },
            new Object[] { 2, S1 },
            new Object[] { 3, S2 },
            new Object[] { 4, S3 },
            new Object[] { 5, C1 },
            new Object[] { 6, C2 },
            new Object[] { 7, C3 },
            new Object[] { 8, I },
            new Object[] { 9, I2 },
            new Object[] { 10, I3 },
            new Object[] { 11, SUP },
            new Object[] { 12, ADM },
        };
    }

    @Test
    @UseDataProvider("dataProviderIdAndEnum")
    public void testResolveStatusFileId_knownId_expectedEnum(int id, ControllerRating expectedRating) {
        // Arrange (nothing to do)

        // Act
        ControllerRating result = ControllerRating.resolveStatusFileId(id);

        // Assert
        assertThat(result, is(equalTo(expectedRating)));
    }

    @Test
    @DataProvider({ "-2", "13", "100" })
    public void testResolveStatusFileId_unknownId_throwsIllegalArgumentException(int unknownId) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        ControllerRating.resolveStatusFileId(unknownId);

        // Assert (nothing to do)
    }

    @DataProvider
    public static Object[][] dataProviderShortNameAndEnum() {
        return new Object[][] {
            new Object[] { "INAC", INAC },
            new Object[] { "SUS", SUS },
            new Object[] { "OBS", OBS },
            new Object[] { "S1", S1 },
            new Object[] { "S2", S2 },
            new Object[] { "S3", S3 },
            new Object[] { "C1", C1 },
            new Object[] { "C2", C2 },
            new Object[] { "C3", C3 },
            new Object[] { "I1", I },
            new Object[] { "I2", I2 },
            new Object[] { "I3", I3 },
            new Object[] { "SUP", SUP },
            new Object[] { "ADM", ADM },
        };
    }

    @Test
    @UseDataProvider("dataProviderShortNameAndEnum")
    public void testResolveShortName_knownShortName_expectedEnum(String shortName, ControllerRating expectedRating) {
        // Arrange (nothing to do)

        // Act
        ControllerRating result = ControllerRating.resolveShortName(shortName);

        // Assert
        assertThat(result, is(equalTo(expectedRating)));
    }

    @Test
    @DataProvider({ "", "INA", "I" })
    public void testResolveShortName_unknownShortName_throwsIllegalArgumentException(String unknownShortName) {
        // Arrange (nothing to do)

        // Act
        ControllerRating result = ControllerRating.resolveShortName(unknownShortName);

        // Assert
        assertThat(result, is(nullValue()));
    }
}
