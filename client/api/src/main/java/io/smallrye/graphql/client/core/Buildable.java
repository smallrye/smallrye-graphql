package io.smallrye.graphql.client.core;

import io.smallrye.graphql.client.core.exceptions.BuildException;

public interface Buildable {
    String build() throws BuildException;
}
