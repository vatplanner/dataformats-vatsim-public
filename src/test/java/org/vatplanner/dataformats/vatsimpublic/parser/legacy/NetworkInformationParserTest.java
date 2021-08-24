package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileFormat;
import org.vatplanner.dataformats.vatsimpublic.parser.NetworkInformation;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

@RunWith(DataProviderRunner.class)
public class NetworkInformationParserTest {

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(NetworkInformationParser.class);

    @DataProvider
    public static String[] dataProviderExpectedDefinitionComments() {
        return new String[] {
            "; msg0         - message to be displayed at application startup"
        };
    }

    @DataProvider
    public static Object[][] dataProviderNonMatchingDefinitionComment() {
        return new Object[][] {
            { "; msg0         - something unexpected", "msg0", "something unexpected" }
        };
    }

    @Before
    public void clearLog() {
        testLogger.clearAll();
    }

    private BufferedReader getBufferedReaderForTestResource(String resourceName) {
        String filePath = getClass().getResource("/NetworkInformation/" + resourceName).getFile();
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), Charset.forName("UTF-8")));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("failed to initialize a BufferedReader for resource " + resourceName, ex);
        }
    }

    @Test
    public void testParse_bufferedReader_returnsNotNull() {
        // Arrange
        String s = "";
        BufferedReader br = new BufferedReader(new StringReader(s));

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        assertThat(res, is(notNullValue()));
    }

    @Test
    public void testParse_unrecognizedKey_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("unknownTestKey=This is the value");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "Unrecognized key \"{}\", value \"{}\"",
            "unknownTestKey",
            "This is the value" //
        );
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    @DataProvider({ //
        "servers.live=someServer", //
        "voice0=someValue", //
    })
    public void testParse_ignoredKey_doesNotLogWarning(String line) {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(line);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_regularComment_doesNotLogWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(";should=not cause log message");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    @UseDataProvider("dataProviderExpectedDefinitionComments")
    public void testParse_expectedDefinitionComment_doesNotLogWarning(String input) {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(input);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    @UseDataProvider("dataProviderNonMatchingDefinitionComment")
    public void testParse_nonMatchingDefinitionComment_logsWarning(String input, String key, String definition) {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(input);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "Mismatch in definition comment for key \"{}\": \"{}\"",
            key,
            definition //
        );
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_unknownDefinitionComment_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("; somethingNew - we should inform user about the change");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.info(
            "Definition comment found for unknown key \"{}\": \"{}\"",
            "somethingNew",
            "we should inform user about the change" //
        );
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_unparsedComment_doesNotGetLogged() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse(";Hello, I'm not parsed!");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_emptyLine_doesNotGetLogged() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_whiteSpace_doesNotGetLogged() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("   ");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_unmatchedLine_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("some unexpected line");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "Uninterpretable line in network file: \"{}\"",
            "some unexpected line" //
        );
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_whazzUpString_doesNotLog() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("; 1234:WHATEVER - used by WhazzUp only\n1234:WHATEVER");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_unexpectedWhazzUpFormatDefinition_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("; ABC.:12-34:.. - used by WhazzUp only");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "WhazzUp format may have changed, header definition: \"{}\"",
            "ABC.:12-34:.." //
        );
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_multipleWhazzUpLines_logWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("9876:TEST\n12345:TEST2");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn("Uninterpretable line in network file: \"{}\"", "12345:TEST2");
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_whazzUpLineNotFirst_logsWarning() {
        // Arrange (nothing to do)

        // Act
        NetworkInformationParser.parse("\n  \nurl0=http://test.com/1234\n9876:TEST");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn("Uninterpretable line in network file: \"{}\"", "9876:TEST");
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_fullExample1_doesNotLog() {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedWhazzUpString() {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        String whazzUpString = res.getWhazzUpString();
        assertThat(whazzUpString, is("1234:TEST"));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedStartupMessages() {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<String> messagesStartup = res.getStartupMessages();
        List<String> expected = Arrays.asList("This is line 1.\nAnd here we got message line 2.".split("\n"));
        assertThat(messagesStartup, is(expected));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedAtisURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> atisUrls = res.getAtisUrls();

        assertThat(atisUrls.size(), is(1));
        assertThat(atisUrls.get(0), is(equalTo(new URL("http://www.could-be-anywhere.local/test.html"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedLegacyDataFileURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> dataFileUrls = res.getDataUrls(DataFileFormat.LEGACY);

        assertThat(dataFileUrls.size(), is(3));
        assertThat(dataFileUrls.get(0), is(equalTo(new URL("http://where-ever.com/fetchme.txt"))));
        assertThat(dataFileUrls.get(1), is(equalTo(new URL("http://some.where.else/fetchme2.txt"))));
        assertThat(dataFileUrls.get(2), is(equalTo(new URL("http://checking-misplaced.out.of.group/"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedJsonDataFileURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> dataFileUrls = res.getDataUrls(DataFileFormat.JSON3);

        assertThat(dataFileUrls.size(), is(3));
        assertThat(dataFileUrls.get(0), is(equalTo(new URL("http://where-ever.com/fetchme.json"))));
        assertThat(dataFileUrls.get(1), is(equalTo(new URL("http://some.where.else/fetchme2.json"))));
        assertThat(dataFileUrls.get(2), is(equalTo(new URL("http://checking-json-misplaced.out.of.group/"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedMetarURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> metarUrls = res.getMetarUrls();

        assertThat(metarUrls.size(), is(1));
        assertThat(metarUrls.get(0), is(equalTo(new URL("http://someurl.com/test"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedMovedToURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> movedToUrls = res.getMovedToUrls();

        assertThat(movedToUrls.size(), is(1));
        assertThat(movedToUrls.get(0), is(equalTo(new URL("http://go-and-ask.there/"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedServerFileURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> serverFileUrls = res.getServersFileUrls();

        assertThat(serverFileUrls.size(), is(3));
        assertThat(serverFileUrls.get(0), is(equalTo(new URL("https://theres-more.com/another-file.txt"))));
        assertThat(serverFileUrls.get(1), is(equalTo(new URL("http://and-again.de/check-this.dat"))));
        assertThat(serverFileUrls.get(2), is(equalTo(new URL("http://after-a-blank-line.de/we-continue.txt"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedUserStatisticsURLs() throws MalformedURLException {
        // Arrange
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        // Act
        NetworkInformation res = NetworkInformationParser.parse(br);

        // Assert
        List<URL> userStatsUrls = res.getUserStatisticsUrls();

        assertThat(userStatsUrls.size(), is(1));
        assertThat(userStatsUrls.get(0), is(equalTo(new URL("https://stats-here.and.now/getStats.php"))));
    }

    @Test
    public void testParse_startupMessagesNotDefined_resultHasNoStartupMessages() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("");

        // Assert
        List<String> messagesStartup = res.getStartupMessages();
        assertThat(messagesStartup, is(empty()));
    }

    @Test
    public void testParse_expectedWhazzUpFormatWithPriorDefinition_resultContainsExpectedValue() {
        // Arrange
        String definition = "1234:EXAMPLE";
        String expected = "54321:WHATSTHAT";

        // Act
        NetworkInformation res = NetworkInformationParser.parse(
            "; " + definition + "         - used by WhazzUp only\n" + expected //
        );

        // Assert
        String actual = res.getWhazzUpString();
        assertThat(actual, is(expected));
    }

    @Test
    public void testParse_expectedWhazzUpFormatWithPriorDefinitionButNeverUsed_resultReturnsNull() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("; 13245:WONTSHOW - used by WhazzUp only");

        // Assert
        String actual = res.getWhazzUpString();
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testParse_unexpectedWhazzUpFormat_returnsNull() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("; ABC.:12-34:.. - used by WhazzUp only");

        // Assert
        String actual = res.getWhazzUpString();
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testParse_whazzUpLineFirstAfterWhitespace_resultContainsExpectedValue() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("\n  \n9876:TEST");

        // Assert
        assertThat(res.getWhazzUpString(), is("9876:TEST"));
    }

    @Test
    public void testParse_whazzUpLineNotFirst_resultReturnsNull() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("url0=http://test.com/1234\n9876:TEST");

        // Assert
        assertThat(res.getWhazzUpString(), is(nullValue()));
    }

    @Test
    public void testParse_multipleWhazzUpLines_resultReturnsFirstValue() {
        // Arrange (nothing to do)

        // Act
        NetworkInformation res = NetworkInformationParser.parse("9876:TEST\n12345:TEST2");

        // Assert
        String whazzUpString = res.getWhazzUpString();
        assertThat(whazzUpString, is("9876:TEST"));
    }
}
