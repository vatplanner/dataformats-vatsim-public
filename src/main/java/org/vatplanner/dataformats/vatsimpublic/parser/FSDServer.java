package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.vatplanner.dataformats.vatsimpublic.utils.Comparisons.equalsNullSafe;

/**
 * Information about VATSIM FSD servers as available from data.txt status file.
 * FSD is the protocol used to connect actual clients with the network. VATSIM
 * FSD servers have deviated from original public FSD protocol. The VATSIM
 * protocols are only available after signing a NDA. Unless you signed up for
 * the NDA and are working on a pilot/ATC client, this information is pretty
 * useless except for keeping statistics.
 */
public class FSDServer {

    private String id;
    private String address;
    private String location;
    private String name;
    private boolean clientConnectionAllowed;

    /**
     * Returns the identification ("system name") of the described server.
     *
     * @return identification ("system name") of described server
     */
    public String getId() {
        return id;
    }

    public FSDServer setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the server's network address. May be a host name or IP address. May
     * be invalid (although highly unlikely), as no validation is being performed on
     * parsing.
     *
     * @return server network address (host name or IP address)
     */
    public String getAddress() {
        return address;
    }

    public FSDServer setAddress(String address) {
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

    public FSDServer setLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Returns the server name. The name is not the server's address/host name or ID
     * but may actually contain a longer description text.
     *
     * @return server name (not address or ID, used as a description text)
     */
    public String getName() {
        return name;
    }

    public FSDServer setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns if clients are allowed to connect to this server.
     *
     * @return Are clients allowed to connect to this server? (true = allowed, false
     *         = not allowed)
     */
    public boolean isClientConnectionAllowed() {
        return clientConnectionAllowed;
    }

    public FSDServer setClientConnectionAllowed(boolean clientConnectionAllowed) {
        this.clientConnectionAllowed = clientConnectionAllowed;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof FSDServer)) {
            return false;
        }

        FSDServer other = (FSDServer) o;

        boolean isIdEqual = equalsNullSafe(this, other, FSDServer::getId);
        boolean isAddressEqual = equalsNullSafe(this, other, FSDServer::getAddress);
        boolean isLocationEqual = equalsNullSafe(this, other, FSDServer::getLocation);
        boolean isNameEqual = equalsNullSafe(this, other, FSDServer::getName);
        boolean isClientConnectionAllowedEqual = //
            (this.isClientConnectionAllowed() == other.isClientConnectionAllowed());

        return isIdEqual && isAddressEqual && isLocationEqual && isNameEqual && isClientConnectionAllowedEqual;
    }

}
