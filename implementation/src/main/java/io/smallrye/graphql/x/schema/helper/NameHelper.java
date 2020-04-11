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

package io.smallrye.graphql.x.schema.helper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.smallrye.graphql.x.Annotations;

/**
 * Helping with Name of types in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class NameHelper {

    public String getEnumName(ClassInfo classInfo, Annotations annotations) {
        return getNameForClassType(classInfo, annotations, Annotations.ENUM);
    }

    public String getOutputTypeName(ClassInfo classInfo, Annotations annotations) {
        return getNameForClassType(classInfo, annotations, Annotations.TYPE);
    }

    public String getInterfaceName(ClassInfo classInfo, Annotations annotations) {
        return getNameForClassType(classInfo, annotations, Annotations.INTERFACE);
    }

    public String getInputTypeName(ClassInfo classInfo, Annotations annotations) {
        return getNameForClassType(classInfo, annotations, Annotations.INPUT, INPUT);
    }

    public String getInputNameForField(Annotations annotationsForThisField, String fieldName) {
        if (annotationsForThisField.containsKeyAndValidValue(Annotations.NAME)) {
            return annotationsForThisField.getAnnotation(Annotations.NAME).value().asString().trim();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.getAnnotation(Annotations.JSONB_PROPERTY).value().asString().trim();
        }
        return fieldName;
    }

    public String getOutputNameForField(Annotations annotationsForThisField, String fieldName) {
        if (annotationsForThisField.containsKeyAndValidValue(Annotations.NAME)) {
            return annotationsForThisField.getAnnotation(Annotations.NAME).value().asString().trim();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.QUERY)) {
            return annotationsForThisField.getAnnotation(Annotations.QUERY).value().asString().trim();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.getAnnotation(Annotations.JSONB_PROPERTY).value().asString().trim();
        }

        return toNameFromGetter(fieldName);
    }

    public String getArgumentName(Annotations annotations, String defaultName) {
        if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotationValue(Annotations.NAME).asString().trim();
        }
        return defaultName;
    }

    public String getExecutionTypeName(AnnotationInstance annotation, Annotations otherAnnotations) {
        if (annotation.value() != null && !annotation.value().asString().isEmpty()) {
            // If the @Query or @Mutation annotation has a value, use that.
            return annotation.value().asString().trim();
        } else if (hasValidExecutionTypeAnnotation(Annotations.NAME, otherAnnotations)) {
            return getValueAsString(Annotations.NAME, otherAnnotations);
        } else if (hasValidExecutionTypeAnnotation(Annotations.JSONB_PROPERTY, otherAnnotations)) {
            return getValueAsString(Annotations.JSONB_PROPERTY, otherAnnotations);
        }
        // Else use the method name
        return getDefaultExecutionTypeName(annotation);

    }

    public boolean isSetter(String methodName) {
        return methodName.length() > 3 && methodName.startsWith(SET) && hasCapitalAt(methodName, 3);
    }

    public boolean isGetter(String methodName) {
        return (methodName.length() > 3 && methodName.startsWith(GET) && hasCapitalAt(methodName, 3))
                || (methodName.length() > 2 && methodName.startsWith(IS) && hasCapitalAt(methodName, 2));
    }

    public String toNameFromGetter(String methodName) {
        if (methodName.startsWith(GET) && methodName.length() > 3 && hasCapitalAt(methodName, 3)) {
            methodName = removeAndLowerCase(methodName, 3);
        } else if (methodName.startsWith(IS) && methodName.length() > 2 && hasCapitalAt(methodName, 2)) {
            methodName = removeAndLowerCase(methodName, 2);
        }
        return methodName;
    }

    public String toNameFromSetter(String methodName) {
        if (methodName.startsWith(SET) && methodName.length() > 3 && hasCapitalAt(methodName, 3)) {
            methodName = removeAndLowerCase(methodName, 3);
        }
        return methodName;
    }

    private String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName) {
        return getNameForClassType(classInfo, annotations, typeName, "");
    }

    private String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName, String postFix) {
        if (annotations.containsKeyAndValidValue(typeName)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(typeName);
            return annotationValue.asString().trim();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotation(Annotations.NAME).value().asString().trim();
        }

        return classInfo.name().local() + postFix;
    }

    private String getDefaultExecutionTypeName(AnnotationInstance annotation) {
        String methodName = annotation.target().asMethod().name();
        if (annotation.name().equals(Annotations.QUERY)) {
            methodName = toNameFromGetter(methodName);
        } else if (annotation.name().equals(Annotations.MUTATION)) {
            methodName = toNameFromSetter(methodName);
        }
        return methodName;
    }

    private boolean hasCapitalAt(String name, int pos) {
        String letter = new String(new char[] { name.charAt(pos) });
        return !letter.equals(letter.toLowerCase());
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
        return otherAnnotations.getAnnotation(annotation).value().asString().trim();
    }

    private boolean isMethodAnnotation(AnnotationInstance instance) {
        return instance.target().kind().equals(AnnotationTarget.Kind.METHOD);
    }

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String IS = "is";
    private static final String INPUT = "Input";
}
