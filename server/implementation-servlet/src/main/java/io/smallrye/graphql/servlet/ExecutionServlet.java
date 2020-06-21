package io.smallrye.graphql.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLDecoder;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.smallrye.graphql.cdi.config.GraphQLConfig;
import io.smallrye.graphql.execution.ExecutionService;

/**
 * Executing the GraphQL request
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLExecutionServlet", urlPatterns = { "/graphql/*" }, loadOnStartup = 1)
public class ExecutionServlet extends HttpServlet {
    private static final long serialVersionUID = -2859915918802356120L;

    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
    private static final JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(null);

    @Inject
    ExecutionService executionService;

    @Inject
    GraphQLConfig config;

    public ExecutionServlet() {
    }

    public ExecutionServlet(ExecutionService executionService, GraphQLConfig config) {
        this.executionService = executionService;
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (config.isAllowGet()) {
                String query = request.getParameter(QUERY);
                String variables = request.getParameter(VARIABLES);

                JsonObjectBuilder input = Json.createObjectBuilder();
                input.add(QUERY, URLDecoder.decode(query, "UTF8"));
                if (variables != null && !variables.isEmpty()) {
                    JsonObject jsonObject = toJsonObject(URLDecoder.decode(variables, "UTF8"));
                    input.add(VARIABLES, jsonObject);
                }
                handleInput(input.build(), response);
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
        try (BufferedReader reader = request.getReader()) {
            handleInput(reader, response);
        } catch (IOException ex) {
            SmallRyeGraphQLServletLogging.log.ioException(ex);
            throw ex;
        }
    }

    private void handleInput(Reader inputReader, HttpServletResponse response) throws IOException {
        try (JsonReader jsonReader = jsonReaderFactory.createReader(inputReader)) {
            JsonObject jsonInput = jsonReader.readObject();
            handleInput(jsonInput, response);
        }
    }

    private void handleInput(JsonObject jsonInput, HttpServletResponse response) throws IOException {
        JsonObject outputJson = executionService.execute(jsonInput);
        if (outputJson != null) {
            ServletOutputStream out = response.getOutputStream();
            response.setContentType(APPLICATION_JSON_UTF8);

            try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(out)) {
                jsonWriter.writeObject(outputJson);
                out.flush();
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

    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";
}
