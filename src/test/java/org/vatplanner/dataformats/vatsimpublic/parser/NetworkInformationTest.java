package org.vatplanner.dataformats.vatsimpublic.parser;

import org.vatplanner.dataformats.vatsimpublic.parser.NetworkInformation;
import com.google.common.collect.ImmutableList;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.*;
import org.hamcrest.junit.ExpectedException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.mockito.Mockito.*;
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
        NetworkInformation info = new NetworkInformation();

        List<String> startupMessages = info.getStartupMessages();
        thrown.expect(UnsupportedOperationException.class);
        startupMessages.add("test");
    }

    @Test
    public void testGetMessagesStartup_initially_returnsEmptyCollection() {
        NetworkInformation info = new NetworkInformation();

        List<String> startupMessages = info.getStartupMessages();
        assertThat(startupMessages, is(empty()));
    }

    @Test
    public void testGetMessagesStartup_initially_returnsList() {
        NetworkInformation info = new NetworkInformation();

        List<String> startupMessages = info.getStartupMessages();
        assertThat(startupMessages, is(instanceOf(List.class)));
    }

    @Test
    public void testAddAsUrl_malformedUrl_logsWarning() {
        NetworkInformation info = new NetworkInformation();

        info.addAsUrl("test", "this-is-not-a-url");

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
        NetworkInformation info = new NetworkInformation();

        boolean success = info.addAsUrl("test", "this-is-not-a-url");

        assertThat(success, is(false));
    }

    @Test
    public void testAddAsUrl_validUniqueUrl_doesNotLog() {
        NetworkInformation info = new NetworkInformation();

        info.addAsUrl("test", "http://www.test.com/test?test=123&test");

        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, is(emptyIterable()));
    }

    @Test
    public void testAddAsUrl_validUniqueUrl_returnsTrue() {
        NetworkInformation info = new NetworkInformation();

        boolean success = info.addAsUrl("test", "http://www.test.com/test?test=123&test");

        assertThat(success, is(true));
    }

    @Test
    public void testGetUrlsByKey_undefinedKey_returnsEmptyList() {
        NetworkInformation info = new NetworkInformation();

        List<URL> res = info.getUrlsByKey("surely-undefined");

        assertThat(res, is(empty()));
    }

    @Test
    public void testGetUrlsByKey_knownKeyWithoutData_returnsEmptyList() {
        NetworkInformation info = new NetworkInformation();

        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE);

        assertThat(res, is(empty()));
    }

    @Test
    public void testGetUrlsByKey_knownKeyWithData_returnsSameDataInOrder() throws MalformedURLException {
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://a.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://b.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://c.com/");

        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE);

        assertThat(res.size(), is(3));
        assertThat(res.get(0), is(equalTo(new URL("http://a.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://b.com/"))));
        assertThat(res.get(2), is(equalTo(new URL("http://c.com/"))));
    }

    @Test
    public void testGetUrlsByKey_undefinedKey_listIsUnmodifiable() throws MalformedURLException {
        NetworkInformation info = new NetworkInformation();
        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE);
        thrown.expect(UnsupportedOperationException.class);

        res.add(new URL("http://test.de/"));
    }

    @Test
    public void testGetUrlsByKey_knownKeyWithData_listIsUnmodifiable() throws MalformedURLException {
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://a.com/");
        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE);
        thrown.expect(UnsupportedOperationException.class);

        res.add(new URL("http://test.de/"));
    }

    @Test
    public void testGetUrlsByKey_mixedKeys_listContainsOnlyAssignedDataInOrder() throws MalformedURLException {
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE, "http://a.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://b.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_MOVED, "http://c.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://d.com/");

        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        assertThat(res.size(), is(2));
        assertThat(res.get(0), is(equalTo(new URL("http://b.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://d.com/"))));
    }

    @Test
    public void testGetUrlsByKey_duplicateURLs_areRetained() throws MalformedURLException {
        NetworkInformation info = new NetworkInformation();
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://1.com/");
        info.addAsUrl(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, "http://1.com/");

        List<URL> res = info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE);

        assertThat(res.size(), is(2));
        assertThat(res.get(0), is(equalTo(new URL("http://1.com/"))));
        assertThat(res.get(1), is(equalTo(new URL("http://1.com/"))));
    }

    @Test
    public void testGetAtisUrls_proxiesGetURLsByKey() {
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_ATIS)).thenReturn(expectedList);

        List<URL> res = info.getAtisUrls();

        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetDataFileUrls_proxiesGetURLsByKey() {
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_DATA_FILE)).thenReturn(expectedList);

        List<URL> res = info.getDataFileUrls();

        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetMetarUrls_proxiesGetURLsByKey() {
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_METAR)).thenReturn(expectedList);

        List<URL> res = info.getMetarUrls();

        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetMovedToUrls_proxiesGetURLsByKey() {
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_MOVED)).thenReturn(expectedList);

        List<URL> res = info.getMovedToUrls();

        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetServersFileUrls_proxiesGetURLsByKey() {
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE)).thenReturn(expectedList);

        List<URL> res = info.getServersFileUrls();

        assertThat(res, is(sameInstance(expectedList)));
    }

    @Test
    public void testGetUserStatisticsUrls_proxiesGetURLsByKey() {
        List<URL> expectedList = new ArrayList<>();
        NetworkInformation info = spy(new NetworkInformation());
        when(info.getUrlsByKey(NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS)).thenReturn(expectedList);

        List<URL> res = info.getUserStatisticsUrls();

        assertThat(res, is(sameInstance(expectedList)));
    }
}
