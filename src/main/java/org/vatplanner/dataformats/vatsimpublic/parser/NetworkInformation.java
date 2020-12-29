package org.vatplanner.dataformats.vatsimpublic.parser;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds dynamic meta information about the VATSIM network, as parsed from
 * <code>status.txt</code> and <code>status.json</code> files. Some information
 * may only be provided by a specific file (such as legacy data file URLs only
 * being available from legacy <code>status.txt</code>). Information from both
 * sources can be combined using {@link #addAll(NetworkInformation)}.
 * 
 * <p>
 * The original policy stated on the file itself was to fetch that information
 * only once "on application start" to reduce server load. For current policy
 * check the original files regularly.
 * </p>
 * 
 * <p>
 * All URLs should be accessed in a random, round-robin fashion. Documentation
 * of getter methods is based on self-documentation of the original unparsed
 * file and may become out-dated at some point.
 * </p>
 */
public class NetworkInformation {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkInformation.class.getName());

    private String whazzUpString = null;
    private final List<String> startupMessages = new ArrayList<>();
    private final Map<String, List<URL>> urlsByParameter = new HashMap<>();
    private final Map<String, List<URL>> dataFileUrlsByJsonFormatKey = new HashMap<>();

    public static final String PARAMETER_KEY_MESSAGE_STARTUP = "msg0";
    public static final String PARAMETER_KEY_URL_SERVERS_FILE = "url1";
    public static final String PARAMETER_KEY_URL_MOVED = "moveto0";
    public static final String PARAMETER_KEY_URL_METAR = "metar0";
    public static final String PARAMETER_KEY_URL_ATIS = "atis0";
    public static final String PARAMETER_KEY_URL_USER_STATISTICS = "user0";

    /**
     * Returns a list of all URLs for the given key. URLs will be returned in order
     * of their insertion.
     *
     * @param key key to retrieve URLs for
     * @return URLs in order
     */
    List<URL> getUrlsByKey(final String key) {
        List<URL> urls = urlsByParameter.get(key);

        if (urls == null) {
            urls = new ArrayList<>();
        }

        return Collections.unmodifiableList(urls);
    }

    /**
     * Parses and remembers the given URL string for the given key. URLs will retain
     * their order of insertion.
     *
     * @param key key to identify list of URLs by
     * @param value URL string to parse
     * @return Could the URL be parsed and has it been registered to the given key?
     */
    public boolean addAsUrl(String key, String value) {
        return addAsUrl(urlsByParameter, key, value);
    }

    private boolean addAsUrl(Map<String, List<URL>> target, String key, String value) {
        URL url = toUrl(value, key);

        if (url == null) {
            return false;
        }

        List<URL> urls = target.computeIfAbsent(key, x -> new ArrayList<URL>());
        urls.add(url);

        return true;
    }

    private URL toUrl(String value, String logSource) {
        try {
            return new URL(value);
        } catch (MalformedURLException ex) {
            LOGGER.warn("URL for \"{}\" is malformed: \"{}\"", new Object[] { logSource, value }, ex);
        }

        return null;
    }

    /**
     * Parses and remembers the given data file URL string for the given key. The
     * key should be exactly the one used in JSON {@link NetworkInformation} files.
     * URLs will retain their order of insertion.
     * 
     * <p>
     * When parsing legacy files
     * {@link DataFileFormat#getJsonNetworkInformationKey()} should be used.
     * </p>
     * 
     * @param jsonDataFileKey key used to indicate the referenced data file format
     *        in JSON-based {@link NetworkInformation} files
     * @param value URL string to parse
     * @return Could the URL be parsed and has it been registered to the given
     *         {@link DataFileFormat}?
     */
    public boolean addAsDataFileUrl(String jsonDataFileKey, String value) {
        return addAsUrl(dataFileUrlsByJsonFormatKey, jsonDataFileKey, value);
    }

    /**
     * Returns all startup messages.
     *
     * @return startup messages messages to be shown at application start
     */
    public List<String> getStartupMessages() {
        return Collections.unmodifiableList(startupMessages);
    }

    /**
     * Adds a single startup message.
     *
     * @param message message to show on application startup
     */
    public void addStartupMessage(String message) {
        synchronized (startupMessages) {
            startupMessages.add(message);
        }
    }

    /**
     * Returns the WhazzUp string.
     *
     * @return WhazzUp string, null if unavailable
     */
    public String getWhazzUpString() {
        return whazzUpString;
    }

    /**
     * Sets the WhazzUp string.
     *
     * @param whazzUpString WhazzUp string
     */
    public void setWhazzUpString(String whazzUpString) {
        this.whazzUpString = whazzUpString;
    }

    /**
     * Returns all ATIS URLs. Calling these with parameter
     * <code>?callsign=...</code> is supposed to return only ATIS information for
     * the given station. Use, however, is discouraged if retrieving the data file
     * is a more reasonable option. While only one URL should be returned according
     * to documentation from the file header, be prepared to choose one randomly if
     * multiple appear.
     *
     * @return URLs to retrieve ATIS information from by adding
     *         <code>?callsign=...</code>
     * @deprecated as of October 2018 network information file header states service
     *             has been discontinued, controller info and ATIS should be read
     *             from data file instead
     */
    public List<URL> getAtisUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_ATIS);
    }

    /**
     * Returns all legacy data file URLs. The data file contains information about
     * online stations, pilots and pre-filings.
     * <p>
     * Depending on the source of {@link NetworkInformation} legacy format may be
     * available. Furthermore, legacy format is pending service termination, so
     * expect it to disappear soon.
     * </p>
     *
     * @return URLs to retrieve a copy of the current data file from
     * @deprecated use {@link #getDataFileUrls(DataFileFormat)} or
     *             {@link #getAllDataFileUrls()} instead
     */
    @Deprecated
    public List<URL> getDataFileUrls() {
        return getDataFileUrls(DataFileFormat.LEGACY);
    }

    /**
     * Returns all data file URLs for the given format. The data file contains
     * information about online stations, pilots and pre-filings.
     * <p>
     * Depending on the source of {@link NetworkInformation} not all formats may be
     * available.
     * </p>
     *
     * @param format wanted data file format
     * @return URLs to retrieve a copy of the current data file from
     */
    public List<URL> getDataFileUrls(DataFileFormat format) {
        return unmodifiableList(
            dataFileUrlsByJsonFormatKey.getOrDefault(
                format.getJsonNetworkInformationKey(),
                emptyList() //
            ) //
        );
    }

    /**
     * Returns all data file URLs indexed by the key by which they are referenced in
     * JSON-based {@link NetworkInformation} files.
     * 
     * <p>
     * Legacy data files are not referenced in JSON-based {@link NetworkInformation}
     * and are referred to by {@link DataFileFormat.Constants#LEGACY_JSON_KEY}
     * instead.
     * </p>
     * 
     * @return all data file URLs indexed by the key used in JSON-based
     *         {@link NetworkInformation} files
     */
    public Map<String, List<URL>> getAllDataFileUrls() {
        return unmodifiableMap(dataFileUrlsByJsonFormatKey);
    }

    /**
     * Returns all METAR URLs. Calling these with parameter <code>?id=...</code>
     * should return VATSIM METAR information (which may be different from
     * real-world METAR). While only one URL should be returned according to
     * documentation from the file header, be prepared to choose one randomly if
     * multiple appear.
     *
     * @return URLs to retrieve METAR information from by adding
     *         <code>?id=...</code>
     */
    public List<URL> getMetarUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_METAR);
    }

    /**
     * Returns all superseding URLs. If any such URLs are set they should be treated
     * like a redirect as they point to the location of a more recent status.txt
     * file (the raw file of this information). While only one URL should be
     * returned according to documentation from the file header, be prepared to
     * choose one randomly if multiple appear.
     *
     * @return URLs to follow for a more recent version of this information
     */
    public List<URL> getMovedToUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_MOVED);
    }

    /**
     * Returns all server file URLs. The server file contains connection and meta
     * information about VATSIM protocol servers.
     *
     * @return URLS to retrieve a copy of the current server file from
     */
    public List<URL> getServersFileUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_SERVERS_FILE);
    }

    /**
     * Returns all user statistics URLs. Lists URLs where official user statistics
     * can be retrieved from. While only one URL should be returned according to
     * documentation from the file header, be prepared to choose one randomly if
     * multiple appear.
     *
     * @return URLs to retrieve official user statistics from
     */
    public List<URL> getUserStatisticsUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_USER_STATISTICS);
    }

    /**
     * Copies all data from another {@link NetworkInformation} instance to this one,
     * resulting in a combination of data from both instances. Data will be
     * deduplicated, no order of items can be guaranteed.
     * 
     * <p>
     * {@link #getWhazzUpString()} will still retain current instance's value if
     * already set, otherwise it will get copied from the other instance.
     * </p>
     * 
     * @param other other instance to copy information from
     * @return this instance for method-chaining
     */
    public NetworkInformation addAll(NetworkInformation other) {
        merge(this.dataFileUrlsByJsonFormatKey, other.dataFileUrlsByJsonFormatKey);
        merge(this.urlsByParameter, other.urlsByParameter);
        merge(this.startupMessages, other.startupMessages);

        if (this.whazzUpString == null) {
            this.whazzUpString = other.whazzUpString;
        }

        return this;
    }

    private void merge(Map<String, List<URL>> target, Map<String, List<URL>> source) {
        for (Map.Entry<String, List<URL>> entry : source.entrySet()) {
            String key = entry.getKey();
            for (URL url : entry.getValue()) {
                List<URL> targetUrls = target.computeIfAbsent(key, x -> new ArrayList<>());
                if (!targetUrls.contains(url)) {
                    targetUrls.add(url);
                }
            }
        }
    }

    private <T> void merge(Collection<T> target, Collection<T> source) {
        for (T item : source) {
            if (!target.contains(item)) {
                target.add(item);
            }
        }
    }
}
