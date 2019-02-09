package org.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class FSDServerTest {

    @Test
    public void testEquals_null_returnsFalse() {
        // Arrange
        FSDServer a = new FSDServer();
        FSDServer b = null;

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testEquals_wrongClass_returnsFalse() {
        // Arrange
        FSDServer a = new FSDServer();
        Object b = new Object();

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    @DataProvider({
        "MYID, some.server.net, Somewhere, Name, true",
        "serverA, 123.4.56.7, Anywhere, Server A, false",
        "null, 123.4.56.7, Anywhere, Server A, false",
        "serverA, null, Anywhere, Server A, false",
        "serverA, 123.4.56.7, null, Server A, false",
        "serverA, 123.4.56.7, Anywhere, null, false"
    })
    public void testEquals_equal_returnsTrue(String id, String address, String location, String name, boolean clientConnectionAllowed) {
        // Arrange
        FSDServer a = createFSDServer(id, address, location, name, clientConnectionAllowed);
        FSDServer b = createFSDServer(id, address, location, name, clientConnectionAllowed);

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    @DataProvider({
        //              object A                              ||                    object B
        // ID
        "MYID,    some.server.net, Somewhere, Name,     true,   myid,     some.server.net, Somewhere, Name,     true ", // 0
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  server A, 123.4.56.7,      Anywhere,  Server A, false", // 1
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  null,     123.4.56.7,      Anywhere,  Server A, false", // 2
        "null,    123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  Server A, false", // 3
        // address
        "MYID,    some.server.net, Somewhere, Name,     true,   MYID,     someserver.net,  Somewhere, Name,     true ", // 4
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.45.6.7,      Anywhere,  Server A, false", // 5
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  null,            Anywhere,  Server A, false", // 6
        "serverA, null,            Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  Server A, false", // 7
        // location
        "MYID,    some.server.net, Somewhere, Name,     true,   MYID,     some.server.net, SomeWhere, Name,     true ", // 8
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Any where, Server A, false", // 9
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      null,      Server A, false", // 10
        "serverA, 123.4.56.7,      null,      Server A, false,  serverA,  123.4.56.7,      Anywhere,  Server A, false", // 11
        // name
        "MYID,    some.server.net, Somewhere, Name,     true,   MYID,     some.server.net, Somewhere, NAME,     true ", // 12
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  ServerA,  false", // 13
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  null,     false", // 14
        "serverA, 123.4.56.7,      Anywhere,  null,     false,  serverA,  123.4.56.7,      Anywhere,  Server A, false", // 15
        // client connection allowed
        "MYID,    some.server.net, Somewhere, Name,     true,   MYID,     some.server.net, Somewhere, Name,     false", // 16
        "serverA, 123.4.56.7,      Anywhere,  Server A, false,  serverA,  123.4.56.7,      Anywhere,  Server A, true ", // 17
    })
    public void testEquals_nonEqual_returnsFalse(String idA, String addressA, String locationA, String nameA, boolean clientConnectionAllowedA, String idB, String addressB, String locationB, String nameB, boolean clientConnectionAllowedB) {
        // Arrange
        FSDServer a = createFSDServer(idA, addressA, locationA, nameA, clientConnectionAllowedA);
        FSDServer b = createFSDServer(idB, addressB, locationB, nameB, clientConnectionAllowedB);

        // Act
        boolean result = a.equals(b);

        // Assert
        assertThat(result, is(false));
    }

    private FSDServer createFSDServer(String id, String address, String location, String name, boolean clientConnectionAllowed) {
        return new FSDServer()
                .setId(id)
                .setAddress(address)
                .setLocation(location)
                .setName(name)
                .setClientConnectionAllowed(clientConnectionAllowed);
    }
}
