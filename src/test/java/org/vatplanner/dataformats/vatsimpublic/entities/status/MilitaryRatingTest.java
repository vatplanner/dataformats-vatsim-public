package org.vatplanner.dataformats.vatsimpublic.entities.status;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.MilitaryRating.*;

@RunWith(DataProviderRunner.class)
public class MilitaryRatingTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @DataProvider
    public static Object[][] dataProviderIdAndShortName() {
        return new Object[][]{
                new Object[]{"M0", M0},
                new Object[]{"M1", M1},
                new Object[]{"M2", M2},
                new Object[]{"M3", M3},
                new Object[]{"M4", M4},
        };
    }

    @Test
    @UseDataProvider("dataProviderIdAndShortName")
    public void testResolveShortName_knownShortName_expectedEnum(String shortName, MilitaryRating expectedRating) {
        // Arrange (nothing to do)

        // Act
        MilitaryRating result = MilitaryRating.resolveShortName(shortName);

        // Assert
        assertThat(result, is(equalTo(expectedRating)));
    }

    @Test
    @DataProvider({"", "M5", "m1"})
    public void testResolveShortName_unknownShortName_throwsIllegalArgumentException(String unknownShortName) {
        // Arrange (nothing to do)

        // Act
        MilitaryRating result = MilitaryRating.resolveShortName(unknownShortName);

        // Assert
        assertThat(result, is(nullValue()));
    }
}
