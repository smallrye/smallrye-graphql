package io.smallrye.graphql.client.mpapi.core;

import io.smallrye.graphql.client.mpapi.core.exceptions.BuildException;

public interface Buildable {
    String build() throws BuildException;
}
