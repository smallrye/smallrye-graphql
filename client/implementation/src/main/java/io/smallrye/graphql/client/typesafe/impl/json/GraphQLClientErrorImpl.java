package io.smallrye.graphql.client.typesafe.impl.json;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientError;
import io.smallrye.graphql.client.typesafe.api.SourceLocation;

class GraphQLClientErrorImpl implements GraphQLClientError {
    private String message;
    private List<SourceLocation> locations;
    private List<Object> path;
    private Map<String, Object> extensions;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return locations;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLocations(List<SourceLocation> locations) {
        this.locations = locations;
    }

    public void setPath(List<Object> path) {
        this.path = path;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GraphQLClientErrorImpl that = (GraphQLClientErrorImpl) o;
        return Objects.equals(message, that.message) &&
                Objects.equals(locations, that.locations) &&
                Objects.equals(path, that.path) &&
                Objects.equals(extensions, that.extensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, locations, path, extensions);
    }

    @Override
    public String toString() {
        return defaultToString();
    }
}
