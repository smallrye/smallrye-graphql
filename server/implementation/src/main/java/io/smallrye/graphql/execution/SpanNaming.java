package io.smallrye.graphql.execution;

import graphql.ExecutionInput;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.datafetcher.helper.NameHelper;

public final class SpanNaming {

    private static final String PREFIX = "GraphQL";

    public static String getOperationName(final DataFetchingEnvironment env) {
        String parent = NameHelper.getName(env.getParentType());

        String name = PREFIX + ":" + parent + "." + env.getField().getName();

        return name;
    }

    public static String getOperationName(ExecutionInput executionInput) {
        if (executionInput.getOperationName() != null && !executionInput.getOperationName().isEmpty()) {
            return PREFIX + ":" + executionInput.getOperationName();
        }
        return PREFIX;
    }

}
