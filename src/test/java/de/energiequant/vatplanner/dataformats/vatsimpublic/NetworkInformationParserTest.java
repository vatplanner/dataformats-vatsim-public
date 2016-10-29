package de.energiequant.vatplanner.dataformats.vatsimpublic;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.Matchers.*;
import org.hamcrest.junit.ExpectedException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

@RunWith(DataProviderRunner.class)
public class NetworkInformationParserTest {
    private static final Logger logger = Logger.getLogger(NetworkInformationParserTest.class.getName());
    TestLogger testLogger = TestLoggerFactory.getTestLogger(NetworkInformationParser.class);
    
    /*
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    */
    
    @DataProvider
    public static String[] dataProviderExpectedDefinitionComments() {
        return new String[] {
            "; msg0         - message to be displayed at application startup"
        };
    }
    
    @DataProvider
    public static Object[][] dataProviderNonMatchingDefinitionComment() {
        return new Object[][] {
            new Object[] { "; msg0         - something unexpected", "msg0", "something unexpected" }
        };
    }
    
    @Before
    public void clearLog() {
        testLogger.clearAll();
    }
    
    private BufferedReader getBufferedReaderForTestResource(String resourceName) {
        String filePath = getClass().getResource("/NetworkInformation/"+resourceName).getFile();
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), Charset.forName("UTF-8")));
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "failed to initialize a BufferedReader for resource "+resourceName, ex);
            return null;
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
        LoggingEvent expectedEvent = LoggingEvent.warn("Unrecognized key \"{}\", value \"{}\"", "unknownTestKey", "This is the value");
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
        LoggingEvent expectedEvent = LoggingEvent.warn("Mismatch in definition comment for key \"{}\": \"{}\"", key, definition);
        assertThat(loggingEvents, contains(expectedEvent));
    }
    
    @Test
    public void testParse_unknownDefinitionComment_logsWarning() {
        NetworkInformationParser.parse("; somethingNew - we should inform user about the change");
        
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.info("Definition comment found for unknown key \"{}\": \"{}\"", "somethingNew", "we should inform user about the change");
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
        LoggingEvent expectedEvent = LoggingEvent.warn("Uninterpretable line in network file: \"{}\"", "some unexpected line");
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
        LoggingEvent expectedEvent = LoggingEvent.warn("WhazzUp format may have changed, header definition: \"{}\"", "ABC.:12-34:..");
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
    public void testParse_msg0DefinedByFullExample1_resultContainsExpectedStartupMessages() {
        BufferedReader br = getBufferedReaderForTestResource("fullexample1.txt");
        
        NetworkInformation res = NetworkInformationParser.parse(br);
        
        List<String> messagesStartup = res.getStartupMessages();
        List<String> expected = Arrays.asList("This is line 1.\nAnd here we got message line 2.".split("\n"));
        assertThat(messagesStartup, is(expected));
    }
    
    @Test
    public void testParse_msg0NotDefined_resultHasNoStartupMessages() {
        NetworkInformation res = NetworkInformationParser.parse("");
        
        List<String> messagesStartup = res.getStartupMessages();
        assertThat(messagesStartup, is(empty()));
    }
    
    @Test
    public void testParse_expectedWhazzUpFormatWithPriorDefinition_resultContainsExpectedValue() {
        String definition = "1234:EXAMPLE";
        String expected = "54321:WHATSTHAT";
        
        NetworkInformation res = NetworkInformationParser.parse("; "+definition+"         - used by WhazzUp only\n"+expected);
        
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
