package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.time.Duration;
import java.time.Instant;

/**
 * Meta data found in the <code>GENERAL</code> section of VATSIM's data.txt
 * status file.
 */
public class DataFileMetaData {

    private int versionFormat = -1;
    private Instant timestamp = null;
    private int numberOfConnectedClients = -1;
    private Duration minimumDataFileRetrievalInterval = null;
    private Duration minimumAtisRetrievalInterval = null;

    /**
     * Returns the version number of data file format. Will be negative if not
     * set.
     *
     * @return version number of data file format; negative if not set
     */
    public int getVersionFormat() {
        return versionFormat;
    }

    void setVersionFormat(int versionFormat) {
        this.versionFormat = versionFormat;
    }

    /**
     * Returns the timestamp the data has been updated server-side. Will be null
     * if not set.
     *
     * @return server-side update timestamp of data; null if not set
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the number of connected clients at time of update. Will be
     * negative if not set.
     *
     * @return number of connected clients at time of update; negative if not
     * set
     */
    public int getNumberOfConnectedClients() {
        return numberOfConnectedClients;
    }

    void setNumberOfConnectedClients(int numberOfConnectedClients) {
        this.numberOfConnectedClients = numberOfConnectedClients;
    }

    /**
     * Returns the requested minimum retrieval interval for data files.
     * <p>
     * One policy to access VATSIM data servers is to not retrieve data files
     * more often than this interval which is defined by data files themselves.
     * The interval is of course only applicable to downloads directly from
     * VATSIM servers; this does not apply to cached data files retrieved from
     * elsewhere.
     * </p>
     * <p>
     * Will be null if not set.
     * </p>
     * <p>
     * <strong>Disclaimer:</strong> For actual policies, please read comments on
     * raw status and data files. Information about policies given by this
     * documentation is inofficial and only to be used as a reminder to read the
     * actual official documents provided by VATSIM.
     * </p>
     *
     * @return requested minimum retrieval interval for data files; null if not
     * set
     */
    public Duration getMinimumDataFileRetrievalInterval() {
        return minimumDataFileRetrievalInterval;
    }

    void setMinimumDataFileRetrievalInterval(Duration minimumDataFileRetrievalInterval) {
        this.minimumDataFileRetrievalInterval = minimumDataFileRetrievalInterval;
    }

    /**
     * Returns the requested minimum retrieval interval for ATIS information via
     * "web page interface".
     * <p>
     * Similar to {@link #minimumDataFileRetrievalInterval} this minimum
     * interval applies to queries performed to ATIS services such as defined by
     * {@link NetworkInformation#getAtisUrls()}. Policy is not to request
     * information any more frequent from VATSIM servers; this does not apply to
     * cached information retrieved from elsewhere.
     * </p>
     * <p>
     * Will be null if not set.</p>
     * <p>
     * <strong>Disclaimer:</strong> For actual policies, please read comments on
     * raw status and data files. Information about policies given by this
     * documentation is inofficial and only to be used as a reminder to read the
     * actual official documents provided by VATSIM.
     * </p>
     *
     * @return requested minimum retrieval interval for data files; null if not
     * set
     */
    public Duration getMinimumAtisRetrievalInterval() {
        return minimumAtisRetrievalInterval;
    }

    void setMinimumAtisRetrievalInterval(Duration minimumAtisRetrievalInterval) {
        this.minimumAtisRetrievalInterval = minimumAtisRetrievalInterval;
    }
}
