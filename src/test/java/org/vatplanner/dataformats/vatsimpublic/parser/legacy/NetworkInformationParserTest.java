package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileFormat;
import org.vatplanner.dataformats.vatsimpublic.parser.NetworkInformation;

import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

class NetworkInformationParserTest {

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(NetworkInformationParser.class);

    static Stream<Arguments> dataProviderExpectedDefinitionComments() {
        return Stream.of(
            "; msg0         - message to be displayed at application startup"
        ).map(Arguments::of);
    }

    static Stream<Arguments> dataProviderNonMatchingDefinitionComment() {
        return Stream.of(
            Arguments.of("; msg0         - something unexpected", "msg0", "something unexpected")
        );
    }

    @BeforeEach
    void clearLog() {
        testLogger.clearAll();
    }

    private BufferedReader getBufferedReaderForTestResource(String resourceName) {
        String filePath = getClass().getResource("/NetworkInformation/" + resourceName).getFile();
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("failed to initialize a BufferedReader for resource " + resourceName, ex);
        }
    }

    @Test
    void testParse_bufferedReader_returnsNotNull() {
        // Arrange
        String s = "";
        BufferedReader br = new BufferedReader(new StringReader(s));

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res).isNotNull();
    }

    @Test
    void testParse_unrecognizedKey_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("unknownTestKey=This is the value");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "Unrecognized key \"{}\", value \"{}\"",
            "unknownTestKey",
            "This is the value"
        );
        assertThat(loggingEvents).containsExactly(expectedEvent);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "servers.live=someServer",
        "voice0=someValue",
    })
    void testParse_ignoredKey_doesNotLogWarning(String line) {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(line);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testParse_regularComment_doesNotLogWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(";should=not cause log message");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("dataProviderExpectedDefinitionComments")
    void testParse_expectedDefinitionComment_doesNotLogWarning(String input) {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(input);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("dataProviderNonMatchingDefinitionComment")
    void testParse_nonMatchingDefinitionComment_logsWarning(String input, String key, String definition) {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(input);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "Mismatch in definition comment for key \"{}\": \"{}\"",
            key,
            definition
        );
        assertThat(loggingEvents).containsExactly(expectedEvent);
    }

    @Test
    void testParse_unknownDefinitionComment_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("; somethingNew - we should inform user about the change");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.info(
            "Definition comment found for unknown key \"{}\": \"{}\"",
            "somethingNew",
            "we should inform user about the change"
        );
        assertThat(loggingEvents).containsExactly(expectedEvent);
    }

    @Test
    void testParse_unparsedComment_doesNotGetLogged() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(";Hello, I'm not parsed!");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testParse_emptyLine_doesNotGetLogged() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testParse_whiteSpace_doesNotGetLogged() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("   ");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testParse_unmatchedLine_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("some unexpected line");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "Uninterpretable line in network file: \"{}\"",
            "some unexpected line"
        );
        assertThat(loggingEvents).containsExactly(expectedEvent);
    }

    @Test
    void testParse_whazzUpString_doesNotLog() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("; 1234:WHATEVER - used by WhazzUp only\n1234:WHATEVER");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testParse_unexpectedWhazzUpFormatDefinition_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("; ABC.:12-34:.. - used by WhazzUp only");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "WhazzUp format may have changed, header definition: \"{}\"",
            "ABC.:12-34:.."
        );
        assertThat(loggingEvents).containsExactly(expectedEvent);
    }

    @Test
    void testParse_multipleWhazzUpLines_logWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("9876:TEST\n12345:TEST2");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn("Uninterpretable line in network file: \"{}\"", "12345:TEST2");
        assertThat(loggingEvents).containsExactly(expectedEvent);
    }

    @Test
    void testParse_whazzUpLineNotFirst_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("\n  \nurl0=http://test.com/1234\n9876:TEST");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn("Uninterpretable line in network file: \"{}\"", "9876:TEST");
        assertThat(loggingEvents).containsExactly(expectedEvent);
    }

    @Test
    void testParse_fullExample1_doesNotLog() {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformationParser.parse(br);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testParse_fullExample1_resultContainsExpectedWhazzUpString() {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res).extracting(NetworkInformation::getWhazzUpString)
                       .isEqualTo("1234:TEST");
    }

    @Test
    void testParse_fullExample1_resultContainsExpectedStartupMessages() {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res).extracting(NetworkInformation::getStartupMessages, as(LIST))
                       .containsExactly(
                           "This is line 1.",
                           "And here we got message line 2."
                       );
    }

    @SuppressWarnings("deprecation")
    @Test
    void testParse_fullExample1_resultContainsExpectedAtisURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res).extracting(NetworkInformation::getAtisUrls, as(LIST))
                       .containsExactly(new URL("http://www.could-be-anywhere.local/test.html"));
    }

    @Test
    void testParse_fullExample1_resultContainsExpectedLegacyDataFileURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> dataFileUrls = res.getDataUrls(DataFileFormat.LEGACY);
        assertThat(dataFileUrls).containsExactly(
            new URL("http://where-ever.com/fetchme.txt"),
            new URL("http://some.where.else/fetchme2.txt"),
            new URL("http://checking-misplaced.out.of.group/")
        );
    }

    @Test
    void testParse_fullExample1_resultContainsExpectedJsonDataFileURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> dataFileUrls = res.getDataUrls(DataFileFormat.JSON3);
        assertThat(dataFileUrls).containsExactly(
            new URL("http://where-ever.com/fetchme.json"),
            new URL("http://some.where.else/fetchme2.json"),
            new URL("http://checking-json-misplaced.out.of.group/")
        );
    }

    @Test
    void testParse_fullExample1_resultContainsExpectedMetarURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res).extracting(NetworkInformation::getMetarUrls, as(LIST))
                       .containsExactly(
                           new URL("http://someurl.com/test")
                       );
    }

    @Test
    void testParse_fullExample1_resultContainsExpectedMovedToURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res).extracting(NetworkInformation::getMovedToUrls, as(LIST))
                       .containsExactly(
                           new URL("http://go-and-ask.there/")
                       );
    }

    @Test
    void testParse_fullExample1_resultContainsExpectedServerFileURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res).extracting(NetworkInformation::getServersFileUrls, as(LIST))
                       .containsExactly(
                           new URL("https://theres-more.com/another-file.txt"),
                           new URL("http://and-again.de/check-this.dat"),
                           new URL("http://after-a-blank-line.de/we-continue.txt")
                       );
    }

    @Test
    void testParse_fullExample1_resultContainsExpectedUserStatisticsURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res).extracting(NetworkInformation::getUserStatisticsUrls, as(LIST))
                       .containsExactly(
                           new URL("https://stats-here.and.now/getStats.php")
                       );
    }

    @Test
    void testParse_startupMessagesNotDefined_resultHasNoStartupMessages() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("");

        // Assert
        assertThat(res).extracting(NetworkInformation::getStartupMessages, as(LIST))
                       .isEmpty();
    }

    @Test
    void testParse_expectedWhazzUpFormatWithPriorDefinition_resultContainsExpectedValue() {
        // Arrange
        String definition = "1234:EXAMPLE";
        String expected = "54321:WHATSTHAT";

        // Act
        NetworkInformation res = NetworkInformationParser.parse(
            "; " + definition + "         - used by WhazzUp only\n" + expected
        );

        // Assert
        assertThat(res).extracting(NetworkInformation::getWhazzUpString)
                       .isEqualTo(expected);
    }

    @Test
    void testParse_expectedWhazzUpFormatWithPriorDefinitionButNeverUsed_resultReturnsNull() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("; 13245:WONTSHOW - used by WhazzUp only");

        // Assert
        assertThat(res).extracting(NetworkInformation::getWhazzUpString)
                       .isNull();
    }

    @Test
    void testParse_unexpectedWhazzUpFormat_returnsNull() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("; ABC.:12-34:.. - used by WhazzUp only");

        // Assert
        assertThat(res).extracting(NetworkInformation::getWhazzUpString)
                       .isNull();
    }

    @Test
    void testParse_whazzUpLineFirstAfterWhitespace_resultContainsExpectedValue() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("\n  \n9876:TEST");

        // Assert
        assertThat(res).extracting(NetworkInformation::getWhazzUpString)
                       .isEqualTo("9876:TEST");
    }

    @Test
    void testParse_whazzUpLineNotFirst_resultReturnsNull() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("url0=http://test.com/1234\n9876:TEST");

        // Assert
        assertThat(res).extracting(NetworkInformation::getWhazzUpString)
                       .isNull();
    }

    @Test
    void testParse_multipleWhazzUpLines_resultReturnsFirstValue() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("9876:TEST\n12345:TEST2");

        // Assert
        assertThat(res).extracting(NetworkInformation::getWhazzUpString)
                       .isEqualTo("9876:TEST");
    }
}
