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
package io.smallrye.graphql.helper.creator;

import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import graphql.Scalars;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.helper.NameHelper;
import io.smallrye.graphql.helper.NonNullHelper;
import io.smallrye.graphql.holder.AnnotationsHolder;
import io.smallrye.graphql.holder.TypeHolder;
import io.smallrye.graphql.index.Annotations;

/**
 * Create a graphql-java GraphQLOutputType
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class OutputTypeCreator {

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    @Inject
    private Map<DotName, GraphQLEnumType> enumMap;

    @Inject
    private NameHelper nameHelper;

    @Inject
    @Named("output")
    private Map<DotName, TypeHolder> outputClasses;

    @Inject
    private NonNullHelper nonNullHelper;

    public GraphQLOutputType createGraphQLOutputType(Type type, AnnotationsHolder annotations) {
        if (nonNullHelper.markAsNonNull(type, annotations)) {
            return GraphQLNonNull.nonNull(toGraphQLOutputType(type, annotations));
        } else {
            return toGraphQLOutputType(type, annotations);
        }

        // TODO: Deprecate
        // fieldDefinitionBuilder.deprecate(description)

        // TODO: Directives ?
        // fieldDefinitionBuilder.withDirectives(directives) // TODO ?
    }

    private GraphQLOutputType toGraphQLOutputType(Type type, AnnotationsHolder annotations) {

        DotName fieldTypeName = type.name();

        if (annotations.containsOnOfTheseKeys(Annotations.ID)) {
            // ID
            return Scalars.GraphQLID;
        } else if (scalarMap.containsKey(fieldTypeName)) {
            // Scalar
            return scalarMap.get(fieldTypeName);
        } else if (enumMap.containsKey(fieldTypeName)) {
            // Enum  
            return enumMap.get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            return GraphQLList.list(toGraphQLOutputType(typeInArray, annotations));
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLOutputType(typeInCollection, annotations));
        } else if (outputClasses.containsKey(type.name())) {
            String name = nameHelper.getOutputTypeName(outputClasses.get(type.name()));
            return GraphQLTypeReference.typeRef(name);
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with " + type);
        }
    }
}
