package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_OPTIONAL_INLINE_COMMENT;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlightInformationRegionParser implements Function<String, FlightInformationRegion> {
    private static final Pattern PATTERN = Pattern.compile(
        "([^|]+)\\|([^|]+)\\|([^|]*)\\|([^|]*)"
            + SUBPATTERN_OPTIONAL_INLINE_COMMENT
    );
    private static final int ID = 1;
    private static final int NAME = 2;
    private static final int CALLSIGN_PREFIX = 3;
    private static final int BOUNDARY_ID = 4;

    @Override
    public FlightInformationRegion apply(String t) {
        Matcher matcher = PATTERN.matcher(t);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Line does not match pattern: \"" + t + "\"");
        }

        return new FlightInformationRegion(
            matcher.group(ID),
            matcher.group(NAME),
            matcher.group(CALLSIGN_PREFIX),
            matcher.group(BOUNDARY_ID)
        );
    }
}
