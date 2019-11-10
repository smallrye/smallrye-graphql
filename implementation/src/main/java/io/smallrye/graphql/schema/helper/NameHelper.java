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

import javax.enterprise.context.Dependent;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.FieldInfo;

import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.schema.holder.AnnotationsHolder;
import io.smallrye.graphql.schema.holder.TypeHolder;

/**
 * Helping with Name of types in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class NameHelper {

    public String getEnumName(TypeHolder typeHolder) {
        AnnotationsHolder annotations = typeHolder.getAnnotations();
        if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotation(Annotations.NAME).value().asString();
        }
        return typeHolder.getClassInfo().name().local();
    }

    public String getOutputTypeName(TypeHolder typeHolder) {
        AnnotationsHolder annotations = typeHolder.getAnnotations();
        if (annotations.containsKeyAndValidValue(Annotations.TYPE)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.TYPE);
            return annotationValue.asString();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotation(Annotations.NAME).value().asString();
        }
        // TODO: Do we support any other annotations ?
        return typeHolder.getClassInfo().name().local();
    }

    public String getInputTypeName(TypeHolder typeHolder) {
        AnnotationsHolder annotations = typeHolder.getAnnotations();
        if (annotations.containsKeyAndValidValue(Annotations.INPUTTYPE)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.INPUTTYPE);
            return annotationValue.asString();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotation(Annotations.NAME).value().asString();
        }
        // TODO: Do we support any other annotations ?
        return typeHolder.getClassInfo().name().local() + "Input";
    }

    public String getInputNameForField(AnnotationsHolder annotationsForThisField, FieldInfo field) {
        if (annotationsForThisField.containsKeyAndValidValue(Annotations.NAME)) {
            return annotationsForThisField.getAnnotation(Annotations.NAME).value().asString();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.getAnnotation(Annotations.JSONB_PROPERTY).value().asString();
        }
        return field.name();
    }

    public String getOutputNameForField(AnnotationsHolder annotationsForThisField, FieldInfo field) {
        if (annotationsForThisField.containsKeyAndValidValue(Annotations.NAME)) {
            return annotationsForThisField.getAnnotation(Annotations.NAME).value().asString();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.QUERY)) {
            return annotationsForThisField.getAnnotation(Annotations.QUERY).value().asString();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.getAnnotation(Annotations.JSONB_PROPERTY).value().asString();
        }

        return field.name();
    }

    public String getArgumentName(AnnotationsHolder annotations, short argCount) {
        if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotationValue(Annotations.NAME).asString();
        }
        return "arg" + argCount;
    }

    public String getExecutionTypeName(AnnotationInstance annotation, AnnotationsHolder otherAnnotations) {
        if (annotation.value() != null && !annotation.value().asString().isEmpty()) {
            // If the @Query or @Mutation annotation has a value, use that.
            return annotation.value().asString();
        } else if (otherAnnotations.containsKeyAndValidValue(Annotations.NAME)) {
            return otherAnnotations.getAnnotation(Annotations.NAME).value().asString();
        } else if (otherAnnotations.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return otherAnnotations.getAnnotation(Annotations.JSONB_PROPERTY).value().asString();
        } else {
            // Else use the method name (TODO: Remove Get / Set / Is ?)
            return annotation.target().asMethod().name();
        }
    }
}
