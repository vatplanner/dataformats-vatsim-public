package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataFileTest {

    private DataFile dataFile;

    @BeforeEach
    public void setUp() {
        dataFile = new DataFile();
    }

    @Test
    void testGetParserLogEntries_nothingLogged_returnsEmptyCollection() {
        // Arrange (nothing to do)

        // Act
        Collection<ParserLogEntry> result = dataFile.getParserLogEntries();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetParserLogEntries_afterLogging_returnsLoggedEntries() {
        // Arrange
        ParserLogEntry mockLogEntry1 = mock(ParserLogEntry.class);
        ParserLogEntry mockLogEntry2 = mock(ParserLogEntry.class);

        dataFile.addParserLogEntry(mockLogEntry1);
        dataFile.addParserLogEntry(mockLogEntry2);

        // Act
        Collection<ParserLogEntry> result = dataFile.getParserLogEntries();

        // Assert
        assertThat(result).containsExactly(mockLogEntry1, mockLogEntry2);
    }

    @Test
    void testGetParserLogEntries_nothingLogged_returnsUnmodifiableCollection() {
        // Arrange (nothing to do)

        // Act
        Collection<ParserLogEntry> result = dataFile.getParserLogEntries();

        // Assert
        assertThat(result).isUnmodifiable();
    }

    @Test
    void testGetParserLogEntries_afterLogging_returnsUnmodifiableCollection() {
        // Arrange
        ParserLogEntry mockLogEntry1 = mock(ParserLogEntry.class);
        ParserLogEntry mockLogEntry2 = mock(ParserLogEntry.class);

        dataFile.addParserLogEntry(mockLogEntry1);
        dataFile.addParserLogEntry(mockLogEntry2);

        // Act
        Collection<ParserLogEntry> result = dataFile.getParserLogEntries();

        // Assert
        assertThat(result).isUnmodifiable();
    }
}
