package io.smallrye.graphql.transformation;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;

public abstract class AbstractDataFetcherException extends Exception {
    public AbstractDataFetcherException() {
    }

    public AbstractDataFetcherException(Throwable cause) {
        super(cause);
    }

    public abstract DataFetcherResult.Builder<Object> appendDataFetcherResult(DataFetcherResult.Builder<Object> builder,
            DataFetchingEnvironment dfe);
}
