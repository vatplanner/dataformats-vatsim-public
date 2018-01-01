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
public class ClientParserTest {
    private ClientParser parser;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setUp() {
        parser = new ClientParser();
    }
    
    @Test
    @DataProvider({"true", "false"})
    public void testSetIsParsingPrefileSection_anyFlag_returnsSameParserInstance(boolean flag) {
        // Arrange (nothing to do)
        
        // Act
        ClientParser result = parser.setIsParsingPrefileSection(flag);
        
        // Assert
        assertThat(result, is(sameInstance(parser)));
    }
    
    @Test
    @DataProvider({"", "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW::DCT:::::::201801010945:270:29.92:1013", "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW::DCT:::::::201801010945:270:29.92:1013:1:", "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW::DCT:::::::201801010945:270:29.92:1013:a:"})
    public void testParse_genericFormatViolation_throwsIllegalArgumentException(String erroneousLine) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    // <editor-fold defaultstate="collapsed" desc="callsign">
    @Test
    @DataProvider({"ABC123", "DABCD", "N123A"})
    public void testParse_connectedPilotWithCallsign_returnsObjectWithExpectedCallsign(String expectedCallsign) {
        // Arrange
        String line = String.format("%s:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedCallsign);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getCallsign(), is(equalTo(expectedCallsign)));
    }
    
    @Test
    @DataProvider({"ABC123", "DABCD", "N123A"})
    public void testParse_prefiledPilotWithCallsign_returnsObjectWithExpectedCallsign(String expectedCallsign) {
        // Arrange
        String line = String.format("%s:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedCallsign);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getCallsign(), is(equalTo(expectedCallsign)));
    }
    
    @Test
    @DataProvider({"EDDT_TWR", "LOWI_GND"})
    public void testParse_atcWithCallsign_returnsObjectWithExpectedCallsign(String expectedCallsign) {
        // Arrange
        String line = String.format("%s:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedCallsign);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getCallsign(), is(equalTo(expectedCallsign)));
    }
    
    @Test
    public void testParse_connectedPilotWithoutCallsign_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutCallsign_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutCallsign_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Vatsim ID">
    @Test
    @DataProvider({"123456", "987654321"})
    public void testParse_connectedPilotWithCID_returnsObjectWithExpectedVatsimID(int expectedVatsimID) {
        // Arrange
        String line = String.format("ABC123:%d:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedVatsimID);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVatsimID(), is(equalTo(expectedVatsimID)));
    }
    
    @Test
    @DataProvider({"123456", "987654321"})
    public void testParse_prefiledPilotWithCID_returnsObjectWithExpectedVatsimID(int expectedVatsimID) {
        // Arrange
        String line = String.format("ABC123:%d:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedVatsimID);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVatsimID(), is(equalTo(expectedVatsimID)));
    }
    
    @Test
    @DataProvider({"123456", "987654321"})
    public void testParse_atcWithCID_returnsObjectWithExpectedVatsimID(int expectedVatsimID) {
        // Arrange
        String line = String.format("EDDT_TWR:%d:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedVatsimID);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVatsimID(), is(equalTo(expectedVatsimID)));
    }
    
    @Test
    public void testParse_connectedPilotWithoutCID_returnsObjectWithNegativeVatsimID() {
        // Arrange
        String line = "ABC123::realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVatsimID(), is(lessThan(0)));
    }
    
    @Test
    public void testParse_prefiledPilotWithoutCID_returnsObjectWithNegativeVatsimID() {
        // Arrange
        String line = "ABC123::realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVatsimID(), is(lessThan(0)));
    }
    
    @Test
    public void testParse_atcWithoutCID_returnsObjectWithNegativeVatsimID() {
        // Arrange
        String line = "EDDT_TWR::realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVatsimID(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidCID_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format("ABC123:%s:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", invalidInput);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidCID_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format("ABC123:%s:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", invalidInput);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidCID_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format("ABC123:%s:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", invalidInput);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="real name">
    @Test
    @DataProvider({"", "A Name", "Name", "Name ESSA", "A Full Name ESSA"})
    public void testParse_connectedPilot_returnsObjectWithExpectedRealName(String expectedRealName) {
        // Arrange
        String line = String.format("ABC123:123456:%s:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedRealName);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRealName(), is(equalTo(expectedRealName)));
    }
    
    @Test
    @DataProvider({"", "A Name", "Name", "Name ESSA", "A Full Name ESSA"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedRealName(String expectedRealName) {
        // Arrange
        String line = String.format("ABC123:123456:%s:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedRealName);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRealName(), is(equalTo(expectedRealName)));
    }
    
    @Test
    @DataProvider({"", "Some Name", "Name", "Name ESSA", "A Full Name ESSA"})
    public void testParse_atc_returnsObjectWithExpectedVatsimID(String expectedRealName) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:%s:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedRealName);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRealName(), is(equalTo(expectedRealName)));
    }
    // </editor-fold>

    
    // <editor-fold defaultstate="collapsed" desc="real name">
    @Test
    public void testParse_connectedPilotWithClientTypePilot_returnsObjectWithClientTypePilotConnected() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getClientType(), is(equalTo(ClientType.PILOT_CONNECTED)));
    }
    
    @Test
    public void testParse_prefiledPilotWithoutClientType_returnsObjectWithClientTypePilotPrefiled() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getClientType(), is(equalTo(ClientType.PILOT_PREFILED)));
    }
    
    @Test
    public void testParse_atcWithClientTypeATC_returnsObjectWithClientTypeATCConnected() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getClientType(), is(equalTo(ClientType.ATC_CONNECTED)));
    }
    
    // TODO: check for empty client type outside prefile section, should be able to distinguish ATC and PILOT_CONNECTED
    
    /*
    @Test
    public void testParse_missingClientTypeOutsidePrefiledSection_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = "ABC123:123456:realname:::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"ATC", "PILOT"})
    public void testParse_clientTypeInPrefiledSection_throwsIllegalArgumentException(String inputClientType) {
        // Arrange
        String erroneousLine = String.format("ABC123:123456:realname:%s::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", inputClientType);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    */
    
    @Test
    @DataProvider({"A, true", "A, false"})
    public void testParse_invalidClientType_throwsIllegalArgumentException(String inputClientType, boolean isParsingPrefileSection) {
        // Arrange
        String erroneousLine = String.format("ABC123:123456:realname:%s::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", inputClientType);
        parser.setIsParsingPrefileSection(isParsingPrefileSection);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    // </editor-fold>
}
