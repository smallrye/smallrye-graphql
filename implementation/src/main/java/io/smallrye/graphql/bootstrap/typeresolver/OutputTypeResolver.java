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
package io.smallrye.graphql.bootstrap.typeresolver;

import org.jboss.jandex.DotName;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import io.smallrye.graphql.bootstrap.ObjectBag;

/**
 * Just fetch output types from Object bag
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class OutputTypeResolver implements TypeResolver {

    private final DotName interfaceName;

    public OutputTypeResolver(DotName interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment tre) {

        DotName lookingForContrete = DotName.createSimple(tre.getObject().getClass().getName());
        if (ObjectBag.TYPE_MAP.containsKey(lookingForContrete)) {
            return GraphQLObjectType.class.cast(ObjectBag.TYPE_MAP.get(lookingForContrete));
        } else {
            throw new ConcreteImplementationNotFoundException(
                    "No concrete class named [" + lookingForContrete + "] found for interface ["
                            + interfaceName + "]");
        }
    }

}
