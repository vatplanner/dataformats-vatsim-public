package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.FSDServer;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.json.JsonHelpers;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class FSDServerJsonProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FSDServerJsonProcessor.class);

    static final String SECTION_NAME = "servers";

    private static enum Key implements JsonKey {
        CLIENT_CONNECTION_ALLOWED("clients_connection_allowed"),
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
        return JsonHelpers.processArraySkipOnError(//
            array, //
            JsonObject.class, //
            SECTION_NAME, //
            logCollector, //
            x -> deserializeSingle(x, logCollector) //
        );
    }

    public FSDServer deserializeSingle(JsonObject object, ParserLogEntryCollector logCollector) {
        FSDServer out = new FSDServer();

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.ID, //
            SECTION_NAME, //
            logCollector, //
            out::setId //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.NAME, //
            SECTION_NAME, //
            logCollector, //
            out::setName //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.ADDRESS, //
            SECTION_NAME, //
            logCollector, //
            out::setAddress //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.LOCATION, //
            SECTION_NAME, //
            logCollector, //
            out::setLocation //
        );

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.CLIENT_CONNECTION_ALLOWED, //
            SECTION_NAME, //
            logCollector, //
            this::parseClientConnectionAllowed //
        ).ifPresent(out::setClientConnectionAllowed);

        return out;
    }

    private boolean parseClientConnectionAllowed(int clientConnectionAllowed) {
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
                LOGGER.warn( //
                    "Unknown value {} for {}, assuming connections are allowed.", //
                    clientConnectionAllowed, //
                    Key.CLIENT_CONNECTION_ALLOWED.getKey() //
                );
                return true;
        }
    }

    // TODO: unit tests
}
