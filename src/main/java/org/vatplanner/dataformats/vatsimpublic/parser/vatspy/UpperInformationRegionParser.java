package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_OPTIONAL_INLINE_COMMENT;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpperInformationRegionParser implements Function<String, UpperInformationRegion> {
    private static final Pattern PATTERN = Pattern.compile(
        "([^|]+)\\|([^|]+)\\|([^|]+)"
            + SUBPATTERN_OPTIONAL_INLINE_COMMENT //
    );
    private static final int ID = 1;
    private static final int NAME = 2;
    private static final int FIRS = 3;

    private static final String FIR_SEPARATOR = ",";

    @Override
    public UpperInformationRegion apply(String t) {
        Matcher matcher = PATTERN.matcher(t);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Line does not match pattern: \"" + t + "\"");
        }

        return new UpperInformationRegion(
            matcher.group(ID),
            matcher.group(NAME),
            Arrays.asList(matcher.group(FIRS).split(FIR_SEPARATOR)) //
        );
    }

}
