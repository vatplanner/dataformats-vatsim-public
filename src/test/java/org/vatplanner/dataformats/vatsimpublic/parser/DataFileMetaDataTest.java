package org.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.time.Duration;
import java.time.Instant;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class DataFileMetaDataTest {

    @Test
    public void testEquals_null_returnsFalse() {
        // Arrange
        DataFileMetaData a = new DataFileMetaData();
        DataFileMetaData b = null;

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testEquals_wrongClass_returnsFalse() {
        // Arrange
        DataFileMetaData a = new DataFileMetaData();
        Object b = new Object();

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    @DataProvider({
        "8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M",
        "-1, null, -1, null, null",
        "12, 2021-12-31T23:59:59Z, 951, PT1M, PT10M"
    })
    public void testCheckEqualMetadata_equal_returnsTrue(int versionFormat, String timestampIso, int numberOfConnectedClients, String minimumDataFileRetrievalIntervalIso, String minimumAtisRetrievalIntervalIso) {
        // Arrange
        DataFileMetaData a = createMetaData(versionFormat, timestampIso, numberOfConnectedClients, minimumDataFileRetrievalIntervalIso, minimumAtisRetrievalIntervalIso);
        DataFileMetaData b = createMetaData(versionFormat, timestampIso, numberOfConnectedClients, minimumDataFileRetrievalIntervalIso, minimumAtisRetrievalIntervalIso);

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    @DataProvider({
        //              object A                    ||                    object B
        // version format
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     7, 2019-01-16T21:36:00Z, 123, PT2M, PT5M", // 0
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,    -1, 2019-01-16T21:36:00Z, 123, PT2M, PT5M", // 1
        "-1, null,                  -1, null, null,     8, null,                  -1, null, null", // 2
        "-1, null,                  -1, null, null,    10, null,                  -1, null, null", // 3
        // timestamp
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     8, null,                 123, PT2M, PT5M", // 4
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     8, 2019-01-16T21:36:01Z, 123, PT2M, PT5M", // 5
        "-1, null,                  -1, null, null,    -1, 2019-01-16T21:36:00Z,  -1, null, null", // 6
        // number of connected clients
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 122, PT2M, PT5M", // 7
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     8, 2019-01-16T21:36:00Z,  -1, PT2M, PT5M", // 8
        "-1, null,                  -1, null, null,    -1, null,                   0, null, null", // 9
        "-1, null,                  -1, null, null,    -1, null,                  42, null, null", // 10
        // minimum data file retrieval interval
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, PT1M, PT5M", // 11
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, null, PT5M", // 12
        "-1, null,                  -1, null, null,    -1, null,                  -1, PT0S, null", // 13
        // minimum ATIS file retrieval interval
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, PT2M, PT1M", // 14
        " 8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M,     8, 2019-01-16T21:36:00Z, 123, PT2M, null", // 15
        "-1, null,                  -1, null, null,    -1, null,                  -1, null, PT15M" // 16
    })
    public void testCheckEqualMetadata_nonEqual_returnsFalse(int versionFormatA, String timestampIsoA, int numberOfConnectedClientsA, String minimumDataFileRetrievalIntervalIsoA, String minimumAtisRetrievalIntervalIsoA, int versionFormatB, String timestampIsoB, int numberOfConnectedClientsB, String minimumDataFileRetrievalIntervalIsoB, String minimumAtisRetrievalIntervalIsoB) {
        // Arrange
        DataFileMetaData a = createMetaData(versionFormatA, timestampIsoA, numberOfConnectedClientsA, minimumDataFileRetrievalIntervalIsoA, minimumAtisRetrievalIntervalIsoA);
        DataFileMetaData b = createMetaData(versionFormatB, timestampIsoB, numberOfConnectedClientsB, minimumDataFileRetrievalIntervalIsoB, minimumAtisRetrievalIntervalIsoB);

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    private DataFileMetaData createMetaData(int versionFormat, String timestampIso, int numberOfConnectedClients, String minimumDataFileRetrievalIntervalIso, String minimumAtisRetrievalIntervalIso) {
        Instant timestamp = parseInstantNull(timestampIso);
        Duration minimumDataFileRetrievalInterval = parseDurationNull(minimumDataFileRetrievalIntervalIso);
        Duration minimumAtisRetrievalInterval = parseDurationNull(minimumAtisRetrievalIntervalIso);

        return createMetaData(versionFormat, timestamp, numberOfConnectedClients, minimumDataFileRetrievalInterval, minimumAtisRetrievalInterval);
    }

    private DataFileMetaData createMetaData(int versionFormat, Instant timestamp, int numberOfConnectedClients, Duration minimumDataFileRetrievalInterval, Duration minimumAtisRetrievalInterval) {
        return new DataFileMetaData()
                .setMinimumAtisRetrievalInterval(minimumAtisRetrievalInterval)
                .setMinimumDataFileRetrievalInterval(minimumDataFileRetrievalInterval)
                .setNumberOfConnectedClients(numberOfConnectedClients)
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
