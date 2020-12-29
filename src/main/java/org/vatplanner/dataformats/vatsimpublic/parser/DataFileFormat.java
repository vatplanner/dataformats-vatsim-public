package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * High-level identification of data file formats.
 */
public enum DataFileFormat {
    /**
     * Legacy format consisting of multiple header-separated sections holding
     * colon-separated values. Latest indicated version number
     * {@link DataFileMetaData#getVersionFormat()} was 9, rolled back to indicate 8
     * (data still was version 9) after client compatibility issues. Deprecated
     * since November 2020, pending service termination in 2021.
     */
    LEGACY(Constants.LEGACY_JSON_KEY),

    /**
     * JSON format with structure version 3. Introduced in November 2020.
     */
    JSON3("v3");

    /**
     * Auxiliary constants used by {@link DataFileFormat}.
     */
    public static interface Constants {
        /**
         * Legacy data files are not listed in JSON-based {@link NetworkInformation}.
         * For uniform handling this library makes up its own fictional key instead.
         * This key is not found in actual files.
         */
        public static final String LEGACY_JSON_KEY = "_legacy";
    }

    private final String jsonNetworkInformationKey;

    private DataFileFormat(String jsonNetworkInformationKey) {
        this.jsonNetworkInformationKey = jsonNetworkInformationKey;
    }

    /**
     * Returns the key used to identify the {@link DataFileFormat} on JSON-based
     * {@link NetworkInformation} files.
     * 
     * @return key used to identify the {@link DataFileFormat} on JSON-based
     *         {@link NetworkInformation} files
     */
    public String getJsonNetworkInformationKey() {
        return jsonNetworkInformationKey;
    }
}
