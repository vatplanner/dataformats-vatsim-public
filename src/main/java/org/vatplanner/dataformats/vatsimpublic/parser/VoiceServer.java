package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.vatplanner.dataformats.vatsimpublic.utils.Comparisons.equalsNullSafe;

/**
 * Holds information about VATSIM voice servers as available from data.txt
 * status file.
 * <p>
 * Voice servers are integrated with VATSIM's modified FSD network protocol so,
 * just like {@link FSDServer} records, this information is pretty useless
 * except for keeping statistics unless you signed the NDA and are working on an
 * ATC client.</p>
 * <p>
 * Online ATC stations usually list the voice room URL they are connected to on
 * their {@link Client} records.</p>
 */
public class VoiceServer {

    private String address;
    private String location;
    private String name;
    private boolean clientConnectionAllowed;
    private String rawServerType; // raw because I couldn't find any information on what values are allowed and what they are supposed to mean

    /**
     * Returns the server's network address. May be a host name or IP address.
     * May be invalid (although highly unlikely), as no validation is being
     * performed on parsing.
     *
     * @return server network address (host name or IP address)
     */
    public String getAddress() {
        return address;
    }

    VoiceServer setAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Returns the server's physical location name.
     *
     * @return server's physical location name
     */
    public String getLocation() {
        return location;
    }

    VoiceServer setLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Returns the server name. The name is not the server's address/host name
     * but may actually contain a longer description text.
     *
     * @return server name (not address or ID, used as a description text)
     */
    public String getName() {
        return name;
    }

    VoiceServer setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns if clients are allowed to connect to this server.
     *
     * @return Are clients allowed to connect to this server? (true = allowed,
     * false = not allowed)
     */
    public boolean isClientConnectionAllowed() {
        return clientConnectionAllowed;
    }

    VoiceServer setClientConnectionAllowed(boolean clientConnectionAllowed) {
        this.clientConnectionAllowed = clientConnectionAllowed;
        return this;
    }

    /**
     * Returns the raw (uninterpreted) information about the type of voice
     * server. Unfortunately, there is no publicly documented information on
     * what this means, so expect this to be anything and of no use unless you
     * are writing a pilot/ATC client and have the required protocol
     * documentation. Will be null if type has not been specified.
     *
     * @return raw (uninterpreted) information about the type of voice server;
     * may be null if missing
     */
    public String getRawServerType() {
        return rawServerType;
    }

    VoiceServer setRawServerType(String rawServerType) {
        this.rawServerType = rawServerType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof VoiceServer)) {
            return false;
        }

        VoiceServer other = (VoiceServer) o;

        boolean isAddressEqual = equalsNullSafe(this, other, VoiceServer::getAddress);
        boolean isLocationEqual = equalsNullSafe(this, other, VoiceServer::getLocation);
        boolean isNameEqual = equalsNullSafe(this, other, VoiceServer::getName);
        boolean isClientConnectionAllowedEqual = (this.isClientConnectionAllowed() == other.isClientConnectionAllowed());
        boolean isRawServerTypeEqual = equalsNullSafe(this, other, VoiceServer::getRawServerType);

        return isAddressEqual && isLocationEqual && isNameEqual && isClientConnectionAllowedEqual && isRawServerTypeEqual;
    }
}
