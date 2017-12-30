package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.util.Collection;

/**
 * Holds all information about current state of VATSIM network as parsed from a
 * single data.txt status file.
 * <p>Information is usually updated about once per minute by data servers.
 * The policy is to obey the requested minimum delay between requests as
 * specified in the data file's meta data section.</p>
 * <p>Online pilots, pilot prefilings and online ATC share the same data format,
 * so they are combined into one common {@link #clients} collection and
 * distinguished by {@link ClientType}.</p>
 */
public class DataFile {
    private DataFileMetaData metaData;
    private Collection<VoiceServer> voiceServers;
    private Collection<FSDServer> fsdServers;
    private Collection<Client> clients;

    public DataFileMetaData getMetaData() {
        return metaData;
    }

    void setMetaData(DataFileMetaData metaData) {
        this.metaData = metaData;
    }

    public Collection<VoiceServer> getVoiceServers() {
        return voiceServers;
    }

    void setVoiceServers(Collection<VoiceServer> voiceServers) {
        this.voiceServers = voiceServers;
    }

    public Collection<FSDServer> getFsdServers() {
        return fsdServers;
    }

    void setFsdServers(Collection<FSDServer> fsdServers) {
        this.fsdServers = fsdServers;
    }

    public Collection<Client> getClients() {
        return clients;
    }

    void setClients(Collection<Client> clients) {
        this.clients = clients;
    }
    
    
}
