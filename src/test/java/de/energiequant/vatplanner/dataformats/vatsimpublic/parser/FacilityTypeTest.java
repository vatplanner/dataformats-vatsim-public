package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import static de.energiequant.vatplanner.dataformats.vatsimpublic.parser.FacilityType.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;


@RunWith(DataProviderRunner.class)
public class FacilityTypeTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @DataProvider
    public static Object[][] dataProviderIdAndEnum() {
        return new Object[][]{
            new Object[]{ 0, OBSERVER },
            new Object[]{ 1, FSS },
            new Object[]{ 2, DELIVERY },
            new Object[]{ 3, GROUND },
            new Object[]{ 4, TOWER },
            new Object[]{ 5, APPROACH_DEPARTURE },
            new Object[]{ 6, CENTER },
        };
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
    @DataProvider({"-1", "7", "100"})
    public void testResolveStatusFileId_unknownId_throwsIllegalArgumentException(int unknownId) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        FacilityType.resolveStatusFileId(unknownId);
        
        // Assert (nothing to do)
    }
}
