package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.APPROACH_DEPARTURE;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.CENTER;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.DELIVERY;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.FSS;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.GROUND;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.OBSERVER;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.TOWER;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class FacilityTypeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @DataProvider
    public static Object[][] dataProviderIdAndEnum() {
        return new Object[][] {
            new Object[] { 0, OBSERVER },
            new Object[] { 1, FSS },
            new Object[] { 2, DELIVERY },
            new Object[] { 3, GROUND },
            new Object[] { 4, TOWER },
            new Object[] { 5, APPROACH_DEPARTURE },
            new Object[] { 6, CENTER }, };
    }

    @Test
    @UseDataProvider("dataProviderIdAndEnum")
    public void testResolveStatusFileId_knownId_expectedEnum(int id, FacilityType expectedFacilityType) {
        // Arrange (nothing to do)

        // Act
        FacilityType result = FacilityType.resolveStatusFileId(id);

        // Assert
        assertThat(result, is(equalTo(expectedFacilityType)));
    }

    @Test
    @DataProvider({ "-1", "7", "100" })
    public void testResolveStatusFileId_unknownId_throwsIllegalArgumentException(int unknownId) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        FacilityType.resolveStatusFileId(unknownId);

        // Assert (nothing to do)
    }

    @DataProvider
    public static Object[][] dataProviderShortNameAndEnum() {
        return new Object[][] {
            new Object[] { "OBS", OBSERVER },
            new Object[] { "FSS", FSS },
            new Object[] { "DEL", DELIVERY },
            new Object[] { "GND", GROUND },
            new Object[] { "TWR", TOWER },
            new Object[] { "APP", APPROACH_DEPARTURE },
            new Object[] { "CTR", CENTER }, };
    }

    @Test
    @UseDataProvider("dataProviderShortNameAndEnum")
    public void testResolveShortName_knownShortName_expectedEnum(String shortName, FacilityType expectedFacilityType) {
        // Arrange (nothing to do)

        // Act
        FacilityType result = FacilityType.resolveShortName(shortName);

        // Assert
        assertThat(result, is(equalTo(expectedFacilityType)));
    }

    @Test
    @DataProvider({ "", "ctr", "GN" })
    public void testResolveShortName_unknownShortName_returnsNull(String unknownShortName) {
        // Arrange (nothing to do)

        // Act
        FacilityType result = FacilityType.resolveShortName(unknownShortName);

        // Assert
        assertThat(result, is(nullValue()));
    }
}
