package io.smallrye.graphql.execution.datafetcher.decorator;

import io.smallrye.graphql.execution.datafetcher.ExecutionContext;

public interface DataFetcherDecorator {

    Object execute(ExecutionContext executionContext) throws Exception;

}
