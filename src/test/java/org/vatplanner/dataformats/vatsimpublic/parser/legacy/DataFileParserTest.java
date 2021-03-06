package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.vatplanner.dataformats.vatsimpublic.testutils.ParserLogEntryMatcher.matchesParserLogEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileFormat;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import org.vatplanner.dataformats.vatsimpublic.parser.FSDServer;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.VoiceServer;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

@RunWith(DataProviderRunner.class)
public class DataFileParserTest {

    private DataFileParser spyParser;
    private GeneralSectionParser mockGeneralSectionParser;
    private ClientParser mockOnlineClientParser;
    private ClientParser mockPrefileClientParser;
    private FSDServerParser mockFSDServerParser;
    private VoiceServerParser mockVoiceServerParser;

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(DataFileParser.class);

    private static final int HIGHEST_SUPPORTED_FORMAT_VERSION = 9;

    @Before
    public void setUp() {
        testLogger.clearAll();

        spyParser = spy(DataFileParser.class);

        mockGeneralSectionParser = mock(GeneralSectionParser.class);
        doReturn(mockGeneralSectionParser).when(spyParser).getGeneralSectionParser();

        mockOnlineClientParser = mock(ClientParser.class);
        doReturn(mockOnlineClientParser).when(spyParser).getOnlineClientParser();

        mockPrefileClientParser = mock(ClientParser.class);
        doReturn(mockPrefileClientParser).when(spyParser).getPrefileClientParser();

        Mockito.doAnswer((invocation) -> {
            ClientParser mockClientParser = mock(ClientParser.class);
            doReturn(mockClientParser).when(mockClientParser).setIsParsingPrefileSection(anyBoolean());
            return mockClientParser;
        }).when(spyParser).createClientParser();

        mockFSDServerParser = mock(FSDServerParser.class);
        doReturn(mockFSDServerParser).when(spyParser).getFSDServerParser();

        mockVoiceServerParser = mock(VoiceServerParser.class);
        doReturn(mockVoiceServerParser).when(spyParser).getVoiceServerParser();
    }

    @Test
    public void testCreateClientParser_secondCall_returnsNewInstance() {
        // Arrange
        doCallRealMethod().when(spyParser).createClientParser();

        ClientParser firstResult = spyParser.createClientParser();

        // Act
        ClientParser secondResult = spyParser.createClientParser();

        // Assert
        assertThat(secondResult, is(not(sameInstance(firstResult))));
    }

    @Test
    public void testGetGeneralSectionParser_always_doesNotReturnNull() {
        // Arrange
        doCallRealMethod().when(spyParser).getGeneralSectionParser();

        // Act
        GeneralSectionParser result = spyParser.getGeneralSectionParser();

        // Assert
        assertThat(result, is(not(nullValue())));
    }

    @Test
    public void testGetFSDServerParser_always_doesNotReturnNull() {
        // Arrange
        doCallRealMethod().when(spyParser).getFSDServerParser();

        // Act
        FSDServerParser result = spyParser.getFSDServerParser();

        // Assert
        assertThat(result, is(not(nullValue())));
    }

    @Test
    public void testGetVoiceServerParser_always_doesNotReturnNull() {
        // Arrange
        doCallRealMethod().when(spyParser).getVoiceServerParser();

        // Act
        VoiceServerParser result = spyParser.getVoiceServerParser();

        // Assert
        assertThat(result, is(not(nullValue())));
    }

    @Test
    @DataProvider({ "", "  ab \r cd  \r\n ef \n g " })
    public void testDeserialize_CharSequence_forwardsInputWrappedInBufferedReader(String s) throws IOException {
        // Arrange
        AtomicReference<String> capturedString = new AtomicReference<String>();
        doAnswer(invocation -> {
            BufferedReader br = invocation.getArgument(0);
            capturedString.set(readBuffer(br, s.length() + 1));
            return null;
        }).when(spyParser).deserialize(any(Reader.class));

        // Act
        spyParser.deserialize((CharSequence) s);

        // Assert
        assertThat(capturedString.get(), is(equalTo(s)));
    }

    @Test
    public void testDeserialize_CharSequence_returnsResultFromBufferedReaderParser() {
        // Arrange
        DataFile expectedObject = new DataFile();
        doReturn(expectedObject).when(spyParser).deserialize(any(BufferedReader.class));

        // Act
        DataFile result = spyParser.deserialize((CharSequence) "");

        // Assert
        assertThat(result, is(sameInstance(expectedObject)));
    }

    @Test
    public void testDeserialize_always_returnsDataFileWithFormatIndicatedAsLegacy() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        doReturn(null).when(mockGeneralSectionParser).parse(any(Collection.class), any(ParserLogEntryCollector.class),
            anyString());

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile.getFormat(), is(equalTo(DataFileFormat.LEGACY)));
    }

    @Test
    public void testDeserialize_generalSection_forwardsCleanedSectionRelevantLinesToGeneralSectionParserExactlyOnce() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL", "123", "456");

        // Act
        spyParser.deserialize(lines);

        // Assert
        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mockGeneralSectionParser, times(1)).parse( //
            captor.capture(), //
            any(ParserLogEntryCollector.class),
            anyString() //
        );
        Collection<String> capturedCollection = captor.getValue();

        assertThat(capturedCollection, containsInAnyOrder("123", "456"));
    }

    @Test
    public void testDeserialize_generalSection_forwardsDataFileAsLogCollectorToGeneralSectionParserExactlyOnce() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL", "123", "456");

        DataFile mockDataFile = mock(DataFile.class);
        doReturn(mockDataFile).when(spyParser).createDataFile();

        // Act
        spyParser.deserialize(lines);

        // Assert
        verify(mockGeneralSectionParser, times(1)).parse( //
            any(Collection.class), //
            Mockito.same(mockDataFile), //
            anyString() //
        );
    }

    @Test
    public void testDeserialize_generalSection_forwardsSectionNameToGeneralSectionParserExactlyOnce() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL", "123", "456");

        DataFile mockDataFile = mock(DataFile.class);
        doReturn(mockDataFile).when(spyParser).createDataFile();

        // Act
        spyParser.deserialize(lines);

        // Assert
        verify(mockGeneralSectionParser, times(1)).parse( //
            any(Collection.class), //
            any(ParserLogEntryCollector.class), //
            Mockito.eq("GENERAL") //
        );
    }

    @Test
    public void testDeserialize_generalSection_returnsExpectedDataFile() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL", "123", "456");

        DataFile mockDataFile = mock(DataFile.class);
        doReturn(mockDataFile).when(spyParser).createDataFile();

        // Act
        DataFile result = spyParser.deserialize(lines);

        // Assert
        assertThat(result, is(sameInstance(mockDataFile)));
    }

    @Test
    public void testDeserialize_generalSection_createsExactlyOneDataFile() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL", "123", "456");

        // Act
        spyParser.deserialize(lines);

        // Assert
        verify(spyParser, times(1)).createDataFile();
    }

    @Test
    public void testDeserialize_generalSection_returnsDataFileWithResultFromGeneralSectionParserAsMetaData() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData expectedMetaData = new DataFileMetaData();
        doReturn(expectedMetaData).when(mockGeneralSectionParser).parse( //
            any(Collection.class), //
            any(ParserLogEntryCollector.class), //
            anyString() //
        );

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile.getMetaData(), is(sameInstance(expectedMetaData)));
    }

    @Test
    public void testDeserialize_generalSectionMissingMetaData_logsWarningToSLF4J() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        doReturn(null).when(mockGeneralSectionParser).parse(any(Collection.class), any(ParserLogEntryCollector.class),
            anyString());

        // Act
        spyParser.deserialize(lines);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn( //
            "unable to verify data format version, metadata is unavailable (null)" //
        );
        assertThat(loggingEvents, hasItem(expectedEvent));
    }

    @Test
    public void testDeserialize_generalSectionMissingMetaData_logsToDataFile() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        doReturn(null).when(mockGeneralSectionParser).parse(any(Collection.class), any(ParserLogEntryCollector.class),
            anyString());

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat( //
            entries, //
            hasItem(
                matchesParserLogEntry(
                    equalTo("GENERAL"), //
                    nullValue(String.class), //
                    equalTo(false), //
                    equalTo("unable to verify data format version, metadata is unavailable (null)"), //
                    nullValue(Throwable.class) //
                ) //
            ) //
        );
    }

    @Test
    @DataProvider({
        "-1, metadata reports unsupported format version -1 (supported: 8..9)",
        "7, metadata reports unsupported format version 7 (supported: 8..9)",
        "10, metadata reports unsupported format version 10 (supported: 8..9)"
    })
    public void testDeserialize_generalSectionListsUnsupportedFormatVersion_logsWarningToSLF4J(int unsupportedFormatVersion, String expectedMessage) {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData mockMetaData = mockMetaDataWithFormatVersion(unsupportedFormatVersion);
        doReturn(mockMetaData).when(mockGeneralSectionParser).parse( //
            any(Collection.class), //
            any(ParserLogEntryCollector.class), //
            anyString() //
        );

        // Act
        spyParser.deserialize(lines);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(expectedMessage);
        assertThat(loggingEvents, hasItem(expectedEvent));
    }

    @Test
    @DataProvider({
        "-1, metadata reports unsupported format version -1 (supported: 8..9)",
        "7, metadata reports unsupported format version 7 (supported: 8..9)",
        "10, metadata reports unsupported format version 10 (supported: 8..9)"
    })
    public void testDeserialize_generalSectionListsUnsupportedFormatVersion_logsToDataFile(int unsupportedFormatVersion, String expectedMessage) {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData mockMetaData = mockMetaDataWithFormatVersion(unsupportedFormatVersion);
        doReturn(mockMetaData).when(mockGeneralSectionParser).parse( //
            any(Collection.class), //
            any(ParserLogEntryCollector.class), //
            anyString() //
        );

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat( //
            entries, //
            hasItem( //
                matchesParserLogEntry( //
                    equalTo("GENERAL"), //
                    nullValue(String.class), //
                    equalTo(false), //
                    equalTo(expectedMessage), //
                    nullValue(Throwable.class) //
                ) //
            ) //
        );
    }

    @DataProvider
    public static Object[][] dataProviderSupportedFormatVersions() {
        return new Object[][] { { 8 }, { 9 } };
    }

    @Test
    @UseDataProvider("dataProviderSupportedFormatVersions")
    public void testDeserialize_generalSectionListsSupportedFormatVersion_doesNotLogsWarningToSLF4J(int supportedFormatVersion) {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData mockMetaData = mockMetaDataWithFormatVersion(supportedFormatVersion);
        doReturn(mockMetaData).when(mockGeneralSectionParser).parse( //
            any(Collection.class), //
            any(ParserLogEntryCollector.class), //
            anyString() //
        );

        // Act
        spyParser.deserialize(lines);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(empty()));
    }

    @Test
    @UseDataProvider("dataProviderSupportedFormatVersions")
    public void testDeserialize_generalSectionListsSupportedFormatVersion_doesNotLogToDataFile(int supportedFormatVersion) {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData mockMetaData = mockMetaDataWithFormatVersion(supportedFormatVersion);
        doReturn(mockMetaData).when(mockGeneralSectionParser).parse(//
            any(Collection.class), //
            any(ParserLogEntryCollector.class), //
            anyString() //
        );

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries, is(empty()));
    }

    @Test
    public void testGetOnlineClientParser_always_parserIsSetToNotPrefileSection() {
        // Arrange
        doCallRealMethod().when(spyParser).getOnlineClientParser();

        // Act
        ClientParser mockResult = spyParser.getOnlineClientParser();

        // Assert
        verify(mockResult).setIsParsingPrefileSection(Mockito.eq(false));
    }

    @Test
    public void testGetPrefileClientParser_always_parserIsSetToPrefileSection() {
        // Arrange
        doCallRealMethod().when(spyParser).getPrefileClientParser();

        // Act
        ClientParser mockResult = spyParser.getPrefileClientParser();

        // Assert
        verify(mockResult).setIsParsingPrefileSection(Mockito.eq(true));
    }

    @Test
    public void testDeserialize_clientsSectionThrowsIllegalArgumentException_returnsDataFileWithNonExceptionalResultsFromOnlineClientParser() {
        // Arrange
        String lines = buildDataFileForSection("CLIENTS", ":expected line:1:", "trigger error 1", ":expected line:2:",
            "trigger error 2");

        Client mockExpected1 = mock(Client.class);
        doReturn(mockExpected1).when(mockOnlineClientParser).parse(Mockito.eq(":expected line:1:"));

        Client mockExpected2 = mock(Client.class);
        doReturn(mockExpected2).when(mockOnlineClientParser).parse(Mockito.eq(":expected line:2:"));

        doThrow(new IllegalArgumentException("some error")) //
            .when(mockOnlineClientParser)
            .parse(Mockito.startsWith("trigger error"));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile.getClients(), containsInAnyOrder(mockExpected1, mockExpected2));
    }

    @Test
    public void testDeserialize_clientsSectionThrowsIllegalArgumentExceptions_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "CLIENTS";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection(section, ":expected line:1:", triggerLine1, ":expected line:2:",
            triggerLine2);

        doReturn(mockMetaDataWithFormatVersion(HIGHEST_SUPPORTED_FORMAT_VERSION))
            .when(mockGeneralSectionParser)
            .parse(Mockito.any(), Mockito.any(), Mockito.anyString());
        doReturn(mock(Client.class)).when(mockOnlineClientParser).parse(Mockito.anyString());

        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockOnlineClientParser).parse(Mockito.eq(triggerLine1));

        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockOnlineClientParser).parse(Mockito.eq(triggerLine2));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(//
            entries, //
            containsInAnyOrder(
                matchesParserLogEntry(
                    equalTo(section),
                    equalTo(triggerLine1),
                    equalTo(true),
                    containsString(exception1.getMessage()),
                    sameInstance(exception1)),
                matchesParserLogEntry(
                    equalTo(section),
                    equalTo(triggerLine2),
                    equalTo(true),
                    containsString(exception2.getMessage()),
                    sameInstance(exception2) //
                ) //
            ) //
        );
    }

    @Test
    public void testDeserialize_prefileSectionThrowsIllegalArgumentExceptions_returnsDataFileWithNonExceptionResultsFromPrefileClientParser() {
        // Arrange
        String lines = buildDataFileForSection( //
            "PREFILE", //
            ":expected line:1:", //
            "trigger error 1", //
            ":expected line:2:", //
            "trigger error 2" //
        );

        Client mockExpected1 = mock(Client.class);
        doReturn(mockExpected1).when(mockPrefileClientParser).parse(Mockito.eq(":expected line:1:"));

        Client mockExpected2 = mock(Client.class);
        doReturn(mockExpected2).when(mockPrefileClientParser).parse(Mockito.eq(":expected line:2:"));

        doThrow(new IllegalArgumentException("some error"))
            .when(mockPrefileClientParser)
            .parse(Mockito.startsWith("trigger error"));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile.getClients(), containsInAnyOrder(mockExpected1, mockExpected2));
    }

    @Test
    public void testDeserialize_prefileSectionThrowsIllegalArgumentExceptions_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "PREFILE";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection( //
            section, //
            ":expected line:1:", //
            triggerLine1, //
            ":expected line:2:", //
            triggerLine2 //
        );

        doReturn(mockMetaDataWithFormatVersion(HIGHEST_SUPPORTED_FORMAT_VERSION))
            .when(mockGeneralSectionParser)
            .parse(Mockito.any(), Mockito.any(), Mockito.anyString());
        doReturn(mock(Client.class)).when(mockPrefileClientParser).parse(Mockito.anyString());

        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockPrefileClientParser).parse(Mockito.eq(triggerLine1));

        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockPrefileClientParser).parse(Mockito.eq(triggerLine2));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(
            entries,
            containsInAnyOrder(
                matchesParserLogEntry(
                    equalTo(section),
                    equalTo(triggerLine1),
                    equalTo(true),
                    containsString(exception1.getMessage()),
                    sameInstance(exception1)),
                matchesParserLogEntry(
                    equalTo(section),
                    equalTo(triggerLine2),
                    equalTo(true),
                    containsString(exception2.getMessage()),
                    sameInstance(exception2) //
                ) //
            ) //
        );
    }

    @Test
    public void testDeserialize_serversSectionThrowsIllegalArgumentException_returnsDataFileWithNonExceptionalResultsFromFSDServerParser() {
        // Arrange
        String lines = buildDataFileForSection(
            "SERVERS",
            ":expected line:1:",
            "trigger error 1",
            ":expected line:2:",
            "trigger error 2" //
        );

        FSDServer mockExpected1 = mock(FSDServer.class);
        doReturn(mockExpected1).when(mockFSDServerParser).parse(Mockito.eq(":expected line:1:"));

        FSDServer mockExpected2 = mock(FSDServer.class);
        doReturn(mockExpected2).when(mockFSDServerParser).parse(Mockito.eq(":expected line:2:"));

        doThrow(new IllegalArgumentException("some error"))
            .when(mockFSDServerParser)
            .parse(Mockito.startsWith("trigger error"));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile.getFsdServers(), containsInAnyOrder(mockExpected1, mockExpected2));
    }

    @Test
    public void testDeserialize_serversSectionThrowsIllegalArgumentException_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "SERVERS";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection("SERVERS", ":expected line:1:", triggerLine1, ":expected line:2:",
            triggerLine2);

        doReturn(mockMetaDataWithFormatVersion(HIGHEST_SUPPORTED_FORMAT_VERSION))
            .when(mockGeneralSectionParser)
            .parse(Mockito.any(), Mockito.any(), Mockito.anyString());
        doReturn(mock(FSDServer.class)).when(mockFSDServerParser).parse(Mockito.anyString());

        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockFSDServerParser).parse(Mockito.eq(triggerLine1));

        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockFSDServerParser).parse(Mockito.eq(triggerLine2));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(
            entries,
            containsInAnyOrder(
                matchesParserLogEntry(
                    equalTo(section),
                    equalTo(triggerLine1),
                    equalTo(true),
                    containsString(exception1.getMessage()),
                    sameInstance(exception1)),
                matchesParserLogEntry(
                    equalTo(section),
                    equalTo(triggerLine2),
                    equalTo(true),
                    containsString(exception2.getMessage()),
                    sameInstance(exception2) //
                ) //
            ) //
        );
    }

    @Test
    public void testDeserialize_voiceServersSectionThrowsIllegalArgumentException_returnsDataFileWithNonExceptionalResultsFromVoiceServerParser() {
        // Arrange
        String lines = buildDataFileForSection(
            "VOICE SERVERS",
            ":expected line:1:",
            "trigger error 1",
            ":expected line:2:",
            "trigger error 2" //
        );

        VoiceServer mockExpected1 = mock(VoiceServer.class);
        doReturn(mockExpected1).when(mockVoiceServerParser).parse(Mockito.eq(":expected line:1:"));

        VoiceServer mockExpected2 = mock(VoiceServer.class);
        doReturn(mockExpected2).when(mockVoiceServerParser).parse(Mockito.eq(":expected line:2:"));

        doThrow(new IllegalArgumentException("some error"))
            .when(mockVoiceServerParser)
            .parse(Mockito.startsWith("trigger error"));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile.getVoiceServers(), containsInAnyOrder(mockExpected1, mockExpected2));
    }

    @Test
    public void testDeserialize_voiceServersSectionThrowsIllegalArgumentException_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "VOICE SERVERS";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection(
            "VOICE SERVERS",
            ":expected line:1:",
            "trigger error 1",
            ":expected line:2:",
            "trigger error 2" //
        );

        doReturn(mockMetaDataWithFormatVersion(HIGHEST_SUPPORTED_FORMAT_VERSION)).when(mockGeneralSectionParser)
            .parse(Mockito.any(), Mockito.any(), Mockito.anyString());
        doReturn(mock(VoiceServer.class)).when(mockVoiceServerParser).parse(Mockito.anyString());

        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockVoiceServerParser).parse(Mockito.eq(triggerLine1));

        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockVoiceServerParser).parse(Mockito.eq(triggerLine2));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(
            entries,
            containsInAnyOrder(
                matchesParserLogEntry(
                    equalTo(section),
                    equalTo(triggerLine1),
                    equalTo(true),
                    containsString(exception1.getMessage()),
                    sameInstance(exception1)),
                matchesParserLogEntry(
                    equalTo(section),
                    equalTo(triggerLine2),
                    equalTo(true),
                    containsString(exception2.getMessage()),
                    sameInstance(exception2) //
                ) //
            ) //
        );
    }

    private String buildDataFileForSection(String sectionName, String... sectionRelevantLines) {
        return buildDataFileForSection(sectionName, Arrays.asList(sectionRelevantLines));
    }

    private String buildDataFileForSection(String sectionName, List<String> sectionRelevantLines) {
        // Arrange
        String lines = "something before\r\n"
            + ";!" + sectionName + ":\r\n"
            + "decoy before\r\n"
            + "!" + sectionName + ":\r\n"
            + "\r\n"
            + ((sectionRelevantLines.size() < 1) ? "" : sectionRelevantLines.get(0) + "\r\n")
            + "\r\n"
            + "   \r\n"
            + ";comment\r\n"
            + ";!COMMENT:\r\n"
            + ((sectionRelevantLines.size() < 2) ? ""
                : String.join("\r\n", sectionRelevantLines.subList(1, sectionRelevantLines.size())))
            + "\r\n"
            + ";!" + sectionName + ":\r\n"
            + "!SOMETHINGELSE:\r\n"
            + "something after\r\n";
        return lines;
    }

    private String readBuffer(BufferedReader br, int maxLength) throws IOException {
        char[] buffer = new char[maxLength];
        int read = br.read(buffer);
        String brContent = read < 1 ? "" : new String(Arrays.copyOfRange(buffer, 0, read));
        return brContent;
    }

    private DataFileMetaData mockMetaDataWithFormatVersion(int version) {
        DataFileMetaData mockMetaData = mock(DataFileMetaData.class);
        doReturn(version).when(mockMetaData).getVersionFormat();

        return mockMetaData;
    }
}
