package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.time.LocalTime;
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
    
    private static final double ALLOWED_DOUBLE_ERROR = 0.000001;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @DataProvider
    public static Object[][] dataProviderControllerRatingIdAndEnumWithoutOBS() {
        Object[][] allIdsAndEnums = ControllerRatingTest.dataProviderIdAndEnum();
        
        Object[][] exceptOBS = new Object[allIdsAndEnums.length - 1][2];
        int i = 0;
        for (Object[] idAndEnum : allIdsAndEnums) {
            int ratingId = (int) idAndEnum[0];
            ControllerRating ratingEnum = (ControllerRating) idAndEnum[1];
            
            if (ratingEnum != ControllerRating.OBS) {
                exceptOBS[i++] = idAndEnum;
            }
        }
        
        assert(i == exceptOBS.length); // all filled (omitting exactly one)
        
        return exceptOBS;
    }
    
    @DataProvider
    public static Object[][] dataProviderLocalTimeStringAndExpectedTimeApiObjects() {
        return new Object[][]{
            new Object[]{ "0", LocalTime.of(0, 0) },
            new Object[]{ "1", LocalTime.of(0, 1) },
            new Object[]{ "100", LocalTime.of(1, 0) },
            new Object[]{ "1221", LocalTime.of(12, 21) },
            new Object[]{ "2359", LocalTime.of(23, 59) },
        };
    }

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
    public void testParse_atc_returnsObjectWithExpectedRealName(String expectedRealName) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:%s:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedRealName);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRealName(), is(equalTo(expectedRealName)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="client type">
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

    // <editor-fold defaultstate="collapsed" desc="frequency">
    @Test
    public void testParse_connectedPilotWithoutFrequency_returnsObjectWithNegativeServedFrequency() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getServedFrequencyKilohertz(), is(lessThan(0)));
    }
    
    @Test
    public void testParse_connectedPilotWithFrequency_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT:121.750:12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutFrequency_returnsObjectWithNegativeServedFrequency() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getServedFrequencyKilohertz(), is(lessThan(0)));
    }
    
    @Test
    public void testParse_prefiledPilotWithFrequency_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname::121.750:::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"118.500, 118500", "118.50, 118500", "118.5, 118500", "121.725, 121725", "1.21725e2, 121725", "100.0001, 100000", "99.9999, 100000"})
    public void testParse_atcWithValidFrequency_returnsObjectWithExpectedServedFrequency(String input, int expectedFrequencyKilohertz) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:%s:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getServedFrequencyKilohertz(), is(equalTo(expectedFrequencyKilohertz)));
    }
    
    @Test
    @DataProvider({"-1", "0", "0000", "0.000", "1e-10"})
    public void testParse_atcWithInvalidFrequency_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:%s:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutFrequency_returnsObjectWithNegativeServedFrequency() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC::12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getServedFrequencyKilohertz(), is(lessThan(0)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="latitude">
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_connectedPilotWithLatitude_returnsObjectWithExpectedLatitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::%s:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        double expectedOutput = Double.parseDouble(input);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLatitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }
    
    @Test
    public void testParse_connectedPilotWithoutLatitude_returnsObjectWithNaNAsLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT:::12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLatitude(), is(equalTo(Double.NaN)));
    }
    
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithLatitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::%s::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithoutLatitude_returnsObjectWithNaNAsLatitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLatitude(), is(equalTo(Double.NaN)));
    }
    
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_atcWithLatitude_returnsObjectWithExpectedLatitude(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:%s:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        double expectedOutput = Double.parseDouble(input);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLatitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }
    
    @Test
    public void testParse_atcWithoutLatitude_returnsObjectWithNaNAsLatitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500::12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLatitude(), is(equalTo(Double.NaN)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="longitude">
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_connectedPilotWithLongitude_returnsObjectWithExpectedLongitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:%s:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        double expectedOutput = Double.parseDouble(input);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLongitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }
    
    @Test
    public void testParse_connectedPilotWithoutLongitude_returnsObjectWithNaNAsLongitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567::12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLongitude(), is(equalTo(Double.NaN)));
    }
    
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithLongitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname::::%s:::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithoutLongitude_returnsObjectWithNaNAsLongitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLongitude(), is(equalTo(Double.NaN)));
    }
    
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_atcWithLongitude_returnsObjectWithExpectedLongitude(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:%s:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        double expectedOutput = Double.parseDouble(input);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLongitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }
    
    @Test
    public void testParse_atcWithoutLongitude_returnsObjectWithNaNAsLongitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567::0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getLongitude(), is(equalTo(Double.NaN)));
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="altitude">
    @Test
    @DataProvider({"0", "100000", "-5000"})
    public void testParse_connectedPilotWithValidAltitude_returnsObjectWithExpectedAltitude(int expectedAltitude) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:%d:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedAltitude);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getAltitudeFeet(), is(equalTo(expectedAltitude)));
    }
    
    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidAltitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:%s:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutAltitude_returnsObjectWithZeroAltitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567::123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getAltitudeFeet(), is(equalTo(0)));
    }
    
    @Test
    @DataProvider({"100000", "-5000"})
    public void testParse_prefiledPilotWithNonZeroAltitude_throwsIllegalArgumentException(int altitude) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::%d::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", altitude);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidAltitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::%s::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutAltitude_returnsObjectWithZeroAltitude() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getAltitudeFeet(), is(equalTo(0)));
    }
    
    @Test
    @DataProvider({"0", "100000", "-5000"})
    public void testParse_atcWithValidAltitude_returnsObjectWithExpectedAltitude(int expectedAltitude) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:%d:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedAltitude);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getAltitudeFeet(), is(equalTo(expectedAltitude)));
    }
    
    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_atcWithInvalidAltitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:%s:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutAltitude_returnsObjectWithZeroAltitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getAltitudeFeet(), is(equalTo(0)));
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="ground speed">
    @Test
    @DataProvider({"0", "422"})
    public void testParse_connectedPilotWithValidGroundSpeed_returnsObjectWithExpectedGroundSpeed(int expectedGroundSpeed) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:%d:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedGroundSpeed);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getGroundSpeed(), is(equalTo(expectedGroundSpeed)));
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidGroundSpeed_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:%s:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345::B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getGroundSpeed(), is(lessThan(0)));
    }
    
    @Test
    public void testParse_prefiledPilotWithZeroGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "ABC123:123456:realname::::::0:B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getGroundSpeed(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"15", "321"})
    public void testParse_prefiledPilotWithNonZeroGroundSpeed_throwsIllegalArgumentException(int groundSpeed) {
        // Arrange
        String line = String.format("ABC123:123456:realname::::::%d:B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", groundSpeed);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidGroundSpeed_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname::::::%s:B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getGroundSpeed(), is(lessThan(0)));
    }
    
    @Test
    public void testParse_atcWithZeroGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:0::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getGroundSpeed(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"15", "321"})
    public void testParse_atcWithNonZeroGroundSpeed_throwsIllegalArgumentException(int groundSpeed) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:%d::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", groundSpeed);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidGroundSpeed_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:%s::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getGroundSpeed(), is(lessThan(0)));
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="aircraft type">
    @Test
    @DataProvider({"", "B738/M", "H/A332/X", "DH8D"})
    public void testParse_connectedPilot_returnsObjectWithExpectedAircraftType(String expectedAircraftType) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:%s:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedAircraftType);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getAircraftType(), is(equalTo(expectedAircraftType)));
    }
    
    @Test
    @DataProvider({"", "B738/M", "H/A332/X", "DH8D"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedAircraftType(String expectedAircraftType) {
        // Arrange
        String line = String.format("ABC123:123456::::::::%s:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedAircraftType);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getAircraftType(), is(equalTo(expectedAircraftType)));
    }
    
    @Test
    @DataProvider({"", "B738/M", "H/A332/X", "DH8D"})
    public void testParse_atc_returnsObjectWithExpectedAircraftType(String expectedAircraftType) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.
        
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0::%s:0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedAircraftType);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getAircraftType(), is(equalTo(expectedAircraftType)));
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="planned TAS cruise">
    @Test
    @DataProvider({"0", "90", "420"})
    public void testParse_connectedPilotWithPlannedTASCruise_returnsObjectWithExpectedFiledTrueAirSpeed(int expectedTAS) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:%d:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedTAS);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledTrueAirSpeed(), is(equalTo(expectedTAS)));
    }
    
    @Test
    @DataProvider({"0", "90", "420"})
    public void testParse_prefiledPilotWithPlannedTASCruise_returnsObjectWithExpectedFiledTrueAirSpeed(int expectedTAS) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:%d:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedTAS);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledTrueAirSpeed(), is(equalTo(expectedTAS)));
    }
    
    @Test
    @DataProvider({"123456", "987654321"})
    public void testParse_atcWithPlannedTASCruise_returnsObjectWithExpectedFiledTrueAirSpeed(int expectedTAS) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::%d::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedTAS);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledTrueAirSpeed(), is(equalTo(expectedTAS)));
    }
    
    @Test
    public void testParse_connectedPilotWithoutPlannedTASCruise_returnsObjectWithZeroFiledTrueAirSpeed() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738::EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledTrueAirSpeed(), is(equalTo(0)));
    }
    
    @Test
    public void testParse_prefiledPilotWithoutPlannedTASCruise_returnsObjectWithZeroFiledTrueAirSpeed() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738::EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledTrueAirSpeed(), is(equalTo(0)));
    }
    
    @Test
    public void testParse_atcWithoutPlannedTASCruise_returnsObjectWithZeroFiledTrueAirSpeed() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledTrueAirSpeed(), is(equalTo(0)));
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidPlannedTASCruise_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:%s:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", invalidInput);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidPlannedTASCruise_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format("ABC123:123456:realname:::::::B738:%s:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", invalidInput);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidPlannedTASCruise_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format("ABC123:123456:realname:ATC:118.500:12.34567:12.34567:0:::%s::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", invalidInput);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(erroneousLine);
        
        // Assert (nothing to do)
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="filed departure airport">
    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_connectedPilot_returnsObjectWithExpectedFiledDepartureAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:%s:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedAirportCode);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledDepartureAirportCode(), is(equalTo(expectedAirportCode)));
    }
    
    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedFiledDepartureAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:%s:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedAirportCode);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledDepartureAirportCode(), is(equalTo(expectedAirportCode)));
    }
    
    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_atc_returnsObjectWithExpectedFiledDepartureAirportCode(String expectedAirportCode) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.
        
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0:%s:::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedAirportCode);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledDepartureAirportCode(), is(equalTo(expectedAirportCode)));
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="filed altitude">
    @Test
    @DataProvider({"", "30000", "FL300", "F300", "0", "F", "F 300"})
    public void testParse_connectedPilot_returnsObjectWithExpectedRawFiledAltitude(String expectedRawFiledAltitude) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:%s:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedRawFiledAltitude);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRawFiledAltitude(), is(equalTo(expectedRawFiledAltitude)));
    }
    
    @Test
    @DataProvider({"", "30000", "FL300", "F300", "0", "F", "F 300"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedRawFiledAltitude(String expectedRawFiledAltitude) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:%s:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedRawFiledAltitude);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRawFiledAltitude(), is(equalTo(expectedRawFiledAltitude)));
    }
    
    @Test
    @DataProvider({"", "30000", "FL300", "F300", "0", "F", "F 300"})
    public void testParse_atc_returnsObjectWithExpectedRawFiledAltitude(String expectedRawFiledAltitude) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.
        
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::%s::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedRawFiledAltitude);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRawFiledAltitude(), is(equalTo(expectedRawFiledAltitude)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed destination airport">
    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_connectedPilot_returnsObjectWithExpectedFiledDestinationAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:%s:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedAirportCode);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledDestinationAirportCode(), is(equalTo(expectedAirportCode)));
    }
    
    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedFiledDestinationAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:%s:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedAirportCode);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledDestinationAirportCode(), is(equalTo(expectedAirportCode)));
    }
    
    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_atc_returnsObjectWithExpectedFiledDestinationAirportCode(String expectedAirportCode) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.
        
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0:::%s:SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedAirportCode);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFiledDestinationAirportCode(), is(equalTo(expectedAirportCode)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="server ID">
    @Test
    @DataProvider({"SERVER 1", "some-other-server"})
    public void testParse_connectedPilotWithServerId_returnsObjectWithExpectedServerId(String expectedServerId) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:%s:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedServerId);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getServerId(), is(equalTo(expectedServerId)));
    }
    
    @Test
    public void testParse_connectedPilotWithoutServerId_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM::1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"SERVER 1", "some-other-server"})
    public void testParse_prefiledPilotWithServerId_throwsIllegalArgumentException(String serverId) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:%s::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", serverId);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutServerId_returnsObjectWithNullForServerId() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getServerId(), is(nullValue()));
    }
    
    @Test
    @DataProvider({"SERVER 1", "some-other-server"})
    public void testParse_atcWithServerId_returnsObjectWithExpectedServerId(String expectedServerId) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::%s:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedServerId);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getServerId(), is(equalTo(expectedServerId)));
    }
    
    @Test
    public void testParse_atcWithoutServerId_throwsIllegalArgumentException() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0:::::100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="protocol version">
    @Test
    @DataProvider({"0", "10", "100"})
    public void testParse_connectedPilotWithValidProtocolRevision_returnsObjectWithExpectedProtocolVersion(int expectedProtocolVersion) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:%d:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedProtocolVersion);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getProtocolVersion(), is(equalTo(expectedProtocolVersion)));
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidProtocolRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:%s:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutProtocolRevision_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver::1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidProtocolRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM::%s:::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"0", "10", "100"})
    public void testParse_prefiledPilotWithValidProtocolRevision_throwsIllegalArgumentException(int protocolVersion) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM::%d:::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", protocolVersion);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutProtocolRevision_returnsObjectWithNegativeProtocolVersion() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getProtocolVersion(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"0", "10", "100"})
    public void testParse_atcWithValidProtocolRevision_returnsObjectWithExpectedProtocolVersion(int expectedProtocolVersion) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:%d:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", expectedProtocolVersion);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getProtocolVersion(), is(equalTo(expectedProtocolVersion)));
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidProtocolRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:%s:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutProtocolRevision_throwsIllegalArgumentException() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver::3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="controller rating">
    @Test
    public void testParse_connectedPilotWithRatingOBS_returnsObjectWithOBSControllerRating() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getControllerRating(), is(equalTo(ControllerRating.OBS)));
    }
    
    @Test
    @UseDataProvider("dataProviderControllerRatingIdAndEnumWithoutOBS")
    public void testParse_connectedPilotWithRatingOtherThanOBS_throwsIllegalArgumentException(int controllerRatingId, ControllerRating _controllerRating) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:%d:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", controllerRatingId);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutRating_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1::1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "0", "99"})
    public void testParse_connectedPilotWithInvalidRating_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:%s:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutRating_returnsObjectWithNullForControllerRating() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getControllerRating(), is(nullValue()));
    }
    
    @Test
    @UseDataProvider(value = "dataProviderIdAndEnum", location = ControllerRatingTest.class)
    public void testParse_prefiledPilotWithValidRating_throwsIllegalArgumentException(int controllerRatingId, ControllerRating _controllerRating) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::%d::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", controllerRatingId);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "0", "99"})
    public void testParse_prefiledPilotWithInvalidRating_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::%s::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @UseDataProvider(value = "dataProviderIdAndEnum", location = ControllerRatingTest.class)
    public void testParse_atcWithValidRating_returnsObjectWithExpectedControllerRating(int controllerRatingId, ControllerRating expectedControllerRating) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:%d::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", controllerRatingId);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getControllerRating(), is(equalTo(expectedControllerRating)));
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "0", "99"})
    public void testParse_atcWithInvalidRating_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:%s::4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutRating_throwsIllegalArgumentException() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="transponder code">
    @Test
    @DataProvider({"0", "1", "123", "2143", "7000", "9999"})
    public void testParse_connectedPilotWithValidTransponderCode_returnsObjectWithExpectedTransponderCodeDecimal(int expectedTransponderCodeDecimal) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:%d:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedTransponderCodeDecimal);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getTransponderCodeDecimal(), is(equalTo(expectedTransponderCodeDecimal)));
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "10000"})
    public void testParse_connectedPilotWithInvalidTransponderCode_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:%s:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutTransponderCode_returnsObjectWithNegativeTransponderCodeDecimal() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1::::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getTransponderCodeDecimal(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"0", "1", "123", "2143", "7000", "9999"})
    public void testParse_prefiledPilotWithValidTransponderCode_throwsIllegalArgumentException(int transponderCodeNumeric) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM::::%d:::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", transponderCodeNumeric);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "10000"})
    public void testParse_prefiledPilotWithInvalidTransponderCode_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM::::%s:::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutTransponderCode_returnsObjectWithNegativeTransponderCodeDecimal() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getTransponderCodeDecimal(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"0", "1", "123", "2143", "7000", "9999"})
    public void testParse_atcWithValidTransponderCode_throwsIllegalArgumentException(int transponderCodeNumeric) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3:%d:4:50::::::::::::::::atis message:20180101160000:20180101150000::::", transponderCodeNumeric);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "10000"})
    public void testParse_atcWithInvalidTransponderCode_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3:%s:4:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutTransponderCode_returnsObjectWithNegativeTransponderCodeDecimal() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getTransponderCodeDecimal(), is(lessThan(0)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="facility type">
    @Test
    @UseDataProvider(value="dataProviderIdAndEnum", location=FacilityTypeTest.class)
    public void testParse_connectedPilotWithValidFacilityType_throwsIllegalArgumentException(int id, FacilityType _facilityType) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:%d::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", id);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "7", "100"})
    public void testParse_connectedPilotWithInvalidFacilityType_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:%s::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutFacilityType_returnsObjectWithNullForFacilityType() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFacilityType(), is(nullValue()));
    }
    
    @Test
    @UseDataProvider(value="dataProviderIdAndEnum", location=FacilityTypeTest.class)
    public void testParse_prefiledPilotWithValidFacilityType_throwsIllegalArgumentException(int id, FacilityType _facilityType) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::%d::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", id);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "7", "100"})
    public void testParse_prefiledPilotWithInvalidFacilityType_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::%s::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutFacilityType_returnsObjectWithNullForFacilityType() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFacilityType(), is(nullValue()));
    }
    
    @Test
    @UseDataProvider(value="dataProviderIdAndEnum", location=FacilityTypeTest.class)
    public void testParse_atcPilotWithValidFacilityType_returnsObjectWithExpectedFacilityType(int id, FacilityType expectedFacilityType) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::%d:50::::::::::::::::atis message:20180101160000:20180101150000::::", id);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFacilityType(), is(equalTo(expectedFacilityType)));
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "7", "100"})
    public void testParse_atcPilotWithInvalidFacilityType_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::%s:50::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcPilotWithoutFacilityType_returnsObjectWithNullForFacilityType() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3:::50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFacilityType(), is(nullValue()));
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="visual range">
    @Test
    @DataProvider({"0", "5", "50", "200", "1000"})
    public void testParse_connectedPilotWithValidVisualRange_throwsIllegalArgumentException(int visualRange) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234::%d:1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", visualRange);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidVisualRange_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234::%s:1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVisualRange(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"0", "5", "50", "200", "1000"})
    public void testParse_prefiledPilotWithValidVisualRange_throwsIllegalArgumentException(int visualRange) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM::::::%d:1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", visualRange);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidVisualRange_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM::::::%s:1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVisualRange(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"0", "5", "50", "200", "1000"})
    public void testParse_atcWithValidVisualRange_returnsObjectWithExpectedVisualRange(int expectedVisualRange) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:%d::::::::::::::::atis message:20180101160000:20180101150000::::", expectedVisualRange);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVisualRange(), is(equalTo(expectedVisualRange)));
    }
    
    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidVisualRange_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:%s::::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:::::::::::::::::atis message:20180101160000:20180101150000::::";
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getVisualRange(), is(lessThan(0)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="flight plan revision">
    @Test
    @DataProvider({"0", "1", "200"})
    public void testParse_connectedPilotWithValidPlannedRevision_returnsObjectWithExpectedFlightPlanRevision(int expectedRevision) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::%d:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedRevision);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFlightPlanRevision(), is(equalTo(expectedRevision)));
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidPlannedRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::%s:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutPlannedRevision_returnsObjectWithNegativeFlightPlanRevision() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234::::I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFlightPlanRevision(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"0", "1", "200"})
    public void testParse_prefiledPilotWithValidPlannedRevision_returnsObjectWithExpectedFlightPlanRevision(int expectedRevision) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::%d:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedRevision);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFlightPlanRevision(), is(equalTo(expectedRevision)));
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidPlannedRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::%s:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_prefiledPilotWithoutPlannedRevision_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM::::::::I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    @DataProvider({"0", "1", "200"})
    public void testParse_atcWithValidPlannedRevision_returnsObjectWithExpectedFlightPlanRevision(int expectedRevision) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.
        
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:%d:::::::::::::::atis message:20180101160000:20180101150000::::", expectedRevision);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFlightPlanRevision(), is(equalTo(expectedRevision)));
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidPlannedRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50:%s:::::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_atcWithoutPlannedRevision_returnsObjectWithNegativeFlightPlanRevision() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getFlightPlanRevision(), is(lessThan(0)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="flight plan type">
    @Test
    @DataProvider({"", "V", "I", "Y", "Z", "something else 123-ABC"})
    public void testParse_connectedPilot_returnsObjectWithExpectedRawFlightPlanType(String expectedRawFlightPlanType) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:%s:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedRawFlightPlanType);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRawFlightPlanType(), is(equalTo(expectedRawFlightPlanType)));
    }
    
    @Test
    @DataProvider({"", "V", "I", "Y", "Z", "something else 123-ABC"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedRawFlightPlanType(String expectedRawFlightPlanType) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:%s:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", expectedRawFlightPlanType);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRawFlightPlanType(), is(equalTo(expectedRawFlightPlanType)));
    }
    
    @Test
    @DataProvider({"", "V", "I", "Y", "Z", "something else 123-ABC"})
    public void testParse_atc_returnsObjectWithExpectedRawFlightPlanType(String expectedRawFlightPlanType) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.
        
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::%s::::::::::::::atis message:20180101160000:20180101150000::::", expectedRawFlightPlanType);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getRawFlightPlanType(), is(equalTo(expectedRawFlightPlanType)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="departure time planned">
    @Test
    @UseDataProvider("dataProviderLocalTimeStringAndExpectedTimeApiObjects")
    public void testParse_connectedPilotWithValidPlannedDepartureTime_returnsObjectWithExpectedDepartureTimePlanned(String input, LocalTime expectedTime) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:%s:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(equalTo(expectedTime)));
    }
    
    @Test
    @DataProvider({"2400", "60", "1709907214"})
    public void testParse_connectedPilotWithInvalidUnsignedNumericPlannedDepartureTime_returnsObjectWithNullForDepartureTimePlanned(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:%s:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(nullValue()));
    }
    
    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidAlphaNumericPlannedDepartureTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:%s:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }
    
    @Test
    public void testParse_connectedPilotWithoutPlannedDepartureTime_returnsObjectWithNullForDepartureTimePlanned() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I::1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(nullValue()));
    }
    
    @Test
    @UseDataProvider("dataProviderLocalTimeStringAndExpectedTimeApiObjects")
    public void testParse_prefiledPilotWithValidPlannedDepartureTime_returnsObjectWithExpectedDepartureTimePlanned(String input, LocalTime expectedTime) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:%s:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(equalTo(expectedTime)));
    }
    
    @Test
    @DataProvider({"2400", "60", "1709907214"})
    public void testParse_prefiledPilotWithInvalidUnsignedNumericPlannedDepartureTime_returnsObjectWithNullForDepartureTimePlanned(String input) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:%s:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(nullValue()));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidAlphaNumericPlannedDepartureTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:%s:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutPlannedDepartureTime_returnsObjectWithNullForDepartureTimePlanned() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I::1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(nullValue()));
    }
    
    @Test
    @UseDataProvider("dataProviderLocalTimeStringAndExpectedTimeApiObjects")
    public void testParse_atcWithValidPlannedDepartureTime_returnsObjectWithExpectedDepartureTimePlanned(String input, LocalTime expectedTime) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.
        
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::%s:::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(equalTo(expectedTime)));
    }
    
    @Test
    @DataProvider({"2400", "60", "1709907214"})
    public void testParse_atcWithInvalidUnsignedNumericPlannedDepartureTime_returnsObjectWithNullForDepartureTimePlanned(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::%s:::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(nullValue()));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidAlphaNumericPlannedDepartureTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::%s:::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);
        
        thrown.expect(IllegalArgumentException.class);
        
        // Act
        parser.parse(line);
        
        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutPlannedDepartureTime_returnsObjectWithNullForDepartureTimePlanned() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);
        
        // Act
        Client result = parser.parse(line);
        
        // Assert
        assertThat(result.getDepartureTimePlanned(), is(nullValue()));
    }
    // </editor-fold>
}
