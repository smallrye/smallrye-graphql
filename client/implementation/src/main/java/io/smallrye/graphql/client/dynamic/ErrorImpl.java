package io.smallrye.graphql.client.dynamic;

import java.util.List;
import java.util.Map;

import io.smallrye.graphql.client.Error;

public class ErrorImpl implements Error {
    private String message;
    private List<Map<String, Integer>> locations;
    private Object[] path;
    private Map<String, Object> extensions;

    public ErrorImpl() {
    }

    public ErrorImpl(String message, List<Map<String, Integer>> locations, Object[] path, Map<String, Object> extensions) {
        this.message = message;
        this.locations = locations;
        this.path = path;
        this.extensions = extensions;
    }

    public String getMessage() {
        return message;
    }

    public List<Map<String, Integer>> getLocations() {
        return locations;
    }

    public Object[] getPath() {
        return path;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLocations(List<Map<String, Integer>> locations) {
        this.locations = locations;
    }

    public void setPath(Object[] path) {
        this.path = path;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        return "GraphQLError{" + "message=" + message + ", locations=" + locations + ", path=" + path + ", extensions="
                + extensions + '}';
    }
}
