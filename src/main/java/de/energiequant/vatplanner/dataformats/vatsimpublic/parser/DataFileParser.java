package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses a complete VATSIM status data.txt file to {@link DataFile}.
 * File contents can be provided either as {@link CharSequence} (e.g. String) or
 * {@link BufferedReader}.
 * Parsing is thread-safe so one instance of a {@link DataFileParser} can be
 * reused multiple times, even in parallel.
 */
public class DataFileParser {
    private static final Pattern PATTERN_SECTION_HEAD = Pattern.compile("!([^:]+):");
    private static final int PATTERN_SECTION_HEAD_NAME = 1;
    
    private static final String PREFIX_COMMENT = ";";
    
    private static final String SECTION_NAME_GENERAL = "GENERAL";
    private static final String SECTION_NAME_CLIENTS = "CLIENTS";
    private static final String SECTION_NAME_PREFILE = "PREFILE";
    private static final String SECTION_NAME_SERVERS = "SERVERS";
    private static final String SECTION_NAME_VOICE_SERVERS = "VOICE SERVERS";
    
    GeneralSectionParser getGeneralSectionParser() {
        return new GeneralSectionParser();
    }
    
    ClientParser createClientParser() {
        return new ClientParser();
    }
    
    ClientParser getOnlineClientParser() {
        return createClientParser().setIsParsingPrefileSection(false);
    }
    
    ClientParser getPrefileClientParser() {
        return createClientParser().setIsParsingPrefileSection(true);
    }
    
    FSDServerParser getFSDServerParser() {
        return new FSDServerParser();
    }
    
    VoiceServerParser getVoiceServerParser() {
        return new VoiceServerParser();
    }
    
    /**
     * Parses a whole file given as the provided CharSequence (or String).
     * Original file is expected to have been read with ISO8859-1 character set.
     * @param s CharSequence containing the complete file to be parsed
     * @return all parsed information collected in one {@link DataFile} object
     */
    public DataFile parse(CharSequence s) {
        BufferedReader br = new BufferedReader(new StringReader(s.toString()));
        
        return parse(br);
    }
    
    /**
     * Wraps given parser function to catch and log exceptions into a {@link ParserLogEntryCollector}.
     * Caught exceptions result in null being returned instead.
     * @param <U> output type of wrapped function
     * @param wrappedFunction function to be wrapped by exception handling
     * @param section parser section related to wrapped function
     * @param collector collector for all generated {@link ParserLogEntry}s
     * @return function adding exception handling to original wrapped function
     */
    private <U> Function<String,U> logExceptionsFrom(Function<String,U> wrappedFunction, String section, ParserLogEntryCollector collector) {
        return (String line) -> {
            try {
                return wrappedFunction.apply(line);
            } catch (IllegalArgumentException ex) {
                ParserLogEntry logEntry = new ParserLogEntry(section, line, true, ex.getMessage(), ex);
                collector.addParserLogEntry(logEntry);
                return null;
            }
        };
    }
    
    /**
     * Generates a new {@link Predicate} checking for values being not null.
     * @param <T> type of objects to test
     * @return predicate testing objects type-safe (for use in streams)
     */
    private <T> Predicate<T> notNull() {
        return (T t) -> t != null;
    }
    
    /**
     * Parses a whole file by reading from the given {@link BufferedReader}.
     * Content is expected to have been opened with ISO8859-1 character set.
     * @param br {@link BufferedReader} providing access to the complete file contents to be parsed
     * @return all parsed information collected in one {@link DataFile} object
     */
    public DataFile parse(BufferedReader br) {
        // TODO: catch IllegalArgumentExceptions thrown by parsers (collect failed lines per section for analysis and indicate general presence/absence of errors)
        
        GeneralSectionParser generalSectionParser = getGeneralSectionParser();
        ClientParser onlineClientParser = getOnlineClientParser();
        ClientParser prefileClientParser = getPrefileClientParser();
        FSDServerParser fsdServerParser = getFSDServerParser();
        VoiceServerParser voiceServerParser = getVoiceServerParser();
        
        Map<String, List<String>> relevantLinesBySection = readRelevantLinesBySection(br);
        
        DataFile dataFile = new DataFile();
        dataFile.setMetaData(generalSectionParser.parse(relevantLinesBySection.get(SECTION_NAME_GENERAL)));

        // TODO: log warning if data format is unexpected (currently implemented format version 8)

        try {
            Stream<Client> onlineClientsStream = relevantLinesBySection.getOrDefault(SECTION_NAME_CLIENTS, new ArrayList<>()).stream()
                    .map(logExceptionsFrom(onlineClientParser::parse, SECTION_NAME_CLIENTS, dataFile))
                    .filter(notNull());
            
            Stream<Client> prefileClientsStream = relevantLinesBySection.getOrDefault(SECTION_NAME_PREFILE, new ArrayList<>()).stream()
                    .map(logExceptionsFrom(prefileClientParser::parse, SECTION_NAME_PREFILE, dataFile))
                    .filter(notNull());
            
            Stream<Client> allClientsStream = Stream.concat(onlineClientsStream, prefileClientsStream);
            dataFile.setClients(allClientsStream.collect(Collectors.toCollection(ArrayList::new)));

            dataFile.setFsdServers(
                    relevantLinesBySection.getOrDefault(SECTION_NAME_SERVERS, new ArrayList<>())
                    .stream()
                    .map(fsdServerParser::parse)
                    .collect(Collectors.toCollection(ArrayList::new))
            );

            dataFile.setVoiceServers(
                    relevantLinesBySection.getOrDefault(SECTION_NAME_VOICE_SERVERS, new ArrayList<>())
                    .stream()
                    .map(voiceServerParser::parse)
                    .collect(Collectors.toCollection(ArrayList::new))
            );
        } catch (IllegalArgumentException ex) {
            // TODO: replace by internal collector for erroneous lines
            DataFileMetaData metaData = dataFile.getMetaData();
            
            throw new IllegalArgumentException(String.format("Failed to parse data file, metadata: timestamp=%s, version=%d", metaData.getTimestamp(), metaData.getVersionFormat()), ex);
        }
        
        return dataFile;
    }

    /**
     * Reads all lines and groups those that are relevant by section in the
     * returned {@link Map}.
     * @param br {@link BufferedReader} providing the lines to be read
     * @return a map grouping all lines by section (key: section name, value: list of lines)
     */
    private Map<String, List<String>> readRelevantLinesBySection(BufferedReader br) {
        String currentSectionName;
        List<String> currentSectionLines = new ArrayList<>();
        Map<String, List<String>> relevantLinesBySection = new HashMap<>();
        
        Iterator<String> lineIterator = br.lines().iterator();
        while (lineIterator.hasNext()) {
            String line = lineIterator.next();
            
            if (isLineIrrelevant(line)) {
                continue;
            }
            
            Matcher matcher = PATTERN_SECTION_HEAD.matcher(line);
            if (!matcher.matches()) {
                // not a section change
                currentSectionLines.add(line);
            } else {
                // change of section
                currentSectionName = matcher.group(PATTERN_SECTION_HEAD_NAME);
                currentSectionLines = new ArrayList<>();
                relevantLinesBySection.put(currentSectionName, currentSectionLines);
            }
        }
        
        return relevantLinesBySection;
    }

    /**
     * Checks if the given line is relevant to parsers by data format.
     * Lines are irrelevant if they contain only a comment or only whitespace.
     * @param line line to be checked
     * @return Is the line irrelevant? (false = irrelevant, true = relevant)
     */
    private boolean isLineIrrelevant(String line) {
        return line.startsWith(PREFIX_COMMENT) || line.trim().isEmpty();
    }
}
