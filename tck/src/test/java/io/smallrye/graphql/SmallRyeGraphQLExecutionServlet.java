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
import java.io.StringReader;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.ExecutionId;
import graphql.schema.GraphQLSchema;

/**
 * Executing the GraphQL request
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLExecutionServlet", urlPatterns = { "graphql/*" }, loadOnStartup = 1)
public class SmallRyeGraphQLExecutionServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLExecutionServlet.class.getName());

    @Inject
    private GraphQLSchema graphQLSchema;

    private GraphQL graphQL;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOG.error("======================================================");

        final JsonReader jsonReader = Json.createReader(request.getReader());

        JsonObject jsonInput = jsonReader.readObject();

        String query = jsonInput.getString("query");

        LOG.error(query);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                //.operationName(graphQLRequest.getOperationName())
                //.context(context)
                //.root(root)
                //.variables(graphQLRequest.getVariables())
                //.dataLoaderRegistry(context.getDataLoaderRegistry().orElse(new DataLoaderRegistry()))
                .executionId(ExecutionId.generate())
                .build();

        ExecutionResult executionResult = this.graphQL.execute(executionInput);

        //Map<String, Object> toSpecificationResult = executionResult.toSpecification();

        LOG.error("\n\n\n");
        Object data = executionResult.getData();

        JsonObject outputJson = Json.createObjectBuilder()
                .add("data", getJsonObject(data))
                .build();

        PrintWriter out = response.getWriter();
        response.setContentType(APPLICATION_JSON_UTF8);

        final JsonWriter jsonWriter = Json.createWriter(out);
        jsonWriter.writeObject(outputJson);
        out.flush();

        LOG.error("======================================================");
        //List<GraphQLError> errors = executionResult.getErrors();

    }

    private JsonObject getJsonObject(Object pojo) {
        JsonbConfig jsonbConfig = new JsonbConfig();
        Jsonb jsonb = JsonbBuilder.create(jsonbConfig);
        String json = jsonb.toJson(pojo);
        final JsonReader reader = Json.createReader(new StringReader(json));
        return reader.readObject();
    }

    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";
}
