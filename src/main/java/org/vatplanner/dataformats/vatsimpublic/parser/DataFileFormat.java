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
    LEGACY,

    /**
     * JSON format with structure version 3. Introduced in November 2020.
     */
    JSON3;
}
