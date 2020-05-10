package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.Method;
import java.util.Iterator;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.datafetcher.decorator.DataFetcherDecorator;

public class ExecutionContextImpl implements ExecutionContext {

    private final Object target;

    private final Method method;

    private final Object[] arguments;

    private final GraphQLContext newGraphQLContext;

    private final DataFetchingEnvironment dataFetchingEnvironment;

    private final Iterator<DataFetcherDecorator> decoratorIterator;

    public ExecutionContextImpl(Object target,
            Method method,
            Object[] arguments,
            GraphQLContext newGraphQLContext,
            DataFetchingEnvironment dataFetchingEnvironment,
            Iterator<DataFetcherDecorator> decoratorIterator) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.newGraphQLContext = newGraphQLContext;
        this.dataFetchingEnvironment = dataFetchingEnvironment;
        this.decoratorIterator = decoratorIterator;
    }

    @Override
    public Object target() {
        return target;
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public Object[] arguments() {
        return arguments;
    }

    @Override
    public GraphQLContext newGraphQLContext() {
        return newGraphQLContext;
    }

    @Override
    public DataFetchingEnvironment dataFetchingEnvironment() {
        return dataFetchingEnvironment;
    }

    @Override
    public Object proceed() throws Exception {
        if (decoratorIterator.hasNext()) {
            return decoratorIterator.next().execute(this);
        }

        return method.invoke(target, arguments);
    }
}
