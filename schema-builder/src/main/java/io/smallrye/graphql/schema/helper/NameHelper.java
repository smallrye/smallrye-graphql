package io.smallrye.graphql.schema.helper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.model.DefinitionType;

/**
 * Helping with Name of types in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class NameHelper {
    private static final Logger LOG = Logger.getLogger(NameHelper.class.getName());

    private NameHelper() {
    }

    public static boolean isPojoMethod(DefinitionType definitionType, String methodName) {
        if (definitionType.equals(DefinitionType.INTERFACE) || definitionType.equals(DefinitionType.TYPE)) {
            return isGetter(methodName);
        } else if (definitionType.equals(DefinitionType.INPUT)) {
            return isSetter(methodName);
        }
        return false;
    }

    public static String toNameFromPojoMethod(DefinitionType definitionType, String methodName) {
        if (definitionType.equals(DefinitionType.INTERFACE) || definitionType.equals(DefinitionType.TYPE)) {
            return toNameFromGetter(methodName);
        } else if (definitionType.equals(DefinitionType.INPUT)) {
            return toNameFromSetter(methodName);
        }
        return null;
    }

    public static String getAnyTypeName(DefinitionType definitionType, ClassInfo classInfo,
            Annotations annotationsForThisClass) {
        if (Classes.isEnum(classInfo)) {
            return getEnumName(classInfo, annotationsForThisClass);
        } else if (Classes.isInterface(classInfo)) {
            return getInterfaceName(classInfo, annotationsForThisClass);
        } else if (definitionType.equals(DefinitionType.TYPE)) {
            return getOutputTypeName(classInfo, annotationsForThisClass);
        } else if (definitionType.equals(DefinitionType.INPUT)) {
            return getInputTypeName(classInfo, annotationsForThisClass);
        } else {
            LOG.warn("Using default name for [" + classInfo.name().toString() + "]");
            return classInfo.name().withoutPackagePrefix();
        }
    }

    public static String getEnumName(ClassInfo classInfo, Annotations annotations) {
        return getNameForClassType(classInfo, annotations, Annotations.ENUM);
    }

    public static String getOutputTypeName(ClassInfo classInfo, Annotations annotations) {
        return getNameForClassType(classInfo, annotations, Annotations.TYPE);
    }

    public static String getInputTypeName(ClassInfo classInfo, Annotations annotations) {
        return getNameForClassType(classInfo, annotations, Annotations.INPUT, INPUT);
    }

    public static String getInterfaceName(ClassInfo classInfo, Annotations annotations) {
        return getNameForClassType(classInfo, annotations, Annotations.INTERFACE);
    }

    public static String getExecutionTypeName(AnnotationInstance annotation, Annotations otherAnnotations) {
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

    public static String getAnyNameForField(DefinitionType definitionType, Annotations annotationsForThisField,
            String fieldName) {
        if (definitionType.equals(DefinitionType.INTERFACE) || definitionType.equals(DefinitionType.TYPE)) {
            return getOutputNameForField(annotationsForThisField, fieldName);
        } else if (definitionType.equals(DefinitionType.INPUT)) {
            return getInputNameForField(annotationsForThisField, fieldName);
        } else {
            LOG.warn("Using default name for [" + definitionType.name() + "]");
            return fieldName;
        }
    }

    public static String getArgumentName(Annotations annotations, String defaultName) {
        if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotationValue(Annotations.NAME).asString().trim();
        }
        return defaultName;
    }

    private static String toNameFromGetter(String methodName) {
        if (methodName.startsWith(GET) && methodName.length() > 3 && hasCapitalAt(methodName, 3)) {
            methodName = removeAndLowerCase(methodName, 3);
        } else if (methodName.startsWith(IS) && methodName.length() > 2 && hasCapitalAt(methodName, 2)) {
            methodName = removeAndLowerCase(methodName, 2);
        }
        return methodName;
    }

    private static String toNameFromSetter(String methodName) {
        if (methodName.startsWith(SET) && methodName.length() > 3 && hasCapitalAt(methodName, 3)) {
            methodName = removeAndLowerCase(methodName, 3);
        }
        return methodName;
    }

    private static boolean isGetter(String methodName) {
        return (methodName.length() > 3 && methodName.startsWith(GET) && hasCapitalAt(methodName, 3))
                || (methodName.length() > 2 && methodName.startsWith(IS) && hasCapitalAt(methodName, 2));
    }

    private static boolean isSetter(String methodName) {
        return methodName.length() > 3 && methodName.startsWith(SET) && hasCapitalAt(methodName, 3);
    }

    private static String getOutputNameForField(Annotations annotationsForThisField, String fieldName) {
        if (annotationsForThisField.containsKeyAndValidValue(Annotations.NAME)) {
            AnnotationInstance nameAnnotation = annotationsForThisField.getAnnotation(Annotations.NAME);
            if (nameAnnotation.target().kind().equals(AnnotationTarget.Kind.METHOD)) {
                return nameAnnotation.value().asString().trim();
            }
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.QUERY)) {
            return annotationsForThisField.getAnnotation(Annotations.QUERY).value().asString().trim();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.getAnnotation(Annotations.JSONB_PROPERTY).value().asString().trim();
        }

        return toNameFromGetter(fieldName);
    }

    private static String getInputNameForField(Annotations annotationsForThisField, String fieldName) {
        if (annotationsForThisField.containsKeyAndValidValue(Annotations.NAME)) {
            AnnotationInstance nameAnnotation = annotationsForThisField.getAnnotation(Annotations.NAME);
            if (nameAnnotation.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                return nameAnnotation.value().asString().trim();
            }
            return annotationsForThisField.getAnnotation(Annotations.NAME).value().asString().trim();
        } else if (annotationsForThisField.containsKeyAndValidValue(Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.getAnnotation(Annotations.JSONB_PROPERTY).value().asString().trim();
        }
        return fieldName;
    }

    private static String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName) {
        return getNameForClassType(classInfo, annotations, typeName, "");
    }

    private static String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName, String postFix) {
        if (annotations.containsKeyAndValidValue(typeName)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(typeName);
            return annotationValue.asString().trim();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotation(Annotations.NAME).value().asString().trim();
        }

        return classInfo.name().local() + postFix;
    }

    private static boolean hasValidExecutionTypeAnnotation(DotName annotation, Annotations otherAnnotations) {
        if (otherAnnotations.containsKeyAndValidValue(annotation)) {
            AnnotationInstance annotationInstance = otherAnnotations.getAnnotation(annotation);
            if (isMethodAnnotation(annotationInstance)) {
                return true;
            }
        }
        return false;
    }

    private static String getDefaultExecutionTypeName(AnnotationInstance annotation) {
        String methodName = annotation.target().asMethod().name();
        if (annotation.name().equals(Annotations.QUERY)) {
            methodName = toNameFromGetter(methodName);
        } else if (annotation.name().equals(Annotations.MUTATION)) {
            methodName = toNameFromSetter(methodName);
        }
        return methodName;
    }

    private static String removeAndLowerCase(String original, int pre) {
        original = original.substring(pre);
        return original.substring(0, 1).toLowerCase() + original.substring(1);
    }

    private static boolean hasCapitalAt(String name, int pos) {
        String letter = new String(new char[] { name.charAt(pos) });
        return !letter.equals(letter.toLowerCase());
    }

    private static String getValueAsString(DotName annotation, Annotations otherAnnotations) {
        return otherAnnotations.getAnnotation(annotation).value().asString().trim();
    }

    private static boolean isMethodAnnotation(AnnotationInstance instance) {
        return instance.target().kind().equals(AnnotationTarget.Kind.METHOD);
    }

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String IS = "is";

    private static final String INPUT = "Input";
}
