package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.Method;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;

/**
 * Context of a data-fetch execution.
 */
public interface ExecutionContext {

    /**
     * The object the method is called on
     *
     * @return the target object
     */
    Object target();

    /**
     * Arguments passed to the method.
     *
     * @return the arguments
     */
    Object[] arguments();

    /**
     * The method to call.
     *
     * @return the method
     */
    Method method();

    /**
     * GraphQLContext passed to {@code @Source}-child-resolvers, can be used to pass down context-information.
     *
     * @return the GraphQLContext
     */
    GraphQLContext newGraphQLContext();

    /**
     * The current DataFetchingEnvironment
     *
     * @return the current DataFetchingEnvironment
     */
    DataFetchingEnvironment dataFetchingEnvironment();

    /**
     * Proceed the execution,
     *
     * @return the result of the execution
     * @throws Exception can throw any exception
     */
    Object proceed() throws Exception;

}
