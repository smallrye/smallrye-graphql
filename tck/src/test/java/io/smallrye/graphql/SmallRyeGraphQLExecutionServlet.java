/*
 * Copyright 2019 Red Hat, Inc.
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

package io.smallrye.graphql;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.ExecutionService;

/**
 * Executing the GraphQL request
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLExecutionServlet", urlPatterns = { "graphql/*" }, loadOnStartup = 1)
public class SmallRyeGraphQLExecutionServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLExecutionServlet.class.getName());

    @Inject
    private ExecutionService executionService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final JsonReader jsonReader = Json.createReader(request.getReader());
        JsonObject jsonInput = jsonReader.readObject();

        JsonObject outputJson = executionService.execute(jsonInput);

        PrintWriter out = response.getWriter();
        response.setContentType(APPLICATION_JSON_UTF8);

        final JsonWriter jsonWriter = Json.createWriter(out);
        jsonWriter.writeObject(outputJson);
        out.flush();
    }

    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";
}
