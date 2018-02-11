package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static de.energiequant.vatplanner.dataformats.vatsimpublic.testutils.ParserLogEntryMatcher.matchesParserLogEntry;

@RunWith(DataProviderRunner.class)
public class DataFileParserTest {
    private DataFileParser spyParser;
    private GeneralSectionParser mockGeneralSectionParser;
    private ClientParser mockOnlineClientParser;
    private ClientParser mockPrefileClientParser;
    private FSDServerParser mockFSDServerParser;
    private VoiceServerParser mockVoiceServerParser;
    
    @Before
    public void setUp() {
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
    @DataProvider({"", "  ab \r cd  \r\n ef \n g "})
    public void testParse_CharSequence_forwardsInputWrappedInBufferedReader(String s) throws IOException {
        // Arrange
        doReturn(null).when(spyParser).parse(any(BufferedReader.class));
        
        // Act
        spyParser.parse((CharSequence) s);
        
        // Assert
        ArgumentCaptor<BufferedReader> captor = ArgumentCaptor.forClass(BufferedReader.class);
        verify(spyParser).parse(captor.capture());
        BufferedReader br = captor.getValue();

        String brContent = readBuffer(br, s.length() + 1);
        
        assertThat(brContent, is(equalTo(s)));
    }

    @Test
    public void testParse_CharSequence_returnsResultFromBufferedReaderParser() {
        // Arrange
        DataFile expectedObject = new DataFile();
        doReturn(expectedObject).when(spyParser).parse(any(BufferedReader.class));
        
        // Act
        DataFile result = spyParser.parse((CharSequence) "");
        
        // Assert
        assertThat(result, is(sameInstance(expectedObject)));
    }
    
    @Test
    public void testParse_generalSection_forwardsCleanedSectionRelevantLinesToGeneralSectionParser() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL", "123", "456");
        
        // Act
        spyParser.parse(lines);
        
        // Assert
        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mockGeneralSectionParser).parse(captor.capture());
        Collection<String> capturedCollection = captor.getValue();
        
        assertThat(capturedCollection, containsInAnyOrder("123", "456"));
    }
    
    @Test
    public void testParse_generalSection_returnsDataFileWithResultFromGeneralSectionParserAsMetaData() {
        // Arrange
        String lines = buildDataFileForSection("GENERAL");
        
        DataFileMetaData expectedMetaData = new DataFileMetaData();
        doReturn(expectedMetaData).when(mockGeneralSectionParser).parse(any(Collection.class));
        
        // Act
        DataFile dataFile = spyParser.parse(lines);
        
        // Assert
        assertThat(dataFile.getMetaData(), is(sameInstance(expectedMetaData)));
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
    public void testParse_clientsSectionThrowsIllegalArgumentException_returnsDataFileWithNonExceptionalResultsFromOnlineClientParser() {
        // Arrange
        String lines = buildDataFileForSection("CLIENTS", ":expected line:1:", "trigger error 1", ":expected line:2:", "trigger error 2");
        
        Client mockExpected1 = mock(Client.class);
        doReturn(mockExpected1).when(mockOnlineClientParser).parse(Mockito.eq(":expected line:1:"));
        
        Client mockExpected2 = mock(Client.class);
        doReturn(mockExpected2).when(mockOnlineClientParser).parse(Mockito.eq(":expected line:2:"));
        
        doThrow(new IllegalArgumentException("some error")).when(mockOnlineClientParser).parse(Mockito.startsWith("trigger error"));
        
        // Act
        DataFile dataFile = spyParser.parse(lines);
        
        // Assert
        assertThat(dataFile.getClients(), containsInAnyOrder(mockExpected1, mockExpected2));
    }

    @Test
    public void testParse_clientsSectionThrowsIllegalArgumentExceptions_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "CLIENTS";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection(section, ":expected line:1:", triggerLine1, ":expected line:2:", triggerLine2);
        
        Client mockExpected1 = mock(Client.class);
        doReturn(mockExpected1).when(mockOnlineClientParser).parse(Mockito.eq(":expected line:1:"));
        
        Client mockExpected2 = mock(Client.class);
        doReturn(mockExpected2).when(mockOnlineClientParser).parse(Mockito.eq(":expected line:2:"));
        
        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockOnlineClientParser).parse(Mockito.eq(triggerLine1));
        
        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockOnlineClientParser).parse(Mockito.eq(triggerLine2));
        
        // Act
        DataFile dataFile = spyParser.parse(lines);
        
        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries, containsInAnyOrder(
                matchesParserLogEntry(
                        equalTo(section),
                        equalTo(triggerLine1),
                        equalTo(true),
                        containsString(exception1.getMessage()),
                        sameInstance(exception1)
                ),
                
                matchesParserLogEntry(
                        equalTo(section),
                        equalTo(triggerLine2),
                        equalTo(true),
                        containsString(exception2.getMessage()),
                        sameInstance(exception2)
                )
        ));
    }

    @Test
    public void testParse_prefileSectionThrowsIllegalArgumentExceptions_returnsDataFileWithNonExceptionResultsFromPrefileClientParser() {
        // Arrange
        String lines = buildDataFileForSection("PREFILE", ":expected line:1:", "trigger error 1", ":expected line:2:", "trigger error 2");
        
        Client mockExpected1 = mock(Client.class);
        doReturn(mockExpected1).when(mockPrefileClientParser).parse(Mockito.eq(":expected line:1:"));
        
        Client mockExpected2 = mock(Client.class);
        doReturn(mockExpected2).when(mockPrefileClientParser).parse(Mockito.eq(":expected line:2:"));
        
        doThrow(new IllegalArgumentException("some error")).when(mockPrefileClientParser).parse(Mockito.startsWith("trigger error"));
        
        // Act
        DataFile dataFile = spyParser.parse(lines);
        
        // Assert
        assertThat(dataFile.getClients(), containsInAnyOrder(mockExpected1, mockExpected2));
    }

    @Test
    public void testParse_prefileSectionThrowsIllegalArgumentExceptions_returnsDataFileWithExceptionalResultsLoggedCorrectly() {
        // Arrange
        String section = "PREFILE";
        String triggerLine1 = "trigger error 1";
        String triggerLine2 = "trigger error 2";
        String lines = buildDataFileForSection(section, ":expected line:1:", triggerLine1, ":expected line:2:", triggerLine2);
        
        Client mockExpected1 = mock(Client.class);
        doReturn(mockExpected1).when(mockPrefileClientParser).parse(Mockito.eq(":expected line:1:"));
        
        Client mockExpected2 = mock(Client.class);
        doReturn(mockExpected2).when(mockPrefileClientParser).parse(Mockito.eq(":expected line:2:"));
        
        IllegalArgumentException exception1 = new IllegalArgumentException("some error");
        doThrow(exception1).when(mockPrefileClientParser).parse(Mockito.eq(triggerLine1));
        
        IllegalArgumentException exception2 = new IllegalArgumentException("another error");
        doThrow(exception2).when(mockPrefileClientParser).parse(Mockito.eq(triggerLine2));
        
        // Act
        DataFile dataFile = spyParser.parse(lines);
        
        // Assert
        Collection<ParserLogEntry> entries = dataFile.getParserLogEntries();
        assertThat(entries, containsInAnyOrder(
                matchesParserLogEntry(
                        equalTo(section),
                        equalTo(triggerLine1),
                        equalTo(true),
                        containsString(exception1.getMessage()),
                        sameInstance(exception1)
                ),
                
                matchesParserLogEntry(
                        equalTo(section),
                        equalTo(triggerLine2),
                        equalTo(true),
                        containsString(exception2.getMessage()),
                        sameInstance(exception2)
                )
        ));
    }
    
    @Test
    public void testParse_serversSection_returnsDataFileWithResultsFromFSDServerParser() {
        // Arrange
        String lines = buildDataFileForSection("SERVERS", ":expected line:1:", ":expected line:2:");
        
        FSDServer mockExpected1 = mock(FSDServer.class);
        doReturn(mockExpected1).when(mockFSDServerParser).parse(Mockito.eq(":expected line:1:"));
        
        FSDServer mockExpected2 = mock(FSDServer.class);
        doReturn(mockExpected2).when(mockFSDServerParser).parse(Mockito.eq(":expected line:2:"));
        
        // Act
        DataFile dataFile = spyParser.parse(lines);
        
        // Assert
        assertThat(dataFile.getFsdServers(), containsInAnyOrder(mockExpected1, mockExpected2));
    }

    @Test
    public void testParse_voiceServersSection_returnsDataFileWithResultsFromVoiceServerParser() {
        // Arrange
        String lines = buildDataFileForSection("VOICE SERVERS", ":expected line:1:", ":expected line:2:");
        
        VoiceServer mockExpected1 = mock(VoiceServer.class);
        doReturn(mockExpected1).when(mockVoiceServerParser).parse(Mockito.eq(":expected line:1:"));
        
        VoiceServer mockExpected2 = mock(VoiceServer.class);
        doReturn(mockExpected2).when(mockVoiceServerParser).parse(Mockito.eq(":expected line:2:"));
        
        // Act
        DataFile dataFile = spyParser.parse(lines);
        
        // Assert
        assertThat(dataFile.getVoiceServers(), containsInAnyOrder(mockExpected1, mockExpected2));
    }

    private String buildDataFileForSection(String sectionName, String... sectionRelevantLines) {
        return buildDataFileForSection(sectionName, Arrays.asList(sectionRelevantLines));
    }
    
    private String buildDataFileForSection(String sectionName, List<String> sectionRelevantLines) {
        // Arrange
        String lines = "something before\r\n"
                + ";!"+sectionName+":\r\n"
                + "decoy before\r\n"
                + "!"+sectionName+":\r\n"
                + "\r\n"
                + ((sectionRelevantLines.size() < 1) ? "" : sectionRelevantLines.get(0)+"\r\n")
                + "\r\n"
                + "   \r\n"
                + ";comment\r\n"
                + ";!COMMENT:\r\n"
                + ((sectionRelevantLines.size() < 2) ? "" : String.join("\r\n", sectionRelevantLines.subList(1, sectionRelevantLines.size())))
                + "\r\n"
                + ";!"+sectionName+":\r\n"
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
}
