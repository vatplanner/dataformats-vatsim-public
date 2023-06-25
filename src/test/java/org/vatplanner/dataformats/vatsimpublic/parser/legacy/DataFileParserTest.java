package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.vatplanner.dataformats.vatsimpublic.testutils.ParserLogEntryAssert.assertThatParserLogEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

class DataFileParserTest {

    private DataFileParser spyParser;
    private GeneralSectionParser mockGeneralSectionParser;
    private ClientParser mockOnlineClientParser;
    private ClientParser mockPrefileClientParser;
    private FSDServerParser mockFSDServerParser;
    private VoiceServerParser mockVoiceServerParser;

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(DataFileParser.class);

    private static final int HIGHEST_SUPPORTED_FORMAT_VERSION = 9;

    @BeforeEach
    void setUp() {
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
    void testCreateClientParser_secondCall_returnsNewInstance() {
        // Arrange
        doCallRealMethod().when(spyParser).createClientParser();

        ClientParser firstResult = spyParser.createClientParser();

        // Act
        ClientParser secondResult = spyParser.createClientParser();

        // Assert
        assertThat(secondResult).isNotSameAs(firstResult);
    }

    @Test
    void testGetGeneralSectionParser_always_doesNotReturnNull() {
        // Arrange
        doCallRealMethod().when(spyParser).getGeneralSectionParser();

        // Act
        GeneralSectionParser result = spyParser.getGeneralSectionParser();

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void testGetFSDServerParser_always_doesNotReturnNull() {
        // Arrange
        doCallRealMethod().when(spyParser).getFSDServerParser();

        // Act
        FSDServerParser result = spyParser.getFSDServerParser();

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void testGetVoiceServerParser_always_doesNotReturnNull() {
        // Arrange
        doCallRealMethod().when(spyParser).getVoiceServerParser();

        // Act
        VoiceServerParser result = spyParser.getVoiceServerParser();

        // Assert
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ab \r cd  \r\n ef \n g "})
    void testDeserialize_CharSequence_forwardsInputWrappedInBufferedReader(String s) {
        // Arrange
        AtomicReference<String> capturedString = new AtomicReference<>();
        doAnswer(invocation -> {
            BufferedReader br = invocation.getArgument(0);
            capturedString.set(readBuffer(br, s.length() + 1));
            return null;
        }).when(spyParser).deserialize(any(Reader.class));

        // Act
        spyParser.deserialize(s);

        // Assert
        assertThat(capturedString).hasValue(s);
    }

    @Test
    void testDeserialize_CharSequence_returnsResultFromBufferedReaderParser() {
        // Arrange
        DataFile expectedObject = new DataFile();
        doReturn(expectedObject).when(spyParser).deserialize(any(BufferedReader.class));

        // Act
        DataFile result = spyParser.deserialize("");

        // Assert
        assertThat(result).isSameAs(expectedObject);
    }

    @Test
    void testDeserialize_always_returnsDataFileWithFormatIndicatedAsLegacy() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        doReturn(null).when(mockGeneralSectionParser).parse(anyCollection(), any(ParserLogEntryCollector.class),
                                                            anyString()
        );

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile).extracting(DataFile::getFormat)
                            .isSameAs(DataFileFormat.LEGACY);
    }

    @Test
    void testDeserialize_generalSection_forwardsCleanedSectionRelevantLinesToGeneralSectionParserExactlyOnce() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL",
                                               "123",
                                               "456"
        );

        // Act
        spyParser.deserialize(lines);

        // Assert
        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mockGeneralSectionParser, times(1)).parse( //
                                                          captor.capture(),
                                                          any(ParserLogEntryCollector.class),
                                                          anyString() //
        );
        Collection<String> capturedCollection = captor.getValue();

        assertThat(capturedCollection).containsExactlyInAnyOrder("123", "456");
    }

    @Test
    void testDeserialize_generalSection_forwardsDataFileAsLogCollectorToGeneralSectionParserExactlyOnce() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL",
                                               "123",
                                               "456"
        );

        DataFile mockDataFile = mock(DataFile.class);
        doReturn(mockDataFile).when(spyParser).createDataFile();

        // Act
        spyParser.deserialize(lines);

        // Assert
        verify(mockGeneralSectionParser, times(1)).parse( //
                                                          anyCollection(),
                                                          Mockito.same(mockDataFile),
                                                          anyString()
        );
    }

    @Test
    void testDeserialize_generalSection_forwardsSectionNameToGeneralSectionParserExactlyOnce() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL",
                                               "123",
                                               "456"
        );

        DataFile mockDataFile = mock(DataFile.class);
        doReturn(mockDataFile).when(spyParser).createDataFile();

        // Act
        spyParser.deserialize(lines);

        // Assert
        verify(mockGeneralSectionParser, times(1)).parse( //
                                                          anyCollection(),
                                                          any(ParserLogEntryCollector.class),
                                                          eq("GENERAL")
        );
    }

    @Test
    void testDeserialize_generalSection_returnsExpectedDataFile() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL",
                                               "123",
                                               "456"
        );

        DataFile mockDataFile = mock(DataFile.class);
        doReturn(mockDataFile).when(spyParser).createDataFile();

        // Act
        DataFile result = spyParser.deserialize(lines);

        // Assert
        assertThat(result).isSameAs(mockDataFile);
    }

    @Test
    void testDeserialize_generalSection_createsExactlyOneDataFile() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL",
                                               "123",
                                               "456"
        );

        // Act
        spyParser.deserialize(lines);

        // Assert
        verify(spyParser, times(1)).createDataFile();
    }

    @Test
    void testDeserialize_generalSection_returnsDataFileWithResultFromGeneralSectionParserAsMetaData() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData expectedMetaData = new DataFileMetaData();
        doReturn(expectedMetaData).when(mockGeneralSectionParser).parse( //
                                                                         anyCollection(),
                                                                         any(ParserLogEntryCollector.class),
                                                                         anyString()
        );

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile).extracting(DataFile::getMetaData)
                            .isSameAs(expectedMetaData);
    }

    @Test
    void testDeserialize_generalSectionMissingMetaData_logsWarningToSLF4J() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        doReturn(null).when(mockGeneralSectionParser).parse( //
                                                             anyCollection(),
                                                             any(ParserLogEntryCollector.class),
                                                             anyString()
        );

        // Act
        spyParser.deserialize(lines);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn( //
                                                        "unable to verify data format version, metadata is unavailable (null)" //
        );
        assertThat(loggingEvents).contains(expectedEvent);
    }

    @Test
    void testDeserialize_generalSectionMissingMetaData_logsToDataFile() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        doReturn(null).when(mockGeneralSectionParser).parse( //
                                                             anyCollection(),
                                                             any(ParserLogEntryCollector.class),
                                                             anyString()
        );

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries).anySatisfy(
            entry -> assertThatParserLogEntry(entry).hasSection("GENERAL")
                                                    .doesNotHaveLineContent()
                                                    .indicatesAcceptedLine()
                                                    .hasMessage("unable to verify data format version, metadata is unavailable (null)")
                                                    .doesNotHaveThrowable()
        );
    }

    @ParameterizedTest
    @CsvSource({
        "-1, metadata reports unsupported format version -1 (supported: 8..9)",
        "7, metadata reports unsupported format version 7 (supported: 8..9)",
        "10, metadata reports unsupported format version 10 (supported: 8..9)"
    })
    void testDeserialize_generalSectionListsUnsupportedFormatVersion_logsWarningToSLF4J(int unsupportedFormatVersion, String expectedMessage) {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData mockMetaData = mockMetaDataWithFormatVersion(unsupportedFormatVersion);
        doReturn(mockMetaData).when(mockGeneralSectionParser).parse( //
                                                                     anyCollection(),
                                                                     any(ParserLogEntryCollector.class),
                                                                     anyString()
        );

        // Act
        spyParser.deserialize(lines);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent expectedEvent = LoggingEvent.warn(expectedMessage);
        assertThat(loggingEvents).contains(expectedEvent);
    }

    @ParameterizedTest
    @CsvSource({
        "-1, metadata reports unsupported format version -1 (supported: 8..9)",
        "7, metadata reports unsupported format version 7 (supported: 8..9)",
        "10, metadata reports unsupported format version 10 (supported: 8..9)"
    })
    void testDeserialize_generalSectionListsUnsupportedFormatVersion_logsToDataFile(int unsupportedFormatVersion, String expectedMessage) {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData mockMetaData = mockMetaDataWithFormatVersion(unsupportedFormatVersion);
        doReturn(mockMetaData).when(mockGeneralSectionParser).parse( //
                                                                     anyCollection(),
                                                                     any(ParserLogEntryCollector.class),
                                                                     anyString()
        );

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries).anySatisfy(
            entry -> assertThatParserLogEntry(entry).hasSection("GENERAL")
                                                    .doesNotHaveLineContent()
                                                    .indicatesAcceptedLine()
                                                    .hasMessage(expectedMessage)
                                                    .doesNotHaveThrowable()
        );
    }

    static Stream<Arguments> dataProviderSupportedFormatVersions() {
        return Stream.of(8, 9)
                     .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("dataProviderSupportedFormatVersions")
    void testDeserialize_generalSectionListsSupportedFormatVersion_doesNotLogsWarningToSLF4J(int supportedFormatVersion) {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData mockMetaData = mockMetaDataWithFormatVersion(supportedFormatVersion);
        doReturn(mockMetaData).when(mockGeneralSectionParser).parse( //
                                                                     anyCollection(),
                                                                     any(ParserLogEntryCollector.class),
                                                                     anyString()
        );

        // Act
        spyParser.deserialize(lines);

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("dataProviderSupportedFormatVersions")
    void testDeserialize_generalSectionListsSupportedFormatVersion_doesNotLogToDataFile(int supportedFormatVersion) {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");

        DataFileMetaData mockMetaData = mockMetaDataWithFormatVersion(supportedFormatVersion);
        doReturn(mockMetaData).when(mockGeneralSectionParser).parse( //
                                                                     anyCollection(),
                                                                     any(ParserLogEntryCollector.class),
                                                                     anyString()
        );

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries).isEmpty();
    }

    @Test
    void testGetOnlineClientParser_always_parserIsSetToNotPrefileSection() {
        // Arrange
        doCallRealMethod().when(spyParser).getOnlineClientParser();

        // Act
        ClientParser mockResult = spyParser.getOnlineClientParser();

        // Assert
        verify(mockResult).setIsParsingPrefileSection(false);
    }

    @Test
    void testGetPrefileClientParser_always_parserIsSetToPrefileSection() {
        // Arrange
        doCallRealMethod().when(spyParser).getPrefileClientParser();

        // Act
        ClientParser mockResult = spyParser.getPrefileClientParser();

        // Assert
        verify(mockResult).setIsParsingPrefileSection(true);
    }

    @Test
    void testDeserialize_clientsSectionThrowsIllegalArgumentException_returnsDataFileWithNonExceptionalResultsFromOnlineClientParser() {
        // Arrange
        String lines = buildDataFileForSection("CLIENTS",
                                               ":expected line:1:",
                                               "trigger error 1",
                                               ":expected line:2:",
                                               "trigger error 2"
        );

        Client mockExpected1 = mock(Client.class);
        doReturn(mockExpected1).when(mockOnlineClientParser).parse(":expected line:1:");

        Client mockExpected2 = mock(Client.class);
        doReturn(mockExpected2).when(mockOnlineClientParser).parse(":expected line:2:");

        doThrow(new IllegalArgumentException("some error")) //
                                                            .when(mockOnlineClientParser)
                                                            .parse(Mockito.startsWith("trigger error"));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile).extracting(DataFile::getClients, as(COLLECTION))
                            .containsExactlyInAnyOrder(mockExpected1, mockExpected2);
    }

    @Test
    void testDeserialize_clientsSectionThrowsIllegalArgumentExceptions_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "CLIENTS";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection(section,
                                               ":expected line:1:",
                                               triggerLine1,
                                               ":expected line:2:",
                                               triggerLine2
        );

        doReturn(mockMetaDataWithFormatVersion(HIGHEST_SUPPORTED_FORMAT_VERSION))
            .when(mockGeneralSectionParser)
            .parse(any(), any(), anyString());
        doReturn(mock(Client.class)).when(mockOnlineClientParser).parse(anyString());

        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockOnlineClientParser).parse(triggerLine1);

        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockOnlineClientParser).parse(triggerLine2);

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries).satisfiesExactlyInAnyOrder(
            entry -> assertThatParserLogEntry(entry).hasSection(section)
                                                    .hasLineContent(triggerLine1)
                                                    .indicatesRejectedLine()
                                                    .hasMessageContaining(exception1.getMessage())
                                                    .hasThrowable(exception1),

            entry -> assertThatParserLogEntry(entry).hasSection(section)
                                                    .hasLineContent(triggerLine2)
                                                    .indicatesRejectedLine()
                                                    .hasMessageContaining(exception2.getMessage())
                                                    .hasThrowable(exception2)
        );
    }

    @Test
    void testDeserialize_prefileSectionThrowsIllegalArgumentExceptions_returnsDataFileWithNonExceptionResultsFromPrefileClientParser() {
        // Arrange
        String lines = buildDataFileForSection("PREFILE",
                                               ":expected line:1:",
                                               "trigger error 1",
                                               ":expected line:2:",
                                               "trigger error 2"
        );

        Client mockExpected1 = mock(Client.class);
        doReturn(mockExpected1).when(mockPrefileClientParser).parse(":expected line:1:");

        Client mockExpected2 = mock(Client.class);
        doReturn(mockExpected2).when(mockPrefileClientParser).parse(":expected line:2:");

        doThrow(new IllegalArgumentException("some error"))
            .when(mockPrefileClientParser)
            .parse(Mockito.startsWith("trigger error"));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile).extracting(DataFile::getClients, as(COLLECTION))
                            .containsExactlyInAnyOrder(mockExpected1, mockExpected2);
    }

    @Test
    void testDeserialize_prefileSectionThrowsIllegalArgumentExceptions_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
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
            .parse(any(), any(), anyString());
        doReturn(mock(Client.class)).when(mockPrefileClientParser).parse(anyString());

        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockPrefileClientParser).parse(triggerLine1);

        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockPrefileClientParser).parse(triggerLine2);

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries).satisfiesExactlyInAnyOrder(
            entry -> assertThatParserLogEntry(entry).hasSection(section)
                                                    .hasLineContent(triggerLine1)
                                                    .indicatesRejectedLine()
                                                    .hasMessageContaining(exception1.getMessage())
                                                    .hasThrowable(exception1),

            entry -> assertThatParserLogEntry(entry).hasSection(section)
                                                    .hasLineContent(triggerLine2)
                                                    .indicatesRejectedLine()
                                                    .hasMessageContaining(exception2.getMessage())
                                                    .hasThrowable(exception2)
        );
    }

    @Test
    void testDeserialize_serversSectionThrowsIllegalArgumentException_returnsDataFileWithNonExceptionalResultsFromFSDServerParser() {
        // Arrange
        String lines = buildDataFileForSection("SERVERS",
                                               ":expected line:1:",
                                               "trigger error 1",
                                               ":expected line:2:",
                                               "trigger error 2"
        );

        FSDServer mockExpected1 = mock(FSDServer.class);
        doReturn(mockExpected1).when(mockFSDServerParser).parse(":expected line:1:");

        FSDServer mockExpected2 = mock(FSDServer.class);
        doReturn(mockExpected2).when(mockFSDServerParser).parse(":expected line:2:");

        doThrow(new IllegalArgumentException("some error"))
            .when(mockFSDServerParser)
            .parse(Mockito.startsWith("trigger error"));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile).extracting(DataFile::getFsdServers, as(COLLECTION))
                            .containsExactlyInAnyOrder(mockExpected1, mockExpected2);
    }

    @Test
    void testDeserialize_serversSectionThrowsIllegalArgumentException_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "SERVERS";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection("SERVERS",
                                               ":expected line:1:",
                                               triggerLine1,
                                               ":expected line:2:",
                                               triggerLine2
        );

        doReturn(mockMetaDataWithFormatVersion(HIGHEST_SUPPORTED_FORMAT_VERSION))
            .when(mockGeneralSectionParser)
            .parse(any(), any(), anyString());
        doReturn(mock(FSDServer.class)).when(mockFSDServerParser).parse(anyString());

        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockFSDServerParser).parse(triggerLine1);

        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockFSDServerParser).parse(triggerLine2);

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries).satisfiesExactlyInAnyOrder(
            entry -> assertThatParserLogEntry(entry).hasSection(section)
                                                    .hasLineContent(triggerLine1)
                                                    .indicatesRejectedLine()
                                                    .hasMessageContaining(exception1.getMessage())
                                                    .hasThrowable(exception1),

            entry -> assertThatParserLogEntry(entry).hasSection(section)
                                                    .hasLineContent(triggerLine2)
                                                    .indicatesRejectedLine()
                                                    .hasMessageContaining(exception2.getMessage())
                                                    .hasThrowable(exception2)
        );
    }

    @Test
    void testDeserialize_voiceServersSectionThrowsIllegalArgumentException_returnsDataFileWithNonExceptionalResultsFromVoiceServerParser() {
        // Arrange
        String lines = buildDataFileForSection("VOICE SERVERS",
                                               ":expected line:1:",
                                               "trigger error 1",
                                               ":expected line:2:",
                                               "trigger error 2" //
        );

        VoiceServer mockExpected1 = mock(VoiceServer.class);
        doReturn(mockExpected1).when(mockVoiceServerParser).parse(":expected line:1:");

        VoiceServer mockExpected2 = mock(VoiceServer.class);
        doReturn(mockExpected2).when(mockVoiceServerParser).parse(":expected line:2:");

        doThrow(new IllegalArgumentException("some error"))
            .when(mockVoiceServerParser)
            .parse(Mockito.startsWith("trigger error"));

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        assertThat(dataFile).extracting(DataFile::getVoiceServers, as(COLLECTION))
                            .containsExactlyInAnyOrder(mockExpected1, mockExpected2);
    }

    @Test
    void testDeserialize_voiceServersSectionThrowsIllegalArgumentException_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "VOICE SERVERS";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection("VOICE SERVERS",
                                               ":expected line:1:",
                                               "trigger error 1",
                                               ":expected line:2:",
                                               "trigger error 2"
        );

        doReturn(mockMetaDataWithFormatVersion(HIGHEST_SUPPORTED_FORMAT_VERSION)).when(mockGeneralSectionParser)
                                                                                 .parse(any(), any(), anyString());
        doReturn(mock(VoiceServer.class)).when(mockVoiceServerParser).parse(anyString());

        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockVoiceServerParser).parse(triggerLine1);

        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockVoiceServerParser).parse(triggerLine2);

        // Act
        DataFile dataFile = spyParser.deserialize(lines);

        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries).satisfiesExactlyInAnyOrder(
            entry -> assertThatParserLogEntry(entry).hasSection(section)
                                                    .hasLineContent(triggerLine1)
                                                    .indicatesRejectedLine()
                                                    .hasMessageContaining(exception1.getMessage())
                                                    .hasThrowable(exception1),

            entry -> assertThatParserLogEntry(entry).hasSection(section)
                                                    .hasLineContent(triggerLine2)
                                                    .indicatesRejectedLine()
                                                    .hasMessageContaining(exception2.getMessage())
                                                    .hasThrowable(exception2)
        );
    }

    private String buildDataFileForSection(String sectionName, String... sectionRelevantLines) {
        return buildDataFileForSection(sectionName, Arrays.asList(sectionRelevantLines));
    }

    private String buildDataFileForSection(String sectionName, List<String> sectionRelevantLines) {
        // Arrange
        return "something before\r\n"
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
    }

    private String readBuffer(BufferedReader br, int maxLength) throws IOException {
        char[] buffer = new char[maxLength];
        int read = br.read(buffer);
        return read < 1 ? "" : new String(Arrays.copyOfRange(buffer, 0, read));
    }

    private DataFileMetaData mockMetaDataWithFormatVersion(int version) {
        DataFileMetaData mockMetaData = mock(DataFileMetaData.class);
        doReturn(version).when(mockMetaData).getVersionFormat();

        return mockMetaData;
    }
}
