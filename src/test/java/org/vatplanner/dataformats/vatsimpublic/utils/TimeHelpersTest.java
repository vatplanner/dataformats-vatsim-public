package org.vatplanner.dataformats.vatsimpublic.utils;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.time.Duration;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class TimeHelpersTest {

    @DataProvider
    public static Object[][] dataProviderIsLessOrEqualThan() {
        return new Object[][]{
            new Object[]{Duration.ofSeconds(1), Duration.ofSeconds(2), true}, //
            new Object[]{Duration.ofSeconds(2), Duration.ofSeconds(2), true}, //
            new Object[]{Duration.ofSeconds(3), Duration.ofSeconds(2), false}, //

            new Object[]{Duration.ofSeconds(3), Duration.ofMinutes(2), true}, //
            new Object[]{Duration.ofMinutes(3), Duration.ofMinutes(2), false}, //
        };
    }

    @Test
    @UseDataProvider("dataProviderIsLessOrEqualThan")
    public void testIsLessOrEqualThan_validInput_returnsExpectedResult(Duration a, Duration b, boolean expectedResult) {
        // Arrange (nothing to do)

        // Act
        boolean result = TimeHelpers.isLessOrEqualThan(a, b);

        // Assert
        assertThat(result, is(expectedResult));
    }
}
