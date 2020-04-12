package org.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.stream.Collectors;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating;
import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRatingTest;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityTypeTest;

@RunWith(DataProviderRunner.class)
public class ClientParserTest {

    private ClientParser parser;

    private static final double ALLOWED_DOUBLE_ERROR = 0.000001;

    private static final String CONTROLLER_MESSAGE_LINEBREAK = new String(new byte[]{(byte) 0x5E, (byte) 0xA7}, Charset.forName("ISO-8859-1"));

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

        assert (i == exceptOBS.length); // all filled (omitting exactly one)

        return exceptOBS;
    }

    @DataProvider
    public static Object[][] dataProviderHoursAndMinutesAndDuration() {
        return new Object[][]{
            new Object[]{0, 0, Duration.ofMinutes(0)},
            new Object[]{0, 1, Duration.ofMinutes(1)},
            new Object[]{1, 0, Duration.ofHours(1)},
            new Object[]{2, 59, Duration.ofMinutes(179)},
            new Object[]{2, 60, Duration.ofMinutes(180)}, // excessive minutes (>59) are valid
            new Object[]{13, 7, Duration.ofMinutes(787)},
            new Object[]{0, 787, Duration.ofMinutes(787)}, // excessive minutes (>59) are valid

            // negative values are (unfortunately) also... valid :/
            new Object[]{-8, 0, Duration.ofHours(-8)},
            new Object[]{0, -2, Duration.ofMinutes(-2)},
            new Object[]{-8, -2, Duration.ofMinutes(-482)}, //

            // Since negative values don't make any sense we need to make sure
            // that such input does not mix up when using different signs per
            // hour/minute number. We expect result to remain negative in those
            // cases.
            new Object[]{1, -60, Duration.ofMinutes(-120)},
            new Object[]{-1, 60, Duration.ofMinutes(-120)},};
    }

    @DataProvider
    public static Object[][] dataProviderControllerMessageRawAndDecoded() {
        return new Object[][]{
            new Object[]{"simple one-liner with /-.$,#\\ special characters", "simple one-liner with /-.$,#\\ special characters"},
            new Object[]{":colons : :: are:valid::", ":colons : :: are:valid::"},
            new Object[]{"first line" + CONTROLLER_MESSAGE_LINEBREAK + "second line" + CONTROLLER_MESSAGE_LINEBREAK, "first line\nsecond line\n"}, // FIXME: charset detection & decoding, Russian controllers send windows-1251 encapsulated in UTF-8
        };
    }

    @DataProvider
    public static Object[][] dataProviderFullTimestampStringAndObject() {
        return new Object[][]{
            new Object[]{"20171014170050", LocalDateTime.of(2017, 10, 14, 17, 0, 50).toInstant(ZoneOffset.UTC)},
            new Object[]{"20180101000000", LocalDateTime.of(2018, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)},};
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
    public void testParse_connectedPilotWithClientTypePilot_returnsObjectWithRawClientTypePilotConnected() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawClientType(), is(equalTo(ClientType.PILOT_CONNECTED)));
    }

    @Test
    public void testParse_prefiledPilotWithoutClientType_returnsObjectWithRawClientTypePilotPrefiled() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawClientType(), is(equalTo(ClientType.PILOT_PREFILED)));
    }

    @Test
    public void testParse_atcWithClientTypeATC_returnsObjectWithRawClientTypeATCConnected() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawClientType(), is(equalTo(ClientType.ATC_CONNECTED)));
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
    @DataProvider({"121.750", "198.999"})
    public void testParse_connectedPilotWithNonPlaceholderFrequency_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT:%s:12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"199.000, 199000", "199.998, 199998"})
    public void testParse_connectedPilotWithPlaceholderFrequency_throwsIllegalArgumentException(String input, int expectedFrequencyKilohertz) {
        // This has not actually be seen in the wild but ATC clients may be
        // interpreted as effectively pilots so the test fit in here.
        // Placeholder frequencies in general should be allowed, just not active
        // frequencies.

        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT:%s:12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getServedFrequencyKilohertz(), is(equalTo(expectedFrequencyKilohertz)));
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
    @DataProvider({"118.500, 118500", "118.50, 118500", "118.5, 118500", "121.725, 121725", "1.21725e2, 121725", "199.998, 199998", "100.0001, 100000", "99.9999, 100000"})
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
    @DataProvider({"12.34567", "-80.23456", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithNonZeroLatitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::%s::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithZeroLatitude_returnsObjectWithNaNAsLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:::0::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getLatitude(), is(equalTo(Double.NaN)));
    }

    @Test
    public void testParse_prefiledPilotWithoutLatitude_returnsObjectWithNaNAsLatitude() {
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
    @DataProvider({"12.34567", "-80.23456", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithNonZeroLongitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname::::%s:::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithZeroLongitude_returnsObjectWithNaNAsLongitude() {
        // Arrange
        String line = "ABC123:123456:realname::::0:::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getLongitude(), is(equalTo(Double.NaN)));
    }

    @Test
    public void testParse_prefiledPilotWithoutLongitude_returnsObjectWithNaNAsLongitude() {
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
    public void testParse_connectedPilotWithoutProtocolRevision_returnsObjectWithNegativeProtocolVersion() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver::1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getProtocolVersion(), is(lessThan(0)));
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
    @DataProvider({"1", "10", "100"})
    public void testParse_prefiledPilotWithValidNonZeroProtocolRevision_throwsIllegalArgumentException(int protocolVersion) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM::%d:::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", protocolVersion);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithZeroProtocolRevision_returnsObjectWithNegativeProtocolVersion() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM::0:::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getProtocolVersion(), is(lessThan(0)));
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
    public void testParse_atcWithoutProtocolRevision_returnsObjectWithNegativeProtocolVersion() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver::3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getProtocolVersion(), is(lessThan(0)));
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
    public void testParse_prefiledPilotWithZeroRating_returnsObjectWithNullForControllerRating() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::0::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
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
    @DataProvider({"-1", "abc", "1a", "a1", "99"})
    public void testParse_prefiledPilotWithInvalidNonZeroRating_throwsIllegalArgumentException(String input) {
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
    @DataProvider({"0", "1", "123", "2143", "7000", "9999", "12345"})
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
    @DataProvider({"-1", "abc", "1a", "a1"})
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
    @DataProvider({"1", "123", "2143", "7000", "9999", "12345"})
    public void testParse_prefiledPilotWithValidNonZeroTransponderCode_throwsIllegalArgumentException(int transponderCodeNumeric) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM::::%d:::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", transponderCodeNumeric);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
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
    @DataProvider({"1", "123", "2143", "7000", "9999", "12345"})
    public void testParse_atcWithValidNonZeroTransponderCode_throwsIllegalArgumentException(int transponderCodeNumeric) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3:%d:4:50::::::::::::::::atis message:20180101160000:20180101150000::::", transponderCodeNumeric);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
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
    @DataProvider
    public static Object[][] dataProviderNonZeroFacilityTypeIds() {
        return Arrays.stream(FacilityTypeTest.dataProviderIdAndEnum())
                .map(arguments -> (Integer) arguments[0])
                .filter(id -> id != 0)
                .distinct()
                .map(id -> new Object[]{id})
                .collect(Collectors.toList())
                .toArray(new Object[0][0]);
    }

    @Test
    @UseDataProvider("dataProviderNonZeroFacilityTypeIds")
    public void testParse_connectedPilotWithValidNonZeroFacilityType_throwsIllegalArgumentException(int id) {
        // 0 is the default value for all pilots since data format version 9

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
    public void testParse_connectedPilotWithZeroFacilityType_returnsObjectWithNullForFacilityType() {
        // 0 is the default value for all pilots since data format version 9

        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:0::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFacilityType(), is(nullValue()));
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
    @UseDataProvider("dataProviderNonZeroFacilityTypeIds")
    public void testParse_prefiledPilotWithValidNonZeroFacilityType_throwsIllegalArgumentException(int id) {
        // 0 is the default value for all pilots since data format version 9

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
    public void testParse_prefiledPilotWithZeroFacilityType_returnsObjectWithNullForFacilityType() {
        // 0 is the default value for all pilots since data format version 9

        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::0::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFacilityType(), is(nullValue()));
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
    @UseDataProvider(value = "dataProviderIdAndEnum", location = FacilityTypeTest.class)
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
    @DataProvider({"1", "5", "50", "200", "1000"})
    public void testParse_connectedPilotWithValidNonZeroVisualRange_throwsIllegalArgumentException(int visualRange) {
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
    public void testParse_connectedPilotWithZeroVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234::0:1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getVisualRange(), is(lessThan(0)));
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
    @DataProvider({"1", "5", "50", "200", "1000"})
    public void testParse_prefiledPilotWithValidNonZeroVisualRange_throwsIllegalArgumentException(int visualRange) {
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
    public void testParse_prefiledPilotWithZeroVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM::::::0:1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getVisualRange(), is(lessThan(0)));
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
    @DataProvider({"0", "30", "2359", "123456789"})
    public void testParse_connectedPilotWithValidPlannedDeparture_returnsObjectWithExpectedRawDepartureTimePlanned(int rawValue) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:%d:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", rawValue);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimePlanned(), is(equalTo(rawValue)));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidPlannedDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:%s:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutPlannedDeparture_returnsObjectWithNegativeRawDepartureTimePlanned() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I::1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimePlanned(), is(lessThan(0)));
    }

    @Test
    @DataProvider({"0", "30", "2359", "123456789"})
    public void testParse_prefiledPilotWithValidPlannedDeparture_returnsObjectWithExpectedRawDepartureTimePlanned(int rawValue) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:%d:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", rawValue);
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimePlanned(), is(equalTo(rawValue)));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidPlannedDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:%s:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutPlannedDeparture_returnsObjectWithNegativeRawDepartureTimePlanned() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I::1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimePlanned(), is(lessThan(0)));
    }

    @Test
    @DataProvider({"0", "30", "2359", "123456789"})
    public void testParse_atcWithValidPlannedDeparture_returnsObjectWithExpectedRawDepartureTimePlanned(int rawValue) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::%d:::::::::::::atis message:20180101160000:20180101150000::::", rawValue);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimePlanned(), is(equalTo(rawValue)));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidPlannedDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::%s:::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutPlannedDeparture_returnsObjectWithNegativeRawDepartureTimePlanned() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimePlanned(), is(lessThan(0)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="departure time actual">
    @Test
    @DataProvider({"0", "30", "2359", "123456789"})
    public void testParse_connectedPilotWithValidActualDeparture_returnsObjectWithExpectedRawDepartureTimeActual(int rawValue) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:%d:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", rawValue);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimeActual(), is(equalTo(rawValue)));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidActualDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:%s:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutActualDeparture_returnsObjectWithNegativeRawDepartureTimeActual() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000::1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimeActual(), is(lessThan(0)));
    }

    @Test
    @DataProvider({"0", "30", "2359", "123456789"})
    public void testParse_prefiledPilotWithValidActualDeparture_returnsObjectWithExpectedRawDepartureTimeActual(int rawValue) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:%d:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", rawValue);
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimeActual(), is(equalTo(rawValue)));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidActualDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:%s:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutActualDeparture_returnsObjectWithNegativeRawDepartureTimeActual() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000::1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimeActual(), is(lessThan(0)));
    }

    @Test
    @DataProvider({"0", "30", "2359", "123456789"})
    public void testParse_atcWithValidActualDeparture_returnsObjectWithExpectedRawDepartureTimeActual(int rawValue) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::%d::::::::::::atis message:20180101160000:20180101150000::::", rawValue);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimeActual(), is(equalTo(rawValue)));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidActualDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::%s::::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutActualDeparture_returnsObjectWithNegativeRawDepartureTimeActual() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawDepartureTimeActual(), is(lessThan(0)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed time enroute">
    @Test
    @UseDataProvider("dataProviderHoursAndMinutesAndDuration")
    public void testParse_connectedPilotWithValidPlannedEnroute_returnsObjectWithExpectedFiledTimeEnroute(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:%d:%d:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", hours, minutes);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeEnroute(), is(equalTo(expectedDuration)));
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidPlannedHoursEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:%s:0:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidPlannedMinutesEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:0:%s:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_connectedPilotWithPlannedHoursEnrouteButWithoutPlannedMinutesEnroute_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:%d::3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", hours);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_connectedPilotWithPlannedMinutesEnrouteButWithoutPlannedHoursEnroute_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000::%d:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", minutes);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutPlannedEnroute_returnsObjectWithNullForFiledTimeEnroute() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000::::3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeEnroute(), is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderHoursAndMinutesAndDuration")
    public void testParse_prefiledPilotWithValidPlannedEnroute_returnsObjectWithExpectedFiledTimeEnroute(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:%d:%d:3:0:EDDW:remark:DCT:0:0:0:0:::::::", hours, minutes);
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeEnroute(), is(equalTo(expectedDuration)));
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidPlannedHoursEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:%s:0:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidPlannedMinutesEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:0:%s:3:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_prefiledPilotWithPlannedHoursEnrouteButWithoutPlannedMinutesEnroute_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:%d::3:0:EDDW:remark:DCT:0:0:0:0:::::::", hours);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_prefiledPilotWithPlannedMinutesEnrouteButWithoutPlannedHoursEnroute_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000::%d:3:0:EDDW:remark:DCT:0:0:0:0:::::::", minutes);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutPlannedEnroute_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:::3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @UseDataProvider("dataProviderHoursAndMinutesAndDuration")
    public void testParse_atcWithValidPlannedEnroute_returnsObjectWithExpectedFiledTimeEnroute(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::%d:%d::::::::::atis message:20180101160000:20180101150000::::", hours, minutes);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeEnroute(), is(equalTo(expectedDuration)));
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_atcWithInvalidPlannedHoursEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::%s:0::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_atcWithInvalidPlannedMinutesEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::0:%s::::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_atcWithPlannedHoursEnrouteButWithoutPlannedMinutesEnroute_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::%d:::::::::::atis message:20180101160000:20180101150000::::", hours);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_atcWithPlannedMinutesEnrouteButWithoutPlannedHoursEnroute_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::%d::::::::::atis message:20180101160000:20180101150000::::", minutes);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutPlannedEnroute_returnsObjectWithNullForFiledTimeEnroute() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeEnroute(), is(nullValue()));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed time fuel">
    @Test
    @UseDataProvider("dataProviderHoursAndMinutesAndDuration")
    public void testParse_connectedPilotWithValidPlannedFuel_returnsObjectWithExpectedFiledTimeFuel(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:%d:%d:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", hours, minutes);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeFuel(), is(equalTo(expectedDuration)));
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidPlannedHoursFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:%s:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidPlannedMinutesFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:0:%s:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_connectedPilotWithPlannedHoursFuelButWithoutPlannedMinutesFuel_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:%d::EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", hours);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_connectedPilotWithPlannedMinutesFuelButWithoutPlannedHoursFuel_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30::%d:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", minutes);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutPlannedFuel_returnsObjectWithNullForFiledTimeFuel() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000::1:30:::EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeFuel(), is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderHoursAndMinutesAndDuration")
    public void testParse_prefiledPilotWithValidPlannedFuel_returnsObjectWithExpectedFiledTimeFuel(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:%d:%d:EDDW:remark:DCT:0:0:0:0:::::::", hours, minutes);
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeFuel(), is(equalTo(expectedDuration)));
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidPlannedHoursFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:%s:0:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidPlannedMinutesFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:0:%s:EDDW:remark:DCT:0:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_prefiledPilotWithPlannedHoursFuelButWithoutPlannedMinutesFuel_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:%d::EDDW:remark:DCT:0:0:0:0:::::::", hours);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_prefiledPilotWithPlannedMinutesFuelButWithoutPlannedHoursFuel_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30::%d:EDDW:remark:DCT:0:0:0:0:::::::", minutes);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutPlannedFuel_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:::EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @UseDataProvider("dataProviderHoursAndMinutesAndDuration")
    public void testParse_atcWithValidPlannedFuel_returnsObjectWithExpectedFiledTimeFuel(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::%d:%d::::::::atis message:20180101160000:20180101150000::::", hours, minutes);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeFuel(), is(equalTo(expectedDuration)));
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_atcWithInvalidPlannedHoursFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::%s:0::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_atcWithInvalidPlannedMinutesFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::0:%s::::::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_atcWithPlannedHoursFuelButWithoutPlannedMinutesFuel_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::%d:::::::::atis message:20180101160000:20180101150000::::", hours);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"0", "1"})
    public void testParse_atcWithPlannedMinutesFuelButWithoutPlannedHoursFuel_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::%d::::::::atis message:20180101160000:20180101150000::::", minutes);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutPlannedFuel_returnsObjectWithNullForFiledTimeFuel() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledTimeFuel(), is(nullValue()));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed alternate airport">
    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_connectedPilot_returnsObjectWithExpectedFiledAlternateAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:%s:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedAirportCode);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledAlternateAirportCode(), is(equalTo(expectedAirportCode)));
    }

    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedFiledAlternateAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:%s:remark:DCT:0:0:0:0:::::::", expectedAirportCode);
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledAlternateAirportCode(), is(equalTo(expectedAirportCode)));
    }

    @Test
    @DataProvider({"", "EDDT", "05S"})
    public void testParse_atc_returnsObjectWithExpectedFiledAlternateAirportCode(String expectedAirportCode) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::::%s:::::::atis message:20180101160000:20180101150000::::", expectedAirportCode);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledAlternateAirportCode(), is(equalTo(expectedAirportCode)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="flight plan remarks">
    @Test
    @DataProvider({"", "my remarks", "+-/;.#!\"%&()=_"})
    public void testParse_connectedPilot_returnsObjectWithExpectedFlightPlanRemarks(String expectedRemarks) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:%s:DCT:0:0:0:0:::20180101094500:270:29.92:1013:", expectedRemarks);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFlightPlanRemarks(), is(equalTo(expectedRemarks)));
    }

    @Test
    @DataProvider({"", "my remarks", "+-/;.#!\"%&()=_"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedFlightPlanRemarks(String expectedRemarks) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:%s:DCT:0:0:0:0:::::::", expectedRemarks);
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFlightPlanRemarks(), is(equalTo(expectedRemarks)));
    }

    @Test
    @DataProvider({"", "my remarks", "+-/;.#!\"%&()=_"})
    public void testParse_atc_returnsObjectWithExpectedFlightPlanRemarks(String expectedRemarks) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::%s::::::atis message:20180101160000:20180101150000::::", expectedRemarks);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFlightPlanRemarks(), is(equalTo(expectedRemarks)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed route">
    @Test
    @DataProvider({"", "DCT", "SID1A/12L WPT UA123 ANASA DCT ENTRY STAR2B/31R", "just special chars +#-.,%\\"})
    public void testParse_connectedPilot_returnsObjectWithExpectedFiledRoute(String expectedRoute) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:%s:0:0:0:0:::20180101094500:270:29.92:1013:", expectedRoute);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledRoute(), is(equalTo(expectedRoute)));
    }

    @Test
    @DataProvider({"", "DCT", "SID1A/12L WPT UA123 ANASA DCT ENTRY STAR2B/31R", "just special chars +#-.,%\\"})
    public void testParse_prefiledPilot_returnsObjectWithExpectedFiledRoute(String expectedRoute) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remarks:%s:0:0:0:0:::::::", expectedRoute);
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledRoute(), is(equalTo(expectedRoute)));
    }

    @Test
    @DataProvider({"", "DCT", "SID1A/12L WPT UA123 ANASA DCT ENTRY STAR2B/31R", "just special chars +#-.,%\\"})
    public void testParse_atc_returnsObjectWithExpectedFiledRoute(String expectedRoute) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::::::%s:::::atis message:20180101160000:20180101150000::::", expectedRoute);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getFiledRoute(), is(equalTo(expectedRoute)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="departure airport latitude">
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_connectedPilotWithDepartureAirportLatitude_returnsObjectWithExpectedDepartureAirportLatitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:%s:0:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLatitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    public void testParse_connectedPilotWithoutDepartureAirportLatitude_returnsObjectWithNaNAsDepartureAirportLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT::0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLatitude(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithDepartureAirportLatitude_returnsObjectWithExpectedDepartureAirportLatitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:%s:0:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLatitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithoutDepartureAirportLatitude_returnsObjectWithNaNAsDepartureAirportLatitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT::0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLatitude(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_atcWithDepartureAirportLatitude_returnsObjectWithExpectedDepartureAirportLatitude(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::%s::::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLatitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    public void testParse_atcWithoutDepartureAirportLatitude_returnsObjectWithNaNAsDepartureAirportLatitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLatitude(), is(equalTo(Double.NaN)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="departure airport longitude">
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_connectedPilotWithDepartureAirportLongitude_returnsObjectWithExpectedDepartureAirportLongitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:%s:0:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLongitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    public void testParse_connectedPilotWithoutDepartureAirportLongitude_returnsObjectWithNaNAsDepartureAirportLongitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0::0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLongitude(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithDepartureAirportLongitude_returnsObjectWithExpectedDepartureAirportLongitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:%s:0:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLongitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithoutDepartureAirportLongitude_returnsObjectWithNaNAsDepartureAirportLongitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0::0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLongitude(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_atcWithDepartureAirportLongitude_returnsObjectWithExpectedDepartureAirportLongitude(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::::::::%s:::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLongitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    public void testParse_atcWithoutDepartureAirportLongitude_returnsObjectWithNaNAsDepartureAirportLongitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDepartureAirportLongitude(), is(equalTo(Double.NaN)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="destination airport latitude">
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_connectedPilotWithDestinationAirportLatitude_returnsObjectWithExpectedDestinationAirportLatitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:%s:0:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLatitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    public void testParse_connectedPilotWithoutDestinationAirportLatitude_returnsObjectWithNaNAsDestinationAirportLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0::0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLatitude(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithDestinationAirportLatitude_returnsObjectWithExpectedDestinationAirportLatitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:%s:0:::::::", input);
        parser.setIsParsingPrefileSection(true);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLatitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithoutDestinationAirportLatitude_returnsObjectWithNaNAsDestinationAirportLatitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0::0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLatitude(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_atcWithDestinationAirportLatitude_returnsObjectWithExpectedDestinationAirportLatitude(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::%s::atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLatitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    public void testParse_atcWithoutDestinationAirportLatitude_returnsObjectWithNaNAsDestinationAirportLatitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLatitude(), is(equalTo(Double.NaN)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="destination airport longitude">
    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_connectedPilotWithDestinationAirportLongitude_returnsObjectWithExpectedDestinationAirportLongitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:%s:::20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLongitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    public void testParse_connectedPilotWithoutDestinationAirportLongitude_returnsObjectWithNaNAsDestinationAirportLongitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0::::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLongitude(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithDestinationAirportLongitude_returnsObjectWithExpectedDestinationAirportLongitude(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:%s:::::::", input);
        parser.setIsParsingPrefileSection(true);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLongitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_prefiledPilotWithoutDestinationAirportLongitude_returnsObjectWithNaNAsDestinationAirportLongitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0::::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLongitude(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"12.34567", "-80.23456", "0", "1", "-0", "-1.234e-5", "9999", "-9999"})
    public void testParse_atcWithDestinationAirportLongitude_returnsObjectWithExpectedDestinationAirportLongitude(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::::::::::%s:atis message:20180101160000:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLongitude(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    public void testParse_atcWithoutDestinationAirportLongitude_returnsObjectWithNaNAsDestinationAirportLongitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getDestinationAirportLongitude(), is(equalTo(Double.NaN)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="controller message">
    @Test
    public void testParse_connectedPilotWithControllerMessage_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:controller message::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutControllerMessage_returnsObjectWithEmptyControllerMessage() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessage(), is(emptyString()));
    }

    @Test
    public void testParse_prefiledPilotWithControllerMessage_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:controller message::::::";
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutControllerMessage_returnsObjectWithEmptyControllerMessage() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessage(), is(emptyString()));
    }

    @Test
    @UseDataProvider("dataProviderControllerMessageRawAndDecoded")
    public void testParse_atcWithControllerMessage_returnsObjectWithExpectedControllerMessage(String rawMessage, String expectedMessage) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::%s:20180101160000:20180101150000::::", rawMessage);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessage(), is(equalTo(expectedMessage)));
    }

    @Test
    public void testParse_atcWithoutControllerMessage_returnsObjectWithEmptyControllerMessage() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50:::::::::::::::::20180101160000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessage(), is(emptyString()));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="controller message last updated">
    @Test
    @UseDataProvider("dataProviderFullTimestampStringAndObject")
    public void testParse_connectedPilotWithValidLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated(String input, Instant _instant) {
        // server appears to randomly assign some ATIS timestamp to pilots in format 9...

        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::%s:20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert (nothing to do)
        assertThat(result.getControllerMessageLastUpdated(), is(nullValue()));
    }

    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidLastAtisReceived_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::%s:20180101094500:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithDummyLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::00010101000000:20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessageLastUpdated(), is(nullValue()));
    }

    @Test
    public void testParse_connectedPilotWithoutLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessageLastUpdated(), is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderFullTimestampStringAndObject")
    public void testParse_prefiledPilotWithValidLastAtisReceived_throwsIllegalArgumentException(String input, Instant _instant) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::%s:::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidLastAtisReceived_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::%s:::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithDummyLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::00010101000000:::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessageLastUpdated(), is(nullValue()));
    }

    @Test
    public void testParse_prefiledPilotWithoutLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessageLastUpdated(), is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderFullTimestampStringAndObject")
    public void testParse_atcWithValidLastAtisReceived_returnsObjectWithExpectedControllerMessageLastUpdated(String input, Instant expectedInstant) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:%s:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessageLastUpdated(), is(equalTo(expectedInstant)));
    }

    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidLastAtisReceived_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:%s:20180101150000::::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithDummyLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:00010101000000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessageLastUpdated(), is(nullValue()));
    }

    @Test
    public void testParse_atcWithoutLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message::20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getControllerMessageLastUpdated(), is(nullValue()));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="logon time">
    @Test
    @UseDataProvider("dataProviderFullTimestampStringAndObject")
    public void testParse_connectedPilotWithValidLogonTime_throwsIllegalArgumentException(String input, Instant expectedInstant) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::%s:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getLogonTime(), is(equalTo(expectedInstant)));
    }

    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidLogonTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::%s:270:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutLogonTime_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::::270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @UseDataProvider("dataProviderFullTimestampStringAndObject")
    public void testParse_prefiledPilotWithValidLogonTime_throwsIllegalArgumentException(String input, Instant _instant) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::%s::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidLogonTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::%s::::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutLogonTime_returnsObjectWithNullForLogonTime() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getLogonTime(), is(nullValue()));
    }

    @Test
    @UseDataProvider("dataProviderFullTimestampStringAndObject")
    public void testParse_atcWithValidLogonTime_returnsObjectWithExpectedLogonTime(String input, Instant expectedInstant) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:20180101160000:%s::::", input);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getLogonTime(), is(equalTo(expectedInstant)));
    }

    @Test
    @DataProvider({"-123", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidLogonTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:20180101160000:%s::::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutLastAtisReceived_throwsIllegalArgumentException() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:20180101160000:::::";

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="heading">
    @Test
    @DataProvider({"0", "123", "359"})
    public void testParse_connectedPilotWithValidRegularHeading_returnsObjectWithExpectedHeading(int expectedHeading) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:%d:29.92:1013:", expectedHeading);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getHeading(), is(equalTo(expectedHeading)));
    }

    @Test
    public void testParse_connectedPilotWithValid360Heading_returnsObjectWithZeroHeading() {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:360:29.92:1013:");
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getHeading(), is(equalTo(0)));
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "361", "1080"})
    public void testParse_connectedPilotWithInvalidHeading_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:%s:29.92:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutHeading_returnsObjectWithNegativeHeading() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500::29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getHeading(), is(lessThan(0)));
    }

    @Test
    @DataProvider({"1", "123", "359", "360"})
    public void testParse_prefiledPilotWithValidNonZeroHeading_throwsIllegalArgumentException(int heading) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::::%d:::", heading);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "361", "1080"})
    public void testParse_prefiledPilotWithInvalidHeading_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::::%s:::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutHeading_returnsObjectWithNegativeHeading() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getHeading(), is(lessThan(0)));
    }

    @Test
    @DataProvider({"1", "123", "359", "360"})
    public void testParse_atcWithValidNonZeroHeading_throwsIllegalArgumentException(int heading) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:%d:::", heading);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-1", "abc", "1a", "a1", "361", "1080"})
    public void testParse_atcWithInvalidHeading_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:%s:::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutHeading_returnsObjectWithNegativeHeading() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getHeading(), is(lessThan(0)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="QNH Inch Mercury">
    @Test
    @DataProvider({"29.92", "2.992e02", "30", "27.9", "-1", "-29.92", "-2.992e02"})
    public void testParse_connectedPilotWithValidQnhIHg_returnsObjectWithExpectedQnhInchMercury(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:%s:1013:", input);
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getQnhInchMercury(), is(closeTo(expectedOutput, ALLOWED_DOUBLE_ERROR)));
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidQnhIHg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:%s:1013:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutQnhIHg_returnsObjectWithNaNAsQnhInchMercury() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270::1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getQnhInchMercury(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"29.92", "2.992e02", "30", "27.9", "-1", "-29.92", "-2.992e02"})
    public void testParse_prefiledPilotWithValidQnhIhg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::%s::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidQnhIHg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::%s::", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutQnhIHg_returnsObjectWithNaNAsQnhInchMercury() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getQnhInchMercury(), is(equalTo(Double.NaN)));
    }

    @Test
    @DataProvider({"29.92", "2.992e02", "30", "27.9", "-1", "-29.92", "-2.992e02"})
    public void testParse_atcWithValidQnhIHg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::%s::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"abc", "1a", "a1"})
    public void testParse_atcWithInvalidQnhIHg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::%s::", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutQnhIHg_returnsObjectWithNaNAsQnhInchMercury() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getQnhInchMercury(), is(equalTo(Double.NaN)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="QNH Hectopascal">
    @Test
    @DataProvider({"997", "1013", "1030"})
    public void testParse_connectedPilotWithValidQnhMB_returnsObjectWithExpectedQnhHectopascal(int expectedQnh) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:%d:", expectedQnh);
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getQnhHectopascal(), is(equalTo(expectedQnh)));
    }

    @Test
    @DataProvider({"-1", "29.92", "1e03", "abc", "1a", "a1"})
    public void testParse_connectedPilotWithInvalidQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:%s:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_connectedPilotWithoutQnhMB_returnsObjectWithNegativeQnhHectopascal() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getQnhHectopascal(), is(lessThan(0)));
    }

    @Test
    @DataProvider({"997", "1013", "1030"})
    public void testParse_prefiledPilotWithValidQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::::::%s:", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-1", "29.92", "1e03", "abc", "1a", "a1"})
    public void testParse_prefiledPilotWithInvalidQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::::::%s:", input);
        parser.setIsParsingPrefileSection(true);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_prefiledPilotWithoutQnhMB_returnsObjectWithNegativeQnhHectopascal() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getQnhHectopascal(), is(lessThan(0)));
    }

    @Test
    @DataProvider({"997", "1013", "1030"})
    public void testParse_atcWithValidQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:::%s:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-1", "29.92", "1e03", "abc", "1a", "a1"})
    public void testParse_atcWithInvalidQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format("EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:::%s:", input);
        parser.setIsParsingPrefileSection(false);

        thrown.expect(IllegalArgumentException.class);

        // Act
        parser.parse(line);

        // Assert (nothing to do)
    }

    @Test
    public void testParse_atcWithoutQnhMB_returnsObjectWithNegativeQnhHectopascal() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getQnhHectopascal(), is(lessThan(0)));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="special: client type detection/tolerance">
    @Test
    public void testParse_ghostWithPositionHeadingQNHTransponderLogonTimeAndServerIdInOnlineSection_returnsObjectAsEffectiveConnectedPilotWithExpectedData() {
        // Occurence on 12 Oct 2017 (with server ID):
        // Client was briefly connected earlier with incomplete data, then
        // appears with a new login time and full position, heading, QNH and
        // transponder information but without VATSIM ID or server protocol.
        // VATSIM Statistics Center does not list pilot as online from
        // beginning of ghost login time on data file.
        // Possible cause: Left-over information after simulator crash on
        // sim startup?

        // Arrange
        String line = "ANY123:::::10.12345:-20.54321:80:0::0::::SOME_SERVER::1:1200::::::::::::::::::::20171012123456:120:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        Instant expectedLogonTime = LocalDateTime.of(2017, Month.OCTOBER, 12, 12, 34, 56).toInstant(ZoneOffset.UTC);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawClientType(), is(nullValue()));
        assertThat(result.getEffectiveClientType(), is(equalTo(ClientType.PILOT_CONNECTED)));
        assertThat(result.getCallsign(), is(equalTo("ANY123")));
        assertThat(result.getLatitude(), is(closeTo(10.12345, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getLongitude(), is(closeTo(-20.54321, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getAltitudeFeet(), is(equalTo(80)));
        assertThat(result.getGroundSpeed(), is(equalTo(0)));
        assertThat(result.getServerId(), is(equalTo("SOME_SERVER")));
        assertThat(result.getControllerRating(), is(equalTo(ControllerRating.OBS)));
        assertThat(result.getTransponderCodeDecimal(), is(equalTo(1200)));
        assertThat(result.getLogonTime(), is(equalTo(expectedLogonTime)));
        assertThat(result.getHeading(), is(equalTo(120)));
        assertThat(result.getQnhInchMercury(), is(closeTo(29.92, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getQnhHectopascal(), is(equalTo(1013)));
    }

    @Test
    public void testParse_ghostWithPositionHeadingQNHTransponderLogonTimeButWithoutServerIdInOnlineSection_returnsObjectAsEffectiveConnectedPilotWithExpectedData() {
        // Occurence on 21 Oct 2017 (without server ID):
        // Client had correct data one retrieval later. It appears like this
        // incomplete data might have been captured while the client had not
        // finished connecting?

        // Arrange
        String line = "ANY123:::::10.12345:-20.54321:80:0::0::::::1:1200::::::::::::::::::::20171021123456:120:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        Instant expectedLogonTime = LocalDateTime.of(2017, Month.OCTOBER, 21, 12, 34, 56).toInstant(ZoneOffset.UTC);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawClientType(), is(nullValue()));
        assertThat(result.getEffectiveClientType(), is(equalTo(ClientType.PILOT_CONNECTED)));
        assertThat(result.getCallsign(), is(equalTo("ANY123")));
        assertThat(result.getLatitude(), is(closeTo(10.12345, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getLongitude(), is(closeTo(-20.54321, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getAltitudeFeet(), is(equalTo(80)));
        assertThat(result.getGroundSpeed(), is(equalTo(0)));
        assertThat(result.getServerId(), is(nullValue()));
        assertThat(result.getControllerRating(), is(equalTo(ControllerRating.OBS)));
        assertThat(result.getTransponderCodeDecimal(), is(equalTo(1200)));
        assertThat(result.getLogonTime(), is(equalTo(expectedLogonTime)));
        assertThat(result.getHeading(), is(equalTo(120)));
        assertThat(result.getQnhInchMercury(), is(closeTo(29.92, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getQnhHectopascal(), is(equalTo(1013)));
    }

    @Test
    public void testParse_atcWithHeadingQNHAndTransponderInOnlineSectionOnPlaceholderFrequency_returnsObjectAsEffectiveConnectedPilotWithExpectedData() {
        // Seen in datafiles from 6 Oct 2017:
        // Observer on placeholder frequency but actually moving (GS > 0) so
        // supposedly the observer was an actual online pilot.

        // Arrange
        String line = "ANY123:987654321:some name:ATC:199.998:12.34567:-12.34567:123:1::0::::SOMESERVER:100:1:1200:0:40::::::::::::::::::20171006123456:180:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        Instant expectedLogonTime = LocalDateTime.of(2017, Month.OCTOBER, 6, 12, 34, 56).toInstant(ZoneOffset.UTC);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawClientType(), is(equalTo(ClientType.ATC_CONNECTED)));
        assertThat(result.getEffectiveClientType(), is(equalTo(ClientType.PILOT_CONNECTED)));
        assertThat(result.getCallsign(), is(equalTo("ANY123")));
        assertThat(result.getLatitude(), is(closeTo(12.34567, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getLongitude(), is(closeTo(-12.34567, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getAltitudeFeet(), is(equalTo(123)));
        assertThat(result.getGroundSpeed(), is(equalTo(1)));
        assertThat(result.getServerId(), is(equalTo("SOMESERVER")));
        assertThat(result.getControllerRating(), is(equalTo(ControllerRating.OBS)));
        assertThat(result.getTransponderCodeDecimal(), is(equalTo(1200)));
        assertThat(result.getLogonTime(), is(equalTo(expectedLogonTime)));
        assertThat(result.getHeading(), is(equalTo(180)));
        assertThat(result.getQnhInchMercury(), is(closeTo(29.92, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getQnhHectopascal(), is(equalTo(1013)));
    }

    @Test
    public void testParse_atcWithZeroHeadingQNHGroundSpeedAndTransponderInOnlineSectionOnNonPlaceholderFrequency_returnsObjectAsEffectiveATCWithExpectedData() {
        // Default in data format version 9:
        // ATC appears like a pilot with heading, QNH and transponder fields set to zero

        // Arrange
        String line = "SOME_ATC:987654321:some name:ATC:124.525:12.34567:-12.34567:123:0::0::::SOMESERVER:100:10:0:0:40::::::::::::::::::20171006123456:0:0:0:";
        parser.setIsParsingPrefileSection(false);

        Instant expectedLogonTime = LocalDateTime.of(2017, Month.OCTOBER, 6, 12, 34, 56).toInstant(ZoneOffset.UTC);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result.getRawClientType(), is(equalTo(ClientType.ATC_CONNECTED)));
        assertThat(result.getEffectiveClientType(), is(equalTo(ClientType.ATC_CONNECTED)));
        assertThat(result.getCallsign(), is(equalTo("SOME_ATC")));
        assertThat(result.getLatitude(), is(closeTo(12.34567, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getLongitude(), is(closeTo(-12.34567, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getAltitudeFeet(), is(equalTo(123)));
        assertThat(result.getGroundSpeed(), is(equalTo(-1)));
        assertThat(result.getServerId(), is(equalTo("SOMESERVER")));
        assertThat(result.getControllerRating(), is(equalTo(ControllerRating.I3)));
        assertThat(result.getTransponderCodeDecimal(), is(equalTo(0)));
        assertThat(result.getLogonTime(), is(equalTo(expectedLogonTime)));
        assertThat(result.getHeading(), is(equalTo(0)));
        assertThat(result.getQnhInchMercury(), is(closeTo(0, ALLOWED_DOUBLE_ERROR)));
        assertThat(result.getQnhHectopascal(), is(equalTo(0)));
        assertThat(result.getServedFrequencyKilohertz(), is(equalTo(124525)));
    }
    // </editor-fold>
}
