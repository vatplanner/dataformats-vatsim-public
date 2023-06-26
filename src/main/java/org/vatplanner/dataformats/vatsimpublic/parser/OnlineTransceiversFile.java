package org.vatplanner.dataformats.vatsimpublic.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Provides information about currently online Audio for VATSIM transceiver
 * locations.
 */
public class OnlineTransceiversFile implements ParserLogEntryCollector {
    private Format format;
    private Collection<OnlineTransceiverStation> stations;

    private final ArrayList<ParserLogEntry> parserLogEntries = new ArrayList<>();

    /**
     * Data formats for online transceiver information.
     */
    public static enum Format implements NetworkInformationDataKeyProvider {
        /**
         * Introduced/opened up in April 2021 to supplement JSON data files.
         */
        INITIAL("transceivers");

        private final String jsonNetworkInformationKey;

        private Format(String jsonNetworkInformationKey) {
            this.jsonNetworkInformationKey = jsonNetworkInformationKey;
        }

        @Override
        public String getNetworkInformationDataKey() {
            return jsonNetworkInformationKey;
        }
    }

    /**
     * Returns the {@link Format} of the parsed data source.
     *
     * @return {@link Format} of parsed data source
     */
    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     * Returns all stations described by this file. Note that due to the data format
     * stations with the same call signs could theoretically repeat with a second
     * station having different transceivers. There is no upstream contract to avoid
     * such duplicates.
     *
     * @return all stations described by this file
     */
    public Collection<OnlineTransceiverStation> getStations() {
        return stations;
    }

    public void setStations(Collection<OnlineTransceiverStation> stations) {
        this.stations = stations;
    }

    @Override
    public void addParserLogEntry(ParserLogEntry entry) {
        synchronized (parserLogEntries) {
            parserLogEntries.add(entry);
        }
    }

    @Override
    public Collection<ParserLogEntry> getParserLogEntries() {
        synchronized (parserLogEntries) {
            return Collections.unmodifiableCollection(new ArrayList<>(parserLogEntries));
        }
    }
}
