package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

@RunWith(DataProviderRunner.class)
public class RemoveLineStrategyTest {

    @Test
    @DataProvider({ //
        "unwanted, ", //
        "abc:def, de:abc:f", //
        "original, Z", //
    })
    public void testHandleError_always_returnsNull(String rawLine, String filteredLine) {
        // Arrange
        RemoveLineStrategy strategy = new RemoveLineStrategy();

        // Act
        String output = strategy.handleError(rawLine, filteredLine, asList());

        // Assert
        assertThat(output, is(nullValue()));
    }
}
