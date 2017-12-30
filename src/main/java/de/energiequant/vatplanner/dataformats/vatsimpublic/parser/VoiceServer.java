package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Holds information about VATSIM voice servers as available from data.txt
 * status file.
 * <p>Voice servers are integrated with VATSIM's modified FSD network protocol so,
 * just like {@link FSDServer} records, this information is pretty useless
 * except for keeping statistics unless you signed the NDA and are working on an
 * ATC client.</p>
 * <p>Online ATC stations usually list the voice room URL they are connected to
 * on their {@link Client} records.</p>
 */
public class VoiceServer {
    private String hostname;
    private String location;
    private String name;
    private boolean clientConnectionAllowed;
    private String serverType;

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

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }
    
    
}
