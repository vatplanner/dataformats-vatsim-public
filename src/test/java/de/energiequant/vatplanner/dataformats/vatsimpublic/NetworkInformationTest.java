package de.energiequant.vatplanner.dataformats.vatsimpublic;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.Matchers.*;
import org.hamcrest.junit.ExpectedException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class NetworkInformationTest {
    private static final Logger logger = Logger.getLogger(NetworkInformationTest.class.getName());
    TestLogger testLogger = TestLoggerFactory.getTestLogger(NetworkInformation.class);
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    /*
    @Before
    public void clearLog() {
        testLogger.clearAll();
    }
    */
    
    @Test
    public void testGetMessagesStartup_initially_listIsUnmodifiable() {
        NetworkInformation info = new NetworkInformation();
        
        List<String> startupMessages = info.getStartupMessages();
        thrown.expect(UnsupportedOperationException.class);
        startupMessages.add("test");
    }
}
