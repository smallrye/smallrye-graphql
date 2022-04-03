package io.smallrye.graphql.execution.context;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import graphql.ExecutionInput;
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
import io.smallrye.graphql.execution.schema.SchemaManager;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.graphql.spi.SmallRyeContextService;

/**
 * Make it easy to manage the context values
 * 
 * This will load the correct context service (Could be thread local, could be CDI)
 * Adds methods to make the life cycle of the context easy to implement
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeContextManager {
    private static final JsonBuilderFactory jsonbuilder = Json.createBuilderFactory(null);
    private static final SmallRyeContextService smallRyeContextService = SmallRyeContextService.get();

    /**
     * This returns the current context. Could be null if not set yet
     * 
     * @return the current context
     */
    public static SmallRyeContext getCurrentSmallRyeContext() {
        return smallRyeContextService.getSmallRyeContext();
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
            smallRyeContext = new SmallRyeContext();
            smallRyeContextService.setSmallRyeContext(smallRyeContext);
        }

        smallRyeContext.setRequest(request);
        return smallRyeContext;
    }

    /**
     * This will populate the context with the input, and happens on every new request, just before GraphQL execute is called
     * 
     * @param executionInput
     * @param queryCache
     */
    public static void populateFromExecutionInput(ExecutionInput executionInput, QueryCache queryCache) {
        SmallRyeContext smallRyeContext = smallRyeContextService.getSmallRyeContext();
        if (smallRyeContext == null)
            throw new RuntimeException("No context available, can not populate data from Execution input");
        if (!smallRyeContext.hasRequest())
            throw new RuntimeException("Invalid context provided, can not populate data from Execution input");
        smallRyeContext.setExecutionInput(executionInput);
        smallRyeContext.setQueryCache(queryCache);
        smallRyeContext.setDocumentSupplier(new DocumentSupplier(executionInput, queryCache));
        smallRyeContextService.setSmallRyeContext(smallRyeContext);
    }

    /**
     * This will populate the provided smallRyeContext from the GraphQL-Java DataFetchingEnvironment, and is per field.
     * 
     * @param field
     * @param dataFetchingEnvironment
     */
    public static void populateFromDataFetchingEnvironment(Field field, DataFetchingEnvironment dataFetchingEnvironment) {
        SmallRyeContext smallRyeContext = smallRyeContextService.getSmallRyeContext();
        if (smallRyeContext == null)
            throw new RuntimeException("No context available, can not populate data from Data Fetching Environment");
        if (!smallRyeContext.hasRequest())
            throw new RuntimeException("Invalid context provided, can not populate data from Data Fetching Environment");
        smallRyeContext.setDataFetchingEnvironment(dataFetchingEnvironment);
        smallRyeContext.setField(field);
        smallRyeContext.setArguments(dataFetchingEnvironment.getArguments());
        smallRyeContext.setPath(dataFetchingEnvironment.getExecutionStepInfo().getPath().toString());
        smallRyeContext.setExecutionId(dataFetchingEnvironment.getExecutionId().toString());
        smallRyeContext.setFieldName(dataFetchingEnvironment.getField().getName());
        smallRyeContext.setSource(dataFetchingEnvironment.getSource());
        smallRyeContext.setSelectedFields(buildSelectedFields(dataFetchingEnvironment, field, false));
        smallRyeContext.setSelectedAndSourceFields(buildSelectedFields(dataFetchingEnvironment, field, true));
        smallRyeContext.setOperationType(getOperationTypeFromDefinition(dataFetchingEnvironment.getOperationDefinition()));
        smallRyeContext.setParentTypeName(getGraphQLTypeName(dataFetchingEnvironment.getParentType()).orElse(null));
        smallRyeContext.setOperationName(getOperationName(smallRyeContext));
        smallRyeContextService.setSmallRyeContext(smallRyeContext);
    }

    public static void setCurrentSmallRyeContext(SmallRyeContext smallRyeContext) {
        smallRyeContextService.setSmallRyeContext(smallRyeContext);
    }

    /**
     * This will clear the context. Usually happens at the end of the request.
     */
    public static void clear() {
        smallRyeContextService.clearSmallRyeContext();
    }

    private static String getOperationName(SmallRyeContext smallRyeContext) {
        // Check if it's set in the request
        if (smallRyeContext.hasRequest()) {
            String fromRequest = smallRyeContext.getRequest().getString(Context.OPERATION_NAME, null);
            if (fromRequest != null && !fromRequest.isEmpty()) {
                return fromRequest;
            }
        }

        // Else try the input
        if (smallRyeContext.getExecutionInput() != null && smallRyeContext.getExecutionInput().getOperationName() != null
                && !smallRyeContext.getExecutionInput().getOperationName().isEmpty()) {
            return smallRyeContext.getExecutionInput().getOperationName();
        }

        // Else try DataFetchingEnvironment
        if (smallRyeContext.getDataFetchingEnvironment() != null
                && smallRyeContext.getDataFetchingEnvironment().getOperationDefinition() != null
                && smallRyeContext.getDataFetchingEnvironment().getOperationDefinition().getName() != null
                && !smallRyeContext.getDataFetchingEnvironment().getOperationDefinition().getName().isEmpty()) {
            return smallRyeContext.getDataFetchingEnvironment().getOperationDefinition().getName();
        }

        return null;
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

    private static JsonArray buildSelectedFields(DataFetchingEnvironment dfe, Field field, boolean includeSourceFields) {
        DataFetchingFieldSelectionSet selectionSet = dfe.getSelectionSet();
        Set<SelectedField> fields = new LinkedHashSet<>(selectionSet.getFields());
        return toJsonArrayBuilder(fields, field, includeSourceFields).build();
    }

    private static JsonArrayBuilder toJsonArrayBuilder(Set<SelectedField> fields, Field field, boolean includeSourceFields) {
        JsonArrayBuilder builder = jsonbuilder.createArrayBuilder();

        for (SelectedField selectedField : fields) {
            if (!isFlattenScalar(selectedField)) {
                if (includeSourceFields || !isSourceField(selectedField, field)) {
                    if (isScalar(selectedField)) {
                        builder = builder.add(selectedField.getName());
                    } else {
                        builder = builder.add(toJsonObjectBuilder(selectedField, field, includeSourceFields));
                    }
                }
            }
        }
        return builder;
    }

    private static boolean isFlattenScalar(SelectedField field) {
        return field.getQualifiedName().contains("/");
    }

    private static JsonObjectBuilder toJsonObjectBuilder(SelectedField selectedField, Field field,
            boolean includeSourceFields) {
        JsonObjectBuilder builder = jsonbuilder.createObjectBuilder();
        Set<SelectedField> fields = new LinkedHashSet<>(selectedField.getSelectionSet().getFields());
        builder = builder.add(selectedField.getName(),
                toJsonArrayBuilder(fields, field, includeSourceFields));
        return builder;
    }

    private static boolean isSourceField(SelectedField selectedField, Field field) {
        if (field.getReference().getType().equals(ReferenceType.TYPE)) {
            Type type = SchemaManager.getSchema().getTypes().get(field.getReference().getName());
            return type.hasOperation(selectedField.getName());
        }
        return false; // Only Type has source field (for now)
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
}
