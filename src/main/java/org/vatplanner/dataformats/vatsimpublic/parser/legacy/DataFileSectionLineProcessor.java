package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a complete file in VATSIM data file format for easy line-by-line
 * processing confined to specified sections.
 */
public class DataFileSectionLineProcessor {

    private final List<ContentLine> lines = new ArrayList<>();
    private final Map<String, List<ContentLine>> linesBySectionName = new HashMap<>();

    private static final Pattern CONTENT_SPLIT = Pattern.compile("([^\\r\\n]*)([\\r\\n]*)");
    private static final int CONTENT_SPLIT_CONTENT = 1;
    private static final int CONTENT_SPLIT_SEPARATOR = 2;

    private static final Pattern SECTION = Pattern.compile("\\s*!(\\S+):\\s*");
    private static final int SECTION_NAME = 1;

    /**
     * Represents a "logical" line of content which means actual content is
     * separated from all line separator characters.
     */
    private class ContentLine {

        private String content;
        private String separator;

        private void applyToContent(UnaryOperator<String> function) {
            if (!content.isEmpty()) {
                content = function.apply(content);
            }
        }
    }

    /**
     * Creates a new processor for the given datafile-formatted content.
     *
     * @param s datafile-formatted content
     */
    public DataFileSectionLineProcessor(String s) {
        String sectionName = null;
        Matcher splitMatcher = CONTENT_SPLIT.matcher(s);

        while (splitMatcher.find()) {
            ContentLine contentLine = new ContentLine();
            contentLine.content = splitMatcher.group(CONTENT_SPLIT_CONTENT);
            contentLine.separator = splitMatcher.group(CONTENT_SPLIT_SEPARATOR);

            // at end of input we may encounter a completely blank match; ignore
            if (contentLine.content.isEmpty() && contentLine.separator.isEmpty()) {
                continue;
            }

            // every line should be collected for full retrieval
            lines.add(contentLine);

            // all further processing is only relevant to non-empty contents
            if (contentLine.content.isEmpty()) {
                continue;
            }

            // skip comments
            boolean isComment = contentLine.content.startsWith(";");
            if (isComment) {
                continue;
            }

            // if this line starts a new section, just remember its name
            Matcher sectionMatcher = SECTION.matcher(contentLine.content);
            if (sectionMatcher.matches()) {
                // section names should be recognized case-insensitive
                sectionName = sectionMatcher.group(SECTION_NAME).toUpperCase();
                continue;
            }

            // skip if no section has been started yet
            if (sectionName == null) {
                continue;
            }

            // link line with section for later processing
            linesBySectionName
                .computeIfAbsent(sectionName, k -> new ArrayList<>())
                .add(contentLine);
        }
    }

    /**
     * Applies the given function to all non-empty, non-comment lines of the
     * specified section.
     *
     * @param sectionName section to apply function to; must neither be null nor
     *                    empty
     * @param function    will be applied to every non-empty, non-comment line; must
     *                    not be null
     * @return this processor instance for method-chaining
     * @throws IllegalArgumentException if requirements are not fulfilled
     */
    public DataFileSectionLineProcessor apply(String sectionName, UnaryOperator<String> function) {
        if (sectionName == null) {
            throw new IllegalArgumentException("section name must not be null");
        }

        if (sectionName.isEmpty()) {
            throw new IllegalArgumentException("section name must not be empty");
        }

        if (function == null) {
            throw new IllegalArgumentException("function must not be null");
        }

        List<ContentLine> sectionLines = linesBySectionName.get(sectionName.toUpperCase());
        if (sectionLines != null) {
            sectionLines.stream().forEach(contentLine -> contentLine.applyToContent(function));
        }

        return this;
    }

    /**
     * Returns the result as a string. The result maintains all line-end characters
     * and comments.
     *
     * @return result as string
     */
    public String getResultAsString() {
        StringBuilder sb = new StringBuilder();

        for (ContentLine line : lines) {
            if (line.content != null) {
                sb.append(line.content);
            }

            if (line.separator != null) {
                sb.append(line.separator);
            }
        }

        return sb.toString();
    }
}
