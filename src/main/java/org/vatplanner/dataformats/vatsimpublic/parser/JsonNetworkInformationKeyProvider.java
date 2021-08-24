package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * To be implemented if an object can be identified by a well-known key on
 * JSON-based {@link NetworkInformation}.
 */
public interface JsonNetworkInformationKeyProvider {
    /**
     * Returns the key used to identify a file/format on JSON-based
     * {@link NetworkInformation} files.
     * 
     * @return key used to identify files/formats on JSON-based
     *         {@link NetworkInformation} files
     */
    String getJsonNetworkInformationKey();
}
