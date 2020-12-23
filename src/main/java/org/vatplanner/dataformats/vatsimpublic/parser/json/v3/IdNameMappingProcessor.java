package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * Generic processing of mappings with a structure of:
 * 
 * <ul>
 * <li><code>id</code> - integer representing a supposedly unique ID used to
 * refer to the described entry from other JSON objects</li>
 * <li><code>short</code> - a short name (or key) which is used to uniquely
 * resolve types used internally by this library to the described entry</li>
 * <li><code>long</code> - a long name/description, only used by the processor
 * on error messages</li>
 * </ul>
 */
public class IdNameMappingProcessor {
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

    private class JsonEntry {
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
            return "JsonEntry[id=" + id + ", shortName=" + shortName + ", longName=" + longName + "]";
        }
    }

    /**
     * Deserializes the mapping described by given <code>array</code> to
     * <code>expectedValues</code> using the provided <code>resolveShortName</code>
     * {@link Function} for lookups to a {@link Map} linking the read numeric IDs to
     * given values.
     * 
     * <p>
     * {@link ParserLogEntry}s will be logged if data cannot be fully read:
     * </p>
     * 
     * <ul>
     * <li>if deserialization of individual JSON entries fails</li>
     * <li>if resolution of a <code>short</code> name fails</li>
     * <li>if multiple entries refer to the same <code>id</code></li>
     * <li>if an expected value was not mapped</li>
     * </ul>
     * 
     * @param <T> type of values to resolve to
     * @param array a {@link JsonArray} holding objects of required structure; see
     *        class JavaDoc
     * @param resolveShortName {@link Function} used to resolve a short name to an
     *        expected value
     * @param expectedValues all values expected to be mapped
     * @param section description of section being worked on
     * @param logCollector receives {@link ParserLogEntry}s for any issues being
     *        found
     * @return mapping from <code>array</code> item's <code>id</code> to resolved
     *         values <code>T</code>
     */
    public <T> Map<Integer, T> deserializeMappingFromJsonId(JsonArray array, Function<String, T> resolveShortName, T[] expectedValues, String section, ParserLogEntryCollector logCollector) {
        Map<Integer, T> out = new HashMap<Integer, T>();

        List<JsonEntry> jsonEntries = deserializeMultiple(array, section, logCollector);
        for (JsonEntry jsonEntry : jsonEntries) {
            T resolvedValue = resolveShortName.apply(jsonEntry.shortName);
            if (resolvedValue == null) {
                logCollector.addParserLogEntry(new ParserLogEntry( //
                    section, //
                    jsonEntry.toString(), //
                    true, //
                    "JSON object with short name " + jsonEntry.shortName + " could not be resolved", //
                    null //
                ));
                continue;
            }

            T previousResolvedValue = out.put(jsonEntry.id, resolvedValue);
            if (previousResolvedValue != null) {
                logCollector.addParserLogEntry(new ParserLogEntry( //
                    section, //
                    jsonEntry.toString(), //
                    true, //
                    "JSON object with short name " + jsonEntry.shortName + " has same ID " + jsonEntry.id
                        + " as previous resolved value " + previousResolvedValue
                        + "; previous entry has been overwritten", //
                    null //
                ));
            }
        }

        Collection<T> mappedValues = out.values();
        for (T expectedValue : expectedValues) {
            if (!mappedValues.contains(expectedValue)) {
                logCollector.addParserLogEntry(new ParserLogEntry( //
                    section, //
                    "check for completion", //
                    true, //
                    "missing mapping for expected value " + expectedValue + ", value will not be used", //
                    null //
                ));
            }
        }

        return out;
    }

    private List<JsonEntry> deserializeMultiple(JsonArray array, String section, ParserLogEntryCollector logCollector) {
        return JsonHelpers.processArraySkipOnError(//
            array, //
            JsonObject.class, //
            section, //
            logCollector, //
            x -> deserializeSingle(x, section, logCollector) //
        );
    }

    private JsonEntry deserializeSingle(JsonObject object, String section, ParserLogEntryCollector logCollector) {
        JsonEntry out = new JsonEntry();

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.ID, //
            section, //
            logCollector, //
            out::setId //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.SHORT_NAME, //
            section, //
            logCollector, //
            out::setShortName //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.LONG_NAME, //
            section, //
            logCollector, //
            out::setLongName //
        );

        return out;
    }

    // TODO: unit tests
}
