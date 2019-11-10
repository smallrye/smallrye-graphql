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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;

import io.smallrye.graphql.schema.holder.AnnotationsHolder;

/**
 * Getting all the annotations for something.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class AnnotationsHelper {

    /**
     * Get the annotations on the class.
     * 
     * @param classInfo
     * @return All annotations on this class.
     */
    public AnnotationsHolder getAnnotationsForClass(ClassInfo classInfo) {

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : classInfo.classAnnotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        return new AnnotationsHolder(annotationMap);
    }

    public AnnotationsHolder getAnnotationsForField(FieldInfo fieldInfo, MethodInfo methodInfo) {

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : fieldInfo.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        for (AnnotationInstance annotationInstance : methodInfo.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        return new AnnotationsHolder(annotationMap);
    }

    public AnnotationsHolder getAnnotationsForArgument(MethodInfo methodInfo, short pos) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance anno : methodInfo.annotations()) {

            if (anno.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                MethodParameterInfo methodParameter = anno.target().asMethodParameter();
                short position = methodParameter.position();
                if (position == pos) {
                    annotationMap.put(anno.name(), anno);
                }
            } else if (anno.target().kind().equals(AnnotationTarget.Kind.METHOD)) {
                annotationMap.put(anno.name(), anno);
            }

        }
        return new AnnotationsHolder(annotationMap);
    }

    public AnnotationsHolder getAnnotationsForMethod(MethodInfo methodInfo) {

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : methodInfo.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        return new AnnotationsHolder(annotationMap);
    }
}
