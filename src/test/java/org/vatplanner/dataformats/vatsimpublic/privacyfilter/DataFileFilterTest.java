package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;

@RunWith(DataProviderRunner.class)
public class DataFileFilterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor_nullConfiguration_throwsIllegalArgumentException() {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        new DataFileFilter(null);

        // Assert (nothing to do)
    }

    @Test
    public void testConstructor_enabledFlightPlanRemarksRemoveAll_throwsUnsupportedOperationException() {
        // FIXME: temporarily, remove when feature is implemented

        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
                .setFlightPlanRemarksRemoveAll(true);

        thrown.expect(UnsupportedOperationException.class);

        // Act
        new DataFileFilter(configuration);

        // Assert (nothing to do)
    }

    @Test
    public void testConstructor_nonEmptyListOfFlightPlanRemarksRemoveAll_throwsUnsupportedOperationException() {
        // FIXME: temporarily, remove when feature is implemented

        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
                .setFlightPlanRemarksRemoveAllIfContaining(Arrays.asList(""));

        thrown.expect(UnsupportedOperationException.class);

        // Act
        new DataFileFilter(configuration);

        // Assert (nothing to do)
    }

    @Test
    public void testConstructor_enabledRemoveRealNameAndHomebase_throwsUnsupportedOperationException() {
        // FIXME: temporarily, remove when feature is implemented

        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
                .setRemoveRealNameAndHomebase(true);

        thrown.expect(UnsupportedOperationException.class);

        // Act
        new DataFileFilter(configuration);

        // Assert (nothing to do)
    }

    @Test
    public void testConstructor_enabledRemoveStreamingChannels_throwsUnsupportedOperationException() {
        // FIXME: temporarily, remove when feature is implemented

        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
                .setRemoveStreamingChannels(true);

        thrown.expect(UnsupportedOperationException.class);

        // Act
        new DataFileFilter(configuration);

        // Assert (nothing to do)
    }

    @Test
    public void testConstructor_enabledSubstituteObserverPrefix_throwsUnsupportedOperationException() {
        // FIXME: temporarily, remove when feature is implemented

        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
                .setSubstituteObserverPrefix(true);

        thrown.expect(UnsupportedOperationException.class);

        // Act
        new DataFileFilter(configuration);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"-1", "0", "1", "7", "9", "10"})
    public void testIsFormatVersionSupported_unsupported_returnsFalse(int formatVersion) {
        // Arrange
        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.isFormatVersionSupported(formatVersion);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testIsFormatVersionSupported_supported_returnsTrue() {
        // Arrange
        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.isFormatVersionSupported(8);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    @DataProvider({
        "8, 2019-01-16T21:36:00Z, 123, PT2M, PT5M",
        "-1, null, -1, null, null",
        "12, 2021-12-31T23:59:59Z, 951, PT1M, PT10M"
    })
    public void testCheckEqualMetadata_equal_returnsTrue(int versionFormat, String timestampIso, int numberOfConnectedClients, String minimumDataFileRetrievalIntervalIso, String minimumAtisRetrievalIntervalIso) {
        // Arrange
        DataFileMetaData mockA = mockMetaData(versionFormat, timestampIso, numberOfConnectedClients, minimumDataFileRetrievalIntervalIso, minimumAtisRetrievalIntervalIso);
        DataFileMetaData mockB = mockMetaData(versionFormat, timestampIso, numberOfConnectedClients, minimumDataFileRetrievalIntervalIso, minimumAtisRetrievalIntervalIso);

        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualMetadata(mockA, mockB);

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
        DataFileMetaData mockA = mockMetaData(versionFormatA, timestampIsoA, numberOfConnectedClientsA, minimumDataFileRetrievalIntervalIsoA, minimumAtisRetrievalIntervalIsoA);
        DataFileMetaData mockB = mockMetaData(versionFormatB, timestampIsoB, numberOfConnectedClientsB, minimumDataFileRetrievalIntervalIsoB, minimumAtisRetrievalIntervalIsoB);

        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualMetadata(mockA, mockB);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testCheckEqualMetadata_nullBoth_returnsTrue() {
        // Arrange
        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualMetadata(null, null);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    public void testCheckEqualMetadata_nullA_returnsFalse() {
        // Arrange
        DataFileMetaData mockMetaData = mock(DataFileMetaData.class);

        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualMetadata(null, mockMetaData);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testCheckEqualMetadata_nullB_returnsFalse() {
        // Arrange
        DataFileMetaData mockMetaData = mock(DataFileMetaData.class);

        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualMetadata(mockMetaData, null);

        // Assert
        assertThat(result, is(false));
    }

    private DataFileMetaData mockMetaData(int versionFormat, String timestampIso, int numberOfConnectedClients, String minimumDataFileRetrievalIntervalIso, String minimumAtisRetrievalIntervalIso) {
        Instant timestamp = parseInstantNull(timestampIso);
        Duration minimumDataFileRetrievalInterval = parseDurationNull(minimumDataFileRetrievalIntervalIso);
        Duration minimumAtisRetrievalInterval = parseDurationNull(minimumAtisRetrievalIntervalIso);

        return mockMetaData(versionFormat, timestamp, numberOfConnectedClients, minimumDataFileRetrievalInterval, minimumAtisRetrievalInterval);
    }

    private DataFileMetaData mockMetaData(int versionFormat, Instant timestamp, int numberOfConnectedClients, Duration minimumDataFileRetrievalInterval, Duration minimumAtisRetrievalInterval) {
        DataFileMetaData mock = mock(DataFileMetaData.class);

        doReturn(versionFormat).when(mock).getVersionFormat();
        doReturn(timestamp).when(mock).getTimestamp();
        doReturn(numberOfConnectedClients).when(mock).getNumberOfConnectedClients();
        doReturn(minimumDataFileRetrievalInterval).when(mock).getMinimumDataFileRetrievalInterval();
        doReturn(minimumAtisRetrievalInterval).when(mock).getMinimumAtisRetrievalInterval();

        return mock;
    }

    private DataFileFilter createDefaultConfigFilter() {
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();
        return new DataFileFilter(configuration);
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
