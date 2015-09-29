package com.getbase.hackkrk.tanks.server.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@UtilityClass
public class CollectionUtils {

    public static <T> Optional<T> nth(List<T> list, int index) {
        if (index < 0 || index >= list.size()) {
            return Optional.empty();
        }

        return Optional.of(list.get(index));
    }

    public static <T> Optional<T> last(List<T> list) {
        return nth(list, list.size() - 1);
    }

    public static <T> Optional<T> random(List<T> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        }

        final int index = ThreadLocalRandom.current().nextInt(list.size());
        return nth(list, index);
    }

    public static <K, V> Map<K, V> expandKeys(Map<String, V> map, Function<String, K> keyExpander) {
        return transformKeys(map, keyExpander);
    }

    public static <K, V> Map<String, V> unexpandKeys(Map<K, V> map, Function<K, String> keyUnexpander) {
        return transformKeys(map, keyUnexpander);
    }

    private static <K1, K2, V> Map<K2, V> transformKeys(Map<K1, V> map, Function<K1, K2> keyTransformer) {
        return map
                .entrySet()
                .stream()
                .collect(toMap(
                        e -> keyTransformer.apply(e.getKey()),
                        Map.Entry::getValue,
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        () -> new TreeMap<K2, V>()));
    }
}
