package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import java.util.Optional;

class Helper {
    static final String SUBPATTERN_FLOATING_POINT = "-?[0-9]+(?:\\.[0-9]+|)";

    static final String COMMENT_PREFIX = ";";
    static final String SUBPATTERN_OPTIONAL_INLINE_COMMENT = "(?:\\s*" + COMMENT_PREFIX + ".*|)";

    static Optional<String> emptyIfBlank(String s) {
        if ((s == null) || s.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(s);
    }
}
