package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;

public class MockExecutionContext implements ExecutionContext {
    private Object target;

    private Method method;

    private Object[] arguments;

    private GraphQLContext newGraphQLContext;

    private DataFetchingEnvironment dataFetchingEnvironment;

    private Supplier<Object> result;

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
        return result != null ? result.get() : null;
    }

    public void setTarget(final Object target) {
        this.target = target;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    public void setArguments(final Object[] arguments) {
        this.arguments = arguments;
    }

    public void setNewGraphQLContext(final GraphQLContext newGraphQLContext) {
        this.newGraphQLContext = newGraphQLContext;
    }

    public void setDataFetchingEnvironment(final DataFetchingEnvironment dataFetchingEnvironment) {
        this.dataFetchingEnvironment = dataFetchingEnvironment;
    }

    public void setResult(final Object result) {
        this.result = () -> result;
    }

    public void setResult(final Supplier<Object> result) {
        this.result = result;
    }
}
