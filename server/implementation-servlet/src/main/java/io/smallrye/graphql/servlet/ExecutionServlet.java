package io.smallrye.graphql.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.spi.config.Config;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Executing the GraphQL request
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLExecutionServlet", urlPatterns = { "/graphql/*" }, loadOnStartup = 1)
public class ExecutionServlet extends HttpServlet {
    private static final long serialVersionUID = -2859915918802356120L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    @Inject
    ExecutionService executionService;

    private final Config config = Config.get();

    public ExecutionServlet() {
    }

    public ExecutionServlet(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (config.isAllowGet()) {
                ObjectNode jsonObject = getObjectNodeFromQueryParameters(request);
                executionService.executeSync(jsonObject, getMetaData(request), new HttpServletResponseWriter(response));
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET Queries is not enabled");
            }
        } catch (IOException ex) {
            SmallRyeGraphQLServletLogging.log.ioException(ex);
            throw ex;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> metaData = getMetaData(request);
            ObjectNode objectNodeFromBody = getObjectNodeFromBody(request);
            if (request.getQueryString() != null && !request.getQueryString().isEmpty()
                    && config.isAllowPostWithQueryParameters()) {
                ObjectNode objectNodeFromQueryParameters = getObjectNodeFromQueryParameters(request);
                ObjectNode mergedNode = mergeNodes(objectNodeFromBody, objectNodeFromQueryParameters);
                executionService.executeSync(mergedNode, metaData, new HttpServletResponseWriter(response));
            } else {
                executionService.executeSync(objectNodeFromBody, metaData, new HttpServletResponseWriter(response));
            }
        } catch (IOException ex) {
            SmallRyeGraphQLServletLogging.log.ioException(ex);
            throw ex;
        }
    }

    private ObjectNode getObjectNodeFromQueryParameters(HttpServletRequest request) throws UnsupportedEncodingException {
        ObjectNode input = NODE_FACTORY.objectNode();
        // Query
        String query = request.getParameter(QUERY);
        if (query != null && !query.isEmpty()) {
            input.put(QUERY, URLDecoder.decode(query, StandardCharsets.UTF_8));
        }
        // OperationName
        String operationName = request.getParameter(OPERATION_NAME);
        if (operationName != null && !operationName.isEmpty()) {
            input.put(OPERATION_NAME, URLDecoder.decode(operationName, StandardCharsets.UTF_8));
        }

        // Variables
        String variables = request.getParameter(VARIABLES);
        if (variables != null && !variables.isEmpty()) {
            ObjectNode jsonObject = toObjectNode(URLDecoder.decode(variables, StandardCharsets.UTF_8));
            input.set(VARIABLES, jsonObject);
        }

        // Extensions
        String extensions = request.getParameter(EXTENSIONS);
        if (extensions != null && !extensions.isEmpty()) {
            ObjectNode jsonObject = toObjectNode(URLDecoder.decode(extensions, StandardCharsets.UTF_8));
            input.set(EXTENSIONS, jsonObject);
        }

        return input;
    }

    private ObjectNode getObjectNodeFromBody(HttpServletRequest request) throws IOException {

        String contentType = request.getContentType();
        try (BufferedReader bufferedReader = request.getReader()) {
            if (contentType != null && contentType.startsWith(APPLICATION_GRAPHQL)) {
                String query = bufferedReader.lines().collect(Collectors.joining("\n"));
                return NODE_FACTORY.objectNode().put(QUERY, query);
            } else {
                return (ObjectNode) OBJECT_MAPPER.readTree(bufferedReader);
            }
        }
    }

    private static ObjectNode toObjectNode(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        return (ObjectNode) OBJECT_MAPPER.readTree(jsonString);
    }

    /**
     * RFC 7386 JSON Merge Patch: fields from patch override fields in target;
     * null-valued fields in patch remove the field from target.
     */
    private static ObjectNode mergeNodes(ObjectNode target, ObjectNode patch) {
        ObjectNode result = target.deepCopy();
        Iterator<Map.Entry<String, JsonNode>> fields = patch.properties().iterator();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if (entry.getValue().isNull()) {
                result.remove(entry.getKey());
            } else {
                result.set(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    protected Map<String, Object> getMetaData(HttpServletRequest request) {
        Map<String, Object> metaData = new ConcurrentHashMap<>();
        metaData.put("httpHeaders", getHeaders(request));
        return metaData;
    }

    private Map<String, List<String>> getHeaders(HttpServletRequest request) {
        Map<String, List<String>> h = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            List<String> valuesList = Collections.list(headerValues);
            h.put(headerName, valuesList);
        }
        return h;
    }

    private static final String QUERY = "query";
    private static final String OPERATION_NAME = "operationName";
    private static final String VARIABLES = "variables";
    private static final String EXTENSIONS = "extensions";
    private static final String APPLICATION_GRAPHQL = "application/graphql";

}
