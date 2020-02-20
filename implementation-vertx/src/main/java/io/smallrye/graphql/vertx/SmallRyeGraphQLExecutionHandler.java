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
import java.io.StringReader;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import io.smallrye.graphql.execution.ExecutionService;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Handle all execution requests
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */

public class SmallRyeGraphQLExecutionHandler implements Handler<RoutingContext> {

    @Inject
    private ExecutionService executionService;

    @Override
    public void handle(RoutingContext context) {
        String body = context.getBodyAsString();
        try (StringReader sr = new StringReader(body);
                JsonReader jsonReader = Json.createReader(sr)) {

            JsonObject jsonInput = jsonReader.readObject();

            JsonObject outputJson = executionService.execute(jsonInput);
            if (outputJson != null) {
                String jsonString = jsonToString(outputJson);
                context.response().setStatusCode(200).end(jsonString);
            }
        }
    }

    private String jsonToString(JsonObject outputJson) {
        try (StringWriter sw = new StringWriter();
                JsonWriter jsonWriter = Json.createWriter(sw)) {
            jsonWriter.writeObject(outputJson);
            sw.flush();
            return sw.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
