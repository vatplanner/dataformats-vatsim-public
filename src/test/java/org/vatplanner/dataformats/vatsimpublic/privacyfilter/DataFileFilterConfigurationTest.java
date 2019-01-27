package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import java.util.ArrayList;
import java.util.Collection;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DataFileFilterConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSetFlightPlanRemarksRemoveAllIfContaining_null_throwsIllegalArgumentException() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        thrown.expect(IllegalArgumentException.class);

        // Act
        configuration.setFlightPlanRemarksRemoveAllIfContaining(null);

        // Assert (nothing to do)
    }

    @Test
    public void testGetFlightPlanRemarksRemoveAllIfContaining_setNull_returnsNotNull() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        try {
            configuration.setFlightPlanRemarksRemoveAllIfContaining(null);
        } catch (Exception ex) {
            // ignore
        }

        // Act
        Collection<String> result = configuration.getFlightPlanRemarksRemoveAllIfContaining();

        // Assert
        assertThat(result, is(not(nullValue())));
    }

    @Test
    public void testSetFlightPlanRemarksRemoveAllIfContaining_emptyList_doesNotThrowAnyException() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();
        ArrayList<String> emptyList = new ArrayList<>();

        // Act
        configuration.setFlightPlanRemarksRemoveAllIfContaining(emptyList);

        // Assert (nothing to do)
    }
}
