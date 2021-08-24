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
 * Note that there is a difference between <code>data</code> and parameter URLs:
 * Historically, information was only keyed on root-level which this
 * implementation internally calls "parameters" (e.g. <code>url0</code> or
 * <code>metar0</code>) which can be accessed through dedicated getters. With
 * the introduction of JSON-based formats a root-level "parameter" key
 * <code>data</code> was added which first only held {@link DataFile} URLs
 * indexed by a format identifier. However, its usage got expanded to also cover
 * the {@link OnlineTransceiversFile} which is not a {@link DataFile}. The
 * result is that whatever is sub-keyed on the <code>data</code> root-level
 * field is now tracked as "data" by this class to make sense of that
 * restructuring.
 * </p>
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
    private final Map<String, List<URL>> urlsByParameterKey = new HashMap<>();
    private final Map<String, List<URL>> urlsByDataKey = new HashMap<>();

    public static final String PARAMETER_KEY_MESSAGE_STARTUP = "msg0";
    public static final String PARAMETER_KEY_URL_SERVERS_FILE = "url1";
    public static final String PARAMETER_KEY_URL_MOVED = "moveto0";
    public static final String PARAMETER_KEY_URL_METAR = "metar0";
    public static final String PARAMETER_KEY_URL_ATIS = "atis0";
    public static final String PARAMETER_KEY_URL_USER_STATISTICS = "user0";

    /**
     * Returns a list of all URLs for the given parameter key. URLs will be returned
     * in order of their insertion.
     *
     * @param key key to retrieve URLs for
     * @return URLs in order
     */
    List<URL> getParameterUrls(final String key) {
        List<URL> urls = urlsByParameterKey.get(key);

        if (urls == null) {
            urls = new ArrayList<>();
        }

        return Collections.unmodifiableList(urls);
    }

    /**
     * Parses and remembers the given URL string for the given parameter key. URLs
     * will retain their order of insertion.
     *
     * @param key key to identify list of URLs by
     * @param value URL string to parse
     * @return Could the URL be parsed and has it been registered to the given key?
     */
    public boolean addAsUrl(String key, String value) {
        return addAsUrl(urlsByParameterKey, key, value);
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
     * Parses and remembers the given URL string for the given key. The key should
     * be exactly the one used on <code>data</code> field of JSON
     * {@link NetworkInformation} files for JSON information. URLs will retain their
     * order of insertion.
     * 
     * <p>
     * {@link NetworkInformationDataKeyProvider#getNetworkInformationDataKey()}
     * should be used to retrieve the matching key constants. The pseudo-key
     * returned by {@link DataFileFormat#LEGACY} is the only allowed key not present
     * on actual JSON {@link NetworkInformation} files.
     * </p>
     * 
     * @param jsonKey key used to indicate the referenced file/format in JSON-based
     *        {@link NetworkInformation} files
     * @param value URL string to parse
     * @return Could the URL be parsed and has it been registered to the given key?
     */
    public boolean addAsDataUrl(String jsonKey, String value) {
        return addAsUrl(urlsByDataKey, jsonKey, value);
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
        return getParameterUrls(PARAMETER_KEY_URL_ATIS);
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
     * @deprecated use {@link #getDataUrls(NetworkInformationDataKeyProvider)} or
     *             {@link #getAllUrlsByDataKey()} instead
     */
    @Deprecated
    public List<URL> getDataFileUrls() {
        return getDataUrls(DataFileFormat.LEGACY);
    }

    /**
     * Returns all <code>data</code> URLs matching the given key provider. Other
     * data may be available through dedicated getters.
     * <p>
     * Depending on the source of {@link NetworkInformation} not all possible data
     * may be available.
     * </p>
     *
     * @param keyProvider returns the key of wanted data
     * @return URLs to retrieve a copy of the current data resource from
     */
    public List<URL> getDataUrls(NetworkInformationDataKeyProvider keyProvider) {
        return unmodifiableList(
            urlsByDataKey.getOrDefault(
                keyProvider.getNetworkInformationDataKey(),
                emptyList() //
            ) //
        );
    }

    /**
     * Returns all data URLs (i.e. URLs from the <code>data</code> field) indexed by
     * the key by which they are referenced in JSON-based {@link NetworkInformation}
     * files.
     * 
     * <p>
     * Legacy data files are not referenced in JSON-based {@link NetworkInformation}
     * and are referred to by {@link DataFileFormat.Constants#LEGACY_JSON_KEY}
     * instead.
     * </p>
     * 
     * @return all data URLs indexed by the key used in JSON-based
     *         {@link NetworkInformation} files
     */
    public Map<String, List<URL>> getAllUrlsByDataKey() {
        return unmodifiableMap(urlsByDataKey);
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
        return getParameterUrls(PARAMETER_KEY_URL_METAR);
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
        return getParameterUrls(PARAMETER_KEY_URL_MOVED);
    }

    /**
     * Returns all server file URLs. The server file contains connection and meta
     * information about VATSIM protocol servers.
     *
     * @return URLS to retrieve a copy of the current server file from
     */
    public List<URL> getServersFileUrls() {
        return getParameterUrls(PARAMETER_KEY_URL_SERVERS_FILE);
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
        return getParameterUrls(PARAMETER_KEY_URL_USER_STATISTICS);
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
        merge(this.urlsByDataKey, other.urlsByDataKey);
        merge(this.urlsByParameterKey, other.urlsByParameterKey);
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
