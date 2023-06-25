package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
import static org.assertj.core.api.InstanceOfAssertFactories.INTEGER;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating;
import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRatingTest;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityTypeTest;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;

class ClientParserTest {

    private ClientParser parser;

    private static final Offset<Double> ALLOWED_DOUBLE_ERROR = Offset.offset(0.000001);

    private static final String CONTROLLER_MESSAGE_LINEBREAK = new String(
        new byte[]{(byte) 0x5E, (byte) 0xA7},
        StandardCharsets.ISO_8859_1
    );

    static Stream<Arguments> dataProviderControllerRatingIdAndEnumWithoutOBS() {
        return ControllerRatingTest.dataProviderIdAndEnum()
                                   .filter(args -> args.get()[1] != ControllerRating.OBS);
    }

    static Stream<Arguments> dataProviderHoursAndMinutesAndDuration() {
        return Stream.of(
            Arguments.of(0, 0, Duration.ofMinutes(0)),
            Arguments.of(0, 1, Duration.ofMinutes(1)),
            Arguments.of(1, 0, Duration.ofHours(1)),
            Arguments.of(2, 59, Duration.ofMinutes(179)),
            Arguments.of(2, 60, Duration.ofMinutes(180)), // excessive minutes (>59) are valid
            Arguments.of(13, 7, Duration.ofMinutes(787)),
            Arguments.of(0, 787, Duration.ofMinutes(787)), // excessive minutes (>59) are valid

            // negative values are (unfortunately) also... valid :/
            Arguments.of(-8, 0, Duration.ofHours(-8)),
            Arguments.of(0, -2, Duration.ofMinutes(-2)),
            Arguments.of(-8, -2, Duration.ofMinutes(-482)), //

            // Since negative values don't make any sense we need to make sure
            // that such input does not mix up when using different signs per
            // hour/minute number. We expect result to remain negative in those
            // cases.
            Arguments.of(1, -60, Duration.ofMinutes(-120)),
            Arguments.of(-1, 60, Duration.ofMinutes(-120))
        );
    }

    static Stream<Arguments> dataProviderControllerMessageRawAndDecoded() {
        // FIXME: charset detection & decoding, Russian controllers send windows-1251 encapsulated in UTF-8

        return Stream.of(
            Arguments.of(
                "simple one-liner with /-.$,#\\ special characters",
                "simple one-liner with /-.$,#\\ special characters"
            ),
            Arguments.of(
                ":colons : :: are:valid::",
                ":colons : :: are:valid::"
            ),
            Arguments.of(
                "first line" + CONTROLLER_MESSAGE_LINEBREAK + "second line" + CONTROLLER_MESSAGE_LINEBREAK,
                "first line\nsecond line\n"
            )
        );
    }

    static Stream<Arguments> dataProviderFullTimestampStringAndObject() {
        return Stream.of(
            Arguments.of(
                "20171014170050",
                LocalDateTime.of(2017, 10, 14, 17, 0, 50)
                             .toInstant(ZoneOffset.UTC)
            ),
            Arguments.of(
                "20180101000000",
                LocalDateTime.of(2018, 1, 1, 0, 0, 0)
                             .toInstant(ZoneOffset.UTC)
            )
        );
    }

    @BeforeEach
    public void setUp() {
        parser = new ClientParser();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSetIsParsingPrefileSection_anyFlag_returnsSameParserInstance(boolean flag) {
        // Arrange (nothing to do)

        // Act
        ClientParser result = parser.setIsParsingPrefileSection(flag);

        // Assert
        assertThat(result).isSameAs(parser);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW::DCT:::::::201801010945:270:29.92:1013",
        "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW::DCT:::::::201801010945:270:29.92:1013:1:",
        "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW::DCT:::::::201801010945:270:29.92:1013:a:"
    })
    void testParse_genericFormatViolation_throwsIllegalArgumentException(String erroneousLine) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    // <editor-fold defaultstate="collapsed" desc="callsign">
    @ParameterizedTest
    @ValueSource(strings = {"ABC123", "DABCD", "N123A"})
    void testParse_connectedPilotWithCallsign_returnsObjectWithExpectedCallsign(String expectedCallsign) {
        // Arrange
        String line = String.format(
            "%s:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedCallsign
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getCallsign)
                          .isEqualTo(expectedCallsign);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC123", "DABCD", "N123A"})
    void testParse_prefiledPilotWithCallsign_returnsObjectWithExpectedCallsign(String expectedCallsign) {
        // Arrange
        String line = String.format(
            "%s:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedCallsign
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getCallsign)
                          .isEqualTo(expectedCallsign);
    }

    @ParameterizedTest
    @ValueSource(strings = {"EDDT_TWR", "LOWI_GND"})
    void testParse_atcWithCallsign_returnsObjectWithExpectedCallsign(String expectedCallsign) {
        // Arrange
        String line = String.format(
            "%s:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedCallsign
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getCallsign)
                          .isEqualTo(expectedCallsign);
    }

    @Test
    void testParse_connectedPilotWithoutCallsign_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutCallsign_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutCallsign_throwsIllegalArgumentException() {
        // Arrange
        String erroneousLine = ":123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Vatsim ID">
    @ParameterizedTest
    @ValueSource(ints = {123456, 987654321})
    void testParse_connectedPilotWithCID_returnsObjectWithExpectedVatsimID(int expectedVatsimID) {
        // Arrange
        String line = String.format(
            "ABC123:%d:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedVatsimID //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVatsimID)
                          .isEqualTo(expectedVatsimID);
    }

    @ParameterizedTest
    @ValueSource(ints = {123456, 987654321})
    void testParse_prefiledPilotWithCID_returnsObjectWithExpectedVatsimID(int expectedVatsimID) {
        // Arrange
        String line = String.format(
            "ABC123:%d:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedVatsimID //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVatsimID)
                          .isEqualTo(expectedVatsimID);
    }

    @ParameterizedTest
    @ValueSource(ints = {123456, 987654321})
    void testParse_atcWithCID_returnsObjectWithExpectedVatsimID(int expectedVatsimID) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:%d:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedVatsimID //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVatsimID)
                          .isEqualTo(expectedVatsimID);
    }

    @Test
    void testParse_connectedPilotWithoutCID_returnsObjectWithNegativeVatsimID() {
        // Arrange
        String line = "ABC123::realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVatsimID, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_prefiledPilotWithoutCID_returnsObjectWithNegativeVatsimID() {
        // Arrange
        String line = "ABC123::realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVatsimID, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_atcWithoutCID_returnsObjectWithNegativeVatsimID() {
        // Arrange
        String line = "EDDT_TWR::realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVatsimID, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidCID_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format(
            "ABC123:%s:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            invalidInput //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidCID_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format(
            "ABC123:%s:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            invalidInput //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_atcWithInvalidCID_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format(
            "ABC123:%s:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            invalidInput //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="real name">
    @ParameterizedTest
    @ValueSource(strings = {"", "A Name", "Name", "Name ESSA", "A Full Name ESSA"})
    void testParse_connectedPilot_returnsObjectWithExpectedRealName(String expectedRealName) {
        // Arrange
        String line = String.format(
            "ABC123:123456:%s:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedRealName //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRealName)
                          .isEqualTo(expectedRealName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "A Name", "Name", "Name ESSA", "A Full Name ESSA"})
    void testParse_prefiledPilot_returnsObjectWithExpectedRealName(String expectedRealName) {
        // Arrange
        String line = String.format(
            "ABC123:123456:%s:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedRealName //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRealName)
                          .isEqualTo(expectedRealName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Some Name", "Name", "Name ESSA", "A Full Name ESSA"})
    void testParse_atc_returnsObjectWithExpectedRealName(String expectedRealName) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:%s:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedRealName //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRealName)
                          .isEqualTo(expectedRealName);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="client type">
    @Test
    void testParse_connectedPilotWithClientTypePilot_returnsObjectWithRawClientTypePilotConnected() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawClientType)
                          .isSameAs(ClientType.PILOT_CONNECTED);
    }

    @Test
    void testParse_prefiledPilotWithoutClientType_returnsObjectWithRawClientTypePilotPrefiled() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawClientType)
                          .isSameAs(ClientType.PILOT_PREFILED);
    }

    @Test
    void testParse_atcWithClientTypeATC_returnsObjectWithRawClientTypeATCConnected() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawClientType)
                          .isSameAs(ClientType.ATC_CONNECTED);
    }

    // TODO: check for empty client type outside prefile section, should be able to distinguish ATC and PILOT_CONNECTED

    /*
     * @Test public void
     * testParse_missingClientTypeOutsidePrefiledSection_throwsIllegalArgumentException
     * () { // Arrange String erroneousLine =
     * "ABC123:123456:realname:::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
     * parser.setIsParsingPrefileSection(false);
     *
     * thrown.expect(IllegalArgumentException.class);
     *
     * // Act parser.parse(erroneousLine);
     *
     * // Assert (nothing to do) }
     *
     * @Test
     *
     * @DataProvider({"ATC", "PILOT"}) public void
     * testParse_clientTypeInPrefiledSection_throwsIllegalArgumentException(String
     * inputClientType) { // Arrange String erroneousLine = String.format(
     * "ABC123:123456:realname:%s::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
     * inputClientType); parser.setIsParsingPrefileSection(true);
     *
     * thrown.expect(IllegalArgumentException.class);
     *
     * // Act parser.parse(erroneousLine);
     *
     * // Assert (nothing to do) }
     */

    @ParameterizedTest
    @CsvSource({
        "A, true",
        "A, false"
    })
    void testParse_invalidClientType_throwsIllegalArgumentException(String inputClientType, boolean isParsingPrefileSection) {
        // Arrange
        String erroneousLine = String.format(
            "ABC123:123456:realname:%s::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            inputClientType //
        );
        parser.setIsParsingPrefileSection(isParsingPrefileSection);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="frequency">
    @Test
    void testParse_connectedPilotWithoutFrequency_returnsObjectWithNegativeServedFrequency() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getServedFrequencyKilohertz, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(strings = {"121.750", "198.999"})
    void testParse_connectedPilotWithNonPlaceholderFrequency_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT:%s:12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "199.000, 199000",
        "199.998, 199998"
    })
    void testParse_connectedPilotWithPlaceholderFrequency_throwsIllegalArgumentException(String input, int expectedFrequencyKilohertz) {
        // This has not actually been seen in the wild but ATC clients may be
        // interpreted as effectively pilots so the test fit in here.
        // Placeholder frequencies in general should be allowed, just not active
        // frequencies.

        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT:%s:12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getServedFrequencyKilohertz)
                          .isEqualTo(expectedFrequencyKilohertz);
    }

    @Test
    void testParse_prefiledPilotWithoutFrequency_returnsObjectWithNegativeServedFrequency() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getServedFrequencyKilohertz, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_prefiledPilotWithFrequency_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname::121.750:::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "118.500, 118500",
        "118.50, 118500",
        "118.5, 118500",
        "121.725, 121725",
        "1.21725e2, 121725",
        "199.998, 199998",
        "100.0001, 100000",
        "99.9999, 100000"
    })
    void testParse_atcWithValidFrequency_returnsObjectWithExpectedServedFrequency(String input, int expectedFrequencyKilohertz) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:%s:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getServedFrequencyKilohertz)
                          .isEqualTo(expectedFrequencyKilohertz);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "0000", "0.000", "1e-10"})
    void testParse_atcWithInvalidFrequency_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:%s:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutFrequency_returnsObjectWithNegativeServedFrequency() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC::12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getServedFrequencyKilohertz, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="latitude">
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_connectedPilotWithLatitude_returnsObjectWithExpectedLatitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::%s:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLatitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @Test
    void testParse_connectedPilotWithoutLatitude_returnsObjectWithNaNAsLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT:::12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLatitude, as(DOUBLE))
                          .isNaN();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithNonZeroLatitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::%s::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithZeroLatitude_returnsObjectWithNaNAsLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:::0::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLatitude, as(DOUBLE))
                          .isNaN();
    }

    @Test
    void testParse_prefiledPilotWithoutLatitude_returnsObjectWithNaNAsLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLatitude, as(DOUBLE))
                          .isNaN();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_atcWithLatitude_returnsObjectWithExpectedLatitude(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:%s:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLatitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @Test
    void testParse_atcWithoutLatitude_returnsObjectWithNaNAsLatitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500::12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLatitude, as(DOUBLE))
                          .isNaN();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="longitude">
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_connectedPilotWithLongitude_returnsObjectWithExpectedLongitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:%s:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLongitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @Test
    void testParse_connectedPilotWithoutLongitude_returnsObjectWithNaNAsLongitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567::12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLongitude, as(DOUBLE))
                          .isNaN();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithNonZeroLongitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname::::%s:::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithZeroLongitude_returnsObjectWithNaNAsLongitude() {
        // Arrange
        String line = "ABC123:123456:realname::::0:::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLongitude, as(DOUBLE))
                          .isNaN();
    }

    @Test
    void testParse_prefiledPilotWithoutLongitude_returnsObjectWithNaNAsLongitude() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLongitude, as(DOUBLE))
                          .isNaN();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_atcWithLongitude_returnsObjectWithExpectedLongitude(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:%s:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLongitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @Test
    void testParse_atcWithoutLongitude_returnsObjectWithNaNAsLongitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567::0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLongitude, as(DOUBLE))
                          .isNaN();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="altitude">
    @ParameterizedTest
    @ValueSource(ints = {0, 100000, -5000})
    void testParse_connectedPilotWithValidAltitude_returnsObjectWithExpectedAltitude(int expectedAltitude) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:%d:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedAltitude //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAltitudeFeet)
                          .isEqualTo(expectedAltitude);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidAltitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:%s:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutAltitude_returnsObjectWithZeroAltitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567::123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAltitudeFeet)
                          .isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {100000, -5000})
    void testParse_prefiledPilotWithNonZeroAltitude_throwsIllegalArgumentException(int altitude) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::%d::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            altitude //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidAltitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::%s::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutAltitude_returnsObjectWithZeroAltitude() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAltitudeFeet)
                          .isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100000, -5000})
    void testParse_atcWithValidAltitude_returnsObjectWithExpectedAltitude(int expectedAltitude) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:%d:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedAltitude //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAltitudeFeet)
                          .isEqualTo(expectedAltitude);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_atcWithInvalidAltitude_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:%s:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutAltitude_returnsObjectWithZeroAltitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAltitudeFeet)
                          .isEqualTo(0);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ground speed">
    @ParameterizedTest
    @ValueSource(ints = {0, 422})
    void testParse_connectedPilotWithValidGroundSpeed_returnsObjectWithExpectedGroundSpeed(int expectedGroundSpeed) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:%d:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedGroundSpeed //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getGroundSpeed)
                          .isEqualTo(expectedGroundSpeed);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidGroundSpeed_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:%s:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345::B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getGroundSpeed, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_prefiledPilotWithZeroGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "ABC123:123456:realname::::::0:B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getGroundSpeed, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {15, 321})
    void testParse_prefiledPilotWithNonZeroGroundSpeed_throwsIllegalArgumentException(int groundSpeed) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname::::::%d:B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            groundSpeed //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidGroundSpeed_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname::::::%s:B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getGroundSpeed, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_atcWithZeroGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:0::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getGroundSpeed, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {15, 321})
    void testParse_atcWithNonZeroGroundSpeed_throwsIllegalArgumentException(int groundSpeed) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:%d::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            groundSpeed //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_atcWithInvalidGroundSpeed_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:%s::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutGroundSpeed_returnsObjectWithNegativeGroundSpeed() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getGroundSpeed, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="aircraft type">
    @ParameterizedTest
    @ValueSource(strings = {"", "B738/M", "H/A332/X", "DH8D"})
    void testParse_connectedPilot_returnsObjectWithExpectedAircraftType(String expectedAircraftType) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:%s:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedAircraftType //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAircraftType)
                          .isEqualTo(expectedAircraftType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "B738/M", "H/A332/X", "DH8D"})
    void testParse_prefiledPilot_returnsObjectWithExpectedAircraftType(String expectedAircraftType) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::%s:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedAircraftType //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAircraftType)
                          .isEqualTo(expectedAircraftType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "B738/M", "H/A332/X", "DH8D"})
    void testParse_atc_returnsObjectWithExpectedAircraftType(String expectedAircraftType) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0::%s:0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedAircraftType //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAircraftType)
                          .isEqualTo(expectedAircraftType);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="planned TAS cruise">
    @ParameterizedTest
    @ValueSource(ints = {0, 90, 420})
    void testParse_connectedPilotWithPlannedTASCruise_returnsObjectWithExpectedFiledTrueAirSpeed(int expectedTAS) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:%d:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedTAS //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTrueAirSpeed)
                          .isEqualTo(expectedTAS);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 90, 420})
    void testParse_prefiledPilotWithPlannedTASCruise_returnsObjectWithExpectedFiledTrueAirSpeed(int expectedTAS) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:%d:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedTAS //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTrueAirSpeed)
                          .isEqualTo(expectedTAS);
    }

    @ParameterizedTest
    @ValueSource(ints = {123456, 987654321})
    void testParse_atcWithPlannedTASCruise_returnsObjectWithExpectedFiledTrueAirSpeed(int expectedTAS) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::%d::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedTAS //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTrueAirSpeed)
                          .isEqualTo(expectedTAS);
    }

    @Test
    void testParse_connectedPilotWithoutPlannedTASCruise_returnsObjectWithZeroFiledTrueAirSpeed() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738::EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTrueAirSpeed)
                          .isEqualTo(0);
    }

    @Test
    void testParse_prefiledPilotWithoutPlannedTASCruise_returnsObjectWithZeroFiledTrueAirSpeed() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738::EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTrueAirSpeed)
                          .isEqualTo(0);
    }

    @Test
    void testParse_atcWithoutPlannedTASCruise_returnsObjectWithZeroFiledTrueAirSpeed() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTrueAirSpeed)
                          .isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidPlannedTASCruise_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:%s:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            invalidInput //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidPlannedTASCruise_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format(
            "ABC123:123456:realname:::::::B738:%s:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            invalidInput //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_atcWithInvalidPlannedTASCruise_throwsIllegalArgumentException(String invalidInput) {
        // Arrange
        String erroneousLine = String.format(
            "ABC123:123456:realname:ATC:118.500:12.34567:12.34567:0:::%s::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            invalidInput //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(erroneousLine);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed departure airport">
    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_connectedPilot_returnsObjectWithExpectedFiledDepartureAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:%s:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledDepartureAirportCode)
                          .isEqualTo(expectedAirportCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_prefiledPilot_returnsObjectWithExpectedFiledDepartureAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:%s:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledDepartureAirportCode)
                          .isEqualTo(expectedAirportCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_atc_returnsObjectWithExpectedFiledDepartureAirportCode(String expectedAirportCode) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0:%s:::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledDepartureAirportCode)
                          .isEqualTo(expectedAirportCode);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed altitude">
    @ParameterizedTest
    @ValueSource(strings = {"", "30000", "FL300", "F300", "0", "F", "F 300"})
    void testParse_connectedPilot_returnsObjectWithExpectedRawFiledAltitude(String expectedRawFiledAltitude) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:%s:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedRawFiledAltitude //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawFiledAltitude)
                          .isEqualTo(expectedRawFiledAltitude);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "30000", "FL300", "F300", "0", "F", "F 300"})
    void testParse_prefiledPilot_returnsObjectWithExpectedRawFiledAltitude(String expectedRawFiledAltitude) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:%s:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedRawFiledAltitude //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawFiledAltitude)
                          .isEqualTo(expectedRawFiledAltitude);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "30000", "FL300", "F300", "0", "F", "F 300"})
    void testParse_atc_returnsObjectWithExpectedRawFiledAltitude(String expectedRawFiledAltitude) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::%s::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedRawFiledAltitude //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawFiledAltitude)
                          .isEqualTo(expectedRawFiledAltitude);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed destination airport">
    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_connectedPilot_returnsObjectWithExpectedFiledDestinationAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:%s:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledDestinationAirportCode)
                          .isEqualTo(expectedAirportCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_prefiledPilot_returnsObjectWithExpectedFiledDestinationAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:%s:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledDestinationAirportCode)
                          .isEqualTo(expectedAirportCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_atc_returnsObjectWithExpectedFiledDestinationAirportCode(String expectedAirportCode) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0:::%s:SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledDestinationAirportCode)
                          .isEqualTo(expectedAirportCode);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="server ID">
    @ParameterizedTest
    @ValueSource(strings = {"SERVER 1", "some-other-server"})
    void testParse_connectedPilotWithServerId_returnsObjectWithExpectedServerId(String expectedServerId) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:%s:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedServerId //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getServerId)
                          .isEqualTo(expectedServerId);
    }

    @Test
    void testParse_connectedPilotWithoutServerId_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM::1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SERVER 1", "some-other-server"})
    void testParse_prefiledPilotWithServerId_throwsIllegalArgumentException(String serverId) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:%s::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            serverId //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutServerId_returnsObjectWithNullForServerId() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getServerId)
                          .isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SERVER 1", "some-other-server"})
    void testParse_atcWithServerId_returnsObjectWithExpectedServerId(String expectedServerId) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::%s:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedServerId //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getServerId)
                          .isEqualTo(expectedServerId);
    }

    @Test
    void testParse_atcWithoutServerId_throwsIllegalArgumentException() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0:::::100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="protocol version">
    @ParameterizedTest
    @ValueSource(ints = {0, 10, 100})
    void testParse_connectedPilotWithValidProtocolRevision_returnsObjectWithExpectedProtocolVersion(int expectedProtocolVersion) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:%d:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedProtocolVersion //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getProtocolVersion)
                          .isEqualTo(expectedProtocolVersion);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidProtocolRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:%s:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutProtocolRevision_returnsObjectWithNegativeProtocolVersion() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver::1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getProtocolVersion, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidProtocolRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM::%s:::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100})
    void testParse_prefiledPilotWithValidNonZeroProtocolRevision_throwsIllegalArgumentException(int protocolVersion) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM::%d:::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            protocolVersion //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithZeroProtocolRevision_returnsObjectWithNegativeProtocolVersion() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM::0:::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getProtocolVersion, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_prefiledPilotWithoutProtocolRevision_returnsObjectWithNegativeProtocolVersion() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getProtocolVersion, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10, 100})
    void testParse_atcWithValidProtocolRevision_returnsObjectWithExpectedProtocolVersion(int expectedProtocolVersion) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:%d:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedProtocolVersion //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getProtocolVersion)
                          .isEqualTo(expectedProtocolVersion);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_atcWithInvalidProtocolRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:%s:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutProtocolRevision_returnsObjectWithNegativeProtocolVersion() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver::3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getProtocolVersion, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="controller rating">
    @Test
    void testParse_connectedPilotWithRatingOBS_returnsObjectWithOBSControllerRating() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerRating)
                          .isSameAs(ControllerRating.OBS);
    }

    @ParameterizedTest
    @MethodSource("dataProviderControllerRatingIdAndEnumWithoutOBS")
    void testParse_connectedPilotWithRatingOtherThanOBS_throwsIllegalArgumentException(int controllerRatingId, ControllerRating _controllerRating) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:%d:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            controllerRatingId //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutRating_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1::1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1", "0", "99"})
    void testParse_connectedPilotWithInvalidRating_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:%s:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutRating_returnsObjectWithNullForControllerRating() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerRating)
                          .isNull();
    }

    @Test
    void testParse_prefiledPilotWithZeroRating_returnsObjectWithNullForControllerRating() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::0::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerRating)
                          .isNull();
    }

    static Stream<Arguments> dataProviderValidNonZeroControllerRatingIds() {
        return ControllerRatingTest.dataProviderIdAndEnum()
                                   .filter(args -> (int) args.get()[0] != 0);
    }

    @ParameterizedTest
    @MethodSource("dataProviderValidNonZeroControllerRatingIds")
    void testParse_prefiledPilotWithValidNonZeroRating_throwsIllegalArgumentException(int controllerRatingId, ControllerRating _controllerRating) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::%d::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            controllerRatingId //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-2", "abc", "1a", "a1", "99"})
    void testParse_prefiledPilotWithInvalidNonZeroRating_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::%s::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRatingTest#dataProviderIdAndEnum")
    void testParse_atcWithValidRating_returnsObjectWithExpectedControllerRating(int controllerRatingId, ControllerRating expectedControllerRating) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:%d::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            controllerRatingId //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerRating)
                          .isSameAs(expectedControllerRating);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-2", "abc", "1a", "a1", "13", "99"})
    void testParse_atcWithInvalidRating_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:%s::4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutRating_throwsIllegalArgumentException() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="transponder code">
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 123, 2143, 7000, 9999, 12345})
    void testParse_connectedPilotWithValidTransponderCode_returnsObjectWithExpectedTransponderCodeDecimal(int expectedTransponderCodeDecimal) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:%d:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedTransponderCodeDecimal //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getTransponderCodeDecimal)
                          .isEqualTo(expectedTransponderCodeDecimal);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidTransponderCode_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:%s:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutTransponderCode_returnsObjectWithNegativeTransponderCodeDecimal() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1::::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getTransponderCodeDecimal, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 123, 2143, 7000, 9999, 12345})
    void testParse_prefiledPilotWithValidNonZeroTransponderCode_throwsIllegalArgumentException(int transponderCodeNumeric) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM::::%d:::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            transponderCodeNumeric //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidTransponderCode_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM::::%s:::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutTransponderCode_returnsObjectWithNegativeTransponderCodeDecimal() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getTransponderCodeDecimal, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 123, 2143, 7000, 9999, 12345})
    void testParse_atcWithValidNonZeroTransponderCode_throwsIllegalArgumentException(int transponderCodeNumeric) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3:%d:4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            transponderCodeNumeric //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_atcWithInvalidTransponderCode_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3:%s:4:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutTransponderCode_returnsObjectWithNegativeTransponderCodeDecimal() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getTransponderCodeDecimal, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="facility type">
    static Stream<Arguments> dataProviderNonZeroFacilityTypeIds() {
        return FacilityTypeTest.dataProviderIdAndEnum()
                               .mapToInt(arguments -> (int) arguments.get()[0])
                               .filter(id -> id != 0)
                               .distinct()
                               .mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("dataProviderNonZeroFacilityTypeIds")
    void testParse_connectedPilotWithValidNonZeroFacilityType_throwsIllegalArgumentException(int id) {
        // 0 is the default value for all pilots since data format version 9

        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:%d::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            id //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1", "7", "100"})
    void testParse_connectedPilotWithInvalidFacilityType_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:%s::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithZeroFacilityType_returnsObjectWithNullForFacilityType() {
        // 0 is the default value for all pilots since data format version 9

        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:0::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFacilityType)
                          .isNull();
    }

    @Test
    void testParse_connectedPilotWithoutFacilityType_returnsObjectWithNullForFacilityType() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFacilityType)
                          .isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderNonZeroFacilityTypeIds")
    void testParse_prefiledPilotWithValidNonZeroFacilityType_throwsIllegalArgumentException(int id) {
        // 0 is the default value for all pilots since data format version 9

        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::%d::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            id //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1", "7", "100"})
    void testParse_prefiledPilotWithInvalidFacilityType_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::%s::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithZeroFacilityType_returnsObjectWithNullForFacilityType() {
        // 0 is the default value for all pilots since data format version 9

        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::0::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFacilityType)
                          .isNull();
    }

    @Test
    void testParse_prefiledPilotWithoutFacilityType_returnsObjectWithNullForFacilityType() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFacilityType)
                          .isNull();
    }

    @ParameterizedTest
    @MethodSource("org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityTypeTest#dataProviderIdAndEnum")
    void testParse_atcPilotWithValidFacilityType_returnsObjectWithExpectedFacilityType(int id, FacilityType expectedFacilityType) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::%d:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            id //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFacilityType)
                          .isSameAs(expectedFacilityType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1", "7", "100"})
    void testParse_atcPilotWithInvalidFacilityType_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::%s:50::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcPilotWithoutFacilityType_returnsObjectWithNullForFacilityType() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3:::50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFacilityType)
                          .isNull();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="visual range">
    @ParameterizedTest
    @ValueSource(ints = {1, 5, 50, 200, 1000})
    void testParse_connectedPilotWithValidNonZeroVisualRange_returnsObjectWithNegativeVisualRange(int visualRange) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234::%d:1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            visualRange //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVisualRange, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidVisualRange_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234::%s:1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithZeroVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234::0:1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVisualRange, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_connectedPilotWithoutVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVisualRange, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 50, 200, 1000})
    void testParse_prefiledPilotWithValidNonZeroVisualRange_throwsIllegalArgumentException(int visualRange) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM::::::%d:1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            visualRange //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidVisualRange_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM::::::%s:1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithZeroVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM::::::0:1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVisualRange, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_prefiledPilotWithoutVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVisualRange, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 50, 200, 1000})
    void testParse_atcWithValidVisualRange_returnsObjectWithExpectedVisualRange(int expectedVisualRange) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:%d::::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedVisualRange //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVisualRange)
                          .isEqualTo(expectedVisualRange);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_atcWithInvalidVisualRange_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:%s::::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutVisualRange_returnsObjectWithNegativeVisualRange() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:::::::::::::::::atis message:20180101160000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getVisualRange, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="flight plan revision">
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 200})
    void testParse_connectedPilotWithValidPlannedRevision_returnsObjectWithExpectedFlightPlanRevision(int expectedRevision) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::%d:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedRevision //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFlightPlanRevision)
                          .isEqualTo(expectedRevision);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidPlannedRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::%s:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutPlannedRevision_returnsObjectWithNegativeFlightPlanRevision() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234::::I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFlightPlanRevision, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 200})
    void testParse_prefiledPilotWithValidPlannedRevision_returnsObjectWithExpectedFlightPlanRevision(int expectedRevision) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::%d:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedRevision //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFlightPlanRevision)
                          .isEqualTo(expectedRevision);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidPlannedRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::%s:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutPlannedRevision_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM::::::::I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 200})
    void testParse_atcWithValidPlannedRevision_returnsObjectWithExpectedFlightPlanRevision(int expectedRevision) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:%d:::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedRevision //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFlightPlanRevision)
                          .isEqualTo(expectedRevision);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_atcWithInvalidPlannedRevision_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50:%s:::::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutPlannedRevision_returnsObjectWithNegativeFlightPlanRevision() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFlightPlanRevision, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="flight plan type">
    @ParameterizedTest
    @ValueSource(strings = {"", "V", "I", "Y", "Z", "something else 123-ABC"})
    void testParse_connectedPilot_returnsObjectWithExpectedRawFlightPlanType(String expectedRawFlightPlanType) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:%s:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedRawFlightPlanType //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawFlightPlanType)
                          .isEqualTo(expectedRawFlightPlanType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "V", "I", "Y", "Z", "something else 123-ABC"})
    void testParse_prefiledPilot_returnsObjectWithExpectedRawFlightPlanType(String expectedRawFlightPlanType) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:%s:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            expectedRawFlightPlanType //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawFlightPlanType)
                          .isEqualTo(expectedRawFlightPlanType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "V", "I", "Y", "Z", "something else 123-ABC"})
    void testParse_atc_returnsObjectWithExpectedRawFlightPlanType(String expectedRawFlightPlanType) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::%s::::::::::::::atis message:20180101160000:20180101150000::::",
            expectedRawFlightPlanType //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawFlightPlanType)
                          .isEqualTo(expectedRawFlightPlanType);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="departure time planned">
    @ParameterizedTest
    @ValueSource(ints = {0, 30, 2359, 123456789})
    void testParse_connectedPilotWithValidPlannedDeparture_returnsObjectWithExpectedRawDepartureTimePlanned(int rawValue) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:%d:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            rawValue //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimePlanned)
                          .isEqualTo(rawValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidPlannedDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:%s:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutPlannedDeparture_returnsObjectWithNegativeRawDepartureTimePlanned() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I::1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimePlanned, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 30, 2359, 123456789})
    void testParse_prefiledPilotWithValidPlannedDeparture_returnsObjectWithExpectedRawDepartureTimePlanned(int rawValue) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:%d:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            rawValue //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimePlanned)
                          .isEqualTo(rawValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidPlannedDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:%s:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutPlannedDeparture_returnsObjectWithNegativeRawDepartureTimePlanned() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I::1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimePlanned, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 30, 2359, 123456789})
    void testParse_atcWithValidPlannedDeparture_returnsObjectWithExpectedRawDepartureTimePlanned(int rawValue) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::%d:::::::::::::atis message:20180101160000:20180101150000::::",
            rawValue //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimePlanned)
                          .isEqualTo(rawValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_atcWithInvalidPlannedDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::%s:::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutPlannedDeparture_returnsObjectWithNegativeRawDepartureTimePlanned() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimePlanned, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="departure time actual">
    @ParameterizedTest
    @ValueSource(ints = {0, 30, 2359, 123456789})
    void testParse_connectedPilotWithValidActualDeparture_returnsObjectWithExpectedRawDepartureTimeActual(int rawValue) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:%d:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            rawValue //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimeActual)
                          .isEqualTo(rawValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidActualDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:%s:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutActualDeparture_returnsObjectWithNegativeRawDepartureTimeActual() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000::1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimeActual, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 30, 2359, 123456789})
    void testParse_prefiledPilotWithValidActualDeparture_returnsObjectWithExpectedRawDepartureTimeActual(int rawValue) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:%d:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            rawValue //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimeActual)
                          .isEqualTo(rawValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidActualDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:%s:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutActualDeparture_returnsObjectWithNegativeRawDepartureTimeActual() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000::1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimeActual, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 30, 2359, 123456789})
    void testParse_atcWithValidActualDeparture_returnsObjectWithExpectedRawDepartureTimeActual(int rawValue) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::%d::::::::::::atis message:20180101160000:20180101150000::::",
            rawValue //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimeActual)
                          .isEqualTo(rawValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1"})
    void testParse_atcWithInvalidActualDeparture_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::%s::::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutActualDeparture_returnsObjectWithNegativeRawDepartureTimeActual() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567::::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getRawDepartureTimeActual, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed time enroute">
    @ParameterizedTest
    @MethodSource("dataProviderHoursAndMinutesAndDuration")
    void testParse_connectedPilotWithValidPlannedEnroute_returnsObjectWithExpectedFiledTimeEnroute(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:%d:%d:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            hours, minutes //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeEnroute)
                          .isEqualTo(expectedDuration);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidPlannedHoursEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:%s:0:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidPlannedMinutesEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:0:%s:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_connectedPilotWithPlannedHoursEnrouteButWithoutPlannedMinutesEnroute_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:%d::3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            hours //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_connectedPilotWithPlannedMinutesEnrouteButWithoutPlannedHoursEnroute_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000::%d:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            minutes //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutPlannedEnroute_returnsObjectWithNullForFiledTimeEnroute() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000::::3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeEnroute)
                          .isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderHoursAndMinutesAndDuration")
    void testParse_prefiledPilotWithValidPlannedEnroute_returnsObjectWithExpectedFiledTimeEnroute(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:%d:%d:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            hours, minutes //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeEnroute)
                          .isEqualTo(expectedDuration);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidPlannedHoursEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:%s:0:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidPlannedMinutesEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:0:%s:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_prefiledPilotWithPlannedHoursEnrouteButWithoutPlannedMinutesEnroute_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:%d::3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            hours //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_prefiledPilotWithPlannedMinutesEnrouteButWithoutPlannedHoursEnroute_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000::%d:3:0:EDDW:remark:DCT:0:0:0:0:::::::",
            minutes //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutPlannedEnroute_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:::3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("dataProviderHoursAndMinutesAndDuration")
    void testParse_atcWithValidPlannedEnroute_returnsObjectWithExpectedFiledTimeEnroute(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::%d:%d::::::::::atis message:20180101160000:20180101150000::::",
            hours, minutes //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeEnroute)
                          .isEqualTo(expectedDuration);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_atcWithInvalidPlannedHoursEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::%s:0::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_atcWithInvalidPlannedMinutesEnroute_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::0:%s::::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_atcWithPlannedHoursEnrouteButWithoutPlannedMinutesEnroute_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::%d:::::::::::atis message:20180101160000:20180101150000::::",
            hours //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_atcWithPlannedMinutesEnrouteButWithoutPlannedHoursEnroute_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::%d::::::::::atis message:20180101160000:20180101150000::::",
            minutes //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutPlannedEnroute_returnsObjectWithNullForFiledTimeEnroute() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeEnroute)
                          .isNull();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed time fuel">
    @ParameterizedTest
    @MethodSource("dataProviderHoursAndMinutesAndDuration")
    void testParse_connectedPilotWithValidPlannedFuel_returnsObjectWithExpectedFiledTimeFuel(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:%d:%d:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            hours, minutes //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeFuel)
                          .isEqualTo(expectedDuration);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidPlannedHoursFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:%s:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidPlannedMinutesFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:0:%s:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_connectedPilotWithPlannedHoursFuelButWithoutPlannedMinutesFuel_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:%d::EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            hours //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_connectedPilotWithPlannedMinutesFuelButWithoutPlannedHoursFuel_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30::%d:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            minutes //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutPlannedFuel_returnsObjectWithNullForFiledTimeFuel() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000::1:30:::EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeFuel)
                          .isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderHoursAndMinutesAndDuration")
    void testParse_prefiledPilotWithValidPlannedFuel_returnsObjectWithExpectedFiledTimeFuel(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:%d:%d:EDDW:remark:DCT:0:0:0:0:::::::",
            hours, minutes //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeFuel)
                          .isEqualTo(expectedDuration);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidPlannedHoursFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:%s:0:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidPlannedMinutesFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:0:%s:EDDW:remark:DCT:0:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_prefiledPilotWithPlannedHoursFuelButWithoutPlannedMinutesFuel_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:%d::EDDW:remark:DCT:0:0:0:0:::::::",
            hours //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_prefiledPilotWithPlannedMinutesFuelButWithoutPlannedHoursFuel_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30::%d:EDDW:remark:DCT:0:0:0:0:::::::",
            minutes //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutPlannedFuel_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:::EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("dataProviderHoursAndMinutesAndDuration")
    void testParse_atcWithValidPlannedFuel_returnsObjectWithExpectedFiledTimeFuel(int hours, int minutes, Duration expectedDuration) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::%d:%d::::::::atis message:20180101160000:20180101150000::::",
            hours, minutes //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeFuel)
                          .isEqualTo(expectedDuration);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_atcWithInvalidPlannedHoursFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::%s:0::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_atcWithInvalidPlannedMinutesFuel_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::0:%s::::::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_atcWithPlannedHoursFuelButWithoutPlannedMinutesFuel_throwsIllegalArgumentException(int hours) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::%d:::::::::atis message:20180101160000:20180101150000::::",
            hours //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testParse_atcWithPlannedMinutesFuelButWithoutPlannedHoursFuel_throwsIllegalArgumentException(int minutes) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::%d::::::::atis message:20180101160000:20180101150000::::",
            minutes //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutPlannedFuel_returnsObjectWithNullForFiledTimeFuel() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledTimeFuel)
                          .isNull();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed alternate airport">
    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_connectedPilot_returnsObjectWithExpectedFiledAlternateAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:%s:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledAlternateAirportCode)
                          .isEqualTo(expectedAirportCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_prefiledPilot_returnsObjectWithExpectedFiledAlternateAirportCode(String expectedAirportCode) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:%s:remark:DCT:0:0:0:0:::::::",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledAlternateAirportCode)
                          .isEqualTo(expectedAirportCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "EDDT", "05S"})
    void testParse_atc_returnsObjectWithExpectedFiledAlternateAirportCode(String expectedAirportCode) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::::%s:::::::atis message:20180101160000:20180101150000::::",
            expectedAirportCode //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledAlternateAirportCode)
                          .isEqualTo(expectedAirportCode);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="flight plan remarks">
    @ParameterizedTest
    @ValueSource(strings = {"", "my remarks", "+-/;.#!\"%&()=_"})
    void testParse_connectedPilot_returnsObjectWithExpectedFlightPlanRemarks(String expectedRemarks) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:%s:DCT:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedRemarks //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFlightPlanRemarks)
                          .isEqualTo(expectedRemarks);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "my remarks", "+-/;.#!\"%&()=_"})
    void testParse_prefiledPilot_returnsObjectWithExpectedFlightPlanRemarks(String expectedRemarks) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:%s:DCT:0:0:0:0:::::::",
            expectedRemarks //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFlightPlanRemarks)
                          .isEqualTo(expectedRemarks);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "my remarks", "+-/;.#!\"%&()=_"})
    void testParse_atc_returnsObjectWithExpectedFlightPlanRemarks(String expectedRemarks) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::%s::::::atis message:20180101160000:20180101150000::::",
            expectedRemarks //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFlightPlanRemarks)
                          .isEqualTo(expectedRemarks);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="filed route">
    @ParameterizedTest
    @ValueSource(strings = {"", "DCT", "SID1A/12L WPT UA123 ANASA DCT ENTRY STAR2B/31R", "just special chars +#-.,%\\"})
    void testParse_connectedPilot_returnsObjectWithExpectedFiledRoute(String expectedRoute) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:%s:0:0:0:0:::20180101094500:270:29.92:1013:",
            expectedRoute //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledRoute)
                          .isEqualTo(expectedRoute);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "DCT", "SID1A/12L WPT UA123 ANASA DCT ENTRY STAR2B/31R", "just special chars +#-.,%\\"})
    void testParse_prefiledPilot_returnsObjectWithExpectedFiledRoute(String expectedRoute) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remarks:%s:0:0:0:0:::::::",
            expectedRoute //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledRoute)
                          .isEqualTo(expectedRoute);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "DCT", "SID1A/12L WPT UA123 ANASA DCT ENTRY STAR2B/31R", "just special chars +#-.,%\\"})
    void testParse_atc_returnsObjectWithExpectedFiledRoute(String expectedRoute) {
        // An ATC with a flight plan doesn't make any sense but can actually be
        // found on data files...
        // Needs to be parseable but resulting data should be ignored
        // nevertheless unless there really is some strange use case that makes
        // sense.

        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::::::%s:::::atis message:20180101160000:20180101150000::::",
            expectedRoute //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getFiledRoute)
                          .isEqualTo(expectedRoute);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="departure airport latitude">
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_connectedPilotWithDepartureAirportLatitude_returnsObjectWithExpectedDepartureAirportLatitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:%s:0:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLatitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_connectedPilotWithoutDepartureAirportLatitude_returnsObjectWithNaNAsDepartureAirportLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT::0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLatitude, as(DOUBLE))
                          .isNaN();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithDepartureAirportLatitude_returnsObjectWithExpectedDepartureAirportLatitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:%s:0:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLatitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithoutDepartureAirportLatitude_returnsObjectWithNaNAsDepartureAirportLatitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT::0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLatitude, as(DOUBLE))
                          .isNaN();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_atcWithDepartureAirportLatitude_returnsObjectWithExpectedDepartureAirportLatitude(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::%s::::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLatitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_atcWithoutDepartureAirportLatitude_returnsObjectWithNaNAsDepartureAirportLatitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLatitude, as(DOUBLE))
                          .isNaN();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="departure airport longitude">
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_connectedPilotWithDepartureAirportLongitude_returnsObjectWithExpectedDepartureAirportLongitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:%s:0:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLongitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_connectedPilotWithoutDepartureAirportLongitude_returnsObjectWithNaNAsDepartureAirportLongitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0::0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLongitude, as(DOUBLE))
                          .isNaN();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithDepartureAirportLongitude_returnsObjectWithExpectedDepartureAirportLongitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:%s:0:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLongitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithoutDepartureAirportLongitude_returnsObjectWithNaNAsDepartureAirportLongitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0::0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLongitude, as(DOUBLE))
                          .isNaN();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_atcWithDepartureAirportLongitude_returnsObjectWithExpectedDepartureAirportLongitude(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::::::::%s:::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLongitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_atcWithoutDepartureAirportLongitude_returnsObjectWithNaNAsDepartureAirportLongitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDepartureAirportLongitude, as(DOUBLE))
                          .isNaN();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="destination airport latitude">
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_connectedPilotWithDestinationAirportLatitude_returnsObjectWithExpectedDestinationAirportLatitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:%s:0:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLatitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_connectedPilotWithoutDestinationAirportLatitude_returnsObjectWithNaNAsDestinationAirportLatitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0::0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLatitude, as(DOUBLE))
                          .isNaN();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithDestinationAirportLatitude_returnsObjectWithExpectedDestinationAirportLatitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:%s:0:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLatitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithoutDestinationAirportLatitude_returnsObjectWithNaNAsDestinationAirportLatitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0::0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLatitude, as(DOUBLE))
                          .isNaN();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_atcWithDestinationAirportLatitude_returnsObjectWithExpectedDestinationAirportLatitude(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::%s::atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLatitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_atcWithoutDestinationAirportLatitude_returnsObjectWithNaNAsDestinationAirportLatitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLatitude, as(DOUBLE))
                          .isNaN();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="destination airport longitude">
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_connectedPilotWithDestinationAirportLongitude_returnsObjectWithExpectedDestinationAirportLongitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:%s:::20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLongitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_connectedPilotWithoutDestinationAirportLongitude_returnsObjectWithNaNAsDestinationAirportLongitude() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0::::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLongitude, as(DOUBLE))
                          .isNaN();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithDestinationAirportLongitude_returnsObjectWithExpectedDestinationAirportLongitude(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:%s:::::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLongitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_prefiledPilotWithoutDestinationAirportLongitude_returnsObjectWithNaNAsDestinationAirportLongitude(String input) {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0::::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLongitude, as(DOUBLE))
                          .isNaN();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {
        "12.34567",
        "-80.23456",
        "0",
        "1",
        "-0",
        "-1.234e-5",
        "-1.234E-5",
        "-1.234E-05",
        "-1.234E02",
        "-1.234E+2",
        "-1.234E+02",
        "9999",
        "-9999"
    })
    void testParse_atcWithDestinationAirportLongitude_returnsObjectWithExpectedDestinationAirportLongitude(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50:::::::::::::::%s:atis message:20180101160000:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLongitude, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_atcWithoutDestinationAirportLongitude_returnsObjectWithNaNAsDestinationAirportLongitude() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getDestinationAirportLongitude, as(DOUBLE))
                          .isNaN();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="controller message">
    @Test
    void testParse_connectedPilotWithControllerMessage_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:controller message::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutControllerMessage_returnsObjectWithEmptyControllerMessage() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessage, as(STRING))
                          .isEmpty();
    }

    @Test
    void testParse_prefiledPilotWithControllerMessage_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:controller message::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutControllerMessage_returnsObjectWithEmptyControllerMessage() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessage, as(STRING))
                          .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("dataProviderControllerMessageRawAndDecoded")
    void testParse_atcWithControllerMessage_returnsObjectWithExpectedControllerMessage(String rawMessage, String expectedMessage) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::%s:20180101160000:20180101150000::::",
            rawMessage //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessage)
                          .isEqualTo(expectedMessage);
    }

    @Test
    void testParse_atcWithoutControllerMessage_returnsObjectWithEmptyControllerMessage() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50:::::::::::::::::20180101160000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessage, as(STRING))
                          .isEmpty();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="controller message last updated">
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("dataProviderFullTimestampStringAndObject")
    void testParse_connectedPilotWithValidLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated(String input, Instant _instant) {
        // server appears to randomly assign some ATIS timestamp to pilots in format
        // 9...

        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::%s:20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessageLastUpdated)
                          .isNull();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_connectedPilotWithDummyLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::00010101000000:20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessageLastUpdated)
                          .isNull();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_connectedPilotWithoutLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessageLastUpdated)
                          .isNull();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_prefiledPilotWithDummyLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::00010101000000:::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessageLastUpdated)
                          .isNull();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_prefiledPilotWithoutLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessageLastUpdated)
                          .isNull();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("dataProviderFullTimestampStringAndObject")
    void testParse_atcWithValidLastAtisReceived_returnsObjectWithExpectedControllerMessageLastUpdated(String input, Instant expectedInstant) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:%s:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessageLastUpdated)
                          .isEqualTo(expectedInstant);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_atcWithDummyLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:00010101000000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessageLastUpdated)
                          .isNull();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_atcWithoutLastAtisReceived_returnsObjectWithNullForControllerMessageLastUpdated() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message::20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getControllerMessageLastUpdated)
                          .isNull();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="last updated timestamp">
    @ParameterizedTest
    @MethodSource("dataProviderFullTimestampStringAndObject")
    void testParse_connectedPilotWithValidLastAtisReceived_returnsObjectWithNullForLastUpdated(String input, Instant _instant) {
        // server appears to randomly assign some ATIS timestamp to pilots in format
        // 9...

        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::%s:20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLastUpdated)
                          .isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidLastAtisReceived_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::%s:20180101094500:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithDummyLastAtisReceived_returnsObjectWithNullForLastUpdated() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::00010101000000:20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLastUpdated)
                          .isNull();
    }

    @Test
    void testParse_connectedPilotWithoutLastAtisReceived_returnsObjectWithNullForLastUpdated() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLastUpdated)
                          .isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderFullTimestampStringAndObject")
    void testParse_prefiledPilotWithValidLastAtisReceived_throwsIllegalArgumentException(String input, Instant _instant) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::%s:::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidLastAtisReceived_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::%s:::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithDummyLastAtisReceived_returnsObjectWithNullForLastUpdated() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::00010101000000:::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLastUpdated)
                          .isNull();
    }

    @Test
    void testParse_prefiledPilotWithoutLastAtisReceived_returnsObjectWithNullForLastUpdated() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLastUpdated)
                          .isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderFullTimestampStringAndObject")
    void testParse_atcWithValidLastAtisReceived_returnsObjectWithExpectedLastUpdated(String input, Instant expectedInstant) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:%s:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLastUpdated)
                          .isEqualTo(expectedInstant);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_atcWithInvalidLastAtisReceived_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:%s:20180101150000::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithDummyLastAtisReceived_returnsObjectWithNullForLastUpdated() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:00010101000000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLastUpdated)
                          .isNull();
    }

    @Test
    void testParse_atcWithoutLastAtisReceived_returnsObjectWithNullForLastUpdated() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message::20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLastUpdated)
                          .isNull();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="logon time">
    @ParameterizedTest
    @MethodSource("dataProviderFullTimestampStringAndObject")
    void testParse_connectedPilotWithValidLogonTime_throwsIllegalArgumentException(String input, Instant expectedInstant) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::%s:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLogonTime)
                          .isEqualTo(expectedInstant);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidLogonTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::%s:270:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutLogonTime_throwsIllegalArgumentException() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0::::270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("dataProviderFullTimestampStringAndObject")
    void testParse_prefiledPilotWithValidLogonTime_throwsIllegalArgumentException(String input, Instant _instant) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::%s::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidLogonTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::%s::::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutLogonTime_returnsObjectWithNullForLogonTime() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLogonTime)
                          .isNull();
    }

    @ParameterizedTest
    @MethodSource("dataProviderFullTimestampStringAndObject")
    void testParse_atcWithValidLogonTime_returnsObjectWithExpectedLogonTime(String input, Instant expectedInstant) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:20180101160000:%s::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getLogonTime)
                          .isEqualTo(expectedInstant);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-123", "abc", "1a", "a1"})
    void testParse_atcWithInvalidLogonTime_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:20180101160000:%s::::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutLastAtisReceived_throwsIllegalArgumentException() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::atis message:20180101160000:::::";

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ATIS designator">
    @Test
    void testParse_connectedPilot_returnsObjectWithEmptyAtisDesignator() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAtisDesignator, as(STRING))
                          .isEmpty();
    }

    @Test
    void testParse_prefiledPilot_returnsObjectWithEmptyAtisDesignator() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAtisDesignator, as(STRING))
                          .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("dataProviderControllerMessageRawAndDecoded")
    void testParse_atcWithControllerMessage_returnsObjectWithEmptyAtisDesignator(String rawMessage, String _message) {
        // Arrange
        String line = String.format(
            "EDDT_ATIS:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50::::::::::::::::%s:20180101160000:20180101150000::::",
            rawMessage //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAtisDesignator, as(STRING))
                          .isEmpty();
    }

    @Test
    void testParse_atcWithoutControllerMessage_returnsObjectWithEmptyAtisDesignator() {
        // Arrange
        String line = "EDDT_ATIS:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50:::::::::::::::::20180101160000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getAtisDesignator, as(STRING))
                          .isEmpty();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="heading">
    @ParameterizedTest
    @ValueSource(ints = {0, 123, 359})
    void testParse_connectedPilotWithValidRegularHeading_returnsObjectWithExpectedHeading(int expectedHeading) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:%d:29.92:1013:",
            expectedHeading //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getHeading)
                          .isEqualTo(expectedHeading);
    }

    @Test
    void testParse_connectedPilotWithValid360Heading_returnsObjectWithZeroHeading() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:360:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getHeading)
                          .isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1", "361", "1080"})
    void testParse_connectedPilotWithInvalidHeading_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:%s:29.92:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutHeading_returnsObjectWithNegativeHeading() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500::29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getHeading, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 123, 359, 360})
    void testParse_prefiledPilotWithValidNonZeroHeading_throwsIllegalArgumentException(int heading) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::::%d:::",
            heading //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1", "361", "1080"})
    void testParse_prefiledPilotWithInvalidHeading_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::::%s:::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutHeading_returnsObjectWithNegativeHeading() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getHeading, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 123, 359, 360})
    void testParse_atcWithValidNonZeroHeading_throwsIllegalArgumentException(int heading) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:%d:::",
            heading //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "1a", "a1", "361", "1080"})
    void testParse_atcWithInvalidHeading_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:%s:::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutHeading_returnsObjectWithNegativeHeading() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getHeading, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="QNH Inch Mercury">
    @ParameterizedTest
    @ValueSource(strings = {"29.92", "2.992e02", "30", "27.9", "-1", "-29.92", "-2.992e02", "-2.992E5", "-2.992E05",
        "-2.992E-1", "-2.992E+1", "-2.992E+01"})
    void testParse_connectedPilotWithValidQnhIHg_returnsObjectWithExpectedQnhInchMercury(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:%s:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        double expectedOutput = Double.parseDouble(input);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhInchMercury, as(DOUBLE))
                          .isCloseTo(expectedOutput, ALLOWED_DOUBLE_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidQnhIHg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:%s:1013:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutQnhIHg_returnsObjectWithNaNAsQnhInchMercury() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270::1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhInchMercury, as(DOUBLE))
                          .isNaN();
    }

    @ParameterizedTest
    @ValueSource(strings = {"29.92", "2.992e02", "30", "27.9", "-1", "-29.92", "-2.992e02", "-2.992E5", "-2.992E05",
        "-2.992E-1", "-2.992E+1", "-2.992E+01"})
    void testParse_prefiledPilotWithValidQnhIhg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::%s::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidQnhIHg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::%s::",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutQnhIHg_returnsObjectWithNaNAsQnhInchMercury() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhInchMercury, as(DOUBLE))
                          .isNaN();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "29.92",
        "2.992e02",
        "30",
        "27.9",
        "-1",
        "-29.92",
        "-2.992e02",
        "-2.992E5",
        "-2.992E05",
        "-2.992E-1",
        "-2.992E+1",
        "-2.992E+01"
    })
    void testParse_atcWithValidQnhIHg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::%s::",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "a1"})
    void testParse_atcWithInvalidQnhIHg_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::%s::",
            input
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithoutQnhIHg_returnsObjectWithNaNAsQnhInchMercury() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhInchMercury, as(DOUBLE))
                          .isNaN();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="QNH Hectopascal">
    @ParameterizedTest
    @ValueSource(ints = {-12345, 0, 997, 1013, 1030})
    void testParse_connectedPilotWithValidQnhMB_returnsObjectWithExpectedQnhHectopascal(int expectedQnh) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:%d:",
            expectedQnh //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhHectopascal)
                          .isEqualTo(expectedQnh);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.0", "29.92", "1e03", "abc", "1a", "a1"})
    void testParse_connectedPilotWithInvalidQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:%s:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_connectedPilotWithoutQnhMB_returnsObjectWithNegativeQnhHectopascal() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhHectopascal, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-12345", "0", "997", "1013", "1030"})
    void testParse_prefiledPilotWithValidQnhMB_returnsObjectWithNegativeQnhHectopascal(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::::::%s:",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhHectopascal, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.0", "29.92", "1e03", "abc", "1a", "a1"})
    void testParse_prefiledPilotWithInvalidQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0::::::%s:",
            input //
        );
        parser.setIsParsingPrefileSection(true);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_prefiledPilotWithoutQnhMB_returnsObjectWithNegativeQnhHectopascal() {
        // Arrange
        String line = "ABC123:123456:realname:::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhHectopascal, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-12345", "-1", "997", "1013", "1030"})
    void testParse_atcWithValidNonZeroQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:::%s:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.0", "29.92", "1e03", "abc", "1a", "a1"})
    void testParse_atcWithInvalidQnhMB_throwsIllegalArgumentException(String input) {
        // Arrange
        String line = String.format(
            "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:::%s:",
            input //
        );
        parser.setIsParsingPrefileSection(false);

        // Act
        ThrowingCallable action = () -> parser.parse(line);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse_atcWithZeroQnhMB_returnsObjectWithNegativeQnhHectopascal() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000:::0:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhHectopascal, as(INTEGER))
                          .isNegative();
    }

    @Test
    void testParse_atcWithoutQnhMB_returnsObjectWithNegativeQnhHectopascal() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::SERVER1:100:3::4:50::::::::::::::::atis message:20180101160000:20180101150000::::";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getQnhHectopascal, as(INTEGER))
                          .isNegative();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="pilot rating">
    @Test
    void testParse_connectedPilot_returnsObjectWithNullForPilotRating() {
        // Arrange
        String line = "ABC123:123456:realname:PILOT::12.34567:12.34567:12345:123:B738:420:EDDT:30000:EHAM:someserver:1:1:1234:::1:I:1000:1000:1:30:3:0:EDDW:remarks:DCT:0:0:0:0:::20180101094500:270:29.92:1013:";
        parser.setIsParsingPrefileSection(false);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getPilotRating)
                          .isNull();
    }

    @Test
    void testParse_prefiledPilot_returnsObjectWithNullForPilotRating() {
        // Arrange
        String line = "ABC123:123456::::::::B738:420:EDDT:30000:EHAM:::::::1:I:1000:1000:1:30:3:0:EDDW:remark:DCT:0:0:0:0:::::::";
        parser.setIsParsingPrefileSection(true);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getPilotRating)
                          .isNull();
    }

    @Test
    void testParse_atc_returnsObjectWithNullForPilotRating() {
        // Arrange
        String line = "EDDT_TWR:123456:realname:ATC:118.500:12.34567:12.34567:0:::0::::someserver:100:3::4:50:::::::::::::::::20180101160000:20180101150000::::";

        // Act
        Client result = parser.parse(line);

        // Assert
        assertThat(result).extracting(Client::getPilotRating)
                          .isNull();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="special: client type detection/tolerance">
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    void testParse_ghostWithPositionHeadingQNHTransponderLogonTimeAndServerIdInOnlineSection_returnsObjectAsEffectiveConnectedPilotWithExpectedData() {
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
        assertAll(
            () -> assertThat(result).describedAs("raw client type")
                                    .extracting(Client::getRawClientType)
                                    .isNull(),

            () -> assertThat(result).describedAs("effective client type")
                                    .extracting(Client::getEffectiveClientType)
                                    .isSameAs(ClientType.PILOT_CONNECTED),

            () -> assertThat(result).describedAs("callsign")
                                    .extracting(Client::getCallsign)
                                    .isEqualTo("ANY123"),

            () -> assertThat(result).describedAs("latitude")
                                    .extracting(Client::getLatitude, as(DOUBLE))
                                    .isCloseTo(10.12345, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("longitude")
                                    .extracting(Client::getLongitude, as(DOUBLE))
                                    .isCloseTo(-20.54321, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("altitude feet")
                                    .extracting(Client::getAltitudeFeet)
                                    .isEqualTo(80),

            () -> assertThat(result).describedAs("ground speed")
                                    .extracting(Client::getGroundSpeed)
                                    .isEqualTo(0),

            () -> assertThat(result).describedAs("server ID")
                                    .extracting(Client::getServerId)
                                    .isEqualTo("SOME_SERVER"),

            () -> assertThat(result).describedAs("controller rating")
                                    .extracting(Client::getControllerRating)
                                    .isSameAs(ControllerRating.OBS),

            () -> assertThat(result).describedAs("transponder code decimal")
                                    .extracting(Client::getTransponderCodeDecimal)
                                    .isEqualTo(1200),

            () -> assertThat(result).describedAs("logon time")
                                    .extracting(Client::getLogonTime)
                                    .isEqualTo(expectedLogonTime),

            () -> assertThat(result).describedAs("heading")
                                    .extracting(Client::getHeading)
                                    .isEqualTo(120),

            () -> assertThat(result).describedAs("QNH Inch Mercury")
                                    .extracting(Client::getQnhInchMercury, as(DOUBLE))
                                    .isCloseTo(29.92, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("QNH Hectopascal")
                                    .extracting(Client::getQnhHectopascal)
                                    .isEqualTo(1013)
        );
    }

    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    void testParse_ghostWithPositionHeadingQNHTransponderLogonTimeButWithoutServerIdInOnlineSection_returnsObjectAsEffectiveConnectedPilotWithExpectedData() {
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
        assertAll(
            () -> assertThat(result).describedAs("raw client type")
                                    .extracting(Client::getRawClientType)
                                    .isNull(),

            () -> assertThat(result).describedAs("effective client type")
                                    .extracting(Client::getEffectiveClientType)
                                    .isSameAs(ClientType.PILOT_CONNECTED),

            () -> assertThat(result).describedAs("callsign")
                                    .extracting(Client::getCallsign)
                                    .isEqualTo("ANY123"),

            () -> assertThat(result).describedAs("latitude")
                                    .extracting(Client::getLatitude, as(DOUBLE))
                                    .isCloseTo(10.12345, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("longitude")
                                    .extracting(Client::getLongitude, as(DOUBLE))
                                    .isCloseTo(-20.54321, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("altitude feet")
                                    .extracting(Client::getAltitudeFeet)
                                    .isEqualTo(80),

            () -> assertThat(result).describedAs("ground speed")
                                    .extracting(Client::getGroundSpeed)
                                    .isEqualTo(0),

            () -> assertThat(result).describedAs("server ID")
                                    .extracting(Client::getServerId)
                                    .isNull(),

            () -> assertThat(result).describedAs("controller rating")
                                    .extracting(Client::getControllerRating)
                                    .isSameAs(ControllerRating.OBS),

            () -> assertThat(result).describedAs("transponder code decimal")
                                    .extracting(Client::getTransponderCodeDecimal)
                                    .isEqualTo(1200),

            () -> assertThat(result).describedAs("logon time")
                                    .extracting(Client::getLogonTime)
                                    .isEqualTo(expectedLogonTime),

            () -> assertThat(result).describedAs("heading")
                                    .extracting(Client::getHeading)
                                    .isEqualTo(120),

            () -> assertThat(result).describedAs("QNH Inch Mercury")
                                    .extracting(Client::getQnhInchMercury, as(DOUBLE))
                                    .isCloseTo(29.92, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("QNH Hectopascal")
                                    .extracting(Client::getQnhHectopascal)
                                    .isEqualTo(1013)
        );
    }

    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    void testParse_atcWithHeadingQNHAndTransponderInOnlineSectionOnPlaceholderFrequency_returnsObjectAsEffectiveConnectedPilotWithExpectedData() {
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
        assertAll(
            () -> assertThat(result).describedAs("raw client type")
                                    .extracting(Client::getRawClientType)
                                    .isSameAs(ClientType.ATC_CONNECTED),

            () -> assertThat(result).describedAs("effective client type")
                                    .extracting(Client::getEffectiveClientType)
                                    .isSameAs(ClientType.PILOT_CONNECTED),

            () -> assertThat(result).describedAs("callsign")
                                    .extracting(Client::getCallsign)
                                    .isEqualTo("ANY123"),

            () -> assertThat(result).describedAs("latitude")
                                    .extracting(Client::getLatitude, as(DOUBLE))
                                    .isCloseTo(12.34567, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("longitude")
                                    .extracting(Client::getLongitude, as(DOUBLE))
                                    .isCloseTo(-12.34567, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("altitude feet")
                                    .extracting(Client::getAltitudeFeet)
                                    .isEqualTo(123),

            () -> assertThat(result).describedAs("ground speed")
                                    .extracting(Client::getGroundSpeed)
                                    .isEqualTo(1),

            () -> assertThat(result).describedAs("server ID")
                                    .extracting(Client::getServerId)
                                    .isEqualTo("SOMESERVER"),

            () -> assertThat(result).describedAs("controller rating")
                                    .extracting(Client::getControllerRating)
                                    .isSameAs(ControllerRating.OBS),

            () -> assertThat(result).describedAs("transponder code decimal")
                                    .extracting(Client::getTransponderCodeDecimal)
                                    .isEqualTo(1200),

            () -> assertThat(result).describedAs("logon time")
                                    .extracting(Client::getLogonTime)
                                    .isEqualTo(expectedLogonTime),

            () -> assertThat(result).describedAs("heading")
                                    .extracting(Client::getHeading)
                                    .isEqualTo(180),

            () -> assertThat(result).describedAs("QNH Inch Mercury")
                                    .extracting(Client::getQnhInchMercury, as(DOUBLE))
                                    .isCloseTo(29.92, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("QNH Hectopascal")
                                    .extracting(Client::getQnhHectopascal)
                                    .isEqualTo(1013)
        );
    }

    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    void testParse_atcWithZeroHeadingQNHGroundSpeedAndTransponderInOnlineSectionOnNonPlaceholderFrequency_returnsObjectAsEffectiveATCWithExpectedData() {
        // Default in data format version 9:
        // ATC appears like a pilot with heading, QNH and transponder fields set to zero

        // Arrange
        String line = "SOME_ATC:987654321:some name:ATC:124.525:12.34567:-12.34567:123:0::0::::SOMESERVER:100:10:0:0:40::::::::::::::::::20171006123456:0:0:0:";
        parser.setIsParsingPrefileSection(false);

        Instant expectedLogonTime = LocalDateTime.of(2017, Month.OCTOBER, 6, 12, 34, 56).toInstant(ZoneOffset.UTC);

        // Act
        Client result = parser.parse(line);

        // Assert
        assertAll(
            () -> assertThat(result).describedAs("raw client type")
                                    .extracting(Client::getRawClientType)
                                    .isSameAs(ClientType.ATC_CONNECTED),

            () -> assertThat(result).describedAs("effective client type")
                                    .extracting(Client::getEffectiveClientType)
                                    .isSameAs(ClientType.ATC_CONNECTED),

            () -> assertThat(result).describedAs("callsign")
                                    .extracting(Client::getCallsign)
                                    .isEqualTo("SOME_ATC"),

            () -> assertThat(result).describedAs("latitude")
                                    .extracting(Client::getLatitude, as(DOUBLE))
                                    .isCloseTo(12.34567, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("longitude")
                                    .extracting(Client::getLongitude, as(DOUBLE))
                                    .isCloseTo(-12.34567, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("altitude feet")
                                    .extracting(Client::getAltitudeFeet)
                                    .isEqualTo(123),

            () -> assertThat(result).describedAs("ground speed")
                                    .extracting(Client::getGroundSpeed)
                                    .isEqualTo(-1),

            () -> assertThat(result).describedAs("server ID")
                                    .extracting(Client::getServerId)
                                    .isEqualTo("SOMESERVER"),

            () -> assertThat(result).describedAs("controller rating")
                                    .extracting(Client::getControllerRating)
                                    .isSameAs(ControllerRating.I3),

            () -> assertThat(result).describedAs("transponder code decimal")
                                    .extracting(Client::getTransponderCodeDecimal)
                                    .isEqualTo(0),

            () -> assertThat(result).describedAs("logon time")
                                    .extracting(Client::getLogonTime)
                                    .isEqualTo(expectedLogonTime),

            () -> assertThat(result).describedAs("heading")
                                    .extracting(Client::getHeading)
                                    .isEqualTo(0),

            () -> assertThat(result).describedAs("QNH Inch Mercury")
                                    .extracting(Client::getQnhInchMercury, as(DOUBLE))
                                    .isCloseTo(0, ALLOWED_DOUBLE_ERROR),

            () -> assertThat(result).describedAs("QNH Hectopascal")
                                    .extracting(Client::getQnhHectopascal)
                                    .isEqualTo(-1),

            () -> assertThat(result).describedAs("served frequency kilohertz")
                                    .extracting(Client::getServedFrequencyKilohertz)
                                    .isEqualTo(124525)
        );
    }
    // </editor-fold>

    // TODO: make NetBeans editor-folds @Nested classes
    // TODO: add null-tests for military rating
}
