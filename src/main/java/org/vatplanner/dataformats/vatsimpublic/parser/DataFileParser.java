package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Proxy to
 * {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy.DataFileParser}
 * for API compatibility. Migrate to use <code>legacy</code> classes directly or
 * {@link DataFileParserFactory} instead, this proxy will be removed shortly.
 * 
 * @deprecated use classes from
 *             {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy}
 *             directly or access parsers via {@link DataFileParserFactory}
 */
@Deprecated
public class DataFileParser extends org.vatplanner.dataformats.vatsimpublic.parser.legacy.DataFileParser {
    /**
     * @deprecated use classes from
     *             {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy}
     *             directly or access parsers via {@link DataFileParserFactory}
     */
    @Deprecated
    public DataFileParser() {
        super();
    }
}
