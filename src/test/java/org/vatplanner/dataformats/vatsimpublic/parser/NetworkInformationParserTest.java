package org.vatplanner.dataformats.vatsimpublic.parser;

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
        String s = "";
        BufferedReader br = new BufferedReader(new StringReader(s));

        NetworkInformation res = NetworkInformationParser.parse(br);

        assertThat(res, is(notNullValue()));
    }

    @Test
    public void testParse_unrecognizedKey_logsWarning() {
        NetworkInformationParser.parse("unknownTestKey=This is the value");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "Unrecognized key \"{}\", value \"{}\"",
            "unknownTestKey",
            "This is the value" //
        );
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_regularComment_doesNotLogWarning() {
        NetworkInformationParser.parse(";should=not cause log message");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    @UseDataProvider("dataProviderExpectedDefinitionComments")
    public void testParse_expectedDefinitionComment_doesNotLogWarning(String input) {
        NetworkInformationParser.parse(input);

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    @UseDataProvider("dataProviderNonMatchingDefinitionComment")
    public void testParse_nonMatchingDefinitionComment_logsWarning(String input, String key, String definition) {
        NetworkInformationParser.parse(input);

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
        NetworkInformationParser.parse("; somethingNew - we should inform user about the change");

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
        NetworkInformationParser.parse(";Hello, I'm not parsed!");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_emptyLine_doesNotGetLogged() {
        NetworkInformationParser.parse("");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_whiteSpace_doesNotGetLogged() {
        NetworkInformationParser.parse("   ");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_unmatchedLine_logsWarning() {
        NetworkInformationParser.parse("some unexpected line");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "Uninterpretable line in network file: \"{}\"",
            "some unexpected line" //
        );
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_whazzUpString_doesNotLog() {
        NetworkInformationParser.parse("; 1234:WHATEVER - used by WhazzUp only\n1234:WHATEVER");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_unexpectedWhazzUpFormatDefinition_logsWarning() {
        NetworkInformationParser.parse("; ABC.:12-34:.. - used by WhazzUp only");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(
            "WhazzUp format may have changed, header definition: \"{}\"",
            "ABC.:12-34:.." //
        );
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_multipleWhazzUpLines_logWarning() {
        NetworkInformationParser.parse("9876:TEST\n12345:TEST2");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn("Uninterpretable line in network file: \"{}\"", "12345:TEST2");
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_whazzUpLineNotFirst_logsWarning() {
        NetworkInformationParser.parse("\n  \nurl0=http://test.com/1234\n9876:TEST");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn("Uninterpretable line in network file: \"{}\"", "9876:TEST");
        assertThat(loggingEvents, contains(expectedEvent));
    }

    @Test
    public void testParse_fullExample1_doesNotLog() {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedWhazzUpString() {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        String whazzUpString = res.getWhazzUpString();
        assertThat(whazzUpString, is("1234:TEST"));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedStartupMessages() {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        List<String> messagesStartup = res.getStartupMessages();
        List<String> expected = Arrays.asList("This is line 1.\nAnd here we got message line 2.".split("\n"));
        assertThat(messagesStartup, is(expected));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedAtisURLs() throws MalformedURLException {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        List<URL> atisUrls = res.getAtisUrls();

        assertThat(atisUrls.size(), is(1));
        assertThat(atisUrls.get(0), is(equalTo(new URL("http://www.could-be-anywhere.local/test.html"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedDataFileURLs() throws MalformedURLException {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        List<URL> dataFileUrls = res.getDataFileUrls();

        assertThat(dataFileUrls.size(), is(3));
        assertThat(dataFileUrls.get(0), is(equalTo(new URL("http://where-ever.com/fetchme.txt"))));
        assertThat(dataFileUrls.get(1), is(equalTo(new URL("http://some.where.else/fetchme2.txt"))));
        assertThat(dataFileUrls.get(2), is(equalTo(new URL("http://checking-misplaced.out.of.group/"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedMetarURLs() throws MalformedURLException {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        List<URL> metarUrls = res.getMetarUrls();

        assertThat(metarUrls.size(), is(1));
        assertThat(metarUrls.get(0), is(equalTo(new URL("http://someurl.com/test"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedMovedToURLs() throws MalformedURLException {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        List<URL> movedToUrls = res.getMovedToUrls();

        assertThat(movedToUrls.size(), is(1));
        assertThat(movedToUrls.get(0), is(equalTo(new URL("http://go-and-ask.there/"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedServerFileURLs() throws MalformedURLException {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        List<URL> serverFileUrls = res.getServersFileUrls();

        assertThat(serverFileUrls.size(), is(3));
        assertThat(serverFileUrls.get(0), is(equalTo(new URL("https://theres-more.com/another-file.txt"))));
        assertThat(serverFileUrls.get(1), is(equalTo(new URL("http://and-again.de/check-this.dat"))));
        assertThat(serverFileUrls.get(2), is(equalTo(new URL("http://after-a-blank-line.de/we-continue.txt"))));
    }

    @Test
    public void testParse_fullExample1_resultContainsExpectedUserStatisticsURLs() throws MalformedURLException {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");

        NetworkInformation res = NetworkInformationParser.parse(br);

        List<URL> userStatsUrls = res.getUserStatisticsUrls();

        assertThat(userStatsUrls.size(), is(1));
        assertThat(userStatsUrls.get(0), is(equalTo(new URL("https://stats-here.and.now/getStats.php"))));
    }

    @Test
    public void testParse_startupMessagesNotDefined_resultHasNoStartupMessages() {
        NetworkInformation res = NetworkInformationParser.parse("");

        List<String> messagesStartup = res.getStartupMessages();
        assertThat(messagesStartup, is(empty()));
    }

    @Test
    public void testParse_expectedWhazzUpFormatWithPriorDefinition_resultContainsExpectedValue() {
        String definition = "1234:EXAMPLE";
        String expected = "54321:WHATSTHAT";

        NetworkInformation res = NetworkInformationParser.parse(
            "; " + definition + "         - used by WhazzUp only\n" + expected //
        );

        String actual = res.getWhazzUpString();
        assertThat(actual, is(expected));
    }

    @Test
    public void testParse_expectedWhazzUpFormatWithPriorDefinitionButNeverUsed_resultReturnsNull() {
        NetworkInformation res = NetworkInformationParser.parse("; 13245:WONTSHOW - used by WhazzUp only");

        String actual = res.getWhazzUpString();
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testParse_unexpectedWhazzUpFormat_returnsNull() {
        NetworkInformation res = NetworkInformationParser.parse("; ABC.:12-34:.. - used by WhazzUp only");

        String actual = res.getWhazzUpString();
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testParse_whazzUpLineFirstAfterWhitespace_resultContainsExpectedValue() {
        NetworkInformation res = NetworkInformationParser.parse("\n  \n9876:TEST");

        assertThat(res.getWhazzUpString(), is("9876:TEST"));
    }

    @Test
    public void testParse_whazzUpLineNotFirst_resultReturnsNull() {
        NetworkInformation res = NetworkInformationParser.parse("url0=http://test.com/1234\n9876:TEST");

        assertThat(res.getWhazzUpString(), is(nullValue()));
    }

    @Test
    public void testParse_multipleWhazzUpLines_resultReturnsFirstValue() {
        NetworkInformation res = NetworkInformationParser.parse("9876:TEST\n12345:TEST2");

        String whazzUpString = res.getWhazzUpString();
        assertThat(whazzUpString, is("9876:TEST"));
    }
}
