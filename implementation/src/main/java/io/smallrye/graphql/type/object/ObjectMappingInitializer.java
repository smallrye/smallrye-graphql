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

package io.smallrye.graphql.type.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.helper.AnnotationsHelper;
import io.smallrye.graphql.holder.AnnotationsHolder;
import io.smallrye.graphql.holder.TypeHolder;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;

/**
 * Create some Maps of all the relevant Objects.
 * This looks at all POJOs annotated with @Type or @InputType
 * and then also all return type and arguments on @Queries and @Mutations.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class ObjectMappingInitializer {
    private static final Logger LOG = Logger.getLogger(ObjectMappingInitializer.class.getName());

    @Inject
    private Index index;

    @Inject
    @Named("scalars")
    private Set<DotName> knownScalars;

    @Produces
    @Named("input")
    private final Map<DotName, TypeHolder> inputClasses = new HashMap<>();

    @Produces
    @Named("output")
    private final Map<DotName, TypeHolder> outputClasses = new HashMap<>();

    @Produces
    @Named("enum")
    private final Map<DotName, TypeHolder> enums = new HashMap<>();

    @Inject
    private AnnotationsHelper annotationsHelper;

    private enum Direction {
        IN,
        OUT
    }

    @PostConstruct
    void init() {
        // Traverse all the types and create referenced types
        scanClassLevelAnnotations(Direction.OUT, Annotations.TYPE);
        scanClassLevelAnnotations(Direction.IN, Annotations.INPUTTYPE);
        scanMethodLevelAnnotations(Annotations.QUERY);
        scanMethodLevelAnnotations(Annotations.MUTATION);
    }

    private void scanClassLevelAnnotations(Direction direction, DotName annotationName) {
        List<AnnotationInstance> annotations = this.index.getAnnotations(annotationName);
        for (AnnotationInstance annotation : annotations) {

            if (annotation.target().kind().equals(AnnotationTarget.Kind.CLASS)) {
                ClassInfo classInfo = annotation.target().asClass();
                scanClass(direction, classInfo);
            }
        }
    }

    private void scanMethodLevelAnnotations(DotName annotationName) {
        List<AnnotationInstance> annotations = this.index.getAnnotations(annotationName);

        for (AnnotationInstance annotation : annotations) {
            switch (annotation.target().kind()) {
                case METHOD:
                    MethodInfo methodInfo = annotation.target().asMethod();
                    // Return types on Queries and Mutations
                    // TODO: What if getting has void ?
                    if (!methodInfo.returnType().kind().equals(Type.Kind.VOID)) {
                        scanType(Direction.OUT, methodInfo.returnType());
                    }
                    // arguments on getters and setter
                    List<Type> parameters = methodInfo.parameters();
                    for (Type parameter : parameters) {
                        scanType(Direction.IN, parameter);
                    }
                    break;
            }
        }
    }

    private void scanClass(Direction direction, ClassInfo classInfo) {

        // Annotations on the field and getter
        AnnotationsHolder annotationsForThisClass = annotationsHelper.getAnnotationsForClass(classInfo);

        if (Classes.isEnum(classInfo)) {
            if (!enums.containsKey(classInfo.name())) {
                enums.put(classInfo.name(), new TypeHolder(classInfo, annotationsForThisClass));
            }
        } else {
            if (Direction.OUT.equals(direction) && !outputClasses.containsKey(classInfo.name())) {
                outputClasses.put(classInfo.name(), new TypeHolder(classInfo, annotationsForThisClass));
                scanFieldsAndMethods(direction, classInfo);
            } else if (Direction.IN.equals(direction) && !inputClasses.containsKey(classInfo.name())) {
                inputClasses.put(classInfo.name(), new TypeHolder(classInfo, annotationsForThisClass));
                scanFieldsAndMethods(direction, classInfo);
            }
        }
    }

    private void scanFieldsAndMethods(Direction direction, ClassInfo classInfo) {
        // fields
        List<FieldInfo> fieldInfos = classInfo.fields();
        for (FieldInfo fieldInfo : fieldInfos) {
            Type type = fieldInfo.type();
            scanType(direction, type);
        }

        // methods
        List<MethodInfo> methodInfos = classInfo.methods();
        for (MethodInfo methodInfo : methodInfos) {
            String methodName = methodInfo.name();

            // return types on getters and setters
            if (isSetter(methodName) || isGetter(methodName)) {
                // TODO: What if getting has void ?
                if (!methodInfo.returnType().kind().equals(Type.Kind.VOID)) {
                    scanType(direction, methodInfo.returnType());
                }

                // arguments on getters and setter
                List<Type> parameters = methodInfo.parameters();
                for (Type parameter : parameters) {
                    scanType(direction, parameter);
                }
            }
        }
    }

    private void scanType(Direction direction, Type type) {
        switch (type.kind()) {
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                scanType(direction, typeInArray);
                break;
            case PARAMETERIZED_TYPE:
                // TODO: Check if there is more than one type in the Collection, throw an exception ?
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                scanType(direction, typeInCollection);
                break;
            case PRIMITIVE:
                if (!knownScalars.contains(type.name())) {
                    LOG.warn("No scalar mapping for " + type.name() + " with kind " + type.kind());
                }
                break;
            case CLASS:
                if (!knownScalars.contains(type.name())) {
                    ClassInfo classInfo = index.getClassByName(type.name());
                    if (classInfo != null) {
                        scanClass(direction, classInfo);
                    } else {
                        LOG.warn("Not indexed class " + type.name() + " with kind " + type.kind());
                    }
                }
                break;
            default:
                LOG.error("What should we do with field type of " + type.name() + " with kind " + type.kind());
                break;
        }
    }

    private boolean isSetter(String methodName) {
        return methodName.length() > 3 && methodName.startsWith(SET);
    }

    private boolean isGetter(String methodName) {
        return (methodName.length() > 3 && methodName.startsWith(GET))
                || (methodName.length() > 2 && methodName.startsWith(IS));
    }

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String IS = "is";

}
