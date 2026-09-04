package io.smallrye.graphql.execution.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Adds methods to make the life cycle of the context easy to implement
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeContextManager {
    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
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
    public static SmallRyeContext fromInitialRequest(ObjectNode request) {

        SmallRyeContext smallRyeContext = getCurrentSmallRyeContext();
        if (smallRyeContext == null) {
            smallRyeContext = new SmallRyeContext(SmallRyeContextManager.class.getName());
        }

        smallRyeContext.setRequest(request);
        smallRyeContext.setOperationName(getOperationName(request));
        smallRyeContext.setExtensionsFromClient(getExtensions(request));
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
        SmallRyeContext clone = smallRyeContext.clone();
        clone.setDataFetchingEnvironment(dataFetchingEnvironment);
        clone.setField(field);
        clone.setArguments(dataFetchingEnvironment.getArguments());
        clone.setPath(dataFetchingEnvironment.getExecutionStepInfo().getPath().toString());
        clone.setExecutionId(dataFetchingEnvironment.getExecutionId().toString());
        clone.setFieldName(dataFetchingEnvironment.getField().getName());
        clone.setSource(dataFetchingEnvironment.getSource());
        clone.setSelectedFields(buildSelectedFields(type, dataFetchingEnvironment, field, false));
        clone.setSelectedAndSourceFields(buildSelectedFields(type, dataFetchingEnvironment, field, true));
        clone.setOperationType(getOperationTypeFromDefinition(dataFetchingEnvironment.getOperationDefinition()));
        clone.setParentTypeName(getGraphQLTypeName(dataFetchingEnvironment.getParentType()).orElse(null));
        if (clone.getOperationName().isEmpty()) {
            clone.setOperationName(getOperationName(dataFetchingEnvironment));
        }
        GraphQLContext graphQLContext = dataFetchingEnvironment.getGraphQlContext();
        graphQLContext.put(CONTEXT, clone);

        current.set(clone);
        return clone;
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

    private static ArrayNode buildSelectedFields(Type type, DataFetchingEnvironment dfe, Field field,
            boolean includeSourceFields) {
        DataFetchingFieldSelectionSet selectionSet = dfe.getSelectionSet();
        Set<SelectedField> fields = new LinkedHashSet<>(selectionSet.getFields());
        return toArrayNode(type, fields, field, includeSourceFields);
    }

    private static ArrayNode toArrayNode(Type type, Set<SelectedField> fields, Field field,
            boolean includeSourceFields) {
        ArrayNode arrayNode = nodeFactory.arrayNode();

        for (SelectedField selectedField : fields) {
            if (!isFlattenScalar(selectedField)) {
                if (includeSourceFields || !isSourceField(type, selectedField)) {
                    if (isScalar(selectedField)) {
                        arrayNode.add(selectedField.getName());
                    } else {
                        arrayNode.add(toObjectNode(type, selectedField, field, includeSourceFields));
                    }
                }
            }
        }
        return arrayNode;
    }

    private static boolean isFlattenScalar(SelectedField field) {
        return field.getQualifiedName().contains("/");
    }

    private static ObjectNode toObjectNode(Type type, SelectedField selectedField, Field field,
            boolean includeSourceFields) {
        ObjectNode objectNode = nodeFactory.objectNode();
        Set<SelectedField> fields = new LinkedHashSet<>(selectedField.getSelectionSet().getFields());
        objectNode.set(selectedField.getName(),
                toArrayNode(type, fields, field, includeSourceFields));
        return objectNode;
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

    private static String getOperationName(ObjectNode request) {
        if (request.has(Context.OPERATION_NAME)
                && request.get(Context.OPERATION_NAME) != null
                && !request.get(Context.OPERATION_NAME).isNull()) {

            return request.get(Context.OPERATION_NAME).asText();
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

    private static final String EXTENSIONS = "extensions";

    private static Map<String, Object> getExtensions(ObjectNode request) {
        if (request.has(EXTENSIONS)
                && request.get(EXTENSIONS) != null
                && !request.get(EXTENSIONS).isNull()) {
            ObjectNode extensionsJson = (ObjectNode) request.get(EXTENSIONS);
            Map<String, Object> result = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = extensionsJson.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                result.put(entry.getKey(), convertJsonNode(entry.getValue()));
            }
            return result;
        }
        return null;
    }

    private static Object convertJsonNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            if (node.isIntegralNumber()) {
                return node.longValue();
            }
            return node.doubleValue();
        } else if (node.isBoolean()) {
            return node.booleanValue();
        } else if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                map.put(entry.getKey(), convertJsonNode(entry.getValue()));
            }
            return map;
        } else if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(convertJsonNode(item));
            }
            return list;
        }
        return node.toString();
    }

}
