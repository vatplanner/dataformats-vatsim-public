package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.junit.ExpectedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

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

        List<String> startupMessages = info.getStartupMessages();
        thrown.expect(UnsupportedOperationException.class);

        // Act
        startupMessages.add("test");

        // Assert (nothing to do)
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
        assertThat(actualEvent.getMessage(), is("URL for key \"{}\" is malformed: \"{}\""));
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
    public void testGetUrlsByKey_undefinedKey_returnsEmptyList() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> res = info.getUrlsByKey("surely-undefined");

        // Assert
        assertThat(res, is(empty()));
    }

    @Test
    public void testGetUrlsByKey_knownKeyWithoutData_returnsEmptyList() {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE);

        // Assert
        assertThat(res, is(empty()));
    }

    @Test
    public void testGetUrlsByKey_knownKeyWithData_returnsSameDataInOrder() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://a.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://b.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://c.com/");

        // Act
        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE);

        // Assert
        assertThat(res.size(), is(3));
        assertThat(res.get(0), is(equalTo(new URL("http://a.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://b.com/"))));
        assertThat(res.get(2), is(equalTo(new URL("http://c.com/"))));
    }

    @Test
    public void testGetUrlsByKey_undefinedKey_listIsUnmodifiable() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();

        // Act
        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE);

        // Assert
        thrown.expect(UnsupportedOperationException.class);

        res.add(new URL("http://test.de/"));
    }

    @Test
    public void testGetUrlsByKey_knownKeyWithData_listIsUnmodifiable() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://a.com/");

        // Act
        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE);

        // Assert
        thrown.expect(UnsupportedOperationException.class);

        res.add(new URL("http://test.de/"));
    }

    @Test
    public void testGetUrlsByKey_mixedKeys_listContainsOnlyAssignedDataInOrder() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://a.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://b.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, "http://c.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://d.com/");

        // Act
        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(res.size(), is(2));
        assertThat(res.get(0), is(equalTo(new URL("http://b.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://d.com/"))));
    }

    @Test
    public void testGetUrlsByKey_duplicateURLs_areRetained() throws MalformedURLException {
        // Arrange
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://1.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://1.com/");

        // Act
        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        // Assert
        assertThat(res.size(), is(2));
        assertThat(res.get(0), is(equalTo(new URL("http://1.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://1.com/"))));
    }

    @Test
    public void testGetAtisUrls_always_proxiesGetURLsByKey() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_ATIS)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getAtisUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetDataFileUrls_always_proxiesGetURLsByKey() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getDataFileUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetMetarUrls_always_proxiesGetURLsByKey() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_METAR)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getMetarUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetMovedToUrls_always_proxiesGetURLsByKey() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_MOVED)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getMovedToUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetServersFileUrls_always_proxiesGetURLsByKey() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getServersFileUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetUserStatisticsUrls_always_proxiesGetURLsByKey() {
        // Arrange
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS)).thenReturn(expectedList);

        // Act
        List<URL> res = info.getUserStatisticsUrls();

        // Assert
        assertThat(res, is(sameInstance(expectedList)));
    }
}
