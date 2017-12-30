package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
     * @param s CharSequence containing the complete file to be parsed
     * @return all parsed information collected in one {@link DataFile} object
     */
    public DataFile parse(CharSequence s) {
        BufferedReader br = new BufferedReader(new StringReader(s.toString()));
        
        return parse(br);
    }
    
    /**
     * Parses a whole file by reading from the given {@link BufferedReader}.
     * @param br {@link BufferedReader} providing access to the complete file contents to be parsed
     * @return all parsed information collected in one {@link DataFile} object
     */
    public DataFile parse(BufferedReader br) {
        GeneralSectionParser generalSectionParser = getGeneralSectionParser();
        ClientParser onlineClientParser = getOnlineClientParser();
        ClientParser prefileClientParser = getPrefileClientParser();
        FSDServerParser fsdServerParser = getFSDServerParser();
        VoiceServerParser voiceServerParser = getVoiceServerParser();
        
        Map<String, List<String>> relevantLinesBySection = readRelevantLinesBySection(br);
        
        DataFile dataFile = new DataFile();
        dataFile.setMetaData(generalSectionParser.parse(relevantLinesBySection.get(SECTION_NAME_GENERAL)));
        
        Stream<Client> onlineClientsStream = relevantLinesBySection.getOrDefault(SECTION_NAME_CLIENTS, new ArrayList<>()).stream().map(onlineClientParser::parse);
        Stream<Client> prefileClientsStream = relevantLinesBySection.getOrDefault(SECTION_NAME_PREFILE, new ArrayList<>()).stream().map(prefileClientParser::parse);
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
