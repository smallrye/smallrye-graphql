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

package io.smallrye.graphql.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.helper.AnnotationsHelper;
import io.smallrye.graphql.helper.DefaultValueHelper;
import io.smallrye.graphql.helper.DescriptionHelper;
import io.smallrye.graphql.helper.IgnoreHelper;
import io.smallrye.graphql.helper.NameHelper;
import io.smallrye.graphql.helper.NonNullHelper;
import io.smallrye.graphql.holder.AnnotationsHolder;
import io.smallrye.graphql.holder.TypeHolder;

/**
 * Create a Map of all input types.
 * This looks at all POJOs annotated with @InputType
 * and then also all arguments on @Queries and @Mutations.
 * 
 * It produces a map, that can be injected anywhere in the code:
 * - inputObjectMap - contains all object type used for requests.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class InputObjectInitializer {
    private static final Logger LOG = Logger.getLogger(InputObjectInitializer.class.getName());

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    @Inject
    private Map<DotName, GraphQLEnumType> enumMap;

    @Inject
    @Named("input")
    private Map<DotName, TypeHolder> inputClasses;

    @Produces
    private final Map<DotName, GraphQLInputObjectType> inputObjectMap = new HashMap<>();

    @Inject
    private NameHelper nameHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @Inject
    private IgnoreHelper ignoreHelper;

    @Inject
    private AnnotationsHelper annotationsHelper;

    @Inject
    private DefaultValueHelper defaultValueHelper;

    @Inject
    private NonNullHelper nonNullHelper;

    @PostConstruct
    void init() {
        for (Map.Entry<DotName, TypeHolder> e : inputClasses.entrySet()) {
            this.inputObjectMap.put(e.getKey(), createInputObjectType(e.getValue()));
            LOG.debug("adding [" + e.getKey() + "] to the input object list");
        }
    }

    private GraphQLInputObjectType createInputObjectType(TypeHolder typeHolder) {
        String name = nameHelper.getInputTypeName(typeHolder);
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject().name(name);

        // Description
        Optional<String> maybeDescription = descriptionHelper.getDescription(typeHolder);
        if (maybeDescription.isPresent()) {
            inputObjectTypeBuilder = inputObjectTypeBuilder.description(maybeDescription.get());
        }

        // Fields
        inputObjectTypeBuilder = inputObjectTypeBuilder.fields(getGraphQLInputObjectField(classInfo, name));

        return inputObjectTypeBuilder.build();
    }

    private List<GraphQLInputObjectField> getGraphQLInputObjectField(ClassInfo classInfo, String name) {
        List<GraphQLInputObjectField> inputObjectFields = new ArrayList<>();
        // Fields (TODO: Look at methods rather ? Or both ?)
        List<FieldInfo> fields = classInfo.fields();
        short count = 0;
        for (FieldInfo field : fields) {
            // Check if there is a setter (for input) 
            Optional<MethodInfo> maybeSetter = getSetMethod(field.name(), classInfo);
            if (maybeSetter.isPresent()) {
                MethodInfo setter = maybeSetter.get();
                // Annotations on the field and setter
                AnnotationsHolder annotationsForThisField = annotationsHelper.getAnnotationsForField(field, setter);
                if (!ignoreHelper.shouldIgnore(annotationsForThisField)) {
                    GraphQLInputObjectField.Builder builder = GraphQLInputObjectField.newInputObjectField();

                    // Name
                    builder = builder.name(nameHelper.getInputNameForField(annotationsForThisField, field));

                    // Description
                    Optional<String> maybeFieldDescription = descriptionHelper.getDescription(annotationsForThisField, field);
                    if (maybeFieldDescription.isPresent()) {
                        builder = builder.description(maybeFieldDescription.get());
                    }

                    // Type
                    Type type = field.type();
                    DotName fieldTypeName = type.name();
                    GraphQLInputType graphQLInputType;
                    if (fieldTypeName.equals(classInfo.name())) {
                        // Myself
                        if (nonNullHelper.markAsNonNull(field.type(), annotationsForThisField)) {
                            graphQLInputType = GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef(name));
                        } else {
                            graphQLInputType = GraphQLTypeReference.typeRef(name);
                        }
                    } else {
                        // Another type
                        if (nonNullHelper.markAsNonNull(field.type(), annotationsForThisField)) {
                            graphQLInputType = GraphQLNonNull.nonNull(toGraphQLInputType(type, annotationsForThisField));
                        } else {
                            graphQLInputType = toGraphQLInputType(type, annotationsForThisField);
                        }
                    }

                    // TODO: Deprecate
                    // fieldDefinitionBuilder.deprecate(description)

                    // TODO: Directives ?
                    // fieldDefinitionBuilder.withDirectives(directives) // TODO ?

                    builder = builder.type(graphQLInputType);

                    // Default value (on method)
                    AnnotationsHolder annotationsForThisArgument = annotationsHelper.getAnnotationsForArgument(setter, count);
                    Optional<Object> maybeDefaultValue = defaultValueHelper.getDefaultValue(annotationsForThisArgument,
                            annotationsForThisField);
                    if (maybeDefaultValue.isPresent()) {
                        builder = builder.defaultValue(maybeDefaultValue.get());
                    }

                    inputObjectFields.add(builder.build());
                }
            }

            count++;
        }
        return inputObjectFields;
    }

    private GraphQLInputType toGraphQLInputType(Type type, AnnotationsHolder annotations) {
        DotName fieldTypeName = type.name();

        if (scalarMap.containsKey(fieldTypeName)) {
            // Scalar
            return scalarMap.get(fieldTypeName);
        } else if (enumMap.containsKey(fieldTypeName)) {
            // Enum  
            return enumMap.get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            return GraphQLList.list(toGraphQLInputType(typeInArray, annotations));
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            // TODO: Check if there is more than one type in the Collection, throw an exception ?
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLInputType(typeInCollection, annotations));
        } else if (inputClasses.containsKey(type.name())) {
            // Reference to some type
            String name = nameHelper.getInputTypeName(inputClasses.get(type.name()));
            GraphQLTypeReference graphQLTypeReference = GraphQLTypeReference.typeRef(name);
            return graphQLTypeReference;
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with " + type);
        }
    }

    private Optional<MethodInfo> getSetMethod(String forField, ClassInfo classInfo) {
        String name = SET + forField;
        List<MethodInfo> methods = classInfo.methods();
        for (MethodInfo methodInfo : methods) {
            if (methodInfo.name().equalsIgnoreCase(name)) {
                return Optional.of(methodInfo);
            }
        }
        return Optional.empty();
    }

    private static final String SET = "set";

}
