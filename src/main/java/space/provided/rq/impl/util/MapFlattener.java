package space.provided.rq.impl.util;

import java.util.HashMap;
import java.util.Map;

public final class MapFlattener {

    public static Map<String, String> flatten(Map<String, Object> map) {
        Map<String, String> result = new HashMap<>();
        flattenMapHelper("", map, result);
        return result;
    }

    private static void flattenMapHelper(String prefix, Map<String, Object> map, Map<String, String> result) {
        map.forEach((key, value) -> putItem(value, prefix + key, result));
    }

    private static void putItem(Object value, String key, Map<String, String> result) {
        if (value instanceof Map) {
            flattenMapHelper(key + ".", (Map<String, Object>) value, result);
            return;
        }

        if (value instanceof Object[]) {
            final Object[] items = (Object[]) value;
            for (int i = 0; i < items.length; i++) {
                putItem(items[i], key + "." + i, result);
            }
            return;
        }

        result.put(key, value.toString());
    }
}
