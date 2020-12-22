package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

import com.github.cliftonlabs.json_simple.JsonKey;

public class JsonHelpers {
    public static <T> Optional<T> getMandatory(Function<JsonKey, T> parentAccessor, JsonKey key, String section, ParserLogEntryCollector logCollector) {
        T object = parentAccessor.apply(key);

        if (object == null) {
            logCollector.addParserLogEntry(new ParserLogEntry(
                section, //
                "content at key " + key.getKey(), //
                true, //
                "key " + key.getKey() + " is undefined", //
                null //
            ));
        }

        return Optional.ofNullable(object);
    }

    public static <T> void processMandatory(Function<JsonKey, T> parentAccessor, JsonKey key, String section, ParserLogEntryCollector logCollector, Consumer<T> consumer) {
        T object = getMandatory(parentAccessor, key, section, logCollector).orElse(null);
        if (object == null) {
            return;
        }

        consumeSafelyLogging(object, key, section, logCollector, consumer);
    }

    public static <T, U> Optional<U> processMandatory(Function<JsonKey, T> parentAccessor, JsonKey key, String section, ParserLogEntryCollector logCollector, Function<T, U> function) {
        T object = getMandatory(parentAccessor, key, section, logCollector).orElse(null);
        if (object == null) {
            return Optional.empty();
        }

        return applySafelyLogging(object, key, section, logCollector, function);
    }

    public static <T> Optional<T> getMandatory(Function<JsonKey, ?> parentAccessor, JsonKey key, Class<T> targetClass, String section, ParserLogEntryCollector logCollector) {
        Object object = getMandatory(parentAccessor, key, section, logCollector).orElse(null);

        if (!targetClass.isInstance(object)) {
            logCollector.addParserLogEntry(new ParserLogEntry(
                section, //
                "content at key " + key.getKey(), //
                true, //
                "value for key " + key.getKey() + " is " + object.getClass().getCanonicalName() + ", expected "
                    + targetClass.getCanonicalName(), //
                null //
            ));
            return Optional.empty();
        }

        return Optional.of(targetClass.cast(object));
    }

    public static <T> void processMandatory(Function<JsonKey, ?> parentAccessor, JsonKey key, Class<T> targetClass, String section, ParserLogEntryCollector logCollector, Consumer<T> consumer) {
        T object = getMandatory(parentAccessor, key, targetClass, section, logCollector).orElse(null);
        if (object == null) {
            return;
        }

        consumeSafelyLogging(object, key, section, logCollector, consumer);
    }

    public static <T, U> Optional<U> processMandatory(Function<JsonKey, T> parentAccessor, JsonKey key, Class<T> targetClass, String section, ParserLogEntryCollector logCollector, Function<T, U> function) {
        T object = getMandatory(parentAccessor, key, targetClass, section, logCollector).orElse(null);
        if (object == null) {
            return Optional.empty();
        }

        return applySafelyLogging(object, key, section, logCollector, function);
    }

    private static <T, U> Optional<U> applySafelyLogging(T object, JsonKey key, String section, ParserLogEntryCollector logCollector, Function<T, U> function) {
        try {
            return Optional.of(function.apply(object));
        } catch (Exception ex) {
            logCollector.addParserLogEntry(new ParserLogEntry(
                section, //
                "content at key " + key.getKey(), //
                true, //
                "processing data for " + key.getKey() + " failed with " + ex.toString(), //
                ex //
            ));
            return Optional.empty();
        }
    }

    private static <T> void consumeSafelyLogging(T object, JsonKey key, String section, ParserLogEntryCollector logCollector, Consumer<T> consumer) {
        try {
            consumer.accept(object);
        } catch (Exception ex) {
            logCollector.addParserLogEntry(new ParserLogEntry(
                section, //
                "content at key " + key.getKey(), //
                true, //
                "processing data for " + key.getKey() + " failed with " + ex.toString(), //
                ex //
            ));
        }
    }

    // TODO: unit tests
}
