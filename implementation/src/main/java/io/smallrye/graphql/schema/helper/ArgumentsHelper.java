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

package io.smallrye.graphql.schema.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputType;
import io.smallrye.graphql.schema.holder.AnnotationsHolder;
import io.smallrye.graphql.schema.type.InputTypeCreator;

/**
 * Helping with Arguments
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class ArgumentsHelper {
    private static final Logger LOG = Logger.getLogger(ArgumentsHelper.class.getName());

    @Inject
    private InputTypeCreator inputTypeCreator;

    @Inject
    private DefaultValueHelper defaultValueHelper;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private AnnotationsHelper annotationsHelper;

    public List<GraphQLArgument> toGraphQLArguments(MethodInfo methodInfo, AnnotationsHolder annotations) {
        List<Type> parameters = methodInfo.parameters();
        List<GraphQLArgument> r = new ArrayList<>();
        short cnt = 0;
        for (Type parameter : parameters) {
            r.add(toGraphQLArgument(methodInfo, cnt, parameter, annotations));
            cnt++;
        }
        return r;
    }

    private GraphQLArgument toGraphQLArgument(MethodInfo methodInfo, short argCount, Type parameter,
            AnnotationsHolder annotations) {
        AnnotationsHolder annotationsForThisArgument = annotationsHelper.getAnnotationsForArgument(methodInfo, argCount);

        String argName = nameHelper.getArgumentName(annotationsForThisArgument, argCount);
        GraphQLInputType inputType = inputTypeCreator.createGraphQLInputType(parameter, annotations);

        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument();
        argumentBuilder = argumentBuilder.name(argName);
        argumentBuilder = argumentBuilder.type(inputType);
        Optional<Object> maybeDefaultValue = defaultValueHelper.getDefaultValue(annotationsForThisArgument);
        argumentBuilder = argumentBuilder.defaultValue(maybeDefaultValue.orElse(null));

        return argumentBuilder.build();
    }
}
