package io.smallrye.graphql.entry.http;

import static com.apollographql.federation.graphqljava.tracing.FederatedTracingInstrumentation.FEDERATED_TRACING_HEADER_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.spi.config.Config;

/**
 * Executing the GraphQL request
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLExecutionServlet", urlPatterns = { "/graphql/*" }, loadOnStartup = 1)
public class ExecutionServlet extends HttpServlet {
    private static final long serialVersionUID = -2859915918802356120L;

    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);

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
            Map<String, Object> context = createContext(request);
            if (config.isAllowGet()) {
                JsonObject jsonObject = getJsonObjectFromQueryParameters(request);
                executionService.executeSync(jsonObject, context, new HttpServletResponseWriter(response));
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
            Map<String, Object> context = createContext(request);
            JsonObject jsonObjectFromBody = getJsonObjectFromBody(request);
            if (request.getQueryString() != null && !request.getQueryString().isEmpty()
                    && config.isAllowPostWithQueryParameters()) {
                JsonObject jsonObjectFromQueryParameters = getJsonObjectFromQueryParameters(request);
                JsonObject mergedJsonObject = Json.createMergePatch(jsonObjectFromQueryParameters).apply(jsonObjectFromBody)
                        .asJsonObject();
                executionService.executeSync(mergedJsonObject, context, new HttpServletResponseWriter(response));
            } else {
                executionService.executeSync(jsonObjectFromBody, context, new HttpServletResponseWriter(response));
            }
        } catch (IOException ex) {
            SmallRyeGraphQLServletLogging.log.ioException(ex);
            throw ex;
        }
    }

    private JsonObject getJsonObjectFromQueryParameters(HttpServletRequest request) throws UnsupportedEncodingException {
        JsonObjectBuilder input = Json.createObjectBuilder();
        // Query
        String query = request.getParameter(QUERY);
        if (query != null && !query.isEmpty()) {
            input.add(QUERY, URLDecoder.decode(query, "UTF8"));
        }
        // OperationName
        String operationName = request.getParameter(OPERATION_NAME);
        if (operationName != null && !operationName.isEmpty()) {
            input.add(OPERATION_NAME, URLDecoder.decode(query, "UTF8"));
        }

        // Variables
        String variables = request.getParameter(VARIABLES);
        if (variables != null && !variables.isEmpty()) {
            JsonObject jsonObject = toJsonObject(URLDecoder.decode(variables, "UTF8"));
            input.add(VARIABLES, jsonObject);
        }

        // Extensions
        String extensions = request.getParameter(EXTENSIONS);
        if (extensions != null && !extensions.isEmpty()) {
            JsonObject jsonObject = toJsonObject(URLDecoder.decode(extensions, "UTF8"));
            input.add(EXTENSIONS, jsonObject);
        }

        return input.build();
    }

    private JsonObject getJsonObjectFromBody(HttpServletRequest request) throws IOException {

        String contentType = request.getContentType();
        try (BufferedReader bufferedReader = request.getReader()) {
            // If the content type is application/graphql, the query is in the body
            if (contentType != null && contentType.startsWith(APPLICATION_GRAPHQL)) {
                String query = bufferedReader.lines().collect(Collectors.joining("\n"));
                JsonObjectBuilder input = Json.createObjectBuilder();
                input.add(QUERY, query);
                return input.build();
                // Else we expect a Json in the content
            } else {
                try (JsonReader jsonReader = jsonReaderFactory.createReader(bufferedReader)) {
                    return jsonReader.readObject();
                }
            }
        }
    }

    private static JsonObject toJsonObject(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.readObject();
        }
    }

    private Map<String, Object> createContext(HttpServletRequest request) {
        Map<String, Object> context = new HashMap<>();
        String tracingHeader = request.getHeader(FEDERATED_TRACING_HEADER_NAME);
        if (tracingHeader != null) {
            context.put(FEDERATED_TRACING_HEADER_NAME, request.getHeader(FEDERATED_TRACING_HEADER_NAME));
        }
        return context;
    }

    private static final String QUERY = "query";
    private static final String OPERATION_NAME = "operationName";
    private static final String VARIABLES = "variables";
    private static final String EXTENSIONS = "extensions";
    private static final String APPLICATION_GRAPHQL = "application/graphql";

}
