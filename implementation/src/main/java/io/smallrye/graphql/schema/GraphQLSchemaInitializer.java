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

package io.smallrye.graphql.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.helper.AnnotationsHelper;
import io.smallrye.graphql.helper.DefaultValueHelper;
import io.smallrye.graphql.helper.DescriptionHelper;
import io.smallrye.graphql.helper.NameHelper;
import io.smallrye.graphql.helper.NonNullHelper;
import io.smallrye.graphql.helper.creator.OutputTypeCreator;
import io.smallrye.graphql.holder.AnnotationsHolder;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;

/**
 * Creates the GraphQL Schema
 * TODO: Check that class is annotated with GraphQLApi ?
 * TODO: Check duplication with TypeMappingInitializer
 * TODO: Exceptions
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLSchemaInitializer {
    private static final Logger LOG = Logger.getLogger(GraphQLSchemaInitializer.class.getName());

    @Inject
    private Index index;

    @Inject
    private OutputTypeCreator outputTypeCreator;

    @Inject
    private Map<DotName, GraphQLInputObjectType> inputObjectMap;

    @Inject
    private Map<DotName, GraphQLObjectType> outputObjectMap;

    @Inject
    private Map<DotName, GraphQLEnumType> enumMap;

    @Inject
    private NonNullHelper nonNullHelper;

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    @Inject
    @ConfigProperty(name = "mp.graphql.queryRootDescription", defaultValue = "Query root")
    private String queryRootDescription;

    @Inject
    @ConfigProperty(name = "mp.graphql.mutationRootDescription", defaultValue = "Mutation root")
    private String mutationRootDescription;

    @Inject
    private AnnotationsHelper annotationsHelper;

    @Inject
    private DefaultValueHelper defaultValueHelper;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    public GraphQLSchema createGraphQLSchema() {
        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
        LOG.error("We have " + inputObjectMap.size() + " input objects");
        LOG.error("We have " + outputObjectMap.size() + " output objects");
        LOG.error("We have " + enumMap.size() + " enums");
        LOG.error("We have " + scalarMap.size() + " scalars");

        Set<GraphQLType> additionalTypes = new HashSet<>();
        additionalTypes.addAll(enumMap.values());
        additionalTypes.addAll(outputObjectMap.values());
        additionalTypes.addAll(inputObjectMap.values());
        schemaBuilder.additionalTypes(additionalTypes);

        GraphQLObjectType allQueries = createGraphQLObjectType(Annotations.QUERY, QUERY, queryRootDescription);
        GraphQLObjectType allMutations = createGraphQLObjectType(Annotations.MUTATION, MUTATION, mutationRootDescription);
        schemaBuilder.query(allQueries);
        schemaBuilder.mutation(allMutations);

        return schemaBuilder.build();
    }

    private GraphQLObjectType createGraphQLObjectType(DotName annotationToScan, String name, String description) {
        List<AnnotationInstance> graphQLAnnotations = this.index.getAnnotations(annotationToScan);

        GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject().name(name).description(description);

        for (AnnotationInstance graphQLAnnotation : graphQLAnnotations) {
            switch (graphQLAnnotation.target().kind()) {
                case METHOD:

                    MethodInfo methodInfo = graphQLAnnotation.target().asMethod();

                    // Annotations on this method
                    AnnotationsHolder annotations = annotationsHelper.getAnnotationsForMethod(methodInfo);

                    Type returnType = methodInfo.returnType();

                    GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
                    // Name // TODO: Need test cases for this
                    builder = builder
                            .name(nameHelper.getExecutionTypeName(graphQLAnnotation, annotations));

                    // Description
                    Optional<String> maybeDescription = descriptionHelper.getDescription(annotations);
                    if (maybeDescription.isPresent()) {
                        builder = builder.description(maybeDescription.get());
                    }
                    // Type (output)
                    builder = builder
                            .type(outputTypeCreator.createGraphQLOutputType(returnType, annotations));

                    // Arguments (input)
                    List<Type> parameters = methodInfo.parameters();

                    short cnt = 0;
                    for (Type parameter : parameters) {
                        builder.argument(toGraphQLArgument(methodInfo, cnt, parameter, annotations));
                        cnt++;
                    }

                    queryTypeBuilder = queryTypeBuilder.field(builder.build());
                    break;
            }
        }

        return queryTypeBuilder.build();
    }

    private GraphQLArgument toGraphQLArgument(MethodInfo methodInfo, short argCount, Type parameter,
            AnnotationsHolder annotations) {
        AnnotationsHolder annotationsForThisArgument = annotationsHelper.getAnnotationsForArgument(methodInfo, argCount);

        String argName = nameHelper.getArgumentName(annotationsForThisArgument, argCount);
        GraphQLInputType inputType = toGraphQLInputType(parameter, methodInfo.name(), annotations);

        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument();
        argumentBuilder = argumentBuilder.name(argName);
        argumentBuilder = argumentBuilder.type(inputType);
        Optional<Object> maybeDefaultValue = defaultValueHelper.getDefaultValue(annotationsForThisArgument);
        if (maybeDefaultValue.isPresent()) {
            argumentBuilder = argumentBuilder.defaultValue(maybeDefaultValue.get());
        }

        return argumentBuilder.build();
    }

    private GraphQLInputType toGraphQLInputType(Type type, String name, AnnotationsHolder annotations) {
        // Type
        switch (type.kind()) {
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                return GraphQLList.list(toGraphQLInputType(typeInArray, name, annotations));
            case PARAMETERIZED_TYPE:
                // TODO: Check if there is more than one type in the Collection, throw an exception ?
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return GraphQLList.list(toGraphQLInputType(typeInCollection, name, annotations));
            case CLASS:
                Optional<GraphQLInputType> maybeInput = getGraphQLInputType(type);
                if (maybeInput.isPresent()) {
                    if (nonNullHelper.markAsNonNull(type, annotations)) {
                        return GraphQLNonNull.nonNull(maybeInput.get());
                    } else {
                        return maybeInput.get();
                    }
                } else {
                    return getNoMappingScalarType(type);
                }
            case PRIMITIVE:
                Optional<GraphQLScalarType> maybeScalar = toGraphQLScalarType(type);
                if (maybeScalar.isPresent()) {
                    return GraphQLNonNull.nonNull(maybeScalar.get());
                } else {
                    return getNoMappingScalarType(type);
                }
                // TODO: check the other kinds (Maps, Interface, Unions etc)    
            default:
                return getNoMappingScalarType(type);
        }
    }

    private GraphQLScalarType getNoMappingScalarType(Type type) {
        // TODO: How should this be handled ? Exception or default Object scalar ?
        LOG.warn("Can not get field type mapping for " + type.name() + " of kind " + type.kind()
                + " - default to String");
        return Scalars.GraphQLString;
    }

    private Optional<GraphQLInputType> getGraphQLInputType(Type type) {
        Optional<GraphQLScalarType> maybeScalar = toGraphQLScalarType(type);
        Optional<GraphQLEnumType> maybeEnum = toGraphQLEnumType(type);
        Optional<GraphQLInputObjectType> maybeObject = toGraphQLInputObjectType(type);

        if (maybeScalar.isPresent()) {
            return Optional.of(maybeScalar.get());
        } else if (maybeEnum.isPresent()) {
            return Optional.of(maybeEnum.get());
        } else if (maybeObject.isPresent()) {
            return Optional.of(maybeObject.get());
        }
        return Optional.empty();
    }

    private Optional<GraphQLScalarType> toGraphQLScalarType(Type type) {
        // ID - First check if the type is set with an annotation like @Id 
        // TODO: What if this annotation is not on a scalar ??
        if (hasIdAnnotation(type)) {
            return Optional.of(Scalars.GraphQLID);
        }

        // Scalar - Else get the type from the mappings
        DotName dotName = type.name();
        if (scalarMap.containsKey(dotName)) {
            return Optional.of(scalarMap.get(dotName));
        }

        return Optional.empty();
    }

    private Optional<GraphQLEnumType> toGraphQLEnumType(Type type) {
        DotName dotName = type.name();
        if (enumMap.containsKey(dotName)) {
            ClassInfo clazz = index.getClassByName(dotName);

            if (Classes.isEnum(clazz)) {
                GraphQLEnumType enumtype = enumMap.get(dotName);
                return Optional.of(enumtype);
            }
        }
        return Optional.empty();
    }

    private Optional<GraphQLInputObjectType> toGraphQLInputObjectType(Type type) {
        DotName dotName = type.name();
        if (inputObjectMap.containsKey(dotName)) {
            ClassInfo clazz = index.getClassByName(dotName);
            if (!Classes.isEnum(clazz)) {
                GraphQLInputObjectType objectType = inputObjectMap.get(dotName);
                return Optional.of(objectType);
            }
        }

        return Optional.empty();
    }

    private boolean hasIdAnnotation(Type type) {
        // See if there is a @Id Annotation on return type
        List<AnnotationInstance> annotations = type.annotations();
        for (AnnotationInstance annotation : annotations) {
            if (annotation.name().equals(Annotations.ID)) {
                return true;
            }
        }
        return false;
    }

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";

}
