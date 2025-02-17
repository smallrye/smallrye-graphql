package io.smallrye.graphql.test;

import org.eclipse.microprofile.graphql.Enum;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Type;

@Type("TestSourceConfiguration")
public record TestSourceConfiguration(boolean active, @Name("state") TestSourceState state) {

    @Enum("TestSourceState")
    public enum TestSourceState {
        PENDING,
        IN_PROGRESS,
        DONE
    }
}
