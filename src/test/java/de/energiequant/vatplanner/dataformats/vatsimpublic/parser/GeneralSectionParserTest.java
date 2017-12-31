package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class GeneralSectionParserTest {
    @Test
    @DataProvider({"8", "123"})
    public void testParse_withVersion_returnsDataFileMetaDataWithExpectedFormatVersion(int expectedVersion) {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("VERSION = %d", expectedVersion)
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getVersionFormat(), is(equalTo(expectedVersion)));
    }
    
    @Test
    public void testParse_withoutVersion_returnsDataFileMetaDataWithNegativeValueForFormatVersion() {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("RELOAD = 123")
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getVersionFormat(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"2", "100"})
    public void testParse_withReload_returnsDataFileMetaDataWithExpectedMinimumDataFileRetrievalInterval(int minimumIntervalMinutes) {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("RELOAD = %d", minimumIntervalMinutes)
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getMinimumDataFileRetrievalInterval(), is(equalTo(Duration.ofMinutes(minimumIntervalMinutes))));
    }
    
    @Test
    public void testParse_withoutReload_returnsDataFileMetaDataWithNullForMinimumDataFileRetrievalInterval() {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("VERSION = 123")
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getMinimumDataFileRetrievalInterval(), is(nullValue()));
    }
    
    @Test
    @DataProvider({"5", "100"})
    public void testParse_withAtisAllowMin_returnsDataFileMetaDataWithExpectedMinimumAtisRetrievalInterval(int minimumIntervalMinutes) {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("ATIS ALLOW MIN = %d", minimumIntervalMinutes)
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getMinimumAtisRetrievalInterval(), is(equalTo(Duration.ofMinutes(minimumIntervalMinutes))));
    }
    
    @Test
    public void testParse_withoutAtisAllowMin_returnsDataFileMetaDataWithNullForMinimumAtisRetrievalInterval() {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("VERSION = 123")
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getMinimumAtisRetrievalInterval(), is(nullValue()));
    }
    
    @Test
    @DataProvider({"5", "100"})
    public void testParse_withConnectedClients_returnsDataFileMetaDataWithExpectedNumberOfConnectedClients(int connectedClients) {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("CONNECTED CLIENTS = %d", connectedClients)
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getNumberOfConnectedClients(), is(equalTo(connectedClients)));
    }
    
    @Test
    @DataProvider({"5", "100"})
    public void testParse_withoutConnectedClients_returnsDataFileMetaDataWithNegativeValueForNumberOfConnectedClients(int connectedClients) {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("VERSION = 123", connectedClients)
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getNumberOfConnectedClients(), is(lessThan(0)));
    }
    
    @Test
    @DataProvider({"20170625232105, 1498432865", "20161105184253, 1478371373"})
    public void testParse_withUpdate_returnsDataFileMetaDataWithExpectedTimestamp(String value, int valueEpochSeconds) {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("UPDATE = %s", value)
        );
        Instant expectedInstant = Instant.ofEpochSecond(valueEpochSeconds);
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getTimestamp(), is(equalTo(expectedInstant)));
    }
    
    @Test
    public void testParse_withoutUpdate_returnsDataFileMetaDataWithNullForTimestamp() {
        // Arrange
        GeneralSectionParser parser = new GeneralSectionParser();
        Collection<String> lines = Arrays.asList(
            String.format("VERSION = 123")
        );
        
        // Act
        DataFileMetaData result = parser.parse(lines);
        
        // Assert
        assertThat(result.getTimestamp(), is(nullValue()));
    }
}
