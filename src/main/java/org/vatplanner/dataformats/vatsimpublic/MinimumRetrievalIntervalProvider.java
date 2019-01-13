package org.vatplanner.dataformats.vatsimpublic;

import java.time.Duration;

/**
 * Should be implemented if network-retrieved data defines a minimum interval
 * (minimum time to wait) before next fetch.
 */
public interface MinimumRetrievalIntervalProvider {

    /**
     * Returns the minimum interval (time to wait) before next fetch of data as
     * requested by upstream sources.
     *
     * @return minimum interval at which data should be retrieved
     */
    public Duration getMinimumRetrievalInterval();
}
