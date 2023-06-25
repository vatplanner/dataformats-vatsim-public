package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;

class DataFileFilterTest {

    @Test
    void testConstructor_nullConfiguration_throwsIllegalArgumentException() {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new DataFileFilter(null);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testConstructor_enabledRemoveStreamingChannels_throwsUnsupportedOperationException() {
        // FIXME: temporarily, remove when feature is implemented

        // Arrange
        DataFileFilterConfiguration configuration = //
            new DataFileFilterConfiguration()
                .setRemoveStreamingChannels(true);

        // Act
        ThrowingCallable action = () -> new DataFileFilter(configuration);

        // Assert
        assertThatThrownBy(action).isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 7, 9, 10})
    void testIsFormatVersionSupported_unsupported_returnsFalse(int formatVersion) {
        // Arrange
        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.isFormatVersionSupported(formatVersion);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testIsFormatVersionSupported_supported_returnsTrue() {
        // Arrange
        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.isFormatVersionSupported(8);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testCheckEqualMetadata_nullBoth_returnsTrue() {
        // Arrange
        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.checkEqualMetadata(null, null);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testCheckEqualMetadata_nullA_returnsFalse() {
        // Arrange
        DataFileMetaData mockMetaData = mock(DataFileMetaData.class);

        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.checkEqualMetadata(null, mockMetaData);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testCheckEqualMetadata_nullB_returnsFalse() {
        // Arrange
        DataFileMetaData mockMetaData = mock(DataFileMetaData.class);

        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.checkEqualMetadata(mockMetaData, null);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testCheckEqualMetadata_notNull_returnsObjectEquality(boolean expectedResult) {
        // Arrange
        AtomicReference<DataFileMetaData> holderA = new AtomicReference<>();
        AtomicReference<DataFileMetaData> holderB = new AtomicReference<>();

        DataFileMetaData a = new DataFileMetaData() {
            @Override
            public boolean equals(Object o) {
                assertThat(o).describedAs("object B must be provided to object A equals")
                             .isSameAs(holderB.get());
                return expectedResult;
            }
        };

        DataFileMetaData b = new DataFileMetaData() {
            @Override
            public boolean equals(Object o) {
                assertThat(o).describedAs("object A must be provided to object B equals")
                             .isSameAs(holderA.get());
                return expectedResult;
            }
        };

        holderA.set(a);
        holderB.set(b);

        DataFileFilter filter = createAnyFilterForIndependentMethods();

        // Act
        boolean result = filter.checkEqualMetadata(a, b);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    private DataFileFilter createAnyFilterForIndependentMethods() {
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setRemoveRealNameAndHomebase(true);

        return new DataFileFilter(configuration);
    }
}
