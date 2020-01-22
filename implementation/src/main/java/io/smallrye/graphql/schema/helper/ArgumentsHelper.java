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

package io.smallrye.graphql.schema.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputType;
import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.type.InputTypeCreator;

/**
 * Helping with Arguments
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class ArgumentsHelper {
    @Inject
    private InputTypeCreator inputTypeCreator;

    @Inject
    private DefaultValueHelper defaultValueHelper;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private AnnotationsHelper annotationsHelper;

    public List<GraphQLArgument> toGraphQLArguments(MethodInfo methodInfo, Annotations annotations) {
        return toGraphQLArguments(methodInfo, annotations, false);
    }

    public List<GraphQLArgument> toGraphQLArguments(MethodInfo methodInfo, Annotations annotations,
            boolean ignoreSourceArgument) {
        List<Type> parameters = methodInfo.parameters();
        List<GraphQLArgument> r = new ArrayList<>();
        short cnt = 0;
        for (Type parameter : parameters) {
            Optional<GraphQLArgument> graphQLArgument = toGraphQLArgument(methodInfo, cnt, parameter, annotations,
                    ignoreSourceArgument);
            if (graphQLArgument.isPresent()) {
                r.add(graphQLArgument.get());
            }
            cnt++;
        }
        return r;
    }

    private Optional<GraphQLArgument> toGraphQLArgument(MethodInfo methodInfo, short argCount, Type parameter,
            Annotations annotations, boolean ignoreSourceArgument) {
        Annotations annotationsForThisArgument = annotationsHelper.getAnnotationsForArgument(methodInfo, argCount);

        if (ignoreSourceArgument && annotationsForThisArgument.containsOneOfTheseKeys(Annotations.SOURCE)) {
            return Optional.empty();
        } else {
            String defaultName = methodInfo.parameterName(argCount);
            String argName = nameHelper.getArgumentName(annotationsForThisArgument, defaultName);
            GraphQLInputType inputType = inputTypeCreator.createGraphQLInputType(parameter, annotations);

            GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument();
            argumentBuilder = argumentBuilder.name(argName);
            argumentBuilder = argumentBuilder.type(inputType);
            Optional<Object> maybeDefaultValue = defaultValueHelper.getDefaultValue(annotationsForThisArgument);
            argumentBuilder = argumentBuilder.defaultValue(maybeDefaultValue.orElse(null));

            return Optional.of(argumentBuilder.build());
        }
    }

    public List<Argument> toArguments(MethodInfo methodInfo) {
        return toArguments(methodInfo, false);
    }

    public List<Argument> toArguments(MethodInfo methodInfo, boolean ignoreSourceArgument) {
        List<Type> parameters = methodInfo.parameters();
        List<Argument> r = new ArrayList<>();
        short cnt = 0;
        for (Type parameter : parameters) {
            Optional<Argument> graphQLArgument = toArgument(methodInfo, cnt, parameter,
                    ignoreSourceArgument);
            if (graphQLArgument.isPresent()) {
                r.add(graphQLArgument.get());
            }
            cnt++;
        }
        return r;
    }

    private Optional<Argument> toArgument(MethodInfo methodInfo, short argCount, Type parameter,
            boolean ignoreSourceArgument) {
        Annotations annotationsForThisArgument = annotationsHelper.getAnnotationsForArgument(methodInfo, argCount);

        if (ignoreSourceArgument && annotationsForThisArgument.containsOneOfTheseKeys(Annotations.SOURCE)) {
            return Optional.empty();
        } else {
            Argument argument = new Argument();
            String defaultName = methodInfo.parameterName(argCount);
            String name = nameHelper.getArgumentName(annotationsForThisArgument, defaultName);
            argument.setName(name);
            argument.setType(parameter);
            argument.setAnnotations(annotationsForThisArgument);
            return Optional.of(argument);
        }
    }

}
