package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.FSDServer;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.json.JsonHelpers;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class FSDServerJsonProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FSDServerJsonProcessor.class);

    static final String SECTION_NAME = "servers";

    private static enum Key implements JsonKey {
        CLIENT_CONNECTION_ALLOWED_INTEGER("clients_connection_allowed"), // legacy field holding an unknown integer
        CLIENT_CONNECTION_ALLOWED_BOOLEAN("client_connections_allowed"), // new field holding an actual boolean
        SWEATBOX("is_sweatbox"),
        ADDRESS("hostname_or_ip"),
        ID("ident"),
        LOCATION("location"),
        NAME("name");

        private final String key;

        private Key(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return null;
        }
    }

    public List<FSDServer> deserializeMultiple(JsonArray array, ParserLogEntryCollector logCollector) {
        return JsonHelpers.processArraySkipOnError(
            array,
            JsonObject.class,
            SECTION_NAME,
            logCollector,
            x -> deserializeSingle(x, logCollector)
        );
    }

    public FSDServer deserializeSingle(JsonObject object, ParserLogEntryCollector logCollector) {
        FSDServer out = new FSDServer();

        JsonHelpers.processMandatory(
            object::getString,
            Key.ID,
            SECTION_NAME,
            logCollector,
            out::setId
        );

        JsonHelpers.processMandatory(
            object::getString,
            Key.NAME,
            SECTION_NAME,
            logCollector,
            out::setName
        );

        JsonHelpers.processMandatory(
            object::getString,
            Key.ADDRESS,
            SECTION_NAME,
            logCollector,
            out::setAddress
        );

        JsonHelpers.processMandatory(
            object::getString,
            Key.LOCATION,
            SECTION_NAME,
            logCollector,
            out::setLocation
        );

        JsonHelpers.processOptional(
            object::getBoolean,
            Key.SWEATBOX,
            SECTION_NAME,
            logCollector,
            out::setSweatbox
        );

        JsonHelpers.processMandatory(
            object::getInteger,
            Key.CLIENT_CONNECTION_ALLOWED_INTEGER,
            SECTION_NAME,
            logCollector,
            this::parseClientConnectionAllowedInteger
        ).ifPresent(out::setClientConnectionAllowed);

        JsonHelpers.getOptional(
            object::getBoolean,
            Key.CLIENT_CONNECTION_ALLOWED_BOOLEAN,
            SECTION_NAME,
            logCollector
        ).ifPresent(booleanAllowed -> {
            // Extra check: This field duplicates the legacy one for which we could only
            // guess what the integer meant. Report any ambiguity but always trust the
            // boolean if present.
            if (booleanAllowed != out.isClientConnectionAllowed()) {
                logCollector.addParserLogEntry(new ParserLogEntry(
                    SECTION_NAME,
                    "content at " + Key.CLIENT_CONNECTION_ALLOWED_BOOLEAN.key,
                    false,
                    "server \"" + out.getId()
                        + "\" has a different boolean indication for allowed client connections than legacy integer value suggests; trusting the boolean",
                    null
                ));
            }
            out.setClientConnectionAllowed(booleanAllowed);
        });

        return out;
    }

    private boolean parseClientConnectionAllowedInteger(int clientConnectionAllowed) {
        /*
         * Only value 1 has been seen so far and on legacy format we assumed this to be
         * a boolean flag although it could also be a minimum permission level, quota or
         * something completely else. JSON spec actually says this is not a boolean but
         * an int32 without giving any further information (v3 in December 2020) so we
         * should be prepared for surprises and log in case we see something we may not
         * interpret correctly...
         */

        switch (clientConnectionAllowed) {
            case 0:
                return false;

            case 1:
                return true;

            default:
                LOGGER.warn(
                    "Unknown value {} for {}, assuming connections are allowed.",
                    clientConnectionAllowed,
                    Key.CLIENT_CONNECTION_ALLOWED_INTEGER.getKey()
                );
                return true;
        }
    }

    // TODO: unit tests
}
