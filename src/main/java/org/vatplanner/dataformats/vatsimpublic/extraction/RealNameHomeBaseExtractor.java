package org.vatplanner.dataformats.vatsimpublic.extraction;

import static org.vatplanner.dataformats.vatsimpublic.utils.StringUtils.nullIfEmpty;
import static org.vatplanner.dataformats.vatsimpublic.utils.StringUtils.nullIfNumeric;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attempts to split the raw name field used in data files into actual name and
 * "home base" airport code. Code of Conduct also allows to log in with just the
 * VATSIM ID instead of a name. Purely numeric names are therefore removed as
 * they are either just redundant or bogus.
 */
public class RealNameHomeBaseExtractor {

    private static final Pattern PATTERN_SPLIT = Pattern.compile("^\\s*(.*?\\S)(\\s+[A-Z]{4}|)\\s*$");
    private static final int PATTERN_SPLIT_REAL_NAME = 1;
    private static final int PATTERN_SPLIT_HOME_BASE = 2;

    private final String realName;
    private final String homeBase;

    /**
     * Splits the given raw name input.
     *
     * @param rawName raw combined name field as available from data files
     */
    public RealNameHomeBaseExtractor(String rawName) {
        Matcher matcher = PATTERN_SPLIT.matcher(rawName);
        if (!matcher.matches()) {
            realName = null;
            homeBase = null;
            return;
        }

        realName = nullIfNumeric(matcher.group(PATTERN_SPLIT_REAL_NAME));
        homeBase = nullIfEmpty(matcher.group(PATTERN_SPLIT_HOME_BASE).trim());
    }

    /**
     * Returns the actual real name (without home base).
     *
     * @return real name without home base; null if unavailable
     */
    public String getRealName() {
        return realName;
    }

    /**
     * Returns the home base airport code. This should usually be a 4-letter ICAO
     * code.
     *
     * @return home base airport code; null if unavailable
     */
    public String getHomeBase() {
        return homeBase;
    }
}
