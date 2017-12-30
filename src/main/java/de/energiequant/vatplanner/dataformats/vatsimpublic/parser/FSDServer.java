package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Information about VATSIM FSD servers as available from data.txt status file.
 * FSD is the protocol used to connect actual clients with the network.
 * VATSIM FSD servers have deviated from original public FSD protocol.
 * The VATSIM protocols are only available after signing a NDA. Unless you
 * signed up for the NDA and are working on a pilot/ATC client, this information
 * is pretty useless except for keeping statistics.
 */
public class FSDServer {
    private String id;
    private String hostname;
    private String location;
    private String name;
    private boolean clientConnectionAllowed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClientConnectionAllowed() {
        return clientConnectionAllowed;
    }

    public void setClientConnectionAllowed(boolean clientConnectionAllowed) {
        this.clientConnectionAllowed = clientConnectionAllowed;
    }
    
    
}
