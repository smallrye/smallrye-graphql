package io.smallrye.graphql.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(ExecutionServlet.class.getName());
    private static final boolean isDebugEnabled = LOG.isDebugEnabled();

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (config.isAllowGet()) {
            try (StringReader reader = new StringReader(request.getParameter(QUERY))) {
                handleInput(reader, response);
            }
        } else {
            try {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET Queries is not enabled");
            } catch (IOException ex) {
                LOG.log(Logger.Level.ERROR, null, ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try (BufferedReader reader = request.getReader()) {
            handleInput(reader, response);
        } catch (IOException ex) {
            LOG.log(Logger.Level.ERROR, null, ex);
        }
    }

    private void handleInput(Reader inputReader, HttpServletResponse response) {
        if (isDebugEnabled) {
            inputReader = logInputReader(inputReader);
        }

        try (JsonReader jsonReader = jsonReaderFactory.createReader(inputReader)) {

            JsonObject jsonInput = jsonReader.readObject();

            JsonObject outputJson = executionService.execute(jsonInput);
            if (outputJson != null) {
                ServletOutputStream out = response.getOutputStream();
                response.setContentType(APPLICATION_JSON_UTF8);

                try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(out)) {
                    jsonWriter.writeObject(outputJson);
                    out.flush();
                }
            }
        } catch (Exception ex) {
            LOG.log(Logger.Level.ERROR, null, ex);
        }
    }

    private static Reader logInputReader(Reader inputReader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int len;
            while ((len = inputReader.read(buf)) > -1) {
                sb.append(buf, 0, len);
            }
            String jsonInput = sb.toString();
            LOG.debugf("JSON input: %s", jsonInput);
            inputReader = new StringReader(jsonInput);
        } catch (Exception e) {
            LOG.warnf(e, "Unable to log reader, %s", inputReader);
        }
        return inputReader;
    }

    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    private static final String QUERY = "query";
}
