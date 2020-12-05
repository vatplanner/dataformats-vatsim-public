package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

@RunWith(DataProviderRunner.class)
public class KeepOriginalContentStrategyTest {

    @Test
    @DataProvider({ //
        "wanted, ", //
        "abc:def, de:abc:f", //
        "original, Z", //
    })
    public void testHandleError_always_returnsRawLine(String expectedOutput, String filteredLine) {
        // Arrange
        KeepOriginalContentStrategy strategy = new KeepOriginalContentStrategy();

        // Act
        String output = strategy.handleError(expectedOutput, filteredLine, asList());

        // Assert
        assertThat(output, is(equalTo(expectedOutput)));
    }
}
