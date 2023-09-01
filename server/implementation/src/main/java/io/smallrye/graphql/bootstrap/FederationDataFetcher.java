package io.smallrye.graphql.bootstrap;

import com.apollographql.federation.graphqljava._Entity;
import graphql.schema.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

class FederationDataFetcher implements DataFetcher<Object> {

    private final GraphQLObjectType queryType;
    private final GraphQLCodeRegistry codeRegistry;

    public FederationDataFetcher(GraphQLObjectType queryType, GraphQLCodeRegistry codeRegistry) {
        this.queryType = queryType;
        this.codeRegistry = codeRegistry;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        return collect(environment.<List<Map<String, Object>>> getArgument(_Entity.argumentName).stream()
                .map(representations -> fetchEntities(environment, representations))
                .collect(toList()));
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

    @SuppressWarnings("unchecked")
    private Object collect(List<Object> entities) {
        if (!entities.isEmpty() && entities.iterator().next() instanceof CompletionStage) {
            List<CompletableFuture<?>> futures = (List<CompletableFuture<?>>)(List<?>) entities;
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                    .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(toList()));
        }
        return entities;
    }
}
