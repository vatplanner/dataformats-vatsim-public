package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileFormat;
import org.vatplanner.dataformats.vatsimpublic.parser.NetworkInformation;

/**
 * Parses legacy status.txt which defines meta information such as reference
 * URLs to fetch other information from. The policy was to fetch that
 * information only once "on application start" to reduce server load; for
 * current policy see the original file.
 */
public class NetworkInformationParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkInformationParser.class.getName());

    // we don't actually know what that string is trying to tell us but we
    // should exclude it from warnings and, if possible, provide it to users
    // anyway
    private static final Pattern PATTERN_WHAZZ_UP = Pattern.compile("^\\d+:[a-z0-9]+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_WHAZZ_UP_DEFINITION = Pattern.compile("^; (\\S+)\\s+- used by WhazzUp only.*");
    private static final int PATTERN_WHAZZ_UP_DEFINITION_EXAMPLE = 1;

    // status file contains comments explaning what parameters are used for
    // we check those to verify we are interpreting the file correctly
    private static final Pattern PATTERN_DEFINITION_SPLIT = Pattern.compile("^;\\s*([^\\s\\-]+)\\s+-\\s+(.+)");
    private static final int PATTERN_DEFINITION_SPLIT_KEY = 1;
    private static final int PATTERN_DEFINITION_SPLIT_VALUE = 2;

    private static final Pattern PATTERN_PARAMETER_SPLIT = Pattern.compile("^((?!;)[^=\\s]+)=(.*)$");
    private static final int PATTERN_PARAMETER_SPLIT_KEY = 1;
    private static final int PATTERN_PARAMETER_SPLIT_VALUE = 2;

    private static final Pattern PATTERN_COMMENT_OR_SPACE = Pattern.compile("^(;.*|\\s*)$");

    private static final String PARAMETER_KEY_URL_DATA_FILE_LEGACY = "url0";
    private static final String PARAMETER_KEY_URL_DATA_FILE_JSON_1 = "json0";
    private static final String PARAMETER_KEY_URL_DATA_FILE_JSON_3 = "json3";

    private static final Set<String> IGNORED_KEYS = new HashSet<String>(asList(
        "servers.live",
        "voice0"
    ));

    private static final Map<String, Pattern> expectedDefinitionPatternsByParameterKey = toStringPatternMap(
        new Object[][]{
            {NetworkInformation.PARAMETER_KEY_MESSAGE_STARTUP, Pattern.compile(".*application startup.*")},
            {PARAMETER_KEY_URL_DATA_FILE_LEGACY, Pattern.compile(".*complete data.*")},
            {PARAMETER_KEY_URL_DATA_FILE_JSON_1, Pattern.compile(".*JSON data file.*")},
            {PARAMETER_KEY_URL_DATA_FILE_JSON_3, Pattern.compile(".*JSON.* 3.*")},
            {NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE, Pattern.compile(".*servers list.*")},
            {NetworkInformation.PARAMETER_KEY_URL_MOVED, Pattern.compile(".*more updated status.*")},
            {NetworkInformation.PARAMETER_KEY_URL_METAR, Pattern.compile(".*passing a parameter.*\\?id=.*")},
            {NetworkInformation.PARAMETER_KEY_URL_ATIS, Pattern.compile(".*no longer available.*")},
            {NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS, Pattern.compile(".*statistics.*page.*")}
        });

    private static Map<String, Pattern> toStringPatternMap(Object[][] pairs) {
        HashMap<String, Pattern> map = new HashMap<>();

        for (Object[] pair : pairs) {
            map.put((String) pair[0], (Pattern) pair[1]);
        }

        return map;
    }

    public static NetworkInformation parse(CharSequence s) {
        BufferedReader br = new BufferedReader(new StringReader(s.toString()));

        return parse(br);
    }

    public static NetworkInformation parse(BufferedReader br) {
        NetworkInformation info = new NetworkInformation();
        Boolean isFirstContentLine = null;

        String whazzUpString = null;

        try {
            String line;
            while ((line = br.readLine()) != null) {
                // check for WhazzUp definition comment
                if (whazzUpString == null) {
                    Matcher matcherWhazzUpDefinition = PATTERN_WHAZZ_UP_DEFINITION.matcher(line);
                    if (matcherWhazzUpDefinition.matches()) {
                        String whazzUpExample = matcherWhazzUpDefinition.group(PATTERN_WHAZZ_UP_DEFINITION_EXAMPLE);

                        // check expected format
                        if (!PATTERN_WHAZZ_UP.matcher(whazzUpExample).matches()) {
                            LOGGER.warn("WhazzUp format may have changed, header definition: \"{}\"", whazzUpExample);
                        }

                        // line is finished, stop parsing
                        continue;
                    }
                }

                // check if definition comment lines match expected use
                Matcher matcherDefinitionSplit = PATTERN_DEFINITION_SPLIT.matcher(line);
                if (matcherDefinitionSplit.matches()) {
                    String key = matcherDefinitionSplit.group(PATTERN_DEFINITION_SPLIT_KEY);
                    String description = matcherDefinitionSplit.group(PATTERN_DEFINITION_SPLIT_VALUE);

                    Pattern expectedDefinitionPattern = expectedDefinitionPatternsByParameterKey.get(key);
                    if (expectedDefinitionPattern == null) {
                        LOGGER.info("Definition comment found for unknown key \"{}\": \"{}\"", key, description);
                    } else if (!expectedDefinitionPattern.matcher(description).matches()) {
                        LOGGER.warn("Mismatch in definition comment for key \"{}\": \"{}\"", key, description);
                    }

                    // line is finished, stop parsing
                    continue;
                }

                // ignore lines which are unmatched comments or only white-space
                if (PATTERN_COMMENT_OR_SPACE.matcher(line).matches()) {
                    continue;
                }

                // Is this the first line that isn't comment or white-space?
                isFirstContentLine = (isFirstContentLine == null) ? true : false;

                // record WhazzUp string if first non-empty, non-comment line
                // in file and format matches
                if (isFirstContentLine && (whazzUpString == null) && PATTERN_WHAZZ_UP.matcher(line).matches()) {
                    whazzUpString = line;
                    info.setWhazzUpString(whazzUpString);

                    continue;
                }

                // record parameter assignment if known
                Matcher matcherParameterSplit = PATTERN_PARAMETER_SPLIT.matcher(line);
                if (matcherParameterSplit.matches()) {
                    String key = matcherParameterSplit.group(PATTERN_PARAMETER_SPLIT_KEY);
                    String value = matcherParameterSplit.group(PATTERN_PARAMETER_SPLIT_VALUE);

                    switch (key) {
                        case NetworkInformation.PARAMETER_KEY_MESSAGE_STARTUP:
                            info.addStartupMessage(value);
                            break;

                        case NetworkInformation.PARAMETER_KEY_URL_ATIS:
                        case NetworkInformation.PARAMETER_KEY_URL_METAR:
                        case NetworkInformation.PARAMETER_KEY_URL_MOVED:
                        case NetworkInformation.PARAMETER_KEY_URL_SERVERS_FILE:
                        case NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS:
                            info.addAsUrl(key, value);
                            break;

                        case PARAMETER_KEY_URL_DATA_FILE_LEGACY:
                            info.addAsDataUrl(DataFileFormat.LEGACY.getNetworkInformationDataKey(), value);
                            break;

                        case PARAMETER_KEY_URL_DATA_FILE_JSON_1:
                            // to be removed in early 2021, JSON v3 should be used instead, so just ignore
                            // and do not process these old v1 entries
                            break;

                        case PARAMETER_KEY_URL_DATA_FILE_JSON_3:
                            info.addAsDataUrl(DataFileFormat.JSON3.getNetworkInformationDataKey(), value);
                            break;

                        default:
                            if (!IGNORED_KEYS.contains(key)) {
                                LOGGER.warn("Unrecognized key \"{}\", value \"{}\"", key, value);
                            }
                    }

                    // line is finished, stop parsing
                    continue;
                }

                // report all other unmatched lines
                LOGGER.warn("Uninterpretable line in network file: \"{}\"", line);
            }
        } catch (IOException ex) {
            LOGGER.warn("Caught IOException while reading lines", ex);
        }

        return info;
    }
}
