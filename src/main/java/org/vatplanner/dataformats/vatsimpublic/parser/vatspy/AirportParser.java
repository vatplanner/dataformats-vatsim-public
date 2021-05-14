package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_FLOATING_POINT;
import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_OPTIONAL_INLINE_COMMENT;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vatplanner.dataformats.vatsimpublic.utils.GeoPoint2D;

public class AirportParser implements Function<String, Airport> {
    private static final Pattern PATTERN = Pattern.compile(
        "([^|]+)\\|([^|]+)" //
            + "\\|(" + SUBPATTERN_FLOATING_POINT + ")\\|(" + SUBPATTERN_FLOATING_POINT + ")" //
            + "\\|([^|]*)\\|([^|]+)\\|([01])" //
            + SUBPATTERN_OPTIONAL_INLINE_COMMENT //
    );
    private static final int ICAO_CODE = 1;
    private static final int NAME = 2;
    private static final int LATITUDE = 3;
    private static final int LONGITUDE = 4;
    private static final int ALTERNATIVE_CODE = 5;
    private static final int FIR = 6;
    private static final int PSEUDO = 7;

    @Override
    public Airport apply(String t) {
        Matcher matcher = PATTERN.matcher(t);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Line does not match pattern: \"" + t + "\"");
        }

        return new Airport(
            matcher.group(ICAO_CODE),
            matcher.group(NAME),
            new GeoPoint2D(
                Double.parseDouble(matcher.group(LATITUDE)),
                Double.parseDouble(matcher.group(LONGITUDE)) //
            ),
            matcher.group(ALTERNATIVE_CODE),
            matcher.group(FIR),
            matcher.group(PSEUDO).equals("1") //
        );
    }
}
