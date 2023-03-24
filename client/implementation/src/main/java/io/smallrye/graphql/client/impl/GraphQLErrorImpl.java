package io.smallrye.graphql.client.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.smallrye.graphql.client.GraphQLError;

public class GraphQLErrorImpl implements GraphQLError {
    private String message;
    private List<Map<String, Integer>> locations;
    private Object[] path;
    private Map<String, Object> extensions;
    private Map<String, Object> otherFields;

    public GraphQLErrorImpl() {
    }

    public GraphQLErrorImpl(String message, List<Map<String, Integer>> locations, Object[] path, Map<String, Object> extensions,
            Map<String, Object> otherFields) {
        this.message = message;
        this.locations = locations;
        this.path = path;
        this.extensions = extensions;
        this.otherFields = otherFields;
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

    @Override
    public Map<String, Object> getOtherFields() {
        return otherFields;
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

    public void setOtherFields(Map<String, Object> otherFields) {
        this.otherFields = otherFields;
    }

    @Override
    public String toString() {
        String other = (otherFields == null || otherFields.isEmpty()) ? "" : ", otherFields=" + otherFields;
        return "Error{message=" + message +
                ", locations=" + locations +
                ", path=" + Arrays.toString(path) +
                ", extensions=" + extensions +
                other + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GraphQLErrorImpl that = (GraphQLErrorImpl) o;
        return Objects.equals(message, that.message) && Objects.equals(locations, that.locations)
                && Arrays.equals(path, that.path) && Objects.equals(extensions, that.extensions)
                && Objects.equals(otherFields, that.otherFields);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(message, locations, extensions, otherFields);
        result = 31 * result + Arrays.hashCode(path);
        return result;
    }
}
