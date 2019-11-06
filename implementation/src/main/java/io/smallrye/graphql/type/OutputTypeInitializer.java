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
import org.jboss.logging.Logger;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import io.smallrye.graphql.helper.AnnotationsHelper;
import io.smallrye.graphql.helper.DescriptionHelper;
import io.smallrye.graphql.helper.IgnoreHelper;
import io.smallrye.graphql.helper.NameHelper;
import io.smallrye.graphql.helper.creator.OutputTypeCreator;
import io.smallrye.graphql.holder.AnnotationsHolder;
import io.smallrye.graphql.holder.TypeHolder;

/**
 * Create a Map of all output types.
 * This looks at all POJOs annotated with @Type
 * and then also all return types on @Queries and @Mutations.
 * 
 * It produces a map, that can be injected anywhere in the code:
 * - outputObjectMap - contains all object type used for requests.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class OutputTypeInitializer {
    private static final Logger LOG = Logger.getLogger(OutputTypeInitializer.class.getName());

    @Inject
    private OutputTypeCreator outputTypeCreator;

    @Inject
    @Named("output")
    private Map<DotName, TypeHolder> outputClasses;

    @Produces
    private final Map<DotName, GraphQLObjectType> outputObjectMap = new HashMap<>();

    @Inject
    private NameHelper nameHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @Inject
    private IgnoreHelper ignoreHelper;

    @Inject
    private AnnotationsHelper annotationsHelper;

    @PostConstruct
    void init() {
        for (Map.Entry<DotName, TypeHolder> e : outputClasses.entrySet()) {
            this.outputObjectMap.put(e.getKey(), createOutputObjectType(e.getValue()));
            LOG.debug("adding [" + e.getKey() + "] to the output object list");
        }
    }

    private GraphQLObjectType createOutputObjectType(TypeHolder typeHolder) {
        String name = nameHelper.getOutputTypeName(typeHolder);
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject();
        objectTypeBuilder = objectTypeBuilder.name(name);

        // Description
        Optional<String> maybeDescription = descriptionHelper.getDescription(typeHolder);
        if (maybeDescription.isPresent()) {
            objectTypeBuilder = objectTypeBuilder.description(maybeDescription.get());
        }

        // Fields
        objectTypeBuilder = objectTypeBuilder.fields(getGraphQLFieldDefinitions(classInfo, name));

        return objectTypeBuilder.build();
    }

    private List<GraphQLFieldDefinition> getGraphQLFieldDefinitions(ClassInfo classInfo, String name) {
        List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
        List<FieldInfo> fields = classInfo.fields();

        for (FieldInfo field : fields) {
            // Check if there is a getter (for output) 
            Optional<MethodInfo> maybeGetter = getGetMethod(field.name(), classInfo);
            if (maybeGetter.isPresent()) {
                MethodInfo getter = maybeGetter.get();
                // Annotations on the field and getter
                AnnotationsHolder annotations = annotationsHelper.getAnnotationsForField(field, getter);

                if (!ignoreHelper.shouldIgnore(annotations)) {
                    GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
                    // Name
                    builder = builder.name(nameHelper.getOutputNameForField(annotations, field));
                    // Description
                    Optional<String> maybeFieldDescription = descriptionHelper.getDescription(annotations, field);
                    if (maybeFieldDescription.isPresent()) {
                        builder = builder.description(maybeFieldDescription.get());
                    }

                    // Type
                    builder = builder
                            .type(outputTypeCreator.createGraphQLOutputType(field.type(), annotations));

                    fieldDefinitions.add(builder.build());

                }
            }
        }
        return fieldDefinitions;
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
