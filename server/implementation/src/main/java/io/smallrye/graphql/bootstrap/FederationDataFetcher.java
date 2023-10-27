package io.smallrye.graphql.bootstrap;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.apollographql.federation.graphqljava._Entity;

import graphql.execution.Async;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DelegatingDataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedSchemaElement;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.spi.config.Config;

class FederationDataFetcher implements DataFetcher<CompletableFuture<List<Object>>> {

    public static final String TYPENAME = "__typename";
    private final GraphQLObjectType queryType;
    private final GraphQLCodeRegistry codeRegistry;

    public FederationDataFetcher(GraphQLObjectType queryType, GraphQLCodeRegistry codeRegistry) {
        this.queryType = queryType;
        this.codeRegistry = codeRegistry;
    }

    @Override
    public CompletableFuture<List<Object>> get(DataFetchingEnvironment environment) throws Exception {

        var reps = environment.<List<Map<String, Object>>> getArgument(_Entity.argumentName);
        //if we use batch resolving we need to make sure we preserve the order
        var representations = IntStream.range(0, reps.size()).boxed().map(i -> new Representation(reps.get(i), i))
                .collect(Collectors.toList());
        if (Config.get().isFederationBatchResolvingEnabled()) {
            //first we group all representations by their type and arguments
            var repsWithPositionPerType = representations.stream().collect(Collectors.groupingBy(r -> r.typeAndArgumentNames));
            //then we search for the field definition to resolve the objects
            var fieldDefinitions = repsWithPositionPerType.keySet().stream()
                    .collect(Collectors.toMap(Function.identity(), typeAndArgumentNames -> {
                        var batchDefinition = findBatchFieldDefinition(typeAndArgumentNames);
                        if (batchDefinition == null) {
                            return findFieldDefinition(typeAndArgumentNames);
                        } else {
                            return batchDefinition;
                        }
                    }));
            return sequence(repsWithPositionPerType.entrySet().stream().map(e -> {
                var fieldDefinition = fieldDefinitions.get(e.getKey());
                if (fieldDefinition.getType() instanceof GraphQLList) {
                    //use batch loader if available
                    return executeList(fieldDefinition, environment, e.getValue());
                } else {
                    //else normal loader
                    return sequence(e.getValue().stream().map(r -> execute(fieldDefinition, environment, r))
                            .collect(Collectors.toList()));

                }
            }).collect(Collectors.toList())).thenApply(l -> l.stream().flatMap(List::stream)
                    //restore order
                    .sorted(Comparator.comparingInt(r -> r.position)).map(r -> r.Result).collect(Collectors.toList()));

        }
        Map<TypeAndArgumentNames, GraphQLFieldDefinition> cache = new HashMap<>();
        return sequence(representations.stream()
                .map(rep -> fetchEntities(environment, rep,
                        cache.computeIfAbsent(rep.typeAndArgumentNames, this::findFieldDefinition)))
                .collect(Collectors.toList())).thenApply(l -> l.stream().map(r -> r.Result).collect(Collectors.toList()));
    }

    private GraphQLFieldDefinition findBatchFieldDefinition(TypeAndArgumentNames typeAndArgumentNames) {
        for (GraphQLFieldDefinition field : queryType.getFields()) {
            if (matchesReturnTypeList(field, typeAndArgumentNames.type) && matchesArguments(typeAndArgumentNames, field)) {
                return field;
            }
        }
        return null;
    }

    private GraphQLFieldDefinition findFieldDefinition(TypeAndArgumentNames typeAndArgumentNames) {
        for (GraphQLFieldDefinition field : queryType.getFields()) {
            if (matchesReturnType(field, typeAndArgumentNames.type) && matchesArguments(typeAndArgumentNames, field)) {
                return field;
            }
        }
        throw new RuntimeException(
                "no query found for " + typeAndArgumentNames.type + " by " + typeAndArgumentNames.argumentNames);
    }

    private CompletableFuture<ResultObject> fetchEntities(DataFetchingEnvironment env, Representation representation,
            GraphQLFieldDefinition field) {
        return execute(field, env, representation);
    }

    private boolean matchesReturnType(GraphQLFieldDefinition field, String typename) {
        GraphQLType returnType = getGraphqlTypeFromField(field);
        return returnType instanceof GraphQLNamedSchemaElement
                && ((GraphQLNamedSchemaElement) returnType).getName().equals(typename);
    }

    private boolean matchesReturnTypeList(GraphQLFieldDefinition field, String typename) {
        GraphQLType listType = getGraphqlTypeFromField(field);
        if (listType instanceof GraphQLList) {
            var returnType = ((GraphQLList) listType).getOriginalWrappedType();
            return returnType instanceof GraphQLNamedSchemaElement
                    && ((GraphQLNamedSchemaElement) returnType).getName().equals(typename);
        } else {
            return false;
        }
    }

    private GraphQLType getGraphqlTypeFromField(GraphQLFieldDefinition field) {
        GraphQLType type = field.getType();
        if (type instanceof GraphQLNonNull) {
            type = ((GraphQLNonNull) type).getOriginalWrappedType();
        }
        return type;
    }

    private boolean matchesArguments(TypeAndArgumentNames typeAndArgumentNames, GraphQLFieldDefinition field) {
        Set<String> argumentNames = field.getArguments().stream().map(GraphQLArgument::getName).collect(toSet());
        return argumentNames.equals(typeAndArgumentNames.argumentNames);
    }

    private CompletableFuture<List<ResultObject>> executeList(GraphQLFieldDefinition field, DataFetchingEnvironment env,
            List<Representation> representations) {
        DataFetcher<?> dataFetcher = codeRegistry.getDataFetcher(queryType, field);
        Map<String, List<Object>> arguments = new HashMap<>();
        representations.forEach(r -> {
            r.arguments.forEach((argumentName, argumentValue) -> {
                arguments.computeIfAbsent(argumentName, ignore -> new ArrayList<>()).add(argumentValue);
            });
        });
        Map<String, Object> argumentsAsObject = new HashMap<>(arguments);
        DataFetchingEnvironment argsEnv = new DelegatingDataFetchingEnvironment(env) {
            @Override
            public Map<String, Object> getArguments() {
                return argumentsAsObject;
            }

            @Override
            public boolean containsArgument(String name) {
                return argumentsAsObject.containsKey(name);
            }

            @Override
            public <T> T getArgument(String name) {
                //noinspection unchecked
                return (T) argumentsAsObject.get(name);
            }

            @Override
            public <T> T getArgumentOrDefault(String name, T defaultValue) {
                return containsArgument(name) ? getArgument(name) : defaultValue;
            }
        };
        try {
            return Async.toCompletableFuture(dataFetcher.get(argsEnv)).thenApply(results -> {
                List<Object> resultList;
                if (results instanceof DataFetcherResult) {
                    //noinspection unchecked
                    resultList = (List<Object>) ((DataFetcherResult<?>) results).getData();
                } else if (results instanceof List<?>) {
                    //noinspection unchecked
                    resultList = (List<Object>) results;
                } else {
                    throw new IllegalStateException(
                            "Result of batchDataFetcher for Field " + field.getName() + " needs to be a list"
                                    + results.toString());
                }

                if (resultList.size() != representations.size()) {
                    throw new IllegalStateException("Size of result list " + resultList.size()
                            + " needs to be equal to size of arguments " + representations.size());
                }
                return IntStream
                        .range(0, resultList.size()).boxed()
                        .map(i -> new ResultObject(resultList.get(i), representations.get(i).position))
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            throw new RuntimeException("can't fetch data from " + field, e);
        }
    }

    private CompletableFuture<ResultObject> execute(GraphQLFieldDefinition field, DataFetchingEnvironment env,
            Representation representation) {
        DataFetcher<?> dataFetcher = codeRegistry.getDataFetcher(queryType, field);
        DataFetchingEnvironment argsEnv = new DelegatingDataFetchingEnvironment(env) {
            @Override
            public Map<String, Object> getArguments() {
                return representation.arguments;
            }

            @Override
            public boolean containsArgument(String name) {
                return representation.arguments.containsKey(name);
            }

            @Override
            public <T> T getArgument(String name) {
                //noinspection unchecked
                return (T) representation.arguments.get(name);
            }

            @Override
            public <T> T getArgumentOrDefault(String name, T defaultValue) {
                return containsArgument(name) ? getArgument(name) : defaultValue;
            }
        };
        try {
            return Async.toCompletableFuture(dataFetcher.get(argsEnv))
                    .thenApply(o -> new ResultObject(o, representation.position));
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

    static class ResultObject {
        final Object Result;
        final int position;

        public ResultObject(Object result, int position) {
            Result = result;
            this.position = position;
        }
    }

    static class Representation {
        final Map<String, Object> arguments;
        final int position;
        final TypeAndArgumentNames typeAndArgumentNames;

        public Representation(Map<String, Object> arguments, int position) {
            this.arguments = arguments;
            this.position = position;
            var argumentNames = new HashSet<>(arguments.keySet());
            argumentNames.remove(TYPENAME);
            this.typeAndArgumentNames = new TypeAndArgumentNames(argumentNames, (String) arguments.get(TYPENAME));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Representation that = (Representation) o;
            return position == that.position && Objects.equals(arguments, that.arguments)
                    && Objects.equals(typeAndArgumentNames, that.typeAndArgumentNames);
        }

        @Override
        public int hashCode() {
            return Objects.hash(arguments, position, typeAndArgumentNames);
        }
    }

    static class TypeAndArgumentNames {
        final Set<String> argumentNames;
        final String type;

        public TypeAndArgumentNames(Set<String> argumentNames, String type) {
            this.argumentNames = argumentNames;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TypeAndArgumentNames that = (TypeAndArgumentNames) o;
            return Objects.equals(argumentNames, that.argumentNames) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(argumentNames, type);
        }
    }
}
