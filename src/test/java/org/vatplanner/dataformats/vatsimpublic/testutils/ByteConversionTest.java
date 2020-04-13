package org.vatplanner.dataformats.vatsimpublic.testutils;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class ByteConversionTest {

    @DataProvider
    public static Object[][] dataProviderIntegersAndBytes() {
        return new Object[][]{
            {0b00000000, (byte) 0},
            {0b00000001, (byte) 1},
            {0b01111111, (byte) 127},
            {0b10000000, (byte) -128},
            {0b10000001, (byte) -127},
            {0b11111111, (byte) -1}
        };
    }

    @Test
    @UseDataProvider("dataProviderIntegersAndBytes")
    public void testRawByte_integersInRange_returnsExpectedByteValue(int in, byte expectedValue) {
        // Arrange (nothing to do)

        // Act
        byte result = ByteConversion.rawByte(in);

        // Assert
        assertThat(result, is(equalTo(expectedValue)));
    }
}
