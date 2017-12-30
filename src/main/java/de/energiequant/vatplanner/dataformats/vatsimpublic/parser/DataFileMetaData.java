package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.time.Duration;
import java.time.Instant;

/**
 * Meta data found in the <code>GENERAL</code> section of VATSIM's data.txt status file.
 */
public class DataFileMetaData {
    private int versionFormat = -1;
    private Instant timestamp = null;
    private int numberOfConnectedClients = -1;
    private Duration minimumDataFileRetrievalInterval = null;
    private Duration minimumAtisRetrievalInterval = null;
    
    // TODO: generate getters & setters
}
