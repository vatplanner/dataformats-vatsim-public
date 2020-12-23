package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

import com.github.cliftonlabs.json_simple.JsonArray;
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

        consumeSafelyLogging(object, section, "key " + key.getKey(), logCollector, consumer);
    }

    public static <T, U> Optional<U> processMandatory(Function<JsonKey, T> parentAccessor, JsonKey key, String section, ParserLogEntryCollector logCollector, Function<T, U> function) {
        T object = getMandatory(parentAccessor, key, section, logCollector).orElse(null);
        if (object == null) {
            return Optional.empty();
        }

        return applySafelyLogging(object, section, "key " + key.getKey(), logCollector, function);
    }

    public static <T> Optional<T> getMandatory(Function<JsonKey, ?> parentAccessor, JsonKey key, Class<T> targetClass, String section, ParserLogEntryCollector logCollector) {
        Object object = getMandatory(parentAccessor, key, section, logCollector).orElse(null);

        return cast(object, targetClass, section, "key " + key.getKey(), logCollector);
    }

    private static <T> Optional<T> cast(Object object, Class<T> targetClass, String section, String location, ParserLogEntryCollector logCollector) {
        if (!targetClass.isInstance(object)) {
            logCollector.addParserLogEntry(new ParserLogEntry(
                section, //
                "content at " + location, //
                true, //
                "value for " + location + " is " + object.getClass().getCanonicalName() + ", expected "
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

        consumeSafelyLogging(object, section, "key " + key.getKey(), logCollector, consumer);
    }

    public static <T, U> Optional<U> processMandatory(Function<JsonKey, T> parentAccessor, JsonKey key, Class<T> targetClass, String section, ParserLogEntryCollector logCollector, Function<T, U> function) {
        T object = getMandatory(parentAccessor, key, targetClass, section, logCollector).orElse(null);
        if (object == null) {
            return Optional.empty();
        }

        return applySafelyLogging(object, section, "key " + key.getKey(), logCollector, function);
    }

    public static <T, U> List<U> processArraySkipOnError(JsonArray array, Class<T> itemTargetClass, String section, ParserLogEntryCollector logCollector, Function<T, U> function) {
        List<U> out = new ArrayList<U>();

        int i = -1;
        for (Object item : array) {
            i++;
            String location = "index " + i;
            T itemCast = cast(item, itemTargetClass, section, location, logCollector).orElse(null);
            if (itemCast != null) {
                applySafelyLogging(itemCast, section, location, logCollector, function) //
                    .ifPresent(out::add);
            }
        }

        return out;
    }

    private static <T, U> Optional<U> applySafelyLogging(T object, String section, String location, ParserLogEntryCollector logCollector, Function<T, U> function) {
        try {
            return Optional.of(function.apply(object));
        } catch (Exception ex) {
            logCollector.addParserLogEntry(new ParserLogEntry(
                section, //
                "content at " + location, //
                true, //
                "processing data for " + location + " failed with " + ex.toString(), //
                ex //
            ));
            return Optional.empty();
        }
    }

    private static <T> void consumeSafelyLogging(T object, String section, String location, ParserLogEntryCollector logCollector, Consumer<T> consumer) {
        try {
            consumer.accept(object);
        } catch (Exception ex) {
            logCollector.addParserLogEntry(new ParserLogEntry(
                section, //
                "content at " + location, //
                true, //
                "processing data for " + location + " failed with " + ex.toString(), //
                ex //
            ));
        }
    }

    // TODO: unit tests
}
