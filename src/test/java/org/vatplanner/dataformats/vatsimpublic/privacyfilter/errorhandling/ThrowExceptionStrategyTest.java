package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import static java.util.Arrays.asList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling.ErrorHandlingStrategy.FailWithException;

@RunWith(DataProviderRunner.class)
public class ThrowExceptionStrategyTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @DataProvider({ //
        "unwanted, ", //
        "abc:def, de:abc:f", //
        "original, Z", //
    })
    public void testHandleError_always_throwsFailWithException(String rawLine, String filteredLine) {
        // Arrange
        ThrowExceptionStrategy strategy = new ThrowExceptionStrategy();

        thrown.expect(FailWithException.class);

        // Act
        strategy.handleError(rawLine, filteredLine, asList());

        // Assert (nothing to do)
    }

}
