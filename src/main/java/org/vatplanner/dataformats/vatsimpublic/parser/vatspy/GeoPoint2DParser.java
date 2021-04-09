package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_FLOATING_POINT;
import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_OPTIONAL_INLINE_COMMENT;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoPoint2DParser implements Function<String, GeoPoint2D> {
    private static final Pattern PATTERN = Pattern.compile(
        "(" + SUBPATTERN_FLOATING_POINT + ")\\|(" + SUBPATTERN_FLOATING_POINT + ")" //
            + SUBPATTERN_OPTIONAL_INLINE_COMMENT //
    );
    private static final int LATITUDE = 1;
    private static final int LONGITUDE = 2;

    @Override
    public GeoPoint2D apply(String t) {
        Matcher matcher = PATTERN.matcher(t);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Data does not match pattern: \"" + t + "\"");
        }

        return new GeoPoint2D(
            Double.parseDouble(matcher.group(LATITUDE)),
            Double.parseDouble(matcher.group(LONGITUDE)) //
        );
    }

}
