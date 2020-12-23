package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.ATPL;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.CMEL;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.IR;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.PPL;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.UNRATED;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class PilotRatingTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @DataProvider
    public static Object[][] dataProviderIdAndShortName() {
        return new Object[][] {
            new Object[] { "NEW", UNRATED },
            new Object[] { "PPL", PPL },
            new Object[] { "IR", IR },
            new Object[] { "CMEL", CMEL },
            new Object[] { "ATPL", ATPL },
        };
    }

    @Test
    @UseDataProvider("dataProviderIdAndShortName")
    public void testResolveShortName_knownShortName_expectedEnum(String shortName, PilotRating expectedRating) {
        // Arrange (nothing to do)

        // Act
        PilotRating result = PilotRating.resolveShortName(shortName);

        // Assert
        assertThat(result, is(equalTo(expectedRating)));
    }

    @Test
    @DataProvider({ "", "P1", "ATP" })
    public void testResolveShortName_unknownShortName_throwsIllegalArgumentException(String unknownShortName) {
        // Arrange (nothing to do)

        // Act
        PilotRating result = PilotRating.resolveShortName(unknownShortName);

        // Assert
        assertThat(result, is(nullValue()));
    }
}
