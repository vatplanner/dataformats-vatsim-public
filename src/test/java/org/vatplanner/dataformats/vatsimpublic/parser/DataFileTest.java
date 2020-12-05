package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link DataFile}.
 */
public class DataFileTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DataFile dataFile;

    @Before
    public void setUp() {
        dataFile = new DataFile();
    }

    @Test
    public void testGetParserLogEntries_nothingLogged_returnsEmptyCollection() {
        // Arrange (nothing to do)

        // Act
        Collection<ParserLogEntry> result = dataFile.getParserLogEntries();

        // Assert
        assertThat(result, is(emptyCollectionOf(ParserLogEntry.class)));
    }

    @Test
    public void testGetParserLogEntries_afterLogging_returnsLoggedEntries() {
        // Arrange
        ParserLogEntry mockLogEntry1 = mock(ParserLogEntry.class);
        ParserLogEntry mockLogEntry2 = mock(ParserLogEntry.class);

        dataFile.addParserLogEntry(mockLogEntry1);
        dataFile.addParserLogEntry(mockLogEntry2);

        // Act
        Collection<ParserLogEntry> result = dataFile.getParserLogEntries();

        // Assert
        assertThat(result, contains(mockLogEntry1, mockLogEntry2));
    }

    @Test
    public void testGetParserLogEntries_nothingLogged_returnsUnmodifiableCollection() {
        // Arrange
        ParserLogEntry mockLogEntry = mock(ParserLogEntry.class);

        thrown.expect(UnsupportedOperationException.class);

        // Act
        Collection<ParserLogEntry> result = dataFile.getParserLogEntries();

        // Assert (supposed to trigger the expected exception)
        result.add(mockLogEntry);
    }

    @Test
    public void testGetParserLogEntries_afterLogging_returnsUnmodifiableCollection() {
        // Arrange
        ParserLogEntry mockLogEntry1 = mock(ParserLogEntry.class);
        ParserLogEntry mockLogEntry2 = mock(ParserLogEntry.class);
        ParserLogEntry mockLogEntry3 = mock(ParserLogEntry.class);

        dataFile.addParserLogEntry(mockLogEntry1);
        dataFile.addParserLogEntry(mockLogEntry2);

        thrown.expect(UnsupportedOperationException.class);

        // Act
        Collection<ParserLogEntry> result = dataFile.getParserLogEntries();

        // Assert (supposed to trigger the expected exception)
        result.add(mockLogEntry3);
    }
}
