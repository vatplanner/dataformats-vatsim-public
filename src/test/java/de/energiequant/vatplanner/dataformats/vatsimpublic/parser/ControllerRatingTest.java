package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import static de.energiequant.vatplanner.dataformats.vatsimpublic.parser.ControllerRating.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;


@RunWith(DataProviderRunner.class)
public class ControllerRatingTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @DataProvider
    public static Object[][] dataProviderIdAndEnum() {
        return new Object[][]{
            new Object[]{ 1, OBS },
            new Object[]{ 2, S1 },
            new Object[]{ 3, S2 },
            new Object[]{ 4, S3 },
            new Object[]{ 5, C1 },
            new Object[]{ 7, C3 },
            new Object[]{ 8, I },
            new Object[]{ 9, I2 },
            new Object[]{ 10, I3 },
            new Object[]{ 11, SUP },
            new Object[]{ 12, ADM },
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
    @DataProvider({"-2", "0", "100"})
    public void testResolveStatusFileId_unknownId_throwsIllegalArgumentException(int unknownId) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        ControllerRating.resolveStatusFileId(unknownId);
        
        // Assert (nothing to do)
    }
}
