package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.INTEGER;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.vatplanner.dataformats.vatsimpublic.testutils.ParserLogEntryAssert.assertThatParserLogEntry;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

class GeneralSectionParserTest {

    private GeneralSectionParser parser;
    private ParserLogEntryCollector logEntryCollector;

    @BeforeEach
    void setUp() {
        parser = new GeneralSectionParser();

        logEntryCollector = new DataFile();
    }

    @Test
    void testParse_null_returnsDefaultMetaData() {
        // Arrange
        DataFileMetaData defaultMetaData = new DataFileMetaData();

        // Act
        DataFileMetaData result = parser.parse(null, logEntryCollector, null);

        // Assert
        assertAll(
            () -> assertThat(result).describedAs("minimum ATIS retrieval interval")
                                    .extracting(DataFileMetaData::getMinimumAtisRetrievalInterval)
                                    .isEqualTo(defaultMetaData.getMinimumAtisRetrievalInterval()),

            () -> assertThat(result).describedAs("minimum data file retrieval interval")
                                    .extracting(DataFileMetaData::getMinimumDataFileRetrievalInterval)
                                    .isEqualTo(defaultMetaData.getMinimumDataFileRetrievalInterval()),

            () -> assertThat(result).describedAs("number of connected clients")
                                    .extracting(DataFileMetaData::getNumberOfConnectedClients)
                                    .isEqualTo(defaultMetaData.getNumberOfConnectedClients()),

            () -> assertThat(result).describedAs("number of unique connected users")
                                    .extracting(DataFileMetaData::getNumberOfUniqueConnectedUsers)
                                    .isEqualTo(defaultMetaData.getNumberOfUniqueConnectedUsers()),

            () -> assertThat(result).describedAs("timestamp")
                                    .extracting(DataFileMetaData::getTimestamp)
                                    .isEqualTo(defaultMetaData.getTimestamp()),

            () -> assertThat(result).describedAs("version format")
                                    .extracting(DataFileMetaData::getVersionFormat)
                                    .isEqualTo(defaultMetaData.getVersionFormat())
        );
    }

    @Test
    void testParse_null_logsGeneralSectionUnparsable() {
        // Arrange
        String expectedSectionName = "section name";

        // Act
        parser.parse(null, logEntryCollector, expectedSectionName);

        // Assert
        Collection<ParserLogEntry> entries = logEntryCollector.getParserLogEntries();
        assertThat(entries).satisfiesExactly(
            entry -> assertThatParserLogEntry(entry).hasSection(expectedSectionName)
                                                    .doesNotHaveLineContent()
                                                    .indicatesRejectedLine()
                                                    .hasMessageMatching(".*meta data.*missing.*empty.*")
                                                    .doesNotHaveThrowable()
        );
    }

    @Test
    void testParse_emptyLines_logsGeneralSectionUnparsable() {
        // Arrange
        String expectedSectionName = "section name";

        // Act
        parser.parse(new ArrayList<>(), logEntryCollector, expectedSectionName);

        // Assert
        Collection<ParserLogEntry> entries = logEntryCollector.getParserLogEntries();
        assertThat(entries).satisfiesExactly(
            entry -> assertThatParserLogEntry(entry).hasSection(expectedSectionName)
                                                    .doesNotHaveLineContent()
                                                    .indicatesRejectedLine()
                                                    .hasMessageMatching(".*meta data.*missing.*empty.*")
                                                    .doesNotHaveThrowable()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 123})
    void testParse_withVersion_returnsDataFileMetaDataWithExpectedFormatVersion(int expectedVersion) {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            String.format("VERSION = %d", expectedVersion) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getVersionFormat)
                          .isEqualTo(expectedVersion);
    }

    @Test
    void testParse_withoutVersion_returnsDataFileMetaDataWithNegativeValueForFormatVersion() {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            "RELOAD = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getVersionFormat, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 100})
    void testParse_withReloadIntegerNumber_returnsDataFileMetaDataWithExpectedMinimumDataFileRetrievalInterval(int minimumIntervalMinutes) {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            String.format("RELOAD = %d", minimumIntervalMinutes) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getMinimumDataFileRetrievalInterval)
                          .isEqualTo(Duration.ofMinutes(minimumIntervalMinutes));
    }

    @ParameterizedTest
    @CsvSource({
        "0.25, 15",
        "1.33, 80"
    })
    void testParse_withReloadFloatingNumber_returnsDataFileMetaDataWithExpectedMinimumDataFileRetrievalInterval(String input, int expectedSeconds) {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            String.format("RELOAD = %s", input) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getMinimumDataFileRetrievalInterval)
                          .isEqualTo(Duration.ofSeconds(expectedSeconds));
    }

    @Test
    void testParse_withoutReload_returnsDataFileMetaDataWithNullForMinimumDataFileRetrievalInterval() {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getMinimumDataFileRetrievalInterval)
                          .isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 100})
    void testParse_withAtisAllowMin_returnsDataFileMetaDataWithExpectedMinimumAtisRetrievalInterval(int minimumIntervalMinutes) {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            String.format("ATIS ALLOW MIN = %d", minimumIntervalMinutes) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getMinimumAtisRetrievalInterval)
                          .isEqualTo(Duration.ofMinutes(minimumIntervalMinutes));
    }

    @Test
    void testParse_withoutAtisAllowMin_returnsDataFileMetaDataWithNullForMinimumAtisRetrievalInterval() {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getMinimumAtisRetrievalInterval)
                          .isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 100})
    void testParse_withConnectedClients_returnsDataFileMetaDataWithExpectedNumberOfConnectedClients(int connectedClients) {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            String.format("CONNECTED CLIENTS = %d", connectedClients) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getNumberOfConnectedClients)
                          .isEqualTo(connectedClients);
    }

    @Test
    void testParse_withoutConnectedClients_returnsDataFileMetaDataWithNegativeValueForNumberOfConnectedClients() {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getNumberOfConnectedClients, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 100})
    void testParse_withUniqueUsers_returnsDataFileMetaDataWithExpectedNumberOfConnectedUniqueUsers(int uniqueUsers) {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            String.format("UNIQUE USERS = %d", uniqueUsers) //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getNumberOfUniqueConnectedUsers)
                          .isEqualTo(uniqueUsers);
    }

    @Test
    void testParse_withoutUniqueUsers_returnsDataFileMetaDataWithNegativeValueForNumberOfConnectedUniqueUsers() {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getNumberOfUniqueConnectedUsers, as(INTEGER))
                          .isNegative();
    }

    @ParameterizedTest
    @CsvSource({
        "20170625232105, 1498432865",
        "20161105184253, 1478371373"
    })
    void testParse_withUpdate_returnsDataFileMetaDataWithExpectedTimestamp(String value, int valueEpochSeconds) {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            String.format("UPDATE = %s", value) //
        );
        Instant expectedInstant = Instant.ofEpochSecond(valueEpochSeconds);

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getTimestamp)
                          .isEqualTo(expectedInstant);
    }

    @Test
    void testParse_withoutUpdate_returnsDataFileMetaDataWithNullForTimestamp() {
        // Arrange
        Collection<String> lines = Collections.singletonList(
            "VERSION = 123" //
        );

        // Act
        DataFileMetaData result = parser.parse(lines, logEntryCollector, null);

        // Assert
        assertThat(result).extracting(DataFileMetaData::getTimestamp)
                          .isNull();
    }

    @Test
    void testParse_completeData_doesNotLog() {
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
        assertThat(logEntries).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABCDEFG", "ZZZ"})
    void testParse_unknownKey_logsKeyUnparsed(String key) {
        // Arrange
        String expectedSectionName = "section name";
        String line = key + " = XYZ";
        Collection<String> lines = Collections.singletonList(line);

        // Act
        parser.parse(lines, logEntryCollector, expectedSectionName);

        // Assert
        Collection<ParserLogEntry> entries = logEntryCollector.getParserLogEntries();
        assertThat(entries).satisfiesExactly(
            entry -> assertThatParserLogEntry(entry).hasSection(expectedSectionName)
                                                    .hasLineContent(line)
                                                    .indicatesRejectedLine()
                                                    .hasMessageMatching(".*key " + key + ".*unknown.*")
                                                    .doesNotHaveThrowable()
        );
    }
}
