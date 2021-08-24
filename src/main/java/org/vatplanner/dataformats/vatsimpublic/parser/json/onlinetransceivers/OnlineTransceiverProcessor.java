package org.vatplanner.dataformats.vatsimpublic.parser.json.onlinetransceivers;

import java.util.List;

import org.vatplanner.dataformats.vatsimpublic.parser.OnlineTransceiver;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.json.JsonHelpers;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class OnlineTransceiverProcessor {
    private static enum Key implements JsonKey {
        ID("id"),
        FREQUENCY("frequency"),
        LATITUDE("latDeg"),
        LONGITUDE("lonDeg"),
        ALTITUDE_METERS("heightMslM"),
        HEIGHT_METERS("heightAglM");

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

    public List<OnlineTransceiver> deserializeMultiple(JsonArray array, String section, ParserLogEntryCollector logCollector) {
        return JsonHelpers.processArraySkipOnError(//
            array, //
            JsonObject.class, //
            section, //
            logCollector, //
            x -> deserializeSingle(x, section, logCollector) //
        );
    }

    public OnlineTransceiver deserializeSingle(JsonObject object, String section, ParserLogEntryCollector logCollector) {
        OnlineTransceiver out = new OnlineTransceiver();

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.ID, //
            section, //
            logCollector, //
            out::setId //
        );

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.FREQUENCY, //
            section, //
            logCollector, //
            out::setFrequencyHertz //
        );

        JsonHelpers.processMandatory( //
            object::getDouble, //
            Key.LATITUDE, //
            section, //
            logCollector, //
            out::setLatitude //
        );

        JsonHelpers.processMandatory( //
            object::getDouble, //
            Key.LONGITUDE, //
            section, //
            logCollector, //
            out::setLongitude //
        );

        JsonHelpers.processMandatory( //
            object::getDouble, //
            Key.ALTITUDE_METERS, //
            section, //
            logCollector, //
            out::setAltitudeMeters //
        );

        JsonHelpers.processMandatory( //
            object::getDouble, //
            Key.HEIGHT_METERS, //
            section, //
            logCollector, //
            out::setHeightMeters //
        );

        return out;
    }

    // TODO: unit tests
}
