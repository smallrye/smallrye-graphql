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
package io.smallrye.graphql.execution.typeResolvers;

import java.util.Map;

import org.jboss.jandex.DotName;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.TypeResolver;

/**
 * Just fetch output types from Object bag
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class OutputTypeResolver implements TypeResolver {

    private final Map<DotName, GraphQLOutputType> typeMap;
    private final DotName interfaceName;

    public OutputTypeResolver(Map<DotName, GraphQLOutputType> typeMap, DotName interfaceName) {
        this.typeMap = typeMap;
        this.interfaceName = interfaceName;
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment tre) {

        DotName lookingForContrete = DotName.createSimple(tre.getObject().getClass().getName());
        if (typeMap.containsKey(lookingForContrete)) {
            return GraphQLObjectType.class.cast(typeMap.get(lookingForContrete));
        } else {
            throw new RuntimeException("No concrete class named [" + lookingForContrete + "] found for interface ["
                    + interfaceName + "]");
        }
    }

}
