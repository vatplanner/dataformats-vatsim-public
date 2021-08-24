package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * To be implemented if an object can be identified by a well-known
 * <code>data</code> key on JSON-based {@link NetworkInformation}.
 */
public interface NetworkInformationDataKeyProvider {
    /**
     * Returns the key used to identify a file/format on the <code>data</code> field
     * of JSON-based {@link NetworkInformation} files.
     * 
     * @return key used to identify files/formats on JSON-based
     *         {@link NetworkInformation} files' <code>data</code> field
     */
    String getNetworkInformationDataKey();
}
