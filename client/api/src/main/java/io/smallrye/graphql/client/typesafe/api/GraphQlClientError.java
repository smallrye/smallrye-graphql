package io.smallrye.graphql.client.typesafe.api;

import java.util.List;
import java.util.Map;

public interface GraphQlClientError {
    String getMessage();

    List<SourceLocation> getLocations();

    // TODO ErrorClassification getErrorType()

    /** The path can be either String or Integer items */
    List<Object> getPath();

    Map<String, Object> getExtensions();

    default String getErrorCode() {
        Map<String, Object> extensions = getExtensions();
        if (extensions == null)
            return null;
        Object errorCode = extensions.get("code");
        return (errorCode == null) ? null : errorCode.toString();
    }

    /** we can't declare a default toString in an interface */
    default String defaultToString() {
        String errorCode = getErrorCode();
        List<Object> path = getPath();
        List<SourceLocation> locations = getLocations();
        Map<String, Object> extensions = getExtensions();
        return ((errorCode == null) ? "" : errorCode + ": ")
                + ((path == null) ? "" : path + " ")
                + getMessage()
                + ((locations == null) ? "" : " " + locations)
                + ((extensions == null || extensions.isEmpty()) ? "" : " " + extensions);
    }
}
