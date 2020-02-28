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
package io.smallrye.graphql.bootstrap.type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.bootstrap.Annotations;
import io.smallrye.graphql.bootstrap.Classes;
import io.smallrye.graphql.bootstrap.ObjectBag;
import io.smallrye.graphql.bootstrap.datafetcher.AnnotatedPropertyDataFetcher;
import io.smallrye.graphql.bootstrap.datafetcher.ReflectionDataFetcher;
import io.smallrye.graphql.bootstrap.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.bootstrap.schema.helper.ArgumentsHelper;
import io.smallrye.graphql.bootstrap.schema.helper.DescriptionHelper;
import io.smallrye.graphql.bootstrap.schema.helper.IgnoreHelper;
import io.smallrye.graphql.bootstrap.schema.helper.NameHelper;
import io.smallrye.graphql.bootstrap.schema.helper.NonNullHelper;
import io.smallrye.graphql.bootstrap.schema.helper.SourceFieldHelper;
import io.smallrye.graphql.bootstrap.typeresolver.OutputTypeResolver;

/**
 * Create a graphql-java GraphQLOutputType
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class OutputTypeCreator implements Creator {
    private static final Logger LOG = Logger.getLogger(OutputTypeCreator.class.getName());

    private final IndexView index;

    private final InputTypeCreator inputTypeCreator;
    private final EnumTypeCreator enumTypeCreator = new EnumTypeCreator();

    private final NameHelper nameHelper = new NameHelper();
    private final NonNullHelper nonNullHelper = new NonNullHelper();
    private final DescriptionHelper descriptionHelper = new DescriptionHelper();
    private final IgnoreHelper ignoreHelper = new IgnoreHelper();
    private final AnnotationsHelper annotationsHelper = new AnnotationsHelper();
    private final ArgumentsHelper argumentsHelper = new ArgumentsHelper();
    private final SourceFieldHelper sourceFieldHelper = new SourceFieldHelper();

    public OutputTypeCreator(IndexView index, InputTypeCreator inputTypeCreator) {
        this.index = index;
        this.inputTypeCreator = inputTypeCreator;
    }

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
        if (ObjectBag.TYPE_MAP.containsKey(classInfo.name())) {
            return ObjectBag.TYPE_MAP.get(classInfo.name());
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
            ObjectBag.TYPE_MAP.put(classInfo.name(), graphQLObjectType);

            return graphQLObjectType;
        }
    }

    private GraphQLInterfaceType createInterface(ClassInfo classInfo) {
        if (ObjectBag.INTERFACE_MAP.containsKey(classInfo.name())) {
            return ObjectBag.INTERFACE_MAP.get(classInfo.name());
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
            ObjectBag.CODE_REGISTRY_BUILDER.typeResolver(graphQLInterfaceType, new OutputTypeResolver(classInfo.name()));

            ObjectBag.INTERFACE_MAP.put(classInfo.name(), graphQLInterfaceType);

            // Also check that we create all implementations
            Collection<ClassInfo> knownDirectImplementors = index.getAllKnownImplementors(classInfo.name());
            for (ClassInfo impl : knownDirectImplementors) {
                if (!ObjectBag.TYPE_MAP.containsKey(impl.name())) {
                    ObjectBag.TYPE_TODO_LIST.add(impl);
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

                    ObjectBag.CODE_REGISTRY_BUILDER.dataFetcher(
                            FieldCoordinates.coordinates(name, graphQLFieldDefinition.getName()),
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

                    ObjectBag.CODE_REGISTRY_BUILDER.dataFetcher(
                            FieldCoordinates.coordinates(name, graphQLFieldDefinition.getName()),
                            new AnnotatedPropertyDataFetcher(field.name(), field.type(), annotations));

                    fieldDefinitions.add(graphQLFieldDefinition);

                }
            }
        }

        // Also check for @Source fields
        Map<DotName, List<MethodParameterInfo>> sourceFields = sourceFieldHelper.getAllSourceAnnotations(index);
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
                    builder.arguments(
                            argumentsHelper.toGraphQLArguments(inputTypeCreator, methodInfo, parameterAnnotations, true));

                    GraphQLFieldDefinition graphQLFieldDefinition = builder.build();

                    ObjectBag.CODE_REGISTRY_BUILDER.dataFetcher(
                            FieldCoordinates.coordinates(name, graphQLFieldDefinition.getName()),
                            new ReflectionDataFetcher(methodParameterInfo.method(),
                                    argumentsHelper.toArguments(methodInfo)));

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
        } else if (ObjectBag.SCALAR_MAP.containsKey(fieldTypeName)) {
            // Scalar
            return ObjectBag.SCALAR_MAP.get(fieldTypeName);
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
                } else if (ObjectBag.TYPE_MAP.containsKey(fieldTypeName)) {
                    return ObjectBag.TYPE_MAP.get(fieldTypeName);
                } else {
                    ObjectBag.TYPE_TODO_LIST.add(classInfo);
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
