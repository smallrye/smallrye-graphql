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
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.smallrye.graphql.schema.Annotations;

/**
 * Helping with Name of types in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class NameHelper {

    public String getEnumName(ClassInfo classInfo, Annotations annotations) {
        if (annotations.containsKeyAndValidValue(Annotations.ENUM)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.ENUM);
            return annotationValue.asString();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotation(Annotations.NAME).value().asString();
        }
        return classInfo.name().local();

    }

    public String getOutputTypeName(ClassInfo classInfo, Annotations annotations) {
        if (annotations.containsKeyAndValidValue(Annotations.TYPE)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.TYPE);
            return annotationValue.asString();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotation(Annotations.NAME).value().asString();
        }
        return classInfo.name().local();
    }

    public String getInputTypeName(ClassInfo classInfo, Annotations annotations) {
        if (annotations.containsKeyAndValidValue(Annotations.INPUT)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.INPUT);
            return annotationValue.asString();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotation(Annotations.NAME).value().asString();
        }
        return classInfo.name().local() + INPUT;
    }

    public String getInputNameForField(Annotations annotationsForThisField, String fieldName) {
        if (annotationsForThisField.containsKeyAndValidValue(Annotations.NAME)) {
            AnnotationInstance nameAnnotation = annotationsForThisField.getAnnotation(Annotations.NAME);
            if (nameAnnotation.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                return nameAnnotation.value().asString();
            }
            return annotationsForThisField.getAnnotation(Annotations.NAME).value().asString();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.getAnnotation(Annotations.JSONB_PROPERTY).value().asString();
        }
        return fieldName;
    }

    public String getOutputNameForField(Annotations annotationsForThisField, String fieldName) {
        if (annotationsForThisField.containsKeyAndValidValue(Annotations.NAME)) {
            AnnotationInstance nameAnnotation = annotationsForThisField.getAnnotation(Annotations.NAME);
            if (nameAnnotation.target().kind().equals(AnnotationTarget.Kind.METHOD)) {
                return nameAnnotation.value().asString();
            }
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.QUERY)) {
            return annotationsForThisField.getAnnotation(Annotations.QUERY).value().asString();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.getAnnotation(Annotations.JSONB_PROPERTY).value().asString();
        }

        return fieldName;
    }

    public String getArgumentName(Annotations annotations, String defaultName) {
        if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotationValue(Annotations.NAME).asString();
        }
        return defaultName;
    }

    public String getExecutionTypeName(AnnotationInstance annotation, Annotations otherAnnotations) {
        if (annotation.value() != null && !annotation.value().asString().isEmpty()) {
            // If the @Query or @Mutation annotation has a value, use that.
            return annotation.value().asString();
        } else if (hasValidExecutionTypeAnnotation(Annotations.NAME, otherAnnotations)) {
            return getValueAsString(Annotations.NAME, otherAnnotations);
        } else if (hasValidExecutionTypeAnnotation(Annotations.JSONB_PROPERTY, otherAnnotations)) {
            return getValueAsString(Annotations.JSONB_PROPERTY, otherAnnotations);
        }
        // Else use the method name
        return getDefaultExecutionTypeName(annotation);

    }

    private String getDefaultExecutionTypeName(AnnotationInstance annotation) {
        String methodName = annotation.target().asMethod().name();
        // TODO: Also check that the word start with a capital ?
        if (annotation.name().equals(Annotations.QUERY)) {
            if (methodName.startsWith(GET) && methodName.length() > 3) {
                methodName = removeAndLowerCase(methodName, 3);
            } else if (methodName.startsWith(IS) && methodName.length() > 2) {
                methodName = removeAndLowerCase(methodName, 2);
            }
        } else if (annotation.name().equals(Annotations.MUTATION)) {
            if (methodName.startsWith(SET) && methodName.length() > 3) {
                methodName = removeAndLowerCase(methodName, 3);
            }
        }

        return methodName;

    }

    private String removeAndLowerCase(String original, int pre) {
        original = original.substring(pre);
        return original.substring(0, 1).toLowerCase() + original.substring(1);
    }

    private boolean hasValidExecutionTypeAnnotation(DotName annotation, Annotations otherAnnotations) {
        if (otherAnnotations.containsKeyAndValidValue(annotation)) {
            AnnotationInstance annotationInstance = otherAnnotations.getAnnotation(annotation);
            if (isMethodAnnotation(annotationInstance)) {
                return true;
            }
        }
        return false;
    }

    private String getValueAsString(DotName annotation, Annotations otherAnnotations) {
        return otherAnnotations.getAnnotation(annotation).value().asString();
    }

    private boolean isMethodAnnotation(AnnotationInstance instance) {
        return instance.target().kind().equals(AnnotationTarget.Kind.METHOD);
    }

    private static final String GET = "get";
    private static final String IS = "is";
    private static final String SET = "set";
    private static final String INPUT = "Input";
}
