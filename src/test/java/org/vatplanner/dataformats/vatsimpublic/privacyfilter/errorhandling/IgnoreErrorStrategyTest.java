package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class IgnoreErrorStrategyTest {

    @Test
    @DataProvider({ //
        "unwanted, ", //
        "abc:def, de:abc:f", //
        "original, Z", //
    })
    public void testHandleError_always_returnsFilteredLine(String rawLine, String expectedOutput) {
        // Arrange
        IgnoreErrorStrategy strategy = new IgnoreErrorStrategy();

        // Act
        String output = strategy.handleError(rawLine, expectedOutput, asList());

        // Assert
        assertThat(output, is(equalTo(expectedOutput)));
    }
}
