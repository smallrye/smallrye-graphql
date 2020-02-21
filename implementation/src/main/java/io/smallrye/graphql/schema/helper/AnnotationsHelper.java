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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;

/**
 * Getting all the annotations for something.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class AnnotationsHelper {

    public Annotations getAnnotationsForClass(ClassInfo classInfo) {

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : classInfo.classAnnotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        return new Annotations(annotationMap);
    }

    public Annotations getAnnotationsForOutputField(MethodInfo methodInfo) {
        return getAnnotationsForOutputField(null, methodInfo);
    }

    public Annotations getAnnotationsForOutputField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationsForField = getAnnotationsForField(fieldInfo, methodInfo);
        Type returnType = methodInfo.returnType();
        if (returnType != null) {
            Map<DotName, AnnotationInstance> returnTypeAnnotations = getAnnotationsWithFilter(returnType,
                    Annotations.DATE_FORMAT,
                    Annotations.NUMBER_FORMAT);
            if (!returnTypeAnnotations.isEmpty()) {
                annotationsForField.putAll(returnTypeAnnotations);
            }
        }
        return new Annotations(annotationsForField);
    }

    public Annotations getAnnotationsForInputField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationsForField = getAnnotationsForField(fieldInfo, methodInfo);

        List<Type> parameters = methodInfo.parameters();
        if (parameters != null && !parameters.isEmpty()) {
            Type param = parameters.get(ZERO);
            Map<DotName, AnnotationInstance> parameterAnnotations = getAnnotationsWithFilter(param, Annotations.DATE_FORMAT,
                    Annotations.NUMBER_FORMAT);
            if (!parameterAnnotations.isEmpty()) {
                annotationsForField.putAll(parameterAnnotations);
            }
        }
        return new Annotations(annotationsForField);
    }

    private Map<DotName, AnnotationInstance> getAnnotationsForField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();
        if (fieldInfo != null)
            annotationMap.putAll(listToMap(fieldInfo.annotations()));
        if (methodInfo != null)
            annotationMap.putAll(listToMap(methodInfo.annotations()));
        return annotationMap;
    }

    private Map<DotName, AnnotationInstance> listToMap(List<AnnotationInstance> annotationInstances) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : annotationInstances) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }
        return annotationMap;
    }

    public Annotations getAnnotationsForArgument(MethodInfo methodInfo) {
        return getAnnotationsForArgument(methodInfo, ZERO); // Default to ZERO for normal setters in Beans
    }

    public Annotations getAnnotationsForArgument(MethodInfo methodInfo, short pos) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance anno : methodInfo.annotations()) {

            List<Type> parameters = methodInfo.parameters();
            if (!parameters.isEmpty()) {
                Type type = parameters.get(pos);
                if (anno.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                    MethodParameterInfo methodParameter = anno.target().asMethodParameter();
                    short position = methodParameter.position();
                    if (position == pos) {
                        annotationMap.put(anno.name(), anno);
                        Map<DotName, AnnotationInstance> annotations = getAnnotations(type);
                        annotationMap.putAll(annotations);
                    }
                }
            }
        }
        return new Annotations(annotationMap);
    }

    private Map<DotName, AnnotationInstance> getAnnotations(Type type) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            annotationMap.putAll(getAnnotations(typeInCollection));
        } else {
            List<AnnotationInstance> annotations = type.annotations();
            for (AnnotationInstance annotationInstance : annotations) {
                annotationMap.put(annotationInstance.name(), annotationInstance);
            }
        }

        return annotationMap;
    }

    private Map<DotName, AnnotationInstance> getAnnotationsWithFilter(Type type, DotName... filter) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            annotationMap.putAll(getAnnotationsWithFilter(typeInCollection, filter));
        } else {
            List<AnnotationInstance> annotations = type.annotations();
            for (AnnotationInstance annotationInstance : annotations) {
                if (Arrays.asList(filter).contains(annotationInstance.name())) {
                    annotationMap.put(annotationInstance.name(), annotationInstance);
                }
            }
        }

        return annotationMap;
    }

    public Annotations getAnnotationsForMethod(MethodInfo methodInfo, AnnotationTarget.Kind... kindsFilter) {
        List<AnnotationTarget.Kind> kinds = Arrays.asList(kindsFilter);
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : methodInfo.annotations()) {
            DotName name = annotationInstance.name();
            AnnotationTarget.Kind kind = annotationInstance.target().kind();

            if (kinds.isEmpty() || kinds.contains(kind)) {
                annotationMap.put(name, annotationInstance);
            }
        }

        return new Annotations(annotationMap);
    }

    public Annotations getAnnotationsForType(Type type, Type methodType) {
        Map<DotName, AnnotationInstance> annotationMap = getAnnotationsForType(type);
        annotationMap.putAll(getAnnotationsForType(methodType));
        return new Annotations(annotationMap);
    }

    private Map<DotName, AnnotationInstance> getAnnotationsForType(Type type) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();
        for (AnnotationInstance annotationInstance : type.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }
        return annotationMap;
    }

    private static final short ZERO = 0;
}
