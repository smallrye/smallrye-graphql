package io.smallrye.graphql.execution.context;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import graphql.ExecutionInput;
import graphql.GraphQLContext;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.SelectedField;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.QueryCache;
import io.smallrye.graphql.execution.error.UnparseableDocumentException;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;

/**
 * Adds methods to make the life cycle of the context easy to implement
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeContextManager {
    private static final JsonBuilderFactory jsonbuilder = Json.createBuilderFactory(null);
    private static final InheritableThreadLocal<SmallRyeContext> current = new InheritableThreadLocal<>();
    public static final String CONTEXT = "context";

    public static SmallRyeContext restoreSmallRyeContext(final DataFetchingEnvironment dfe) {
        GraphQLContext graphQLContext = dfe.getGraphQlContext();
        SmallRyeContext restored = graphQLContext.get(CONTEXT);
        current.set(restored);
        return restored;
    }

    public static SmallRyeContext getCurrentSmallRyeContext() {
        return current.get();
    }

    public static void restore(SmallRyeContext smallRyeContext) {
        current.set(smallRyeContext);
    }

    public static void clearCurrentSmallRyeContext() {
        current.remove();
    }

    /**
     * This creates a new context at the beginning of the request
     * 
     * @param request the original request
     * @return the initial context
     */
    public static SmallRyeContext fromInitialRequest(JsonObject request) {

        SmallRyeContext smallRyeContext = getCurrentSmallRyeContext();
        if (smallRyeContext == null) {
            smallRyeContext = new SmallRyeContext(SmallRyeContextManager.class.getName());
        }

        smallRyeContext.setRequest(request);
        smallRyeContext.setOperationName(getOperationName(request));
        current.set(smallRyeContext);
        return smallRyeContext;
    }

    /**
     * This will populate the context with the input, and happens on every new request, just before GraphQL execute is called
     * 
     * @param executionInput
     * @param queryCache
     * @return the current context
     */
    public static SmallRyeContext populateFromExecutionInput(ExecutionInput executionInput, QueryCache queryCache) {
        SmallRyeContext smallRyeContext = getCurrentSmallRyeContext();
        if (smallRyeContext == null)
            throw new RuntimeException("No context available, can not populate data from Execution input");
        if (!smallRyeContext.hasRequest())
            throw new RuntimeException("Invalid context provided, can not populate data from Execution input");
        smallRyeContext.setExecutionInput(executionInput);
        smallRyeContext.setQueryCache(queryCache);
        DocumentSupplier documentSupplier = new DocumentSupplier(executionInput, queryCache);
        smallRyeContext.setDocumentSupplier(documentSupplier);
        smallRyeContext.setRequestedOperationTypes(getRequestedOperationTypes(documentSupplier));
        smallRyeContext.setExecutionId(executionInput.getExecutionId().toString());
        current.set(smallRyeContext);

        return smallRyeContext;
    }

    /**
     * This will populate the provided smallRyeContext from the GraphQL-Java DataFetchingEnvironment, and is per field.
     * 
     * @param type
     * @param field
     * @param dataFetchingEnvironment
     * @return the current context
     */
    public static SmallRyeContext populateFromDataFetchingEnvironment(
            Type type,
            Field field,
            DataFetchingEnvironment dataFetchingEnvironment) {

        SmallRyeContext smallRyeContext = getCurrentSmallRyeContext();
        if (smallRyeContext == null)
            smallRyeContext = restoreSmallRyeContext(dataFetchingEnvironment);
        if (!smallRyeContext.hasRequest())
            throw new RuntimeException("Invalid context provided, can not populate data from Data Fetching Environment");
        smallRyeContext.setDataFetchingEnvironment(dataFetchingEnvironment);
        smallRyeContext.setField(field);
        smallRyeContext.setArguments(dataFetchingEnvironment.getArguments());
        smallRyeContext.setPath(dataFetchingEnvironment.getExecutionStepInfo().getPath().toString());
        smallRyeContext.setExecutionId(dataFetchingEnvironment.getExecutionId().toString());
        smallRyeContext.setFieldName(dataFetchingEnvironment.getField().getName());
        smallRyeContext.setSource(dataFetchingEnvironment.getSource());
        smallRyeContext.setSelectedFields(buildSelectedFields(type, dataFetchingEnvironment, field, false));
        smallRyeContext.setSelectedAndSourceFields(buildSelectedFields(type, dataFetchingEnvironment, field, true));
        smallRyeContext.setOperationType(getOperationTypeFromDefinition(dataFetchingEnvironment.getOperationDefinition()));
        smallRyeContext.setParentTypeName(getGraphQLTypeName(dataFetchingEnvironment.getParentType()).orElse(null));
        if (smallRyeContext.getOperationName().isEmpty()) {
            smallRyeContext.setOperationName(getOperationName(dataFetchingEnvironment));
        }
        GraphQLContext graphQLContext = dataFetchingEnvironment.getGraphQlContext();
        graphQLContext.put(CONTEXT, smallRyeContext);

        current.set(smallRyeContext);
        return smallRyeContext;
    }

    private static Optional<String> getGraphQLTypeName(GraphQLType graphQLType) {
        if (graphQLType instanceof GraphQLNamedType) {
            return Optional.of(((GraphQLNamedType) graphQLType).getName());
        } else if (graphQLType instanceof GraphQLNonNull) {
            return getGraphQLTypeName(((GraphQLNonNull) graphQLType).getWrappedType());
        } else if (graphQLType instanceof GraphQLList) {
            return getGraphQLTypeName(((GraphQLList) graphQLType).getWrappedType());
        }
        return Optional.empty();
    }

    private static String getOperationTypeFromDefinition(OperationDefinition definition) {
        return definition.getOperation().toString();
    }

    private static JsonArray buildSelectedFields(Type type, DataFetchingEnvironment dfe, Field field,
            boolean includeSourceFields) {
        DataFetchingFieldSelectionSet selectionSet = dfe.getSelectionSet();
        Set<SelectedField> fields = new LinkedHashSet<>(selectionSet.getFields());
        return toJsonArrayBuilder(type, fields, field, includeSourceFields).build();
    }

    private static JsonArrayBuilder toJsonArrayBuilder(Type type, Set<SelectedField> fields, Field field,
            boolean includeSourceFields) {
        JsonArrayBuilder builder = jsonbuilder.createArrayBuilder();

        for (SelectedField selectedField : fields) {
            if (!isFlattenScalar(selectedField)) {
                if (includeSourceFields || !isSourceField(type, selectedField)) {
                    if (isScalar(selectedField)) {
                        builder = builder.add(selectedField.getName());
                    } else {
                        builder = builder.add(toJsonObjectBuilder(type, selectedField, field, includeSourceFields));
                    }
                }
            }
        }
        return builder;
    }

    private static boolean isFlattenScalar(SelectedField field) {
        return field.getQualifiedName().contains("/");
    }

    private static JsonObjectBuilder toJsonObjectBuilder(Type type, SelectedField selectedField, Field field,
            boolean includeSourceFields) {
        JsonObjectBuilder builder = jsonbuilder.createObjectBuilder();
        Set<SelectedField> fields = new LinkedHashSet<>(selectedField.getSelectionSet().getFields());
        builder = builder.add(selectedField.getName(),
                toJsonArrayBuilder(type, fields, field, includeSourceFields));
        return builder;
    }

    private static boolean isSourceField(Type type, SelectedField selectedField) {
        // A source field is an operation
        if (type != null && type.hasOperations()) {
            Map<String, Operation> sourceFields = type.getOperations();
            String fieldName = selectedField.getName();
            if (sourceFields.containsKey(fieldName)) {
                Operation o = sourceFields.get(fieldName);
                return o.isSourceField();
            }
        }
        return false;
    }

    private static boolean isScalar(SelectedField field) {
        List<GraphQLFieldDefinition> fieldDefinitions = field.getFieldDefinitions();
        for (GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
            GraphQLType graphQLType = unwrapGraphQLType(fieldDefinition.getType());
            if (isScalar(graphQLType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isScalar(GraphQLType gqlt) {
        return GraphQLScalarType.class.isAssignableFrom(gqlt.getClass());
    }

    private static GraphQLType unwrapGraphQLType(GraphQLType gqlt) {
        if (isNonNull(gqlt)) {
            GraphQLNonNull graphQLNonNull = (GraphQLNonNull) gqlt;
            return unwrapGraphQLType(graphQLNonNull.getWrappedType());
        } else if (isList(gqlt)) {
            GraphQLList graphQLList = (GraphQLList) gqlt;
            return unwrapGraphQLType(graphQLList.getWrappedType());
        }
        return gqlt;
    }

    private static boolean isNonNull(GraphQLType gqlt) {
        return GraphQLNonNull.class.isAssignableFrom(gqlt.getClass());
    }

    private static boolean isList(GraphQLType gqlt) {
        return GraphQLList.class.isAssignableFrom(gqlt.getClass());
    }

    private static List<String> getRequestedOperationTypes(DocumentSupplier documentSupplier) {
        List<String> allRequestedTypes = new ArrayList<>();

        if (documentSupplier != null) {
            Document document = documentSupplier.get();
            if (document == null) {
                throw new UnparseableDocumentException();
            }
            List<OperationDefinition> definitions = document.getDefinitionsOfType(OperationDefinition.class);
            for (OperationDefinition definition : definitions) {
                String operationType = getOperationTypeFromDefinition(definition);
                if (!allRequestedTypes.contains(operationType)) {
                    allRequestedTypes.add(operationType);
                }
            }
        }
        return allRequestedTypes;
    }

    private static String getOperationName(JsonObject request) {
        if (request.containsKey(Context.OPERATION_NAME)
                && request.get(Context.OPERATION_NAME) != null
                && !request.get(Context.OPERATION_NAME).getValueType().equals(JsonValue.ValueType.NULL)) {

            return request.getString(Context.OPERATION_NAME);
        }
        return null;
    }

    private static String getOperationName(DataFetchingEnvironment dataFetchingEnvironment) {
        if (dataFetchingEnvironment.getOperationDefinition() != null
                && dataFetchingEnvironment.getOperationDefinition().getName() != null
                && !dataFetchingEnvironment.getOperationDefinition().getName().isEmpty()) {

            return dataFetchingEnvironment.getOperationDefinition().getName();
        }
        return null;
    }

}
