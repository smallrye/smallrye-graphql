package io.smallrye.graphql.bootstrap;

import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.apollographql.federation.graphqljava._Entity;

import graphql.execution.Async;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DelegatingDataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNamedSchemaElement;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;

class FederationDataFetcher implements DataFetcher<CompletableFuture<List<Object>>> {

    private final GraphQLObjectType queryType;
    private final GraphQLCodeRegistry codeRegistry;

    public FederationDataFetcher(GraphQLObjectType queryType, GraphQLCodeRegistry codeRegistry) {
        this.queryType = queryType;
        this.codeRegistry = codeRegistry;
    }

    @Override
    public CompletableFuture<List<Object>> get(DataFetchingEnvironment environment) throws Exception {
        return sequence(environment.<List<Map<String, Object>>> getArgument(_Entity.argumentName).stream()
                .map(representations -> fetchEntities(environment, representations)).map(Async::toCompletableFuture)
                .collect(Collectors.toList()));

    }

    private Object fetchEntities(DataFetchingEnvironment env, Map<String, Object> representations) {
        Map<String, Object> requestedArgs = new HashMap<>(representations);
        requestedArgs.remove("__typename");
        String typename = (String) representations.get("__typename");
        for (GraphQLFieldDefinition field : queryType.getFields()) {
            if (matchesReturnType(field, typename) && matchesArguments(requestedArgs, field)) {
                return execute(field, env, requestedArgs);
            }
        }
        throw new RuntimeException("no query found for " + typename + " by " + requestedArgs.keySet());
    }

    private boolean matchesReturnType(GraphQLFieldDefinition field, String typename) {
        GraphQLOutputType returnType = field.getType();
        return returnType instanceof GraphQLNamedSchemaElement
                && ((GraphQLNamedSchemaElement) returnType).getName().equals(typename);
    }

    private boolean matchesArguments(Map<String, Object> requestedArguments, GraphQLFieldDefinition field) {
        Set<String> argumentNames = field.getArguments().stream().map(GraphQLArgument::getName).collect(toSet());
        return argumentNames.equals(requestedArguments.keySet());
    }

    private Object execute(GraphQLFieldDefinition field, DataFetchingEnvironment env, Map<String, Object> requestedArgs) {
        DataFetcher<?> dataFetcher = codeRegistry.getDataFetcher(queryType, field);
        DataFetchingEnvironment argsEnv = new DelegatingDataFetchingEnvironment(env) {
            @Override
            public Map<String, Object> getArguments() {
                return requestedArgs;
            }

            @Override
            public boolean containsArgument(String name) {
                return requestedArgs.containsKey(name);
            }

            @Override
            public <T> T getArgument(String name) {
                //noinspection unchecked
                return (T) requestedArgs.get(name);
            }

            @Override
            public <T> T getArgumentOrDefault(String name, T defaultValue) {
                return containsArgument(name) ? getArgument(name) : defaultValue;
            }
        };
        try {
            return dataFetcher.get(argsEnv);
        } catch (Exception e) {
            throw new RuntimeException("can't fetch data from " + field, e);
        }
    }

    static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
