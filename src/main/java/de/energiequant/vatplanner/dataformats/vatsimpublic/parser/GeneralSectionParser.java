package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses meta information about a VATSIM data.txt status file as available from
 * section <code>GENERAL</code>. Usually, {@link DataFileParser} should be used
 * to have this information parsed from an actual complete {@link DataFile}.
 */
public class GeneralSectionParser {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final Pattern PATTERN_KEYVALUE = Pattern.compile("([^=]+) = (.+)");
    private static final int PATTERN_KEYVALUE_KEY = 1;
    private static final int PATTERN_KEYVALUE_VALUE = 2;

    private static final String KEY_VERSION = "VERSION";
    private static final String KEY_RELOAD = "RELOAD";
    private static final String KEY_ATIS_ALLOW_MIN = "ATIS ALLOW MIN";
    private static final String KEY_CONNECTED_CLIENTS = "CONNECTED CLIENTS";
    private static final String KEY_UPDATE = "UPDATE";

    /**
     * Parses all information from the given lines to a {@link DataFileMetaData}
     * object. All lines are expected to contain the proper syntax used by
     * VATSIM data.txt files and not to be empty or a comment.
     *
     * @param lines lines to be parsed; lines must not be empty, comments or
     * null
     * @param logEntryCollector collecting log entries produced by parsing the
     * given lines
     * @param sectionName section name as read from data file, used to identify
     * log messages
     * @return all parsed data in a {@link DataFileMetaData} object
     */
    public DataFileMetaData parse(Collection<String> lines, ParserLogEntryCollector logEntryCollector, String sectionName) {
        DataFileMetaData metaData = new DataFileMetaData();

        if ((lines == null) || lines.isEmpty()) {
            logEntryCollector.addParserLogEntry(new ParserLogEntry(sectionName, null, true, "meta data is missing or empty", null));
            return metaData;
        }

        for (String line : lines) {
            Matcher matcher = PATTERN_KEYVALUE.matcher(line);
            if (matcher.matches()) {
                String key = matcher.group(PATTERN_KEYVALUE_KEY);
                String value = matcher.group(PATTERN_KEYVALUE_VALUE);

                switch (key) {
                    case KEY_VERSION:
                        metaData.setVersionFormat(Integer.parseInt(value));
                        break;

                    case KEY_RELOAD:
                        metaData.setMinimumDataFileRetrievalInterval(Duration.ofMinutes(Integer.parseInt(value)));
                        break;

                    case KEY_ATIS_ALLOW_MIN:
                        metaData.setMinimumAtisRetrievalInterval(Duration.ofMinutes(Integer.parseInt(value)));
                        break;

                    case KEY_CONNECTED_CLIENTS:
                        metaData.setNumberOfConnectedClients(Integer.parseInt(value));
                        break;

                    case KEY_UPDATE:
                        metaData.setTimestamp(DATE_TIME_FORMATTER.parse(value, LocalDateTime::from).toInstant(ZoneOffset.UTC));
                        break;

                    default:
                        logEntryCollector.addParserLogEntry(
                                new ParserLogEntry(
                                        sectionName,
                                        line,
                                        true,
                                        "key " + key + "is unknown and could not be parsed",
                                        null
                                )
                        );
                        break;
                }
            }
        }

        return metaData;
    }
}
