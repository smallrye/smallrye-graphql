package io.smallrye.graphql.test;

import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Type;

@Type("TestSourceWithConfiguration")
public record TestSourceWithConfiguration(@NonNull TestSourceConfiguration configuration) {
}
