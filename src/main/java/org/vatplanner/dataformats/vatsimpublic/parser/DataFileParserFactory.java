package org.vatplanner.dataformats.vatsimpublic.parser;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory creating parsers handling {@link DataFile}s.
 */
public class DataFileParserFactory {
    private static final Map<DataFileFormat, Supplier<Parser<DataFile>>> parserSuppliersByFormat = new EnumMap<>(DataFileFormat.class);

    static {
        parserSuppliersByFormat.put(
            DataFileFormat.LEGACY,
            () -> new org.vatplanner.dataformats.vatsimpublic.parser.legacy.DataFileParser()
        );

        parserSuppliersByFormat.put(
            DataFileFormat.JSON3,
            () -> new org.vatplanner.dataformats.vatsimpublic.parser.json.v3.DataFileProcessor()
        );
    }

    /**
     * Creates a new parser handling the given {@link DataFileFormat}.
     *
     * @param format format to be handled
     * @return parser handling the given {@link DataFileFormat}
     * @throws IllegalArgumentException if no parser is known for the given format
     */
    public Parser<DataFile> createDataFileParser(DataFileFormat format) {
        Supplier<Parser<DataFile>> supplier = parserSuppliersByFormat.get(format);
        if (supplier == null) {
            throw new IllegalArgumentException("No parser for format " + format);
        }

        return supplier.get();
    }
}
