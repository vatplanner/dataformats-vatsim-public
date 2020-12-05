package org.vatplanner.dataformats.vatsimpublic.extraction;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class RealNameHomeBaseExtractorTest {

    @DataProvider
    public static Object[][] dataProviderValidInputWithExpectedRealNameAndHomeBase() {
        return new Object[][] {
            new Object[] { "just a name", "just a name", null }, //
            new Object[] { "with a base EDDT", "with a base", "EDDT" }, //
            new Object[] { "J. Doe", "J. Doe", null }, //
            new Object[] { "me R2D2", "me R2D2", null }, //
            new Object[] { "ALL CAPS KJFK", "ALL CAPS", "KJFK" }, //
            new Object[] { //
                "  whitespace  only stripped   before and after  LIRF  ", //
                "whitespace  only stripped   before and after", //
                "LIRF" //
            }, //
            new Object[] { "", null, null }, //
            new Object[] { "123456", null, null }, //
            new Object[] { "123456 ESSA", null, "ESSA" }, //
        };
    }

    @Test
    @UseDataProvider("dataProviderValidInputWithExpectedRealNameAndHomeBase")
    public void testGetRealName_validInput_returnsExpectedValue(String input, String expectedRealName, String unusedHomeBase) {
        // Arrange
        RealNameHomeBaseExtractor extractor = new RealNameHomeBaseExtractor(input);

        // Act
        String result = extractor.getRealName();

        // Assert
        assertThat(result, is(equalTo(expectedRealName)));
    }

    @Test
    @UseDataProvider("dataProviderValidInputWithExpectedRealNameAndHomeBase")
    public void testGetHomeBase_validInput_returnsExpectedValue(String input, String unusedRealName, String expectedHomeBase) {
        // Arrange
        RealNameHomeBaseExtractor extractor = new RealNameHomeBaseExtractor(input);

        // Act
        String result = extractor.getHomeBase();

        // Assert
        assertThat(result, is(equalTo(expectedHomeBase)));
    }
}
