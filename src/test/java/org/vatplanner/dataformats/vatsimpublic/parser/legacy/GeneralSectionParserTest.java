package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.testutils.ParserLogEntryMatcher.matchesParserLogEntry;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

@RunWith(DataProviderRunner.class)
public class GeneralSectionParserTest {

    private GeneralSectionParser parser;
    private ParserLogEntryCollector logEntryCollector;

    @Before
    public void setUp() {
        parser = new GeneralSectionParser();

        logEntryCollector = new DataFile();
    }

    @Test
    public void testParse_null_returnsDefaultMetaData() {
        // Arrange
        DataFileMetaData defaultMetaData = new DataFileMetaData();

        // Act
        DataFileMetaData result = parser.parse(null, logEntryCollector, null);

        // Assert
        assertThat(
            result.getMinimumAtisRetrievalInterval(),
            is(equalTo(defaultMetaData.getMinimumAtisRetrievalInterval())) //
        );
        assertThat(
            result.getMinimumDataFileRetrievalInterval(),
            is(equalTo(defaultMetaData.getMinimumDataFileRetrievalInterval())) //
        );
        assertThat(result.getNumberOfConnectedClients(), is(equalTo(defaultMetaData.getNumberOfConnectedClients())));
        assertThat(
            result.getNumberOfUniqueConnectedUsers(),
            is(equalTo(defaultMetaData.getNumberOfUniqueConnectedUsers())) //
        );
        assertThat(result.getTimestamp(), is(equalTo(defaultMetaData.getTimestamp())));
        assertThat(result.getVersionFormat(), is(equalTo(defaultMetaData.getVersionFormat())));
    }

    @Test
    public void testParse_null_logsGeneralSectionUnparsable() {
        // Arrange
        String expectedSectionName = "section name";

        // Act
        parser.parse(null, logEntryCollector, expectedSectionName);

        // Assert
        Collection<ParserLogEntry> entries = logEntryCollector.getParserLogEntries();
        assertThat(
            entries,
            containsInAnyOrder(
                matchesParserLogEntry(
                    equalTo(expectedSectionName),
                    is(nullValue(String.class)),
                    equalTo(true),
                    matchesPattern(".*meta data.*missing.*empty.*"),
                    is(nullValue(Throwable.class)) //
                ) //
            ) //
        );
    }

    @Test
    public void testParse_emptyLines_logsGeneralSectionUnparsable() {
        // Arrange
        String expectedSectionName = "section name";

        // Act
        parser.parse(new ArrayList<>(), logEntryCollector, expectedSectionName);

        // Assert
        Collection<ParserLogEntry> entries = logEntryCollector.getParserLogEntries();
        assertThat(
            entries,
            containsInAnyOrder(
                matchesParserLogEntry(
                    equalTo(expectedSectionName),
                    is(nullValue(String.class)),
                    equalTo(true),
                    matchesPattern(".*meta data.*missing.*empty.*"),
                    is(nullValue(Throwable.class)) //
                ) //
            ) //
        );
    }

    @Test
    @DataProvider({ "8", "123" })
    public void testParse_withVersion_returnsDataFileMetaDataWithExpectedFormatVersion(int expectedVersion) {
        // Arrange
        Collection<String> lines = Arrays.asList(
            String.format("VERSION = %d", expectedVersion) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getVersionFormat(), is(equalTo(expectedVersion)));
    }

    @Test
    public void testParse_withoutVersion_returnsDataFileMetaDataWithNegativeValueForFormatVersion() {
        // Arrange
        Collection<String> lines = Arrays.asList(
            "RELOAD = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getVersionFormat(), is(lessThan(0)));
    }

    @Test
    @DataProvider({ "2", "100" })
    public void testParse_withReloadIntegerNumber_returnsDataFileMetaDataWithExpectedMinimumDataFileRetrievalInterval(int minimumIntervalMinutes) {
        // Arrange
        Collection<String> lines = Arrays.asList(
            String.format("RELOAD = %d", minimumIntervalMinutes) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(//
            result.getMinimumDataFileRetrievalInterval(),
            is(equalTo(Duration.ofMinutes(minimumIntervalMinutes))) //
        );
    }

    @Test
    @DataProvider({ "0.25, 15", "1.33, 80" })
    public void testParse_withReloadFloatingNumber_returnsDataFileMetaDataWithExpectedMinimumDataFileRetrievalInterval(String input, int expectedSeconds) {
        // Arrange
        Collection<String> lines = Arrays.asList(
            String.format("RELOAD = %s", input) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getMinimumDataFileRetrievalInterval(), is(equalTo(Duration.ofSeconds(expectedSeconds))));
    }

    @Test
    public void testParse_withoutReload_returnsDataFileMetaDataWithNullForMinimumDataFileRetrievalInterval() {
        // Arrange
        Collection<String> lines = Arrays.asList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getMinimumDataFileRetrievalInterval(), is(nullValue()));
    }

    @Test
    @DataProvider({ "5", "100" })
    public void testParse_withAtisAllowMin_returnsDataFileMetaDataWithExpectedMinimumAtisRetrievalInterval(int minimumIntervalMinutes) {
        // Arrange
        Collection<String> lines = Arrays.asList(
            String.format("ATIS ALLOW MIN = %d", minimumIntervalMinutes) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getMinimumAtisRetrievalInterval(), is(equalTo(Duration.ofMinutes(minimumIntervalMinutes))));
    }

    @Test
    public void testParse_withoutAtisAllowMin_returnsDataFileMetaDataWithNullForMinimumAtisRetrievalInterval() {
        // Arrange
        Collection<String> lines = Arrays.asList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getMinimumAtisRetrievalInterval(), is(nullValue()));
    }

    @Test
    @DataProvider({ "5", "100" })
    public void testParse_withConnectedClients_returnsDataFileMetaDataWithExpectedNumberOfConnectedClients(int connectedClients) {
        // Arrange
        Collection<String> lines = Arrays.asList(
            String.format("CONNECTED CLIENTS = %d", connectedClients) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getNumberOfConnectedClients(), is(equalTo(connectedClients)));
    }

    @Test
    public void testParse_withoutConnectedClients_returnsDataFileMetaDataWithNegativeValueForNumberOfConnectedClients() {
        // Arrange
        Collection<String> lines = Arrays.asList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getNumberOfConnectedClients(), is(lessThan(0)));
    }

    @Test
    @DataProvider({ "5", "100" })
    public void testParse_withUniqueUsers_returnsDataFileMetaDataWithExpectedNumberOfConnectedUniqueUsers(int uniqueUsers) {
        // Arrange
        Collection<String> lines = Arrays.asList(
            String.format("UNIQUE USERS = %d", uniqueUsers) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getNumberOfUniqueConnectedUsers(), is(equalTo(uniqueUsers)));
    }

    @Test
    public void testParse_withoutUniqueUsers_returnsDataFileMetaDataWithNegativeValueForNumberOfConnectedUniqueUsers() {
        // Arrange
        Collection<String> lines = Arrays.asList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getNumberOfUniqueConnectedUsers(), is(lessThan(0)));
    }

    @Test
    @DataProvider({ "20170625232105, 1498432865", "20161105184253, 1478371373" })
    public void testParse_withUpdate_returnsDataFileMetaDataWithExpectedTimestamp(String value, int valueEpochSeconds) {
        // Arrange
        Collection<String> lines = Arrays.asList(
            String.format("UPDATE = %s", value) //
        );
        Instant expectedInstant = Instant.ofEpochSecond(valueEpochSeconds);

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getTimestamp(), is(equalTo(expectedInstant)));
    }

    @Test
    public void testParse_withoutUpdate_returnsDataFileMetaDataWithNullForTimestamp() {
        // Arrange
        Collection<String> lines = Arrays.asList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result.getTimestamp(), is(nullValue()));
    }

    @Test
    public void testParse_completeData_doesNotLog() {
        // Arrange
        Collection<String> lines = Arrays.asList(
            "UPDATE = 20161105184253",
            "VERSION = 123",
            "CONNECTED CLIENTS = 20",
            "ATIS ALLOW MIN = 5",
            "RELOAD = 2" //
        );

        // Act
        parser.parse(lines, logEntryCollector, null);

        // Assert
        Collection<ParserLogEntry> logEntries = logEntryCollector.getParserLogEntries();
        assertThat(logEntries, is(empty()));
    }

    @Test
    @DataProvider({ "ABCDEFG", "ZZZ" })
    public void testParse_unknownKey_logsKeyUnparsed(String key) {
        // Arrange
        String expectedSectionName = "section name";
        String line = key + " = XYZ";
        Collection<String> lines = Arrays.asList(line);

        // Act
        parser.parse(lines, logEntryCollector, expectedSectionName);

        // Assert
        Collection<ParserLogEntry> entries = logEntryCollector.getParserLogEntries();
        assertThat(
            entries,
            containsInAnyOrder(
                matchesParserLogEntry(
                    equalTo(expectedSectionName),
                    equalTo(line),
                    equalTo(true),
                    matchesPattern(".*key " + key + ".*unknown.*"),
                    is(nullValue(Throwable.class)) //
                ) //
            ) //
        );
    }
}
