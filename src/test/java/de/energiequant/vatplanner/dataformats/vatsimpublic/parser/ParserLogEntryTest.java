package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link ParserLogEntry}.
 */
@RunWith(DataProviderRunner.class)
public class ParserLogEntryTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void testConstructor_nullMessage_throwsIllegalArgumentException() {
        // Arrange
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        new ParserLogEntry("abc", "xyz", false, null, null);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"", "abc"})
    public void testConstructor_nonNullMessage_doesNotFail(String message) {
        // Arrange (nothing to do)
        
        // Act
        new ParserLogEntry("abc", "xyz", false, message, null);
        
        // Assert (nothing to do)
    }
}
