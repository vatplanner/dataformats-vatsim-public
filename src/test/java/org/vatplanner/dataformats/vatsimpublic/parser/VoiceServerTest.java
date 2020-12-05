package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

@RunWith(DataProviderRunner.class)
public class VoiceServerTest {

    @Test
    public void testEquals_null_returnsFalse() {
        // Arrange
        VoiceServer a = new VoiceServer();
        VoiceServer b = null;

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testEquals_wrongClass_returnsFalse() {
        // Arrange
        VoiceServer a = new VoiceServer();
        Object b = new Object();

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    @DataProvider({
        "some.server.net, Somewhere, Some Name, true, ab c12 3",
        "whatever, Don't Know, Yet another name, false, ",
        "null, Don't Know, Yet another name, false, ",
        "whatever, null, Yet another name, false, ",
        "whatever, Don't Know, null, false, ",
        "whatever, Don't Know, Yet another name, false, null"
    })
    public void testCheckEqualVoiceServer_equal_returnsTrue(String address, String location, String name, boolean clientConnectionAllowed, String rawServerType) {
        // Arrange
        VoiceServer a = createVoiceServer(address, location, name, clientConnectionAllowed, rawServerType);
        VoiceServer b = createVoiceServer(address, location, name, clientConnectionAllowed, rawServerType);

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    @DataProvider({
        // object A || object B
        // address
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  someserver.net,  Somewhere,  Some Name,        true,  ab c12 3", // 0
        "whatever,        Don't Know, Yet another name, false, ,          what.ever,       Don't Know, Yet another name, false,         ", // 1
        "whatever,        Don't Know, Yet another name, false, ,          null,            Don't Know, Yet another name, false,         ", // 3
        "null,            Don't Know, Yet another name, false, ,          whatever,        Don't Know, Yet another name, false,         ", // 4

        // location
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  some.server.net, Some where, Some Name,        true,  ab c12 3", // 5
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Dont Know,  Yet another name, false,         ", // 6
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        null,       Yet another name, false,         ", // 7
        "whatever,        null, Yet another name, false, ,                whatever,        Don't Know, Yet another name, false,         ", // 8

        // name
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  some.server.net, Somewhere,  Any Name,         true,  ab c12 3", // 9
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, Some other name,  false,         ", // 10
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, null,             false,         ", // 11
        "whatever,        Don't Know, null,             false, ,          whatever,        Don't Know, Yet another name, false,         ", // 12

        // client connection allowed
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  some.server.net, Somewhere,  Some Name,        false, ab c12 3", // 13
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, Yet another name, true,          ", // 14

        // raw server type
        "some.server.net, Somewhere,  Some Name,        true,  ab c12 3,  some.server.net, Somewhere,  Some Name,        true,  ab c1 23", // 15
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, Yet another name, false, a       ", // 16
        "whatever,        Don't Know, Yet another name, false, ,          whatever,        Don't Know, Yet another name, false, null    ", // 17
        "whatever,        Don't Know, Yet another name, false, null,      whatever,        Don't Know, Yet another name, false,         ", // 18
    })
    public void testCheckEqualVoiceServer_nonEqual_returnsFalse(String addressA, String locationA, String nameA, boolean clientConnectionAllowedA, String rawServerTypeA, String addressB, String locationB, String nameB, boolean clientConnectionAllowedB, String rawServerTypeB) {
        // Arrange
        VoiceServer a = createVoiceServer(addressA, locationA, nameA, clientConnectionAllowedA, rawServerTypeA);
        VoiceServer b = createVoiceServer(addressB, locationB, nameB, clientConnectionAllowedB, rawServerTypeB);

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    private VoiceServer createVoiceServer(String address, String location, String name, boolean clientConnectionAllowed, String rawServerType) {
        return new VoiceServer()
            .setAddress(address)
            .setClientConnectionAllowed(clientConnectionAllowed)
            .setLocation(location)
            .setName(name)
            .setRawServerType(rawServerType);
    }

}
