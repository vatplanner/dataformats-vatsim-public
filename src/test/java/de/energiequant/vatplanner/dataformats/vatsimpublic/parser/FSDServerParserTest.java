package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class FSDServerParserTest {
    private FSDServerParser parser;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setUp() {
        parser = new FSDServerParser();
    }
    
    @Test
    @DataProvider({"", "ident:hostname:location:name:1", "ident:hostname:location:name:1:1:"})
    public void testParse_genericFormatViolation_throwsIllegalArgumentException(String erroneousLine) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"id1", "another ID"})
    public void testParse_validIdent_returnsObjectWithExpectedId(String expectedId) {
        // Arrange
        String line = String.format("%s:hostname:location:name:1:", expectedId);
        
        // Act
        FSDServer result = parser.parse(line);
        
        // Assert
        assertThat(result.getId(), is(equalTo(expectedId)));
    }
    
    @Test
    public void testParse_withoutIdent_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":hostname:location:name:1:";
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"hostname", "some.other.host.name", "123.45.67.89"})
    public void testParse_validHostnameOrIp_returnsObjectWithExpectedAddress(String expectedAddress) {
        // Arrange
        String line = String.format("someId:%s:location:name:1:", expectedAddress);
        
        // Act
        FSDServer result = parser.parse(line);
        
        // Assert
        assertThat(result.getAddress(), is(equalTo(expectedAddress)));
    }
    
    @Test
    public void testParse_withoutHostnameOrIp_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "someId::location:name:1:";
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"Location", "My Location", "My Location, Somewhere"})
    public void testParse_validLocation_returnsObjectWithExpectedLocation(String expectedLocation) {
        // Arrange
        String line = String.format("someId:hostname:%s:name:1:", expectedLocation);
        
        // Act
        FSDServer result = parser.parse(line);
        
        // Assert
        assertThat(result.getLocation(), is(equalTo(expectedLocation)));
    }
    
    @Test
    public void testParse_withoutLocation_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "someId:hostname::name:1:";
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"Simple Name", "special chars -.,%$!\" and numbers 0123456789 are ok too"})
    public void testParse_validName_returnsObjectWithExpectedName(String expectedName) {
        // Arrange
        String line = String.format("someId:hostname:location:%s:1:", expectedName);
        
        // Act
        FSDServer result = parser.parse(line);
        
        // Assert
        assertThat(result.getName(), is(equalTo(expectedName)));
    }
    
    @Test
    public void testParse_withoutName_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "someId:hostname:location::1:";
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"0, false", "1, true"})
    public void testParse_validFlagForClientsConnectionAllowed_returnsObjectWithExpectedConnectionFlag(String inputFlag, boolean expectedOutputFlag) {
        // Arrange
        String line = String.format("someId:hostname:location:name:%s:", inputFlag);
        
        // Act
        FSDServer result = parser.parse(line);
        
        // Assert
        assertThat(result.isClientConnectionAllowed(), is(equalTo(expectedOutputFlag)));
    }
    
    @Test
    @DataProvider({"", "-1", "2", "a", "10", "01"})
    public void testParse_invalidFlagsForClientsConnectionAllowed_throwsIllegalArgumentException(String invalidFlags) {
        // Arrange
        String erroneousLine = String.format("someId:hostname:location:name:%s:", invalidFlags);
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
}
