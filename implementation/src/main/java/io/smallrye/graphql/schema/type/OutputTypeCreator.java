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
package io.smallrye.graphql.schema.type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;

import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.execution.datafetcher.AnnotatedPropertyDataFetcher;
import io.smallrye.graphql.execution.datafetcher.ReflectionDataFetcher;
import io.smallrye.graphql.execution.typeresolver.OutputTypeResolver;
import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.ArgumentsHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;

/**
 * Create a graphql-java GraphQLOutputType
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class OutputTypeCreator implements Creator {
    private static final Logger LOG = Logger.getLogger(OutputTypeCreator.class.getName());

    @Inject
    private EnumTypeCreator enumTypeCreator;

    @Inject
    private Map<DotName, Jsonb> inputJsonbMap;

    @Inject
    private Map<DotName, Map<String, Argument>> argumentMap;

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    @Inject
    private IndexView index;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private NonNullHelper nonNullHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @Inject
    private IgnoreHelper ignoreHelper;

    @Inject
    private AnnotationsHelper annotationsHelper;

    @Inject
    private ArgumentsHelper argumentsHelper;

    @Inject
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Inject
    private Map<DotName, List<MethodParameterInfo>> sourceFields;

    @Inject
    private Map<DotName, GraphQLInterfaceType> interfaceMap;

    @Inject
    private Map<DotName, GraphQLOutputType> typeMap;

    @Inject
    private List<ClassInfo> typeTodoList;

    @Override
    public GraphQLType create(ClassInfo classInfo) {
        boolean isInterface = Modifier.isInterface(classInfo.flags());
        if (isInterface) {
            return createInterface(classInfo);
        } else {
            return createClass(classInfo);
        }
    }

    public GraphQLOutputType createGraphQLOutputType(Type type, Annotations annotations) {
        return createGraphQLOutputType(type, type, annotations);
    }

    private GraphQLOutputType createClass(ClassInfo classInfo) {
        if (typeMap.containsKey(classInfo.name())) {
            return typeMap.get(classInfo.name());
        } else {
            Annotations annotations = annotationsHelper.getAnnotationsForClass(classInfo);
            String name = nameHelper.getOutputTypeName(classInfo, annotations);
            GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject();
            objectTypeBuilder = objectTypeBuilder.name(name);

            // Description
            Optional<String> maybeDescription = descriptionHelper.getDescription(annotations);
            objectTypeBuilder = objectTypeBuilder.description(maybeDescription.orElse(null));

            // Fields
            objectTypeBuilder = objectTypeBuilder.fields(createGraphQLFieldDefinitions(classInfo, name));

            // Interfaces
            List<DotName> interfaceNames = classInfo.interfaceNames();
            for (DotName interfaceName : interfaceNames) {
                ClassInfo c = index.getClassByName(interfaceName);
                GraphQLInterfaceType i = createInterface(c);
                objectTypeBuilder = objectTypeBuilder.withInterface(i);
            }
            GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
            typeMap.put(classInfo.name(), graphQLObjectType);

            return graphQLObjectType;
        }
    }

    private GraphQLInterfaceType createInterface(ClassInfo classInfo) {
        if (interfaceMap.containsKey(classInfo.name())) {
            return interfaceMap.get(classInfo.name());
        } else {

            Annotations annotations = annotationsHelper.getAnnotationsForClass(classInfo);

            String name = nameHelper.getInterfaceName(classInfo, annotations);
            GraphQLInterfaceType.Builder interfaceTypeBuilder = GraphQLInterfaceType.newInterface();
            interfaceTypeBuilder = interfaceTypeBuilder.name(name);

            // Description
            Optional<String> maybeDescription = descriptionHelper.getDescription(annotations);
            interfaceTypeBuilder = interfaceTypeBuilder.description(maybeDescription.orElse(null));

            // Fields
            interfaceTypeBuilder = interfaceTypeBuilder.fields(createGraphQLMethodDefinitions(classInfo, name));

            GraphQLInterfaceType graphQLInterfaceType = interfaceTypeBuilder.build();

            // To resolve the concrete class
            codeRegistryBuilder.typeResolver(graphQLInterfaceType, new OutputTypeResolver(typeMap, classInfo.name()));

            interfaceMap.put(classInfo.name(), graphQLInterfaceType);

            // Also check that we create all implementations
            Collection<ClassInfo> knownDirectImplementors = index.getAllKnownImplementors(classInfo.name());
            for (ClassInfo impl : knownDirectImplementors) {
                if (!typeMap.containsKey(impl.name())) {
                    typeTodoList.add(impl);
                }
            }

            return graphQLInterfaceType;
        }

    }

    private GraphQLOutputType createGraphQLOutputType(Type type, Type getterReturnType, Annotations annotations) {
        if (nonNullHelper.markAsNonNull(type, annotations)) {
            return GraphQLNonNull.nonNull(toGraphQLOutputType(type, getterReturnType, annotations));
        } else {
            return toGraphQLOutputType(type, getterReturnType, annotations);
        }
    }

    private List<GraphQLFieldDefinition> createGraphQLMethodDefinitions(ClassInfo classInfo, String name) {
        List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
        List<MethodInfo> methods = classInfo.methods();

        for (MethodInfo method : methods) {
            if (nameHelper.isGetter(method.name())) {
                String fieldName = nameHelper.toNameFromGetter(method.name());
                // Annotations on the getter
                Annotations annotations = annotationsHelper.getAnnotationsForOutputField(method);

                if (!ignoreHelper.shouldIgnore(annotations)) {
                    GraphQLFieldDefinition.Builder builder = getGraphQLFieldDefinitionBuilder(annotations, fieldName,
                            method.returnType(), method.returnType());

                    GraphQLFieldDefinition graphQLFieldDefinition = builder.build();

                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, graphQLFieldDefinition.getName()),
                            new AnnotatedPropertyDataFetcher(fieldName, method.returnType(), annotations));

                    fieldDefinitions.add(graphQLFieldDefinition);
                }
            }
        }

        return fieldDefinitions;
    }

    private List<GraphQLFieldDefinition> createGraphQLFieldDefinitions(ClassInfo classInfo, String name) {
        List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
        List<FieldInfo> fields = classInfo.fields();

        for (FieldInfo field : fields) {
            // Check if there is a getter (for output) 
            Optional<MethodInfo> maybeGetter = getGetMethod(field.name(), classInfo);
            if (maybeGetter.isPresent()) {
                MethodInfo getter = maybeGetter.get();
                // Annotations on the field and getter
                Annotations annotations = annotationsHelper.getAnnotationsForOutputField(field, getter);

                if (!ignoreHelper.shouldIgnore(annotations)) {
                    GraphQLFieldDefinition.Builder builder = getGraphQLFieldDefinitionBuilder(annotations, field.name(),
                            field.type(), getter.returnType());

                    GraphQLFieldDefinition graphQLFieldDefinition = builder.build();

                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, graphQLFieldDefinition.getName()),
                            new AnnotatedPropertyDataFetcher(field.name(), field.type(), annotations));

                    fieldDefinitions.add(graphQLFieldDefinition);

                }
            }
        }

        // Also check for @Source fields
        if (sourceFields.containsKey(classInfo.name())) {
            List<MethodParameterInfo> methodParameterInfos = sourceFields.get(classInfo.name());
            for (MethodParameterInfo methodParameterInfo : methodParameterInfos) {
                MethodInfo methodInfo = methodParameterInfo.method();

                // Annotations on this method
                Annotations methodAnnotations = annotationsHelper.getAnnotationsForMethod(methodInfo,
                        AnnotationTarget.Kind.METHOD);
                if (!ignoreHelper.shouldIgnore(methodAnnotations)) {

                    Type type = methodParameterInfo.method().returnType();
                    GraphQLFieldDefinition.Builder builder = getGraphQLFieldDefinitionBuilder(methodAnnotations,
                            methodInfo.name(),
                            type, methodInfo.returnType());

                    // Arguments (input) except @Source
                    Annotations parameterAnnotations = annotationsHelper.getAnnotationsForMethod(methodInfo,
                            AnnotationTarget.Kind.METHOD_PARAMETER);
                    builder.arguments(argumentsHelper.toGraphQLArguments(methodInfo, parameterAnnotations, true));

                    GraphQLFieldDefinition graphQLFieldDefinition = builder.build();

                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, graphQLFieldDefinition.getName()),
                            new ReflectionDataFetcher(methodParameterInfo.method(),
                                    argumentsHelper.toArguments(methodInfo), inputJsonbMap, argumentMap, scalarMap));

                    fieldDefinitions.add(graphQLFieldDefinition);
                }
            }
        }

        return fieldDefinitions;
    }

    private GraphQLFieldDefinition.Builder getGraphQLFieldDefinitionBuilder(Annotations annotations, String fieldName,
            Type fieldType, Type getterReturnType) {
        GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
        // Name
        builder = builder.name(nameHelper.getOutputNameForField(annotations, fieldName));

        // Description
        Optional<String> maybeFieldDescription = descriptionHelper.getDescription(annotations, fieldType);
        builder = builder.description(maybeFieldDescription.orElse(null));

        // Type
        builder = builder
                .type(createGraphQLOutputType(fieldType, getterReturnType, annotations));

        return builder;
    }

    private GraphQLOutputType toGraphQLOutputType(Type type, Type methodReturnType, Annotations annotations) {
        DotName fieldTypeName = type.name();

        if (annotations.containsOneOfTheseKeys(Annotations.ID)) {
            // ID
            return Scalars.GraphQLID;
        } else if (scalarMap.containsKey(fieldTypeName)) {
            // Scalar
            return scalarMap.get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            Type typeInReturnList = methodReturnType.asArrayType().component();
            return toParameterizedGraphQLOutputType(typeInArray, typeInReturnList, annotations);
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            Type typeInReturnList = methodReturnType.asParameterizedType().arguments().get(0);
            return toParameterizedGraphQLOutputType(typeInCollection, typeInReturnList, annotations);
        } else if (type.kind().equals(Type.Kind.CLASS)) {

            ClassInfo classInfo = index.getClassByName(fieldTypeName);
            if (classInfo != null) {
                if (Classes.isEnum(classInfo)) {
                    return enumTypeCreator.create(classInfo);
                } else if (typeMap.containsKey(fieldTypeName)) {
                    return typeMap.get(fieldTypeName);
                } else {
                    typeTodoList.add(classInfo);
                    Annotations annotationsForThisClass = annotationsHelper.getAnnotationsForClass(classInfo);
                    String name = nameHelper.getOutputTypeName(classInfo, annotationsForThisClass);
                    return GraphQLTypeReference.typeRef(name);
                }
            } else {
                LOG.warn("Class [" + type.name()
                        + "] in not indexed in Jandex. Can not create Type, defaulting to String Scalar");
                return Scalars.GraphQLString; // default
            }
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new SchemaTypeCreateException("Don't know what to do with [" + type + "] of kind [" + type.kind() + "]");
        }

    }

    private GraphQLOutputType toParameterizedGraphQLOutputType(Type typeInCollection, Type getterReturnType,
            Annotations annotations) {
        Annotations annotationsInParameterizedType = annotationsHelper.getAnnotationsForType(typeInCollection,
                getterReturnType);

        if (nonNullHelper.markAsNonNull(typeInCollection, annotationsInParameterizedType, true)) {
            return GraphQLList
                    .list(GraphQLNonNull.nonNull(toGraphQLOutputType(typeInCollection, getterReturnType, annotations)));
        } else {
            return GraphQLList.list(toGraphQLOutputType(typeInCollection, getterReturnType, annotations));
        }
    }

    private Optional<MethodInfo> getGetMethod(String forField, ClassInfo classInfo) {
        String get = GET + forField;
        String is = IS + forField;
        List<MethodInfo> methods = classInfo.methods();
        for (MethodInfo methodInfo : methods) {
            if (methodInfo.name().equalsIgnoreCase(get) || methodInfo.name().equalsIgnoreCase(is)) {
                return Optional.of(methodInfo);
            }
        }
        return Optional.empty();
    }

    private static final String GET = "get";
    private static final String IS = "is";
}
