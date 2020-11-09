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
}
