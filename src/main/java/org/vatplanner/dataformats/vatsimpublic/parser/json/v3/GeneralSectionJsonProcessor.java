package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;

import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.json.JsonHelpers;

import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class GeneralSectionJsonProcessor {
    private static final DateTimeFormatter UPDATE_DTF = new DateTimeFormatterBuilder() //
        .appendPattern("yyyyMMddHHmmss") //
        .toFormatter() //
        .withZone(ZoneOffset.UTC);

    static final String SECTION_NAME = "general";

    private static final Duration TIMESTAMP_DIFFERENCE_WARNING_THRESHOLD = Duration.ofSeconds(1);

    private static enum Key implements JsonKey {
        CONNECTED_CLIENTS("connected_clients"),
        RELOAD("reload"),
        UNIQUE_USERS("unique_users"),
        UPDATE("update"),
        UPDATE_TIMESTAMP("update_timestamp"),
        VERSION("version");

        private final String key;
        private final Object defaultValue;

        private Key(String key) {
            this.key = key;
            this.defaultValue = null;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return defaultValue;
        }
    }

    public DataFileMetaData deserialize(JsonObject generalRoot, ParserLogEntryCollector logCollector) {
        DataFileMetaData out = new DataFileMetaData();

        JsonHelpers.getMandatory(generalRoot::getInteger, Key.CONNECTED_CLIENTS, SECTION_NAME, logCollector)
            .ifPresent(out::setNumberOfConnectedClients);

        JsonHelpers.getMandatory(generalRoot::getInteger, Key.RELOAD, SECTION_NAME, logCollector)
            .ifPresent(x -> out.setMinimumDataFileRetrievalInterval(Duration.ofMinutes(x)));

        JsonHelpers.getMandatory(generalRoot::getInteger, Key.UNIQUE_USERS, SECTION_NAME, logCollector)
            .ifPresent(out::setNumberOfUniqueConnectedUsers);

        getUpdateTimestamp(generalRoot, logCollector) //
            .ifPresent(out::setTimestamp);

        JsonHelpers.getMandatory(generalRoot::getInteger, Key.VERSION, SECTION_NAME, logCollector)
            .ifPresent(out::setVersionFormat);

        return out;
    }

    private Instant parseCustomTimestamp(String s) {
        return UPDATE_DTF.parse(s, Instant::from);
    }

    /**
     * Returns the best available update timestamp.
     * <p>
     * The same timestamp is expected twice: {@link Key#UPDATE} holds a
     * custom-format UTC timestamp accurate to seconds while
     * {@link Key#UPDATE_TIMESTAMP} may provide even higher accuracy.
     * </p>
     * <p>
     * A {@link ParserLogEntry} will be logged if any timestamp is missing. The
     * other timestamp will be returned instead.
     * </p>
     * <p>
     * ISO timestamp is preferred over custom-format.
     * </p>
     * <p>
     * If both timestamps are present and a significant difference should be found
     * ({@link #TIMESTAMP_DIFFERENCE_WARNING_THRESHOLD}), a {@link ParserLogEntry}
     * will be logged. The ISO timestamp will be trusted in that case.
     * </p>
     * 
     * @param generalRoot JSON object of general section
     * @param logCollector receives log
     * @return
     */
    private Optional<Instant> getUpdateTimestamp(JsonObject generalRoot, ParserLogEntryCollector logCollector) {
        Optional<Instant> customTimestamp = JsonHelpers.processMandatory(generalRoot::getString, Key.UPDATE,
            SECTION_NAME, logCollector, this::parseCustomTimestamp);
        Optional<Instant> isoTimestamp = JsonHelpers.processMandatory(generalRoot::getString, Key.UPDATE_TIMESTAMP,
            SECTION_NAME, logCollector, Instant::parse);

        if (!customTimestamp.isPresent() && isoTimestamp.isPresent()) {
            logCollector.addParserLogEntry(new ParserLogEntry( //
                SECTION_NAME, //
                generalRoot.toJson(), //
                false, //
                "custom-format update timestamp (" + Key.UPDATE.getKey() + ") is missing, using ISO timestamp", //
                null //
            ));
            return isoTimestamp;
        } else if (customTimestamp.isPresent() && !isoTimestamp.isPresent()) {
            logCollector.addParserLogEntry(new ParserLogEntry( //
                SECTION_NAME, //
                generalRoot.toJson(), //
                false, //
                "ISO update timestamp (" + Key.UPDATE_TIMESTAMP.getKey()
                    + ") is missing, using custom-format timestamp", //
                null //
            ));
            return customTimestamp;
        } else if (!customTimestamp.isPresent() && !isoTimestamp.isPresent()) {
            logCollector.addParserLogEntry(new ParserLogEntry( //
                SECTION_NAME, //
                generalRoot.toJson(), //
                true, //
                "update timestamps (" + Key.UPDATE.getKey() + " and " + Key.UPDATE_TIMESTAMP.getKey()
                    + ") are both missing, unable to tell what time the data file was generated at", //
                null //
            ));
            return Optional.empty();
        }

        // if we got here both timestamps are set and should be compared

        Duration difference = Duration.between(customTimestamp.get(), isoTimestamp.get());
        if (difference.abs().compareTo(TIMESTAMP_DIFFERENCE_WARNING_THRESHOLD) > 0) {
            // TODO: add error flags/categories to log entries to indicate machine-readable
            // that timestamp is unreliable
            logCollector.addParserLogEntry(new ParserLogEntry( //
                SECTION_NAME, //
                generalRoot.toJson(), //
                false, //
                "update timestamps are inconsistent (custom-format " + Key.UPDATE.getKey() + " = "
                    + customTimestamp.get() + ", ISO format " + Key.UPDATE_TIMESTAMP.getKey()
                    + " = " + isoTimestamp.get() + "), will continue with ISO timestamp", //
                null //
            ));
        }

        return isoTimestamp;
    }

    // TODO: unit tests
}
