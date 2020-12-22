package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Proxy to
 * {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy.ClientParser}
 * for API compatibility. Migrate to use <code>legacy</code> classes directly or
 * parser factory instead, this proxy will be removed shortly.
 * 
 * @deprecated use classes from
 *             {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy}
 *             directly or access parsers via factory
 */
@Deprecated
public class ClientParser extends org.vatplanner.dataformats.vatsimpublic.parser.legacy.ClientParser {
    // TODO: reference factory class when implemented

    /**
     * @deprecated use classes from
     *             {@link org.vatplanner.dataformats.vatsimpublic.parser.legacy}
     *             directly or access parsers via factory
     */
    @Deprecated
    public ClientParser() {
        super();
    }
}
