package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.vatplanner.dataformats.vatsimpublic.utils.Comparisons.equalsNullSafe;

import java.time.Duration;
import java.time.Instant;

/**
 * Meta data found in the <code>general</code> section of VATSIM's data status
 * file.
 */
public class DataFileMetaData {

    private int versionFormat = -1;
    private Instant timestamp = null;
    private int numberOfConnectedClients = -1;
    private int numberOfUniqueConnectedUsers = -1;
    private Duration minimumDataFileRetrievalInterval = null;
    private Duration minimumAtisRetrievalInterval = null;

    /**
     * Returns the version number of data file format. Will be negative if not set.
     *
     * @return version number of data file format; negative if not set
     */
    public int getVersionFormat() {
        return versionFormat;
    }

    public DataFileMetaData setVersionFormat(int versionFormat) {
        this.versionFormat = versionFormat;
        return this;
    }

    /**
     * Returns the timestamp the data has been updated server-side. Will be null if
     * not set.
     *
     * @return server-side update timestamp of data; null if not set
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    public DataFileMetaData setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Returns the number of connected clients at time of update. Will be negative
     * if not set.
     * <p>
     * This information includes duplicate connections (most coming from ATIS
     * stations) and is <b>not</b> the number that is being used to count connection
     * records on VATSIM - see {@link #getNumberOfUniqueConnectedUsers()} instead if
     * you want to count users.
     * </p>
     *
     * @return number of connected clients at time of update (includes duplicate connections); negative if not set
     * @see #getNumberOfUniqueConnectedUsers() for de-duplicated number of connected users instead of clients
     */
    public int getNumberOfConnectedClients() {
        return numberOfConnectedClients;
    }

    public DataFileMetaData setNumberOfConnectedClients(int numberOfConnectedClients) {
        this.numberOfConnectedClients = numberOfConnectedClients;
        return this;
    }

    /**
     * Returns the number of unique connected users at time of update. Will be
     * negative if not set.
     * <p>
     * This information has only been added in format version 9. On older format
     * data, connected clients need to be de-duplicated and counted to get an
     * equivalent number.
     * </p>
     *
     * @return number of unique connected users at time of update; negative if not set
     * @see #getNumberOfConnectedClients() for total number of connected clients (not users)
     */
    public int getNumberOfUniqueConnectedUsers() {
        return numberOfUniqueConnectedUsers;
    }

    public DataFileMetaData setNumberOfUniqueConnectedUsers(int numberOfUniqueConnectedUsers) {
        this.numberOfUniqueConnectedUsers = numberOfUniqueConnectedUsers;
        return this;
    }

    /**
     * Returns the requested minimum retrieval interval for data files.
     * <p>
     * One policy to access VATSIM data servers is to not retrieve data files more
     * often than this interval which is defined by data files themselves. The
     * interval is of course only applicable to downloads directly from VATSIM
     * servers; this does not apply to cached data files retrieved from elsewhere.
     * </p>
     * <p>
     * Will be null if not set.
     * </p>
     * <p>
     * <strong>Disclaimer:</strong> For actual policies, please read comments on raw
     * status and data files. Information about policies given by this documentation
     * is inofficial and only to be used as a reminder to read the actual official
     * documents provided by VATSIM.
     * </p>
     *
     * @return requested minimum retrieval interval for data files; null if not set
     */
    public Duration getMinimumDataFileRetrievalInterval() {
        return minimumDataFileRetrievalInterval;
    }

    public DataFileMetaData setMinimumDataFileRetrievalInterval(Duration minimumDataFileRetrievalInterval) {
        this.minimumDataFileRetrievalInterval = minimumDataFileRetrievalInterval;
        return this;
    }

    /**
     * Returns the requested minimum retrieval interval for ATIS information via
     * "web page interface".
     * <p>
     * Similar to {@link #minimumDataFileRetrievalInterval} this minimum interval
     * applies to queries performed to ATIS services such as defined by
     * {@link NetworkInformation#getAtisUrls()}. Policy is not to request
     * information any more frequent from VATSIM servers; this does not apply to
     * cached information retrieved from elsewhere.
     * </p>
     * <p>
     * Will be null if not set.
     * </p>
     * <p>
     * <strong>Disclaimer:</strong> For actual policies, please read comments on raw
     * status and data files. Information about policies given by this documentation
     * is inofficial and only to be used as a reminder to read the actual official
     * documents provided by VATSIM.
     * </p>
     *
     * @return requested minimum retrieval interval for data files; null if not set
     */
    public Duration getMinimumAtisRetrievalInterval() {
        return minimumAtisRetrievalInterval;
    }

    public DataFileMetaData setMinimumAtisRetrievalInterval(Duration minimumAtisRetrievalInterval) {
        this.minimumAtisRetrievalInterval = minimumAtisRetrievalInterval;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof DataFileMetaData)) {
            return false;
        }

        DataFileMetaData other = (DataFileMetaData) o;

        boolean isVersionFormatEqual = (this.getVersionFormat() == other.getVersionFormat());
        boolean isTimestampEqual = equalsNullSafe(this, other, DataFileMetaData::getTimestamp);
        boolean isNumberOfConnectedClientsEqual = (this.getNumberOfConnectedClients() == other.getNumberOfConnectedClients());
        boolean isNumberOfConnectedUniqueUsersEqual = (this.getNumberOfUniqueConnectedUsers() == other.getNumberOfUniqueConnectedUsers());
        boolean isMinimumDataFileRetrievalIntervalEqual = equalsNullSafe(
            this, other, DataFileMetaData::getMinimumDataFileRetrievalInterval
        );
        boolean isMinimumAtisFileRetrievalIntervalEqual = equalsNullSafe(
            this, other, DataFileMetaData::getMinimumAtisRetrievalInterval
        );

        boolean isEqual = isVersionFormatEqual && isTimestampEqual
            && isNumberOfConnectedClientsEqual && isNumberOfConnectedUniqueUsersEqual
            && isMinimumDataFileRetrievalIntervalEqual && isMinimumAtisFileRetrievalIntervalEqual;

        return isEqual;
    }
}
