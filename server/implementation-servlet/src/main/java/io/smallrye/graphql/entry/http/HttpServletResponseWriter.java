package io.smallrye.graphql.entry.http;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.servlet.http.HttpServletResponse;

import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionResponseWriter;

/**
 * Writing the response to HTTP servlet
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class HttpServletResponseWriter implements ExecutionResponseWriter {
    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";
    private static final JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(null);

    private HttpServletResponse response;

    public HttpServletResponseWriter(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public void write(ExecutionResponse executionResponse) {
        if (executionResponse != null) {
            try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(response.getOutputStream())) {
                response.setContentType(APPLICATION_JSON_UTF8);
                jsonWriter.writeObject(executionResponse.getExecutionResultAsJsonObject());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
