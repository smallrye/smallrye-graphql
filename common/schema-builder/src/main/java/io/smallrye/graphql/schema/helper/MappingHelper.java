package io.smallrye.graphql.schema.helper;

import java.util.Optional;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Mapping;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Scalars;

/**
 * Helping with mapping of scalars
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MappingHelper {

    private MappingHelper() {
    }

    public static boolean shouldCreateTypeInSchema(Annotations annotations) {
        return !hasAnyMapping(annotations);
    }

    /**
     * Get the mapping for a certain field.
     * 
     * @param field
     * @param annotations the annotations
     * @return Potentially a MappingInfo model
     */
    public static Optional<Mapping> getMapping(Field field, Annotations annotations) {
        return getMapping(field.getReference(), annotations);
    }

    /**
     * Get the mapping for a certain reference.
     * 
     * @param r
     * @param annotations the annotations
     * @return Potentially a MappingInfo model
     */
    public static Optional<Mapping> getMapping(Reference r, Annotations annotations) {
        Type type = getMapTo(annotations);
        if (type != null) {
            String scalarName = getScalarName(type);
            Reference reference = Scalars.getScalar(scalarName);
            Mapping mappingInfo = new Mapping(reference);
            // Check the way to create this (deserializeMethod)
            // First check if the user supplied a way
            String deserializeMethod = getDeserializeMethod(annotations);
            if (deserializeMethod != null) {
                mappingInfo.setDeserializeMethod(deserializeMethod);
            } else {
                // Auto detect this.
                String className = r.getClassName();
                if (!r.getType().equals(ReferenceType.SCALAR)) { // mapping to scalar stays on default NONE
                    ClassInfo classInfo = ScanningContext.getIndex().getClassByName(DotName.createSimple(className));
                    if (classInfo != null) {
                        // Get Parameter type
                        Type parameter = Type.create(DotName.createSimple(reference.getClassName()), Type.Kind.CLASS);

                        // Check if we can use a constructor
                        MethodInfo constructor = classInfo.method(CONTRUCTOR_METHOD_NAME, parameter);
                        if (constructor != null) {
                            mappingInfo.setDeserializeMethod(CONTRUCTOR_METHOD_NAME); // Create new instance with a contructor
                        } else {
                            // Check if we can use setValue
                            MethodInfo setValueMethod = classInfo.method(SET_VALUE_METHOD_NAME, parameter);
                            if (setValueMethod != null) {
                                mappingInfo.setDeserializeMethod(SET_VALUE_METHOD_NAME);
                            } else {
                                // Check if we can use static fromXXXXX
                                String staticFromMethodName = FROM + scalarName;
                                MethodInfo staticFromMethod = classInfo.method(staticFromMethodName, parameter);
                                if (staticFromMethod != null) {
                                    mappingInfo.setDeserializeMethod(staticFromMethodName);
                                } else {
                                    // Check if we can use static getInstance
                                    MethodInfo staticGetInstance = classInfo.method(GET_INSTANCE_METHOD_NAME, parameter);
                                    if (staticGetInstance != null) {
                                        mappingInfo.setDeserializeMethod(GET_INSTANCE_METHOD_NAME);
                                    }
                                }
                            }
                        }

                    }
                }
            }

            // Get serializeMethod (default to toString)
            String serializeMethod = getSerializeMethod(annotations);
            if (serializeMethod != null) {
                mappingInfo.setSerializeMethod(serializeMethod);
            }

            return Optional.of(mappingInfo);
        } else {
            // TODO: Support other than Scalar mapping 
        }
        return Optional.empty();
    }

    private static boolean hasAnyMapping(Annotations annotations) {
        Type type = getMapTo(annotations);
        return type != null;
    }

    private static String getScalarName(Type type) {
        String className = type.name().withoutPackagePrefix();
        if (className.contains("$")) {
            return className.substring(className.lastIndexOf("$") + 1);
        }
        return className;
    }

    private static Type getMapTo(Annotations annotations) {
        if (annotations != null) {
            if (annotations.containsOneOfTheseAnnotations(Annotations.ADAPT_TO_SCALAR)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.ADAPT_TO_SCALAR);
                if (annotationValue != null) {
                    return annotationValue.asClass();
                }
            }
            // TODO: Remove below (deprecated)
            if (annotations.containsOneOfTheseAnnotations(Annotations.TO_SCALAR)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.TO_SCALAR);
                if (annotationValue != null) {
                    return annotationValue.asClass();
                }
            }
        }
        return null;
    }

    private static String getSerializeMethod(Annotations annotations) {
        return getAnnotationParameterAsString(annotations, SERIALIZE_METHOD);
    }

    private static String getDeserializeMethod(Annotations annotations) {
        return getAnnotationParameterAsString(annotations, DESERIALIZE_METHOD);
    }

    private static String getAnnotationParameterAsString(Annotations annotations, String param) {
        if (annotations != null) {
            if (annotations.containsOneOfTheseAnnotations(Annotations.ADAPT_TO_SCALAR)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.ADAPT_TO_SCALAR, param);
                if (annotationValue != null) {
                    String value = annotationValue.asString();
                    if (value != null && !value.isEmpty()) {
                        return value;
                    }
                }
            }
            // TODO: Remove below (deprecated)
            if (annotations.containsOneOfTheseAnnotations(Annotations.TO_SCALAR)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.TO_SCALAR, param);
                if (annotationValue != null) {
                    String value = annotationValue.asString();
                    if (value != null && !value.isEmpty()) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    private static final String CONTRUCTOR_METHOD_NAME = "<init>";
    private static final String SET_VALUE_METHOD_NAME = "setValue";
    private static final String GET_INSTANCE_METHOD_NAME = "getInstance";
    private static final String FROM = "from";
    private static final String SERIALIZE_METHOD = "serializeMethod";
    private static final String DESERIALIZE_METHOD = "deserializeMethod";
}
