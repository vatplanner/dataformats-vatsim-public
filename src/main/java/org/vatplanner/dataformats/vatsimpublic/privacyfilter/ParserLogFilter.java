package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;

public class ParserLogFilter {

    /**
     * Parser logs can contain original raw data when parsing fails or identifies
     * some information as potentially problematic and erroneous. While this is
     * great for development and debugging it may be unwanted in production, so you
     * can shorten raw log lines to a wanted length or remove them completely. Note
     * that due to the nature of most logged lines containing syntax errors of some
     * kind there can not be any privacy filtering on that information.
     * <p>
     * Removed log characters will be indicated by a placeholder like
     * <code>[123 characters removed]</code>.
     * </p>
     *
     * @param dataFile            data file to shorten log lines for
     * @param maxRawLogLineLength maximum number of characters in raw log lines to
     *                            keep, 0 to clear completely
     * @return {@link DataFile} holding shortened log lines only
     */
    public DataFile shortenLogLineContent(DataFile dataFile, int maxRawLogLineLength) {
        // FIXME: implement
        // TODO: allow only >= 0
        return null;
    }
}
