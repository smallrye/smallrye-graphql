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
import java.util.function.BiFunction;
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
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.spi.config.Config;

public class FederationDataFetcher implements DataFetcher<CompletableFuture<List<Object>>> {

    public static final String TYPENAME = "__typename";
    private final GraphQLObjectType queryType;
    private final GraphQLCodeRegistry codeRegistry;
    private final HashMap<TypeAndArgumentNames, TypeFieldWrapper> cache = new HashMap<>();

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
                    .collect(Collectors.toMap(Function.identity(), typeAndArgumentNames -> cache.computeIfAbsent(
                            typeAndArgumentNames, type -> Objects.requireNonNullElseGet(
                                    findBatchFieldDefinition(type),
                                    () -> findFieldDefinition(type)))));
            return sequence(repsWithPositionPerType.entrySet().stream().map(e -> {
                var fieldDefinition = fieldDefinitions.get(e.getKey());
                if (getGraphqlTypeFromField(fieldDefinition.getField()) instanceof GraphQLList) {
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
        return sequence(representations.stream()
                .map(rep -> fetchEntities(environment, rep,
                        cache.computeIfAbsent(rep.typeAndArgumentNames, this::findFieldDefinition)))
                .collect(Collectors.toList())).thenApply(l -> l.stream().map(r -> r.Result).collect(Collectors.toList()));
    }

    private TypeFieldWrapper findRecursiveFieldDefinition(TypeAndArgumentNames typeAndArgumentNames,
            GraphQLFieldDefinition field,
            BiFunction<GraphQLFieldDefinition, String, Boolean> matchesReturnType) {
        if (field.getType() instanceof GraphQLObjectType) {
            for (GraphQLSchemaElement child : field.getType().getChildren()) {
                if (child instanceof GraphQLFieldDefinition) {
                    GraphQLFieldDefinition definition = (GraphQLFieldDefinition) child;
                    if (matchesReturnType.apply(definition, typeAndArgumentNames.type)
                            && matchesArguments(typeAndArgumentNames, definition)) {
                        return new TypeFieldWrapper((GraphQLObjectType) field.getType(), definition);
                    } else if (definition.getType() instanceof GraphQLObjectType) {
                        return findRecursiveFieldDefinition(typeAndArgumentNames, definition, matchesReturnType);
                    }
                }
            }
        } else if (matchesReturnType.apply(field, typeAndArgumentNames.type) && matchesArguments(typeAndArgumentNames, field)) {
            return new TypeFieldWrapper(queryType, field);
        }
        return null;
    }

    private TypeFieldWrapper findBatchFieldDefinition(TypeAndArgumentNames typeAndArgumentNames) {
        for (GraphQLFieldDefinition field : queryType.getFields()) {
            TypeFieldWrapper typeFieldWrapper = findRecursiveFieldDefinition(typeAndArgumentNames, field,
                    this::matchesReturnTypeList);
            if (typeFieldWrapper != null) {
                return typeFieldWrapper;
            }
        }
        return null;
    }

    private TypeFieldWrapper findFieldDefinition(TypeAndArgumentNames typeAndArgumentNames) {
        for (GraphQLFieldDefinition field : queryType.getFields()) {
            TypeFieldWrapper typeFieldWrapper = findRecursiveFieldDefinition(typeAndArgumentNames, field,
                    this::matchesReturnType);
            if (typeFieldWrapper != null) {
                return typeFieldWrapper;
            }
        }
        throw new RuntimeException(
                "no query found for " + typeAndArgumentNames.type + " by " + typeAndArgumentNames.argumentNames);
    }

    private CompletableFuture<ResultObject> fetchEntities(DataFetchingEnvironment env, Representation representation,
            TypeFieldWrapper wrapper) {
        return execute(wrapper, env, representation);
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

    private CompletableFuture<List<ResultObject>> executeList(TypeFieldWrapper wrapper, DataFetchingEnvironment env,
            List<Representation> representations) {
        DataFetcher<?> dataFetcher = codeRegistry.getDataFetcher(wrapper.getType(), wrapper.getField());
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
                            "Result of batchDataFetcher for Field " + wrapper.getField().getName() + " needs to be a list"
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
            throw new RuntimeException("can't fetch data from " + wrapper.getField(), e);
        }
    }

    private CompletableFuture<ResultObject> execute(TypeFieldWrapper wrapper, DataFetchingEnvironment env,
            Representation representation) {
        DataFetcher<?> dataFetcher = codeRegistry.getDataFetcher(wrapper.getType(), wrapper.getField());
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
            throw new RuntimeException("can't fetch data from " + wrapper.getField(), e);
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
