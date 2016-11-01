package de.energiequant.vatplanner.dataformats.vatsimpublic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds dynamic meta information about the VATSIM network, as parsed from
 * status.txt.
 * The policy is to fetch that information only once "on application start" to
 * reduce server load.
 * All URLs should be accessed in a random, round-robin fashion.
 * Documentation of getter methods is based on self-documentation of the
 * original unparsed file and may become out-dated at some point. Remember to
 * occasionally check the original file for any important changes.
 */
public class NetworkInformation {
    private static final Logger logger = LoggerFactory.getLogger(NetworkInformation.class.getName());
    
    private String whazzUpString = null;
    private final List<String> startupMessages = new ArrayList<>();
    Map<String, List<URL>> urlsByParameter = new HashMap<>();
    
    static final String PARAMETER_KEY_MESSAGE_STARTUP = "msg0";
    static final String PARAMETER_KEY_URL_DATA_FILE = "url0";
    static final String PARAMETER_KEY_URL_SERVERS_FILE = "url1";
    static final String PARAMETER_KEY_URL_MOVED = "moveto0";
    static final String PARAMETER_KEY_URL_METAR = "metar0";
    static final String PARAMETER_KEY_URL_ATIS = "atis0";
    static final String PARAMETER_KEY_URL_USER_STATISTICS = "user0";
    
    /**
     * Returns a list of all URLs for the given key.
     * URLs will be returned in order of their insertion.
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
     * Parses and remembers the given URL string for the given key.
     * URLs will retain their order of insertion.
     * @param key key to identify list of URLs by
     * @param value URL string to parse
     * @return Could the URL be parsed and has it been registered to the given key?
     */
    boolean addAsUrl(final String key, final String value) {
        URL url = null;
        try {
            url = new URL(value);
        } catch (MalformedURLException ex) {
            logger.warn("URL for key \"{}\" is malformed: \"{}\"", new Object[]{ key, value }, ex);
        }

        if (url == null) {
            return false;
        }

        List<URL> urls = urlsByParameter.get(key);

        if (urls == null) {
            urls = new ArrayList<>();
            urlsByParameter.put(key, urls);
        }

        urls.add(url);
        
        return true;
    }

    /**
     * Returns all startup messages.
     * @return startup messages messages to be shown at application start
     */
    public List<String> getStartupMessages() {
        return Collections.unmodifiableList(startupMessages);
    }

    /**
     * Adds a single startup message.
     * @param message message to show on application startup
     */
    public void addStartupMessage(String message) {
        synchronized (startupMessages) {
            startupMessages.add(message);
        }
    }
    
    /**
     * Returns the WhazzUp string.
     * @return WhazzUp string
     */
    public String getWhazzUpString() {
        return whazzUpString;
    }

    /**
     * Sets the WhazzUp string.
     * @param whazzUpString WhazzUp string
     */
    public void setWhazzUpString(String whazzUpString) {
        this.whazzUpString = whazzUpString;
    }
    
    /**
     * Returns all ATIS URLs.
     * Calling these with parameter <code>?callsign=...</code> is supposed to
     * return only ATIS information for the given station. Use, however, is
     * discouraged if retrieving the data file is a more reasonable option.
     * While only one URL should be returned according to documentation from the
     * file header, be prepared to choose one randomly if multiple appear.
     * @return URLs to retrieve ATIS information from by adding <code>?callsign=...</code>
     */
    public List<URL> getAtisUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_ATIS);
    }
    
    /**
     * Returns all data file URLs.
     * The data file contains information about online stations, pilots and
     * pre-filings.
     * @return URLs to retrieve a copy of the current data file from
     */
    public List<URL> getDataFileUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_DATA_FILE);
    }
    
    /**
     * Returns all METAR URLs.
     * Calling these with parameter <code>?id=...</code> should return VATSIM
     * METAR information (which may be different from real-world METAR).
     * While only one URL should be returned according to documentation from the
     * file header, be prepared to choose one randomly if multiple appear.
     * @return URLs to retrieve ATIS information from by adding <code>?callsign=...</code>
     */
    public List<URL> getMetarUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_METAR);
    }
    
    /**
     * Returns all superseding URLs.
     * If any such URLs are set they should be treated like a redirect as they
     * point to the location of a more recent status.txt file (the raw file of
     * this information).
     * While only one URL should be returned according to documentation from the
     * file header, be prepared to choose one randomly if multiple appear.
     * @return URLs to follow for a more recent version of this information
     */
    public List<URL> getMovedToUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_MOVED);
    }
    
    /**
     * Returns all server file URLs.
     * The server file contains connection and meta information about VATSIM
     * protocol servers.
     * @return URLS to retrieve a copy of the current server file from
     */
    public List<URL> getServersFileUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_SERVERS_FILE);
    }
    
    /**
     * Returns all user statistics URLs.
     * Lists URLs where official user statistics can be retrieved from.
     * While only one URL should be returned according to documentation from the
     * file header, be prepared to choose one randomly if multiple appear.
     * @return 
     */
    public List<URL> getUserStatisticsUrls() {
        return getUrlsByKey(PARAMETER_KEY_URL_USER_STATISTICS);
    }
}
