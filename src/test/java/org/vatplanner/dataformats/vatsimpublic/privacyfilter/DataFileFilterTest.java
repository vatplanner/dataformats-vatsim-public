package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import org.vatplanner.dataformats.vatsimpublic.testutils.Holder;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

@RunWith(DataProviderRunner.class)
public class DataFileFilterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor_nullConfiguration_throwsIllegalArgumentException() {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        new DataFileFilter(null);

        // Assert (nothing to do)
    }

    @Test
    public void testConstructor_enabledRemoveStreamingChannels_throwsUnsupportedOperationException() {
        // FIXME: temporarily, remove when feature is implemented

        // Arrange
        DataFileFilterConfiguration configuration = //
            new DataFileFilterConfiguration()
                .setRemoveStreamingChannels(true);

        thrown.expect(UnsupportedOperationException.class);

        // Act
        new DataFileFilter(configuration);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({ "-1", "0", "1", "7", "9", "10" })
    public void testIsFormatVersionSupported_unsupported_returnsFalse(int formatVersion) {
        // Arrange
        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.isFormatVersionSupported(formatVersion);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testIsFormatVersionSupported_supported_returnsTrue() {
        // Arrange
        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.isFormatVersionSupported(8);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    public void testCheckEqualMetadata_nullBoth_returnsTrue() {
        // Arrange
        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.checkEqualMetadata(null, null);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    public void testCheckEqualMetadata_nullA_returnsFalse() {
        // Arrange
        DataFileMetaData mockMetaData = mock(DataFileMetaData.class);

        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.checkEqualMetadata(null, mockMetaData);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testCheckEqualMetadata_nullB_returnsFalse() {
        // Arrange
        DataFileMetaData mockMetaData = mock(DataFileMetaData.class);

        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.checkEqualMetadata(mockMetaData, null);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    @DataProvider({ "true", "false" })
    public void testCheckEqualMetadata_notNull_returnsObjectEquality(boolean expectedResult) {
        // Arrange
        Holder<DataFileMetaData> holderA = new Holder<>();
        Holder<DataFileMetaData> holderB = new Holder<>();

        DataFileMetaData a = new DataFileMetaData() {
            @Override
            public boolean equals(Object o) {
                assertThat("object B must be provided to object A equals", o, is(sameInstance(holderB.value)));
                return expectedResult;
            }
        };

        DataFileMetaData b = new DataFileMetaData() {
            @Override
            public boolean equals(Object o) {
                assertThat("object A must be provided to object B equals", o, is(sameInstance(holderA.value)));
                return expectedResult;
            }
        };

        holderA.value = a;
        holderB.value = b;

        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.checkEqualMetadata(a, b);

        // Assert
        assertThat(result, is(expectedResult));
    }

    private DataFileFilter createAnyFilterForIndependentMethods() {
        DataFileFilterConfiguration configuration = //
            new DataFileFilterConfiguration() //
                .setRemoveRealNameAndHomebase(true);
        return new DataFileFilter(configuration);
    }

}
