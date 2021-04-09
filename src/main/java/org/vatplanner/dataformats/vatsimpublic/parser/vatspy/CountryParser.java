package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_OPTIONAL_INLINE_COMMENT;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountryParser implements Function<String, Country> {
    private static final Pattern PATTERN = Pattern.compile(
        "([^|]+)\\|([^|]+)\\|([^|]*)"
            + SUBPATTERN_OPTIONAL_INLINE_COMMENT //
    );
    private static final int NAME = 1;
    private static final int ICAO_PREFIX = 2;
    private static final int RADAR_NAME = 3;

    @Override
    public Country apply(String t) {
        Matcher matcher = PATTERN.matcher(t);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Line does not match pattern: \"" + t + "\"");
        }

        return new Country(
            matcher.group(NAME),
            matcher.group(ICAO_PREFIX),
            matcher.group(RADAR_NAME) //
        );
    }
}
