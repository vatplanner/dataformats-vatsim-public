package de.energiequant.vatplanner.dataformats.vatsimpublic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds dynamic meta information about the VATSIM network.
 * Parses status.txt which mainly defines reference URLs to fetch other
 * information from.
 * The policy is to fetch that information only once "on application start" to
 * reduce server load.
 */
public class NetworkInformation {
    private static final Logger logger = LoggerFactory.getLogger(NetworkInformation.class.getName());
    
    private String whazzUpString = null;
    private List<String> startupMessages = new ArrayList<>();
    protected Map<String, List<URL>> urlsByParameter = new HashMap<>();
    
    public static final String PARAMETER_KEY_MESSAGE_STARTUP = "msg0";
    public static final String PARAMETER_KEY_URL_DATA_FILE = "url0";
    public static final String PARAMETER_KEY_URL_SERVERS_FILE = "url1";
    public static final String PARAMETER_KEY_URL_MOVED = "moveto0";
    public static final String PARAMETER_KEY_URL_METAR = "metar0";
    public static final String PARAMETER_KEY_URL_ATIS = "atis0";
    public static final String PARAMETER_KEY_URL_USER_STATISTICS = "user0";
    
    public void addAsUrl(final String key, final String value) {
        URL url = null;
        try {
            url = new URL(value);
        } catch (MalformedURLException ex) {
            logger.warn("URL for key \"{}\" is malformed: \"{}\"", new Object[]{ key, value }, ex);
        }

        if (url == null) {
            return;
        }

        List<URL> urls = urlsByParameter.get(key);

        if (urls == null) {
            urls = new ArrayList<>();
            urlsByParameter.put(key, urls);
        }

        urls.add(url);
    }

    public List<String> getStartupMessages() {
        return Collections.unmodifiableList(startupMessages);
    }

    public void addStartupMessage(String message) {
        synchronized (startupMessages) {
            startupMessages.add(message);
        }
    }
    
    public String getWhazzUpString() {
        return whazzUpString;
    }

    public void setWhazzUpString(String whazzUpString) {
        this.whazzUpString = whazzUpString;
    }
}
