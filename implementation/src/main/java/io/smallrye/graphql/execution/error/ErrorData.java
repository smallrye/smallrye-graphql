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
package io.smallrye.graphql.execution.error;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

/**
 * Holding error data
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ErrorData {
    private final List<JsonObject> errors = new ArrayList<>();
    private final List<Object> partialResults = new ArrayList<>();

    public void add(JsonObject error) {
        add(error, null);
    }

    public void add(JsonObject error, Object partialResult) {
        if (error != null)
            this.errors.add(error);
        if (partialResult != null)
            this.partialResults.add(partialResult);
    }

    public JsonArray getErrorsInJsonArrayFormat() {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (JsonObject e : errors) {
            arrayBuilder.add(e);
        }
        return arrayBuilder.build();
    }

    public List<Object> getPartialResults() {
        return partialResults;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasPartialResults() {
        return !partialResults.isEmpty();
    }
}
