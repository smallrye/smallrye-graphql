package io.smallrye.graphql.execution.error;

import java.util.HashMap;
import java.util.Map;

import io.smallrye.graphql.schema.model.ErrorInfo;

/**
 * Here we create a mapping of all error info that we know about
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ErrorInfoMap {

    private static final Map<String, ErrorInfo> errorInfoMap = new HashMap<>();

    private ErrorInfoMap() {
    }

    public static void register(Map<String, ErrorInfo> map) {
        if (map != null && !map.isEmpty()) {
            errorInfoMap.putAll(map);
        }
    }

    public static ErrorInfo getErrorInfo(String className) {
        if (errorInfoMap.containsKey(className)) {
            return errorInfoMap.get(className);
        }
        return null;
    }
}
