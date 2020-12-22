package org.vatplanner.dataformats.vatsimpublic.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Holds all information about current state of VATSIM network as parsed from a
 * single data.txt status file.
 * <p>
 * Information is usually updated about once per minute by data servers. The
 * policy is to obey the requested minimum delay between requests as specified
 * in the data file's meta data section.
 * </p>
 * <p>
 * Online pilots, pilot prefilings and online ATC share the same data format, so
 * they are combined into one common {@link #clients} collection and
 * distinguished by {@link ClientType}.
 * </p>
 */
public class DataFile implements ParserLogEntryCollector {

    private DataFileMetaData metaData;
    private Collection<VoiceServer> voiceServers;
    private Collection<FSDServer> fsdServers;
    private Collection<Client> clients;

    private DataFileFormat format = null;

    private final ArrayList<ParserLogEntry> parserLogEntries = new ArrayList<>();

    public DataFileMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(DataFileMetaData metaData) {
        this.metaData = metaData;
    }

    public Collection<VoiceServer> getVoiceServers() {
        return voiceServers;
    }

    public void setVoiceServers(Collection<VoiceServer> voiceServers) {
        this.voiceServers = voiceServers;
    }

    public Collection<FSDServer> getFsdServers() {
        return fsdServers;
    }

    public void setFsdServers(Collection<FSDServer> fsdServers) {
        this.fsdServers = fsdServers;
    }

    public Collection<Client> getClients() {
        return clients;
    }

    public void setClients(Collection<Client> clients) {
        this.clients = clients;
    }

    /**
     * Returns the data file format. This is no official field but indicates which
     * parser was used to generate a parsed {@link DataFile} object.
     * 
     * @return format of data file indicating applied parser
     */
    public DataFileFormat getFormat() {
        return format;
    }

    public void setFormat(DataFileFormat format) {
        this.format = format;
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
            return Collections.unmodifiableCollection(parserLogEntries);
        }
    }
}
