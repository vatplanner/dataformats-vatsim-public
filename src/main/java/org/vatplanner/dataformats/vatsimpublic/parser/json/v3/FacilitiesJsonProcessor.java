package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class FacilitiesJsonProcessor {
    static final String SECTION_NAME = "facilities";

    private static enum Key implements JsonKey {
        ID("id"),
        SHORT_NAME("short"),
        LONG_NAME("long");

        private String key;

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

    private class JsonFacility {
        private int id;
        private String shortName;
        private String longName;

        private void setId(int id) {
            this.id = id;
        }

        private void setShortName(String shortName) {
            this.shortName = shortName;
        }

        private void setLongName(String longName) {
            this.longName = longName;
        }

        @Override
        public String toString() {
            return "JsonFacility[id=" + id + ", shortName=" + shortName + ", longName=" + longName + "]";
        }
    }

    public Map<Integer, FacilityType> deserializeMappingFromJsonIdToEnum(JsonArray array, ParserLogEntryCollector logCollector) {
        Map<Integer, FacilityType> out = new HashMap<Integer, FacilityType>();

        List<JsonFacility> jsonFacilities = deserializeMultiple(array, logCollector);
        for (JsonFacility jsonFacility : jsonFacilities) {
            FacilityType enumFacility = FacilityType.resolveShortName(jsonFacility.shortName);
            if (enumFacility == null) {
                logCollector.addParserLogEntry(new ParserLogEntry( //
                    SECTION_NAME, //
                    jsonFacility.toString(), //
                    true, //
                    "JSON object with short name " + jsonFacility.shortName + " could not be resolved to facility type", //
                    null //
                ));
                continue;
            }

            FacilityType previousEnumFacility = out.put(jsonFacility.id, enumFacility);
            if (previousEnumFacility != null) {
                logCollector.addParserLogEntry(new ParserLogEntry( //
                    SECTION_NAME, //
                    jsonFacility.toString(), //
                    true, //
                    "JSON object with short name " + jsonFacility.shortName + " has same ID " + jsonFacility.id
                        + " as previous facility type " + previousEnumFacility
                        + "; previous entry has been overwritten", //
                    null //
                ));
            }
        }

        Collection<FacilityType> mappedFacilities = out.values();
        for (FacilityType enumFacility : FacilityType.values()) {
            if (!mappedFacilities.contains(enumFacility)) {
                logCollector.addParserLogEntry(new ParserLogEntry( //
                    SECTION_NAME, //
                    "check for completion", //
                    true, //
                    "missing mapping for facility type " + enumFacility + ", facility type will be unavailable", //
                    null //
                ));
            }
        }

        return out;
    }

    public List<JsonFacility> deserializeMultiple(JsonArray array, ParserLogEntryCollector logCollector) {
        return JsonHelpers.processArraySkipOnError(//
            array, //
            JsonObject.class, //
            SECTION_NAME, //
            logCollector, //
            x -> deserializeSingle(x, logCollector) //
        );
    }

    public JsonFacility deserializeSingle(JsonObject object, ParserLogEntryCollector logCollector) {
        JsonFacility out = new JsonFacility();

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.ID, //
            SECTION_NAME, //
            logCollector, //
            out::setId //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.SHORT_NAME, //
            SECTION_NAME, //
            logCollector, //
            out::setShortName //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.LONG_NAME, //
            SECTION_NAME, //
            logCollector, //
            out::setLongName //
        );

        return out;
    }

    // TODO: unit tests
}
