package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Proxy to
 * {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy.DataFileSectionLineProcessor}
 * for API compatibility. Migrate to use <code>legacy</code> classes directly or
 * parser factory instead, this proxy will be removed shortly.
 *
 * @deprecated use classes from {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy} directly
 *     or access parsers via factory
 */
@Deprecated
public class DataFileSectionLineProcessor
    extends org.vatplanner.dataformats.vatsimpublic.parser.legacy.DataFileSectionLineProcessor {
    // TODO: reference factory class when implemented

    /**
     * Creates a new processor for the given datafile-formatted content.
     *
     * @param s datafile-formatted content
     * @see org.vatplanner.dataformats.vatsimpublic.parser.legacy.DataFileSectionLineProcessor#DataFileSectionLineProcessor(String)
     * @deprecated use classes from {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy} directly
     *     or access parsers via factory
     */
    @Deprecated
    public DataFileSectionLineProcessor(String s) {
        super(s);
    }
}
