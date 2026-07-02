package io.smallrye.graphql.client.impl.typesafe;

import static io.smallrye.graphql.client.impl.typesafe.json.JsonUtils.isListOf;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.RequestImpl;
import io.smallrye.graphql.client.impl.ResponseReader;
import io.smallrye.graphql.client.impl.typesafe.json.JsonReader;
import io.smallrye.graphql.client.impl.typesafe.json.JsonUtils;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.TypesafeResponse;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class ResultBuilder {
    private static final ObjectMapper MAPPER = RequestImpl.MAPPER;

    private final MethodInvocation method;
    private final ObjectNode response;
    private final String responseString;
    private final Integer statusCode;
    private final String statusMessage;
    private ObjectNode data;
    private ObjectNode extensions;
    private Map<String, List<String>> transportMeta;

    public ResultBuilder(MethodInvocation method, String responseString, boolean allowUnexpectedResponseFields) {
        this(method, responseString, null, null, null, allowUnexpectedResponseFields);
    }

    public ResultBuilder(MethodInvocation method,
            String responseString,
            Integer statusCode,
            String statusMessage,
            Map<String, List<String>> transportMeta,
            boolean allowUnexpectedResponseFields) {
        this.method = method;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.responseString = responseString;
        this.transportMeta = transportMeta;
        this.response = ResponseReader.parseGraphQLResponse(responseString, allowUnexpectedResponseFields);
    }

    public Object read() {
        if (response == null) {
            throw new InvalidResponseException(
                    "Unexpected response. Code=" + statusCode + ", message=\"" + statusMessage + "\", " +
                            "body=\"" + responseString + "\"");
        }
        data = readData();
        readErrors();
        if (data == null)
            return null;
        JsonNode value = method.isSingle() ? data.get(method.getName()) : data;
        Object result;
        if (method.getReturnType().isTypesafeResponse()) {
            extensions = readExtensions();
            result = JsonReader.readJsonTypesafeResponse(
                    method.toString(),
                    method.getReturnType(),
                    value,
                    null);
            result = TypesafeResponse.withTransportMetaAndExtensions((TypesafeResponse<?>) result,
                    transportMeta,
                    extensions);
        } else {
            result = JsonReader.readJson(
                    method.toString(),
                    method.getReturnType(),
                    value,
                    null);
        }
        return result;
    }

    private ObjectNode readData() {
        if (!response.has("data") || response.get("data").isNull())
            return null;

        ObjectNode data = (ObjectNode) response.get("data");
        for (String namespace : method.getNamespaces()) {
            data = (ObjectNode) data.get(namespace);
        }

        if (method.isSingle() && !data.has(method.getName()))
            throw new InvalidResponseException("No data for '" + method.getName() + "'");
        return data;
    }

    private void readErrors() {
        if (!response.has("errors") || response.get("errors").isNull())
            return;
        JsonNode errorsNode = response.get("errors");
        if (!errorsNode.isArray())
            return;
        ArrayNode jsonErrors = (ArrayNode) errorsNode;
        ArrayNode unapplied = MAPPER.createArrayNode();
        for (JsonNode error : jsonErrors) {
            if (!apply(error)) {
                unapplied.add(error);
            }
        }
        if (unapplied.isEmpty())
            return;
        throw new GraphQLClientException("errors from service",
                StreamSupport.stream(unapplied.spliterator(), false)
                        .map(ResponseReader::readError)
                        .toList());
    }

    private boolean apply(JsonNode error) {
        List<Object> path = getPath(error);
        if (data == null || path == null)
            return false;
        String pointerString = path.stream().map(Object::toString).collect(joining("/", "/", ""));
        JsonPointer pointer = JsonPointer.compile(pointerString);
        JsonNode existing = data.at(pointer);
        if (existing.isMissingNode())
            return false;

        // Build the error-marked array
        ArrayNode errors = MAPPER.createArrayNode();
        if (isListOf(existing, ErrorOr.class.getSimpleName())) {
            for (JsonNode e : existing) {
                errors.add(e);
            }
        }
        // Add __typename to error object to mark it
        ObjectNode errorCopy = ((ObjectNode) error).deepCopy();
        errorCopy.put("__typename", ErrorOr.class.getSimpleName());
        errors.add(errorCopy);

        // Set the error array at the pointer location in data
        setAtPointer(data, pointer, errors);
        return true;
    }

    /**
     * Set a value at a given JsonPointer location in the tree.
     */
    private void setAtPointer(ObjectNode root, JsonPointer pointer, JsonNode newValue) {
        // Navigate to the parent and set the value
        JsonPointer head = pointer.head();
        String lastSegment = pointer.last().getMatchingProperty();

        JsonNode parent;
        if (head != null && !head.toString().isEmpty()) {
            parent = root.at(head);
        } else {
            parent = root;
        }

        if (parent.isObject()) {
            ((ObjectNode) parent).set(lastSegment, newValue);
        } else if (parent.isArray()) {
            int index = Integer.parseInt(lastSegment);
            ((ArrayNode) parent).set(index, newValue);
        }
    }

    private ObjectNode readExtensions() {
        if (!response.has("extensions") || response.get("extensions").isNull())
            return null;
        return (ObjectNode) response.get("extensions");
    }

    private static List<Object> getPath(JsonNode jsonValue) {
        JsonNode value = jsonValue.get("path");
        if (value == null || !value.isArray()) {
            return null;
        }
        ArrayNode jsonArray = (ArrayNode) value;
        return StreamSupport.stream(jsonArray.spliterator(), false)
                .map(JsonUtils::toValue)
                .toList();
    }
}
