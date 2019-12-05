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

package io.smallrye.graphql.execution;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

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
@RequestScoped // TODO: Dependent ?
public class ExecutionService {
    private static final Logger LOG = Logger.getLogger(ExecutionService.class.getName());

    @Inject
    private GraphQLSchema graphQLSchema;

    private GraphQL graphQL;

    @PostConstruct
    void init() {
        // TODO: We should only need this once.
        this.graphQL = GraphQL
                .newGraphQL(graphQLSchema)
                .build();
    }

    public JsonObject execute(JsonObject jsonInput) {

        LOG.error("======================================================");

        LOG.error(jsonInput);

        //        {
        //            "query":"mutation addHeroToTeam($heroName:String, $teamName:String) {\n    addHeroToTeam(hero: $heroName, team: $teamName) {\n        name\n        members {\n            name\n        }\n    }\n}",
        //            "variables":{"heroName":"Starlord","teamName":"Avengers"}
        //        }

        String query = jsonInput.getString("query");

        LOG.error(query);

        Map<String, Object> variables = toMap(jsonInput.getJsonObject("variables"));
        LOG.error(variables);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)

                //.operationName(graphQLRequest.getOperationName())
                //.context(context)
                //.root(root)
                .variables(variables)
                //.dataLoaderRegistry(context.getDataLoaderRegistry().orElse(new DataLoaderRegistry()))
                .executionId(ExecutionId.generate())
                .build();

        ExecutionResult executionResult = this.graphQL.execute(executionInput);

        //        Map<String, Object> specificationResult = executionResult.toSpecification();

        LOG.error("\n\n\n");
        Object pojo = executionResult.getData();
        //LOG.error(pojo);

        JsonObjectBuilder returnObjectBuilder = Json.createObjectBuilder();

        if (pojo != null) {
            JsonObject data = getJsonObject(pojo);
            returnObjectBuilder = returnObjectBuilder.add("data", data);
        } else {
            returnObjectBuilder = returnObjectBuilder.addNull("data");
        }
        return returnObjectBuilder.build();
    }

    private JsonObject getJsonObject(Object pojo) {
        JsonbConfig jsonbConfig = new JsonbConfig();
        Jsonb jsonb = JsonbBuilder.create(jsonbConfig);
        String json = jsonb.toJson(pojo);
        final JsonReader reader = Json.createReader(new StringReader(json));
        return reader.readObject();
    }

    private Map<String, Object> toMap(JsonObject jo) {
        Map<String, Object> ro = new HashMap<>();
        Set<Map.Entry<String, JsonValue>> entrySet = jo.entrySet();
        for (Map.Entry<String, JsonValue> es : entrySet) {
            ro.put(es.getKey(), toObject(es.getValue()));
        }
        return ro;
    }

    private Object toObject(JsonValue jsonValue) {
        Object ret = null;
        JsonValue.ValueType typ = jsonValue.getValueType();
        if (null != typ)
            switch (typ) {
                case NUMBER:
                    ret = ((JsonNumber) jsonValue).bigDecimalValue();
                    break;
                case STRING:
                    ret = ((JsonString) jsonValue).getString();
                    break;
                case FALSE:
                    ret = Boolean.FALSE;
                    break;
                case TRUE:
                    ret = Boolean.TRUE;
                    break;
                case ARRAY:
                    JsonArray arr = (JsonArray) jsonValue;
                    List<Object> vals = new ArrayList<>();
                    int sz = arr.size();
                    for (int i = 0; i < sz; i++) {
                        JsonValue v = arr.get(i);
                        vals.add(toObject(v));
                    }
                    ret = vals;
                    break;
                case OBJECT:
                    ret = jsonValue.toString();
                    break;
                default:
                    break;
            }
        return ret;
    }
}
