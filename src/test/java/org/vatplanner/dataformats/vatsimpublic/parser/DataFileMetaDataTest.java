package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DataFileMetaDataTest {

    @Test
    void testEquals_null_returnsFalse() {
        // Arrange
        DataFileMetaData a = new DataFileMetaData();
        DataFileMetaData b = null;

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testEquals_wrongClass_returnsFalse() {
        // Arrange
        DataFileMetaData a = new DataFileMetaData();
        Object b = new Object();

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M",
            "-1, null, -1, -1, null, null",
            "12, 2021-12-31T23:59:59Z, 951, 890, PT1M, PT10M"
        },
        nullValues = {"null"}
    )
    void testCheckEqualMetadata_equal_returnsTrue(int versionFormat, String timestampIso, int numberOfConnectedClients, int numberOfUniqueConnectedUsers, String minimumDataFileRetrievalIntervalIso, String minimumAtisRetrievalIntervalIso) {
        // Arrange
        DataFileMetaData a = createMetaData(//
                                            versionFormat, //
                                            timestampIso, //
                                            numberOfConnectedClients, //
                                            numberOfUniqueConnectedUsers, //
                                            minimumDataFileRetrievalIntervalIso, //
                                            minimumAtisRetrievalIntervalIso //
        );
        DataFileMetaData b = createMetaData( //
                                             versionFormat, //
                                             timestampIso, //
                                             numberOfConnectedClients, //
                                             numberOfUniqueConnectedUsers, //
                                             minimumDataFileRetrievalIntervalIso, //
                                             minimumAtisRetrievalIntervalIso //
        );

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            // object A || object B
            // version format
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     7, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M", // 0
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,    -1, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M", // 1
            "-1, null,                  -1,  -1, null, null,     8, null,                  -1,  -1, null, null", // 2
            "-1, null,                  -1,  -1, null, null,    10, null,                  -1,  -1, null, null", // 3

            // timestamp
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, null,                 123, 100, PT2M, PT5M", // 4
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:01Z, 123, 100, PT2M, PT5M", // 5
            "-1, null,                  -1,  -1, null, null,    -1, 2019-01-16T21:36:00Z,  -1,  -1, null, null", // 6

            // number of connected clients
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 122, 100, PT2M, PT5M", // 7
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:00Z,  -1, 100, PT2M, PT5M", // 8
            "-1, null,                  -1,  -1, null, null,    -1, null,                   0,  -1, null, null", // 9
            "-1, null,                  -1,  -1, null, null,    -1, null,                  42,  -1, null, null", // 10

            // number of connected unique users
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, 101, PT2M, PT5M", // 11
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123,  -1, PT2M, PT5M", // 12
            "-1, null,                  -1,  -1, null, null,    -1, null,                  -1,   0, null, null", // 13
            "-1, null,                  -1,  -1, null, null,    -1, null,                  -1,  23, null, null", // 14

            // minimum data file retrieval interval
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, 100, PT1M, PT5M", // 15
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, 100, null, PT5M", // 16
            "-1, null,                  -1,  -1, null, null,    -1, null,                  -1,  -1, PT0S, null", // 17

            // minimum ATIS file retrieval interval
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT1M", // 18
            " 8, 2019-01-16T21:36:00Z, 123, 100, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, 100, PT2M, null", // 19
            "-1, null,                  -1,  -1, null, null,    -1, null,                  -1,  -1, null, PT15M" // 20
        },
        nullValues = {"null"}
    )
    void testCheckEqualMetadata_nonEqual_returnsFalse(int versionFormatA, String timestampIsoA, int numberOfConnectedClientsA, int numberOfUniqueConnectedUsersA, String minimumDataFileRetrievalIntervalIsoA, String minimumAtisRetrievalIntervalIsoA, int versionFormatB, String timestampIsoB, int numberOfConnectedClientsB, int numberOfUniqueConnectedUsersB, String minimumDataFileRetrievalIntervalIsoB, String minimumAtisRetrievalIntervalIsoB) {
        // Arrange
        DataFileMetaData a = createMetaData( //
                                             versionFormatA, //
                                             timestampIsoA, //
                                             numberOfConnectedClientsA, //
                                             numberOfUniqueConnectedUsersA, //
                                             minimumDataFileRetrievalIntervalIsoA, //
                                             minimumAtisRetrievalIntervalIsoA //
        );
        DataFileMetaData b = createMetaData( //
                                             versionFormatB, //
                                             timestampIsoB, //
                                             numberOfConnectedClientsB, //
                                             numberOfUniqueConnectedUsersB, //
                                             minimumDataFileRetrievalIntervalIsoB, //
                                             minimumAtisRetrievalIntervalIsoB //
        );

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result).isFalse();
    }

    private DataFileMetaData createMetaData(int versionFormat, String timestampIso, int numberOfConnectedClients, int numberOfUniqueConnectedUsers, String minimumDataFileRetrievalIntervalIso, String minimumAtisRetrievalIntervalIso) {
        Instant timestamp = parseInstantNull(timestampIso);
        Duration minimumDataFileRetrievalInterval = parseDurationNull(minimumDataFileRetrievalIntervalIso);
        Duration minimumAtisRetrievalInterval = parseDurationNull(minimumAtisRetrievalIntervalIso);

        return createMetaData(
            versionFormat,
            timestamp,
            numberOfConnectedClients,
            numberOfUniqueConnectedUsers,
            minimumDataFileRetrievalInterval,
            minimumAtisRetrievalInterval
        );
    }

    private DataFileMetaData createMetaData(int versionFormat, Instant timestamp, int numberOfConnectedClients, int numberOfUniqueConnectedUsers, Duration minimumDataFileRetrievalInterval, Duration minimumAtisRetrievalInterval) {
        return new DataFileMetaData()
            .setMinimumAtisRetrievalInterval(minimumAtisRetrievalInterval)
            .setMinimumDataFileRetrievalInterval(minimumDataFileRetrievalInterval)
            .setNumberOfConnectedClients(numberOfConnectedClients)
            .setNumberOfUniqueConnectedUsers(numberOfUniqueConnectedUsers)
            .setTimestamp(timestamp)
            .setVersionFormat(versionFormat);
    }

    private Duration parseDurationNull(String isoString) {
        // necessary because Duration.parse does not allow null
        return isoString != null ? Duration.parse(isoString) : null;
    }

    private Instant parseInstantNull(String isoString) {
        // necessary because Instant.parse does not allow null
        return isoString != null ? Instant.parse(isoString) : null;
    }
}
