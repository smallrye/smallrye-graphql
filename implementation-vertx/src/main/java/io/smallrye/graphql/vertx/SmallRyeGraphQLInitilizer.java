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
package io.smallrye.graphql.vertx;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.jboss.logging.Logger;
import org.jboss.weld.vertx.web.WebRoute;

import io.smallrye.graphql.bootstrap.SmallRyeGraphQLBootstrap;
import io.smallrye.graphql.execution.BootstrapResults;
import io.smallrye.graphql.execution.ExecutionException;
import io.smallrye.graphql.execution.ExecutionService;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler for the schema and the execution
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class SmallRyeGraphQLInitilizer {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLInitilizer.class.getName());

    @Inject
    private ExecutionService executionService;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        SmallRyeGraphQLBootstrap bootstrap = new SmallRyeGraphQLBootstrap();
        bootstrap.bootstrap();
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        LOG.info("SmallRye GraphQL Server stoped");
    }

    @WebRoute(value = "/graphql/schema.graphql", methods = HttpMethod.GET)
    public void getSchema(@Observes RoutingContext ctx) {
        ctx.response().setStatusCode(200).end(BootstrapResults.graphQLSchemaString);
    }

    @WebRoute(value = "/graphql", methods = HttpMethod.POST)
    public void executeRequest(@Observes RoutingContext ctx) {
        String body = ctx.getBodyAsString();
        try (Reader input = new StringReader(body);
                JsonReader jsonReader = Json.createReader(input)) {

            JsonObject jsonInput = jsonReader.readObject();

            JsonObject outputJson = executionService.execute(jsonInput);
            if (outputJson != null) {
                try (StringWriter output = new StringWriter();
                        JsonWriter jsonWriter = Json.createWriter(output)) {
                    jsonWriter.writeObject(outputJson);
                    output.flush();

                    ctx.response().setStatusCode(200).end(output.toString());
                }
            }
        } catch (IOException ex) {
            throw new ExecutionException(ex);
        }
    }
}
