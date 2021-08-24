package org.vatplanner.dataformats.vatsimpublic.parser;

import static com.tngtech.java.junit.dataprovider.DataProviders.crossProduct;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.junit.ExpectedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

@RunWith(DataProviderRunner.class)
public class NetworkInformationTest {

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(NetworkInformation.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void clearLog() {
        testLogger.clearAll();
    }

    @Test
    public void testGetMessagesStartup_initially_listIsUnmodifiable() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<String> startupMessages = info.getStartupMessages();

        // Assert
        thrown.expect(UnsupportedOperationException.class);
        startupMessages.add("test");
    }

    @Test
    public void testGetMessagesStartup_initially_returnsEmptyCollection() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<String> startupMessages = info.getStartupMessages();

        // Assert
        assertThat(startupMessages, is(empty()));
    }

    @Test
    public void testGetMessagesStartup_initially_returnsList() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<String> startupMessages = info.getStartupMessages();

        // Assert
        assertThat(startupMessages, is(instanceOf(List.class)));
    }

    @Test
    public void testAddAsUrl_malformedUrl_logsWarning() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        info.addAsUrl("test", "this-is-not-a-url");

        // Assert
        // it seems we can not easily compare events which wrap a Throwable :(
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents.size(), is(1));
        LoggingEvent actualEvent = loggingEvents.iterator().next();
        assertThat(actualEvent.getMessage(), is("URL for \"{}\" is malformed: \"{}\""));
        ImmutableList<Object> arguments = actualEvent.getArguments();
        assertThat(arguments.size(), is(1));
        Object[] actualArguments = (Object[]) arguments.get(0);
        assertThat(actualArguments.length, is(2));
        assertThat(actualArguments[0], is("test"));
        assertThat(actualArguments[1], is("this-is-not-a-url"));
    }

    @Test
    public void testAddAsUrl_malformedUrl_returnsFalse() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        boolean success = info.addAsUrl("test", "this-is-not-a-url");

        // Assert
        assertThat(success, is(false));
    }

    @Test
    public void testAddAsUrl_validUniqueUrl_doesNotLog() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        info.addAsUrl("test", "http://www.test.com/test?test=123&test");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(emptyIterable()));
    }

    @Test
    public void testAddAsUrl_validUniqueUrl_returnsTrue() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        boolean success = info.addAsUrl("test", "http://www.test.com/test?test=123&test");

        // Assert
        assertThat(success, is(true));
    }

    @Test
    public void testAddAsDataUrl_malformedUrl_logsWarning() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        info.addAsDataUrl("test", "this-is-not-a-url");

        // Assert
        // it seems we can not easily compare events which wrap a Throwable :(
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents.size(), is(1));
        LoggingEvent actualEvent = loggingEvents.iterator().next();
        assertThat(actualEvent.getMessage(), is("URL for \"{}\" is malformed: \"{}\""));
        ImmutableList<Object> arguments = actualEvent.getArguments();
        assertThat(arguments.size(), is(1));
        Object[] actualArguments = (Object[]) arguments.get(0);
        assertThat(actualArguments.length, is(2));
        assertThat(actualArguments[0], is("test"));
        assertThat(actualArguments[1], is("this-is-not-a-url"));
    }

    @Test
    public void testAddAsDataUrl_malformedUrl_returnsFalse() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        boolean success = info.addAsDataUrl("test", "this-is-not-a-url");

        // Assert
        assertThat(success, is(false));
    }

    @Test
    public void testAddAsDataUrl_validUniqueUrl_doesNotLog() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        info.addAsDataUrl("test", "http://www.test.com/test?test=123&test");

        // Assert
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(emptyIterable()));
    }

    @Test
    public void testAddAsDataUrl_validUniqueUrl_returnsTrue() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        boolean success = info.addAsDataUrl("test", "http://www.test.com/test?test=123&test");

        // Assert
        assertThat(success, is(true));
    }

    @Test
    public void testGetParameterUrls_undefinedKey_returnsEmptyList() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> res = info.getParameterUrls("surely-undefined");

        // Assert
        assertThat(res, is(empty()));
    }

    @Test
    public void testGetParameterUrls_knownKeyWithoutData_returnsEmptyList() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> res = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(res, is(empty()));
    }

    @Test
    public void testGetParameterUrls_knownKeyWithData_returnsSameDataInOrder() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://a.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://b.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://c.com/");

        // Act
        List<URL> res = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(res.size(), is(3));
        assertThat(res.get(0), is(equalTo(new URL("http://a.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://b.com/"))));
        assertThat(res.get(2), is(equalTo(new URL("http://c.com/"))));
    }

    @Test
    public void testGetParameterUrls_undefinedKey_listIsUnmodifiable() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> res = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        thrown.expect(UnsupportedOperationException.class);

        res.add(new URL("http://test.de/"));
    }

    @Test
    public void testGetParameterUrls_knownKeyWithData_listIsUnmodifiable() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://a.com/");

        // Act
        List<URL> res = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        thrown.expect(UnsupportedOperationException.class);

        res.add(new URL("http://test.de/"));
    }

    @Test
    public void testGetParameterUrls_mixedKeys_listContainsOnlyAssignedDataInOrder() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://b.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, "http://c.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://d.com/");

        // Act
        List<URL> res = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(res.size(), is(2));
        assertThat(res.get(0), is(equalTo(new URL("http://b.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://d.com/"))));
    }

    @Test
    public void testGetParameterUrls_duplicateURLs_areRetained() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://1.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://1.com/");

        // Act
        List<URL> res = info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(res.size(), is(2));
        assertThat(res.get(0), is(equalTo(new URL("http://1.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://1.com/"))));
    }

    @Test
    public void testGetAtisUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_ATIS)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getAtisUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetDataUrls_withoutParameter_proxiesGetDataFileUrlsForLegacyFormat() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        doReturn(expectedList).when(info).getDataUrls(eq(DataFileFormat.LEGACY));

        // Act
        List<URL> res = info.getDataFileUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetMetarUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_METAR)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getMetarUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetMovedToUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_MOVED)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getMovedToUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetServersFileUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getServersFileUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetUserStatisticsUrls_always_proxiesGetParameterUrls() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getParameterUrls(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getUserStatisticsUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @DataProvider
    public static Object[][] dataProviderDataFileFormats() {
        DataFileFormat[] formats = DataFileFormat.values();
        Object[][] out = new Object[formats.length][1];

        int i = 0;
        for (DataFileFormat format : formats) {
            out[i++][0] = format;
        }

        return out;
    }

    @Test
    @UseDataProvider("dataProviderDataFileFormats")
    public void testGetDataUrls_initially_returnsEmpty(DataFileFormat format) {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> result = info.getDataUrls(format);

        // Assert
        assertThat(result, is(empty()));
    }

    @Test
    public void testGetDataUrls_initially_listIsUnmodifiable() throws Exception {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> result = info.getDataUrls(DataFileFormat.JSON3);

        // Assert
        thrown.expect(UnsupportedOperationException.class);
        result.add(new URL("http://new/"));
    }

    @DataProvider
    public static Object[][] dataProviderDataFileFormatsAndUrlStrings() {
        return crossProduct( //
            dataProviderDataFileFormats(), //
            new Object[][] { //
                { //
                    new String[] { //
                        "http://a.com/somewhere.txt", //
                        "https://www.wherever.net/", //
                        "https://data.some-place.de/here/as/well" //
                    } //
                } //
            } //
        );
    }

    @Test
    @UseDataProvider("dataProviderDataFileFormatsAndUrlStrings")
    public void testGetDataUrls_addedUrlsForMatchingJsonKey_returnsExpectedUrls(DataFileFormat format, String[] expectedUrlStrings) {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        for (String urlString : expectedUrlStrings) {
            info.addAsDataUrl(format.getNetworkInformationDataKey(), urlString);
        }

        // Act
        List<URL> result = info.getDataUrls(format);

        // Assert
        assertThat(asStrings(result), contains(expectedUrlStrings));
    }

    @Test
    public void testGetDataUrls_addedUrlsForMultipleJsonKeys_returnsOnlyExpectedUrls() {
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
            "http://this/is/also/unexpected");
        info.addAsDataUrl(DataFileFormat.LEGACY.getNetworkInformationDataKey(), expectedLegacyUrlString2);
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), expectedJson3UrlString2);

        // Act
        List<URL> resultLegacy = info.getDataUrls(DataFileFormat.LEGACY);
        List<URL> resultJson3 = info.getDataUrls(DataFileFormat.JSON3);

        // Assert
        assertThat(asStrings(resultLegacy), contains(expectedLegacyUrlString1, expectedLegacyUrlString2));
        assertThat(asStrings(resultJson3), contains(expectedJson3UrlString1, expectedJson3UrlString2));
    }

    @Test
    public void testGetDataUrls_addedUrlsForMatchingJsonKey_listIsUnmodifiable() throws Exception {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), "http://some/url");

        // Act
        List<URL> result = info.getDataUrls(DataFileFormat.JSON3);

        // Assert
        thrown.expect(UnsupportedOperationException.class);
        result.add(new URL("http://new/"));
    }

    @Test
    public void testGetAllUrlsByDataKey_initially_returnsEmpty() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        Map<String, List<URL>> result = info.getAllUrlsByDataKey();

        // Assert
        assertThat(result.entrySet(), is(empty()));
    }

    @Test
    public void testGetAllUrlsByDataKey_initially_mapIsUnmodifiable() throws Exception {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        Map<String, List<URL>> result = info.getAllUrlsByDataKey();

        // Assert
        thrown.expect(UnsupportedOperationException.class);
        result.put("some_key", new ArrayList<>());
    }

    @Test
    public void testGetAllUrlsByDataKey_addedUrls_returnsAllUrlsIndexedByKey() {
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
        assertThat(result.size(), is(equalTo(4)));
        assertThat(
            asStrings(result.get(DataFileFormat.LEGACY.getNetworkInformationDataKey())),
            contains(expectedLegacyUrlString1, expectedLegacyUrlString2) //
        );
        assertThat(
            asStrings(result.get(DataFileFormat.JSON3.getNetworkInformationDataKey())),
            contains(expectedJson3UrlString1, expectedJson3UrlString2) //
        );
        assertThat(
            asStrings(result.get(someExtraKey1)),
            contains(expectedExtraValue1) //
        );
        assertThat(
            asStrings(result.get(someExtraKey2)),
            contains(expectedExtraValue2) //
        );
    }

    @Test
    public void testGetAllUrlsByDataKey_addedUrls_listIsUnmodifiable() throws Exception {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), "http://some/url");

        // Act
        Map<String, List<URL>> result = info.getAllUrlsByDataKey();

        // Assert
        thrown.expect(UnsupportedOperationException.class);
        result.put("some_key", new ArrayList<>());
    }

    @Test
    public void testGetAllUrlsByDataKey_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
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
        assertThat(result.size(), is(equalTo(4)));
        assertThat(
            asStrings(result.get(bothInstancesKey1)),
            containsInAnyOrder(
                expectedValueBoth1_1,
                expectedValueBoth1_2,
                expectedValueTestInstanceOnly1,
                expectedValueOtherInstanceOnly1 //
            ) //
        );
        assertThat(
            asStrings(result.get(bothInstancesKey2)),
            containsInAnyOrder(
                expectedValueBoth2_1,
                expectedValueTestInstanceOnly2,
                expectedValueOtherInstanceOnly2 //
            ) //
        );
        assertThat(
            asStrings(result.get(onlyTestInstanceKey)),
            containsInAnyOrder(
                expectedValueTestInstanceOnly1 //
            ) //
        );
        assertThat(
            asStrings(result.get(onlyOtherInstanceKey)),
            containsInAnyOrder(
                expectedValueOtherInstanceOnly1 //
            ) //
        );
    }

    @Test
    public void testGetMetarUrls_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
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
        assertThat(result.size(), is(equalTo(4)));
        assertThat(
            asStrings(result),
            containsInAnyOrder(
                expectedValueBoth1,
                expectedValueBoth2,
                expectedValueTestInstanceOnly,
                expectedValueOtherInstanceOnly //
            ) //
        );
    }

    @Test
    public void testGetMovedToUrls_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
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
        assertThat(result.size(), is(equalTo(4)));
        assertThat(
            asStrings(result),
            containsInAnyOrder(
                expectedValueBoth1,
                expectedValueBoth2,
                expectedValueTestInstanceOnly,
                expectedValueOtherInstanceOnly //
            ) //
        );
    }

    @Test
    public void testGetServersFileUrls_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
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
        assertThat(result.size(), is(equalTo(4)));
        assertThat(
            asStrings(result),
            containsInAnyOrder(
                expectedValueBoth1,
                expectedValueBoth2,
                expectedValueTestInstanceOnly,
                expectedValueOtherInstanceOnly //
            ) //
        );
    }

    @Test
    public void testGetStartupMessages_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
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
        assertThat(result.size(), is(equalTo(4)));
        assertThat(
            result,
            containsInAnyOrder(
                expectedValueBoth1,
                expectedValueBoth2,
                expectedValueTestInstanceOnly,
                expectedValueOtherInstanceOnly //
            ) //
        );
    }

    @Test
    public void testGetUserStatisticsUrls_afterAddAll_returnsDeduplicatedCombinationOfBothInstances() {
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
        assertThat(result.size(), is(equalTo(4)));
        assertThat(
            asStrings(result),
            containsInAnyOrder(
                expectedValueBoth1,
                expectedValueBoth2,
                expectedValueTestInstanceOnly,
                expectedValueOtherInstanceOnly //
            ) //
        );
    }

    @Test
    public void testGetWhazzUpString_afterAddAllAndNotSetPreviously_returnsOtherInstanceWhazzup() {
        // Arrange
        String expectedValue = "5432:whazzup";

        NetworkInformation testedInstance = new NetworkInformation();

        NetworkInformation otherInstance = new NetworkInformation();
        otherInstance.setWhazzUpString(expectedValue);

        testedInstance.addAll(otherInstance);

        // Act
        String result = testedInstance.getWhazzUpString();

        // Assert
        assertThat(result, is(equalTo(expectedValue)));
    }

    @Test
    public void testGetWhazzUpString_afterAddAllAndSetPreviously_returnsTestedInstanceWhazzup() {
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
        assertThat(result, is(equalTo(expectedValue)));
    }

    @Test
    public void testGetWhazzUpString_afterAddAllAndSetOnlyOnTestedInstance_returnsTestedInstanceWhazzup() {
        // Arrange
        String expectedValue = "5432:whazzup";

        NetworkInformation testedInstance = new NetworkInformation();
        testedInstance.setWhazzUpString(expectedValue);

        NetworkInformation otherInstance = new NetworkInformation();

        testedInstance.addAll(otherInstance);

        // Act
        String result = testedInstance.getWhazzUpString();

        // Assert
        assertThat(result, is(equalTo(expectedValue)));
    }

    @Test
    public void testGetWhazzUpString_afterAddAllAndNeverSet_returnsNull() {
        // Arrange
        NetworkInformation testedInstance = new NetworkInformation();
        NetworkInformation otherInstance = new NetworkInformation();
        testedInstance.addAll(otherInstance);

        // Act
        String result = testedInstance.getWhazzUpString();

        // Assert
        assertThat(result, is(nullValue()));
    }

    private List<String> asStrings(Collection<?> items) {
        return items //
            .stream() //
            .map(Object::toString) //
            .collect(Collectors.toList());
    }
}
