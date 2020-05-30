package io.smallrye.graphql.transformation;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;

public abstract class DataFetchingException extends Exception {
    public DataFetchingException() {
    }

    public DataFetchingException(Throwable cause) {
        super(cause);
    }

    public abstract DataFetcherResult.Builder<Object> appendDataFetcherResult(DataFetcherResult.Builder<Object> builder,
            DataFetchingEnvironment dfe);
}
