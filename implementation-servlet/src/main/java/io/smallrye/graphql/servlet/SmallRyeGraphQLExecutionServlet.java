/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.graphql.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.execution.GraphQLConfig;

/**
 * Executing the GraphQL request
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLExecutionServlet", urlPatterns = { "/graphql/*" }, loadOnStartup = 1)
public class SmallRyeGraphQLExecutionServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLExecutionServlet.class.getName());

    @Inject
    ExecutionService executionService;

    @Inject
    GraphQLConfig config;

    public SmallRyeGraphQLExecutionServlet() {
    }

    public SmallRyeGraphQLExecutionServlet(ExecutionService executionService, GraphQLConfig config) {
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
        try (JsonReader jsonReader = Json.createReader(inputReader)) {
            JsonObject jsonInput = jsonReader.readObject();

            JsonObject outputJson = executionService.execute(jsonInput);
            if (outputJson != null) {
                PrintWriter out = response.getWriter();
                response.setContentType(APPLICATION_JSON_UTF8);

                try (JsonWriter jsonWriter = Json.createWriter(out)) {
                    jsonWriter.writeObject(outputJson);
                    out.flush();
                }
            }
        } catch (IOException ex) {
            LOG.log(Logger.Level.ERROR, null, ex);
        }
    }

    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    private static final String QUERY = "query";
}
