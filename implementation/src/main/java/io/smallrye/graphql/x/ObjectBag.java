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
package io.smallrye.graphql.x;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.bind.Jsonb;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.x.type.number.BigDecimalScalar;
import io.smallrye.graphql.x.type.number.BigIntegerScalar;
import io.smallrye.graphql.x.type.number.FloatScalar;
import io.smallrye.graphql.x.type.number.IntegerScalar;
import io.smallrye.graphql.x.type.time.DateScalar;
import io.smallrye.graphql.x.type.time.DateTimeScalar;
import io.smallrye.graphql.x.type.time.TimeScalar;

/**
 * Here we keep all the objects we know about
 * 
 * This is mostly done so that is can be use in Quarkus deployment step
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ObjectBag {

    // Some maps we populate during scanning
    private final Map<DotName, GraphQLInputType> INPUT_MAP = new HashMap<>();
    private final Map<DotName, GraphQLOutputType> TYPE_MAP = new HashMap<>();
    private final Map<DotName, GraphQLEnumType> ENUM_MAP = new HashMap<>();
    private final Map<DotName, GraphQLInterfaceType> INTERFACE_MAP = new HashMap<>();
    private final List<ClassInfo> TYPE_TODO_LIST = new ArrayList<>();
    private final List<ClassInfo> INPUT_TYPE_TODO_LIST = new ArrayList<>();
    private final Map<DotName, Jsonb> INPUT_JSON_MAP = new HashMap<>();
    private final Map<DotName, Map<String, Argument>> ARGUMENT_MAP = new HashMap<>();
    private final GraphQLCodeRegistry.Builder CODE_REGISTRY_BUILDER = GraphQLCodeRegistry.newCodeRegistry();

    // Scalar map we can just create now.
    public final Map<DotName, GraphQLScalarType> SCALAR_MAP = new HashMap<>();

    public ObjectBag() {

        SCALAR_MAP.put(DotName.createSimple(char.class.getName()), Scalars.GraphQLString);
        SCALAR_MAP.put(DotName.createSimple(Character.class.getName()), Scalars.GraphQLString);

        SCALAR_MAP.put(DotName.createSimple(String.class.getName()), Scalars.GraphQLString);
        SCALAR_MAP.put(DotName.createSimple(UUID.class.getName()), Scalars.GraphQLString);
        SCALAR_MAP.put(DotName.createSimple(URL.class.getName()), Scalars.GraphQLString);
        SCALAR_MAP.put(DotName.createSimple(URI.class.getName()), Scalars.GraphQLString);

        SCALAR_MAP.put(DotName.createSimple(Boolean.class.getName()), Scalars.GraphQLBoolean);
        SCALAR_MAP.put(DotName.createSimple(boolean.class.getName()), Scalars.GraphQLBoolean);

        mapType(new IntegerScalar());
        mapType(new FloatScalar());
        mapType(new BigIntegerScalar());
        mapType(new BigDecimalScalar());
        mapType(new DateScalar());
        mapType(new TimeScalar());
        mapType(new DateTimeScalar());
    }

    private void mapType(Transformable transformable) {
        for (Class c : transformable.getSupportedClasses()) {
            SCALAR_MAP.put(DotName.createSimple(c.getName()), (GraphQLScalarType) transformable);
        }
    }

    public Map<DotName, GraphQLInputType> getInputMap() {
        return INPUT_MAP;
    }

    public Map<DotName, GraphQLOutputType> getTypeMap() {
        return TYPE_MAP;
    }

    public Map<DotName, GraphQLEnumType> getEnumMap() {
        return ENUM_MAP;
    }

    public Map<DotName, GraphQLInterfaceType> getInterfaceMap() {
        return INTERFACE_MAP;
    }

    public List<ClassInfo> getTypeTodoList() {
        return TYPE_TODO_LIST;
    }

    public List<ClassInfo> getInputTypeTodoList() {
        return INPUT_TYPE_TODO_LIST;
    }

    public Map<DotName, Jsonb> getInputJsonMap() {
        return INPUT_JSON_MAP;
    }

    public Map<DotName, Map<String, Argument>> getArgumentMap() {
        return ARGUMENT_MAP;
    }

    public GraphQLCodeRegistry.Builder getCodeRegistryBuilder() {
        return CODE_REGISTRY_BUILDER;
    }

    public Map<DotName, GraphQLScalarType> getScalarMap() {
        return SCALAR_MAP;
    }
}
