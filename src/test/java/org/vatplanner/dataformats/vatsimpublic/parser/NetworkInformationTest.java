package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

class NetworkInformationTest {

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(NetworkInformation.class);

    @BeforeEach
    void clearLog() {
        testLogger.clearAll();
    }

    @Test
    void testGetMessagesStartup_initially_listIsUnmodifiable() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<String> result = info.getStartupMessages();

        // Assert
        assertThat(result).isUnmodifiable();
    }

    @Test
    void testGetMessagesStartup_initially_returnsEmptyCollection() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<String> result = info.getStartupMessages();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testAddAsUrl_malformedUrl_logsWarning() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        info.addAsUrl("test", "this-is-not-a-url");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).satisfiesExactly(
            event -> assertAll(
                () -> assertThat(event).extracting(LoggingEvent::getMessage)
                                       .isEqualTo("URL for \"{}\" is malformed: \"{}\""),

                () -> assertThat(event).extracting(LoggingEvent::getArguments, as(LIST))
                                       .satisfiesExactly(
                                           args -> assertThat((Object[]) args).containsExactly(
                                               "test",
                                               "this-is-not-a-url"
                                           )
                                       )
            )
        );
    }

    @Test
    void testAddAsUrl_malformedUrl_returnsFalse() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        boolean result = info.addAsUrl("test", "this-is-not-a-url");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testAddAsUrl_validUniqueUrl_doesNotLog() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        info.addAsUrl("test", "http://www.test.com/test?test=123&test");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testAddAsUrl_validUniqueUrl_returnsTrue() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        boolean result = info.addAsUrl("test", "http://www.test.com/test?test=123&test");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testAddAsDataUrl_malformedUrl_logsWarning() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        info.addAsDataUrl("test", "this-is-not-a-url");

        // Assert
        // it seems we can not easily compare events which wrap a Throwable :(
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).satisfiesExactly(
            event -> assertAll(
                () -> assertThat(event).extracting(LoggingEvent::getMessage)
                                       .isEqualTo("URL for \"{}\" is malformed: \"{}\""),

                () -> assertThat(event).extracting(LoggingEvent::getArguments, as(LIST))
                                       .satisfiesExactly(
                                           args -> assertThat((Object[]) args).containsExactly(
                                               "test",
                                               "this-is-not-a-url"
                                           )
                                       )
            )
        );
    }

    @Test
    void testAddAsDataUrl_malformedUrl_returnsFalse() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        boolean success = info.addAsDataUrl("test", "this-is-not-a-url");

        // Assert
        assertThat(success).isFalse();
    }

    @Test
    void testAddAsDataUrl_validUniqueUrl_doesNotLog() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        info.addAsDataUrl("test", "http://www.test.com/test?test=123&test");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testAddAsDataUrl_validUniqueUrl_returnsTrue() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        boolean result = info.addAsDataUrl("test", "http://www.test.com/test?test=123&test");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testGetParameterUrls_undefinedKey_returnsEmptyList() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> result = info.getParameterUrls("surely-undefined");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetParameterUrls_knownKeyWithoutData_returnsEmptyList() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> result = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetParameterUrls_knownKeyWithData_returnsSameDataInOrder() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://a.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://b.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://c.com/");

        // Act
        List<URL> result = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(result).containsExactly(
            new URL("http://a.com/"),
            new URL("http://b.com/"),
            new URL("http://c.com/")
        );
    }

    @Test
    void testGetParameterUrls_undefinedKey_listIsUnmodifiable() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> result = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(result).isUnmodifiable();
    }

    @Test
    void testGetParameterUrls_knownKeyWithData_listIsUnmodifiable() {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://a.com/");

        // Act
        List<URL> result = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(result).isUnmodifiable();
    }

    @Test
    void testGetParameterUrls_mixedKeys_listContainsOnlyAssignedDataInOrder() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://b.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, "http://c.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://d.com/");

        // Act
        List<URL> result = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(result).containsExactly(
            new URL("http://b.com/"),
            new URL("http://d.com/")
        );
    }

    @Test
    void testGetParameterUrls_duplicateURLs_areRetained() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://1.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://1.com/");

        // Act
        List<URL> result = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(result).containsExactly(
            new URL("http://1.com/"),
            new URL("http://1.com/")
        );
    }

    @SuppressWarnings("deprecation")
    @Test
    void testGetAtisUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_ATIS)).thenReturn(expectedList);

        // Act
        List<URL> result = info.getAtisUrls();

        // Assert
        assertThat(result).isSameAs(expectedList);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testGetDataUrls_withoutParameter_proxiesGetDataFileUrlsForLegacyFormat() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        doReturn(expectedList).when(info).getDataUrls(DataFileFormat.LEGACY);

        // Act
        List<URL> result = info.getDataFileUrls();

        // Assert
        assertThat(result).isSameAs(expectedList);
    }

    @Test
    void testGetMetarUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_METAR)).thenReturn(expectedList);

        // Act
        List<URL> result = info.getMetarUrls();

        // Assert
        assertThat(result).isSameAs(expectedList);
    }

    @Test
    void testGetMovedToUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_MOVED)).thenReturn(expectedList);

        // Act
        List<URL> result = info.getMovedToUrls();

        // Assert
        assertThat(result).isSameAs(expectedList);
    }

    @Test
    void testGetServersFileUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE)).thenReturn(expectedList);

        // Act
        List<URL> result = info.getServersFileUrls();

        // Assert
        assertThat(result).isSameAs(expectedList);
    }

    @Test
    void testGetUserStatisticsUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS)).thenReturn(expectedList);

        // Act
        List<URL> result = info.getUserStatisticsUrls();

        // Assert
        assertThat(result).isSameAs(expectedList);
    }

    @ParameterizedTest
    @EnumSource(DataFileFormat.class)
    void testGetDataUrls_initially_returnsEmpty(DataFileFormat format) {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> result = info.getDataUrls(format);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetDataUrls_initially_listIsUnmodifiable() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> result = info.getDataUrls(DataFileFormat.JSON3);

        // Assert
        assertThat(result).isUnmodifiable();
    }

    @ParameterizedTest
    @EnumSource(DataFileFormat.class)
    void testGetDataUrls_addedUrlsForMatchingJsonKey_returnsExpectedUrls(DataFileFormat format) {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        String[] expectedUrlStrings = new String[]{
            "http://a.com/somewhere.txt",
            "https://www.wherever.net/",
            "https://data.some-place.de/here/as/well"
        };

        for (String urlString : expectedUrlStrings) {
            info.addAsDataUrl(format.getNetworkInformationDataKey(), urlString);
        }

        // Act
        List<URL> result = info.getDataUrls(format);

        // Assert
        assertThat(result).extracting(URL::toString)
                          .containsExactly(expectedUrlStrings);
    }

    @Test
    void testGetDataUrls_addedUrlsForMultipleJsonKeys_returnsOnlyExpectedUrls() {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        String expectedLegacyUrlString1 = "http://legacy/1";
        String expectedLegacyUrlString2 = "http://legacy/2";
        String expectedJson3UrlString1 = "http://json3/1";
        String expectedJson3UrlString2 = "http://json3/2";
        info.addAsDataUrl(DataFileFormat.LEGACY.getNetworkInformationDataKey(), expectedLegacyUrlString1);
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), expectedJson3UrlString1);
        info.addAsDataUrl("something_totally_different", "http://this/is/unexpected");
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey() + "_nope",
                          "http://this/is/also/unexpected"
        );
        info.addAsDataUrl(DataFileFormat.LEGACY.getNetworkInformationDataKey(), expectedLegacyUrlString2);
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), expectedJson3UrlString2);

        // Act
        List<URL> resultLegacy = info.getDataUrls(DataFileFormat.LEGACY);
        List<URL> resultJson3 = info.getDataUrls(DataFileFormat.JSON3);

        // Assert
        assertAll(
            () -> assertThat(resultLegacy).extracting(URL::toString)
                                          .containsExactly(expectedLegacyUrlString1, expectedLegacyUrlString2),

            () -> assertThat(resultJson3).extracting(URL::toString)
                                         .containsExactly(expectedJson3UrlString1, expectedJson3UrlString2)
        );
    }

    @Test
    void testGetDataUrls_addedUrlsForMatchingJsonKey_listIsUnmodifiable() {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), "http://some/url");

        // Act
        List<URL> result = info.getDataUrls(DataFileFormat.JSON3);

        // Assert
        assertThat(result).isUnmodifiable();
    }

    @Test
    void testGetAllUrlsByDataKey_initially_returnsEmpty() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        Map<String, List<URL>> result = info.getAllUrlsByDataKey();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllUrlsByDataKey_initially_mapIsUnmodifiable() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        Map<String, List<URL>> result = info.getAllUrlsByDataKey();

        // Assert
        assertThat(result).isUnmodifiable();
    }

    @Test
    void testGetAllUrlsByDataKey_addedUrls_returnsAllUrlsIndexedByKey() {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        String expectedLegacyUrlString1 = "http://legacy/1";
        String expectedLegacyUrlString2 = "http://legacy/2";
        String expectedJson3UrlString1 = "http://json3/1";
        String expectedJson3UrlString2 = "http://json3/2";
        String someExtraKey1 = "something_totally_different";
        String someExtraKey2 = "this.is.not." + DataFileFormat.JSON3.getNetworkInformationDataKey();
        String expectedExtraValue1 = "http://extra/value1";
        String expectedExtraValue2 = "http://extra/value2";
        info.addAsDataUrl(DataFileFormat.LEGACY.getNetworkInformationDataKey(), expectedLegacyUrlString1);
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), expectedJson3UrlString1);
        info.addAsDataUrl(someExtraKey1, expectedExtraValue1);
        info.addAsDataUrl(someExtraKey2, expectedExtraValue2);
        info.addAsDataUrl(DataFileFormat.LEGACY.getNetworkInformationDataKey(), expectedLegacyUrlString2);
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), expectedJson3UrlString2);

        // Act
        Map<String, List<URL>> result = info.getAllUrlsByDataKey();

        // Assert
        assertAll(
            () -> assertThat(result).hasSize(4),

            () -> assertThat(result.get(DataFileFormat.LEGACY.getNetworkInformationDataKey())).extracting(URL::toString)
                                                                                              .containsExactly(
                                                                                                  expectedLegacyUrlString1,
                                                                                                  expectedLegacyUrlString2
                                                                                              ),

            () -> assertThat(result.get(DataFileFormat.JSON3.getNetworkInformationDataKey())).extracting(URL::toString)
                                                                                             .containsExactly(
                                                                                                 expectedJson3UrlString1,
                                                                                                 expectedJson3UrlString2
                                                                                             ),

            () -> assertThat(result.get(someExtraKey1)).extracting(URL::toString)
                                                       .containsExactly(
                                                           expectedExtraValue1
                                                       ),

            () -> assertThat(result.get(someExtraKey2)).extracting(URL::toString)
                                                       .containsExactly(
                                                           expectedExtraValue2
                                                       )
        );
    }

    @Test
    void testGetAllUrlsByDataKey_addedUrls_listIsUnmodifiable() {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), "http://some/url");

        // Act
        Map<String, List<URL>> result = info.getAllUrlsByDataKey();

        // Assert
        assertThat(result).isUnmodifiable();
    }

    @Test
    void testGetAllUrlsByDataKey_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
        // Arrange
        String bothInstancesKey1 = "both1";
        String bothInstancesKey2 = "both2";
        String onlyTestInstanceKey = "test.only";
        String onlyOtherInstanceKey = "other.only";

        String expectedValueBoth1_1 = "http://expected/1/1";
        String expectedValueBoth1_2 = "http://expected/1/2";
        String expectedValueBoth2_1 = "http://expected/2/1";
        String expectedValueTestInstanceOnly1 = "http://test.only/1";
        String expectedValueTestInstanceOnly2 = "http://test.only/2";
        String expectedValueOtherInstanceOnly1 = "http://other.only/1";
        String expectedValueOtherInstanceOnly2 = "http://other.only/2";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.addAsDataUrl(bothInstancesKey1, expectedValueBoth1_1);
        testedInstance.addAsDataUrl(bothInstancesKey1, expectedValueBoth1_2);
        testedInstance.addAsDataUrl(bothInstancesKey2, expectedValueBoth2_1);
        testedInstance.addAsDataUrl(bothInstancesKey1, expectedValueTestInstanceOnly1);
        testedInstance.addAsDataUrl(bothInstancesKey2, expectedValueTestInstanceOnly2);
        testedInstance.addAsDataUrl(onlyTestInstanceKey, expectedValueTestInstanceOnly1);

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.addAsDataUrl(bothInstancesKey1, expectedValueBoth1_1);
        otherInstance.addAsDataUrl(bothInstancesKey1, expectedValueBoth1_2);
        otherInstance.addAsDataUrl(bothInstancesKey2, expectedValueBoth2_1);
        otherInstance.addAsDataUrl(bothInstancesKey1, expectedValueOtherInstanceOnly1);
        otherInstance.addAsDataUrl(bothInstancesKey2, expectedValueOtherInstanceOnly2);
        otherInstance.addAsDataUrl(onlyOtherInstanceKey, expectedValueOtherInstanceOnly1);

        testedInstance.addAll(otherInstance);

        // Act
        Map<String, List<URL>> result = testedInstance.getAllUrlsByDataKey();

        // Assert
        assertAll(
            () -> assertThat(result).hasSize(4),

            () -> assertThat(result.get(bothInstancesKey1)).extracting(URL::toString)
                                                           .containsExactlyInAnyOrder(
                                                               expectedValueBoth1_1,
                                                               expectedValueBoth1_2,
                                                               expectedValueTestInstanceOnly1,
                                                               expectedValueOtherInstanceOnly1
                                                           ),

            () -> assertThat(result.get(bothInstancesKey2)).extracting(URL::toString)
                                                           .containsExactlyInAnyOrder(
                                                               expectedValueBoth2_1,
                                                               expectedValueTestInstanceOnly2,
                                                               expectedValueOtherInstanceOnly2
                                                           ),

            () -> assertThat(result.get(onlyTestInstanceKey)).extracting(URL::toString)
                                                             .containsExactlyInAnyOrder(
                                                                 expectedValueTestInstanceOnly1
                                                             ),

            () -> assertThat(result.get(onlyOtherInstanceKey)).extracting(URL::toString)
                                                              .containsExactlyInAnyOrder(
                                                                  expectedValueOtherInstanceOnly1
                                                              )
        );
    }

    @Test
    void testGetMetarUrls_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
        // Arrange
        String expectedValueBoth1 = "http://expected/1";
        String expectedValueBoth2 = "http://expected/2";
        String expectedValueTestInstanceOnly = "http://test.only/";
        String expectedValueOtherInstanceOnly = "http://other.only/";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_METAR, expectedValueBoth1);
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_METAR, expectedValueBoth2);
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_METAR, expectedValueTestInstanceOnly);

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_METAR, expectedValueBoth1);
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_METAR, expectedValueBoth2);
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_METAR, expectedValueOtherInstanceOnly);

        testedInstance.addAll(otherInstance);

        // Act
        List<URL> result = testedInstance.getMetarUrls();

        // Assert
        assertThat(result).extracting(URL::toString)
                          .containsExactlyInAnyOrder(
                              expectedValueBoth1,
                              expectedValueBoth2,
                              expectedValueTestInstanceOnly,
                              expectedValueOtherInstanceOnly
                          );
    }

    @Test
    void testGetMovedToUrls_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
        // Arrange
        String expectedValueBoth1 = "http://expected/1";
        String expectedValueBoth2 = "http://expected/2";
        String expectedValueTestInstanceOnly = "http://test.only/";
        String expectedValueOtherInstanceOnly = "http://other.only/";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, expectedValueBoth1);
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, expectedValueBoth2);
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, expectedValueTestInstanceOnly);

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, expectedValueBoth1);
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, expectedValueBoth2);
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, expectedValueOtherInstanceOnly);

        testedInstance.addAll(otherInstance);

        // Act
        List<URL> result = testedInstance.getMovedToUrls();

        // Assert
        assertThat(result).extracting(URL::toString)
                          .containsExactlyInAnyOrder(
                              expectedValueBoth1,
                              expectedValueBoth2,
                              expectedValueTestInstanceOnly,
                              expectedValueOtherInstanceOnly
                          );
    }

    @Test
    void testGetServersFileUrls_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
        // Arrange
        String expectedValueBoth1 = "http://expected/1";
        String expectedValueBoth2 = "http://expected/2";
        String expectedValueTestInstanceOnly = "http://test.only/";
        String expectedValueOtherInstanceOnly = "http://other.only/";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, expectedValueBoth1);
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, expectedValueBoth2);
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, expectedValueTestInstanceOnly);

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, expectedValueBoth1);
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, expectedValueBoth2);
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, expectedValueOtherInstanceOnly);

        testedInstance.addAll(otherInstance);

        // Act
        List<URL> result = testedInstance.getServersFileUrls();

        // Assert
        assertThat(result).extracting(URL::toString)
                          .containsExactlyInAnyOrder(
                              expectedValueBoth1,
                              expectedValueBoth2,
                              expectedValueTestInstanceOnly,
                              expectedValueOtherInstanceOnly
                          );
    }

    @Test
    void testGetStartupMessages_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
        // Arrange
        String expectedValueBoth1 = "first message common to both instances";
        String expectedValueBoth2 = "second message common to both instances";
        String expectedValueTestInstanceOnly = "only on tested instance";
        String expectedValueOtherInstanceOnly = "only on other instance";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.addStartupMessage(expectedValueBoth1);
        testedInstance.addStartupMessage(expectedValueBoth2);
        testedInstance.addStartupMessage(expectedValueTestInstanceOnly);

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.addStartupMessage(expectedValueBoth1);
        otherInstance.addStartupMessage(expectedValueBoth2);
        otherInstance.addStartupMessage(expectedValueOtherInstanceOnly);

        testedInstance.addAll(otherInstance);

        // Act
        List<String> result = testedInstance.getStartupMessages();

        // Assert
        assertThat(result).containsExactlyInAnyOrder(
            expectedValueBoth1,
            expectedValueBoth2,
            expectedValueTestInstanceOnly,
            expectedValueOtherInstanceOnly
        );
    }

    @Test
    void testGetUserStatisticsUrls_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
        // Arrange
        String expectedValueBoth1 = "http://expected/1";
        String expectedValueBoth2 = "http://expected/2";
        String expectedValueTestInstanceOnly = "http://test.only/";
        String expectedValueOtherInstanceOnly = "http://other.only/";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS, expectedValueBoth1);
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS, expectedValueBoth2);
        testedInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS, expectedValueTestInstanceOnly);

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS, expectedValueBoth1);
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS, expectedValueBoth2);
        otherInstance.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS, expectedValueOtherInstanceOnly);

        testedInstance.addAll(otherInstance);

        // Act
        List<URL> result = testedInstance.getUserStatisticsUrls();

        // Assert
        assertThat(result).extracting(URL::toString)
                          .containsExactlyInAnyOrder(
                              expectedValueBoth1,
                              expectedValueBoth2,
                              expectedValueTestInstanceOnly,
                              expectedValueOtherInstanceOnly
                          );
    }

    @Test
    void testGetWhazzUpString_afterAddAllAndNotSetPreviously_returnsOtherInstanceWhazzup() {
        // Arrange
        String expectedValue = "5432:whazzup";

        NetworkInformation testedInstance = new NetworkInformation();

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.setWhazzUpString(expectedValue);

        testedInstance.addAll(otherInstance);

        // Act
        String result = testedInstance.getWhazzUpString();

        // Assert
        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    void testGetWhazzUpString_afterAddAllAndSetPreviously_returnsTestedInstanceWhazzup() {
        // Arrange
        String expectedValue = "5432:whazzup";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.setWhazzUpString(expectedValue);

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.setWhazzUpString("9876:unexpected");

        testedInstance.addAll(otherInstance);

        // Act
        String result = testedInstance.getWhazzUpString();

        // Assert
        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    void testGetWhazzUpString_afterAddAllAndSetOnlyOnTestedInstance_returnsTestedInstanceWhazzup() {
        // Arrange
        String expectedValue = "5432:whazzup";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.setWhazzUpString(expectedValue);

        NetworkInformation otherInstance = new NetworkInformation();

        testedInstance.addAll(otherInstance);

        // Act
        String result = testedInstance.getWhazzUpString();

        // Assert
        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    void testGetWhazzUpString_afterAddAllAndNeverSet_returnsNull() {
        // Arrange
        NetworkInformation testedInstance = new NetworkInformation();
        NetworkInformation otherInstance = new NetworkInformation();
        testedInstance.addAll(otherInstance);

        // Act
        String result = testedInstance.getWhazzUpString();

        // Assert
        assertThat(result).isNull();
    }
}
