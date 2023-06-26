package org.vatplanner.dataformats.vatsimpublic.parser.json.onlinetransceivers;

import java.util.List;
import java.util.function.Function;

import org.vatplanner.dataformats.vatsimpublic.parser.OnlineTransceiver;
import org.vatplanner.dataformats.vatsimpublic.parser.OnlineTransceiverStation;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.json.JsonHelpers;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class OnlineTransceiverStationProcessor {
    private static String SECTION_NAME = "station";

    private static enum Key implements JsonKey {
        CALLSIGN("callsign"),
        TRANSCEIVERS("transceivers");

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

    public List<OnlineTransceiverStation> deserializeMultiple(JsonArray array, ParserLogEntryCollector logCollector) {
        return JsonHelpers.processArraySkipOnError(
            array,
            JsonObject.class,
            SECTION_NAME,
            logCollector,
            x -> deserializeSingle(x, logCollector)
        );
    }

    public OnlineTransceiverStation deserializeSingle(JsonObject object, ParserLogEntryCollector logCollector) {
        OnlineTransceiverProcessor transceiverProcessor = new OnlineTransceiverProcessor();

        OnlineTransceiverStation out = new OnlineTransceiverStation();

        JsonHelpers.processMandatory(
            object::getString,
            Key.CALLSIGN,
            SECTION_NAME,
            logCollector,
            out::setCallsign
        );

        String location = SECTION_NAME + " " + out.getCallsign();

        JsonHelpers.processMandatory(
            object::getCollection,
            Key.TRANSCEIVERS,
            JsonArray.class,
            location,
            logCollector,
            (Function<JsonArray, List<OnlineTransceiver>>) (array -> {
                return transceiverProcessor.deserializeMultiple(array, location, logCollector);
            })
        ).ifPresent(out::setTransceivers);

        return out;
    }

    // TODO: unit tests
}
