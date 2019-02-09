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
import org.vatplanner.dataformats.vatsimpublic.parser.FSDServer;
import org.vatplanner.dataformats.vatsimpublic.parser.VoiceServer;

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

    @Test
    @DataProvider({
        "some.server.net, Somewhere, Some Name, true, ab c12 3",
        "whatever, Don't Know, Yet another name, false, ",
        "null, Don't Know, Yet another name, false, ",
        "whatever, null, Yet another name, false, ",
        "whatever, Don't Know, null, false, ",
        "whatever, Don't Know, Yet another name, false, null"
    })
    public void testCheckEqualVoiceServer_equal_returnsTrue(String address, String location, String name, boolean clientConnectionAllowed, String rawServerType) {
        // Arrange
        VoiceServer mockA = mockVoiceServer(address, location, name, clientConnectionAllowed, rawServerType);
        VoiceServer mockB = mockVoiceServer(address, location, name, clientConnectionAllowed, rawServerType);

        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualVoiceServer(mockA, mockB);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    @DataProvider({
        //              object A                                        ||                    object B
        // address
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  someserver.net,  Somewhere,  Some Name,        true,  ab c12 3", // 0
        "whatever,        Don't Know, Yet another name, false, ,          what.ever,       Don't Know, Yet another name, false,         ", // 1
        "whatever,        Don't Know, Yet another name, false, ,          null,            Don't Know, Yet another name, false,         ", // 3
        "null,            Don't Know, Yet another name, false, ,          whatever,        Don't Know, Yet another name, false,         ", // 4
        // location
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  some.server.net, Some where, Some Name,        true,  ab c12 3", // 5
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Dont Know,  Yet another name, false,         ", // 6
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        null,       Yet another name, false,         ", // 7
        "whatever,        null, Yet another name, false, ,                whatever,        Don't Know, Yet another name, false,         ", // 8
        // name
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  some.server.net, Somewhere,  Any Name,         true,  ab c12 3", // 9
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, Some other name,  false,         ", // 10
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, null,             false,         ", // 11
        "whatever,        Don't Know, null,             false, ,          whatever,        Don't Know, Yet another name, false,         ", // 12
        // client connection allowed
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  some.server.net, Somewhere,  Some Name,        false, ab c12 3", // 13
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, Yet another name, true,          ", // 14
        // raw server type
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  some.server.net, Somewhere,  Some Name,        true,  ab c1 23", // 15
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, Yet another name, false, a       ", // 16
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, Yet another name, false, null    ", // 17
        "whatever,        Don't Know, Yet another name, false, null,      whatever,        Don't Know, Yet another name, false,         ", // 18
    })
    public void testCheckEqualVoiceServer_nonEqual_returnsFalse(String addressA, String locationA, String nameA, boolean clientConnectionAllowedA, String rawServerTypeA, String addressB, String locationB, String nameB, boolean clientConnectionAllowedB, String rawServerTypeB) {
        // Arrange
        VoiceServer mockA = mockVoiceServer(addressA, locationA, nameA, clientConnectionAllowedA, rawServerTypeA);
        VoiceServer mockB = mockVoiceServer(addressB, locationB, nameB, clientConnectionAllowedB, rawServerTypeB);

        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualVoiceServer(mockA, mockB);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    @DataProvider({
        "MYID, some.server.net, Somewhere, Name, true",
        "serverA, 123.4.56.7, Anywhere, Server A, false",
        "null, 123.4.56.7, Anywhere, Server A, false",
        "serverA, null, Anywhere, Server A, false",
        "serverA, 123.4.56.7, null, Server A, false",
        "serverA, 123.4.56.7, Anywhere, null, false"
    })
    public void testCheckEqualFSDServer_equal_returnsTrue(String id, String address, String location, String name, boolean clientConnectionAllowed) {
        // Arrange
        FSDServer mockA = mockFSDServer(id, address, location, name, clientConnectionAllowed);
        FSDServer mockB = mockFSDServer(id, address, location, name, clientConnectionAllowed);

        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualFSDServer(mockA, mockB);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    @DataProvider({
        //              object A                              ||                    object B
        // ID
        "MYID,    some.server.net, Somewhere, Name,     true,   myid,     some.server.net, Somewhere, Name,     true ", // 0
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  server A, 123.4.56.7,      Anywhere,  Server A, false", // 1
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  null,     123.4.56.7,      Anywhere,  Server A, false", // 2
        "null,    123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  Server A, false", // 3
        // address
        "MYID,    some.server.net, Somewhere, Name,     true,   MYID,     someserver.net,  Somewhere, Name,     true ", // 4
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.45.6.7,      Anywhere,  Server A, false", // 5
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  null,            Anywhere,  Server A, false", // 6
        "serverA, null,            Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  Server A, false", // 7
        // location
        "MYID,    some.server.net, Somewhere, Name,     true,   MYID,     some.server.net, SomeWhere, Name,     true ", // 8
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Any where, Server A, false", // 9
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      null,      Server A, false", // 10
        "serverA, 123.4.56.7,      null,      Server A, false,  serverA,  123.4.56.7,      Anywhere,  Server A, false", // 11
        // name
        "MYID,    some.server.net, Somewhere, Name,     true,   MYID,     some.server.net, Somewhere, NAME,     true ", // 12
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  ServerA,  false", // 13
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  null,     false", // 14
        "serverA, 123.4.56.7,      Anywhere,  null,     false,  serverA,  123.4.56.7,      Anywhere,  Server A, false", // 15
        // client connection allowed
        "MYID,    some.server.net, Somewhere, Name,     true,   MYID,     some.server.net, Somewhere, Name,     false", // 16
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  Server A, true ", // 17
    })
    public void testCheckEqualFSDServer_nonEqual_returnsFalse(String idA, String addressA, String locationA, String nameA, boolean clientConnectionAllowedA, String idB, String addressB, String locationB, String nameB, boolean clientConnectionAllowedB) {
        // Arrange
        FSDServer mockA = mockFSDServer(idA, addressA, locationA, nameA, clientConnectionAllowedA);
        FSDServer mockB = mockFSDServer(idB, addressB, locationB, nameB, clientConnectionAllowedB);

        DataFileFilter filter = createDefaultConfigFilter();

        // Act
        boolean result = filter.checkEqualFSDServer(mockA, mockB);

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

    private VoiceServer mockVoiceServer(String address, String location, String name, boolean clientConnectionAllowed, String rawServerType) {
        VoiceServer mock = mock(VoiceServer.class);

        doReturn(address).when(mock).getAddress();
        doReturn(location).when(mock).getLocation();
        doReturn(name).when(mock).getName();
        doReturn(clientConnectionAllowed).when(mock).isClientConnectionAllowed();
        doReturn(rawServerType).when(mock).getRawServerType();

        return mock;
    }

    private FSDServer mockFSDServer(String id, String address, String location, String name, boolean clientConnectionAllowed) {
        FSDServer mock = mock(FSDServer.class);

        doReturn(id).when(mock).getId();
        doReturn(address).when(mock).getAddress();
        doReturn(location).when(mock).getLocation();
        doReturn(name).when(mock).getName();
        doReturn(clientConnectionAllowed).when(mock).isClientConnectionAllowed();

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
