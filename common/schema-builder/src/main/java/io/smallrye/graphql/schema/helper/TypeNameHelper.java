package io.smallrye.graphql.schema.helper;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * Helping with Name of types in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TypeNameHelper {
    private static final Logger LOG = Logger.getLogger(TypeNameHelper.class.getName());

    private TypeNameHelper() {
    }

    /**
     * Get the name for any type.
     * 
     * This will figure out the correct type based on the class info
     * 
     * @param referenceType initial reference type
     * @param classInfo the type class info
     * @param annotationsForThisClass annotations on this class
     * @return name of this type
     */
    public static String getAnyTypeName(ReferenceType referenceType, ClassInfo classInfo,
            Annotations annotationsForThisClass) {
        if (Classes.isEnum(classInfo)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.ENUM);
        } else if (Classes.isInterface(classInfo)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.INTERFACE);
        } else if (referenceType.equals(ReferenceType.TYPE)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.TYPE);
        } else if (referenceType.equals(ReferenceType.INPUT)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.INPUT, INPUT);
        } else if (referenceType.equals(ReferenceType.SCALAR)) {
            return classInfo.name().withoutPackagePrefix();
        } else {
            LOG.warn("Using default name for " + classInfo.simpleName() + " [" + referenceType.name() + "]");
            return classInfo.name().withoutPackagePrefix();
        }
    }

    private static String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName) {
        return getNameForClassType(classInfo, annotations, typeName, EMPTY);
    }

    private static String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName, String postFix) {
        if (annotations.containsKeyAndValidValue(typeName)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(typeName);
            return annotationValue.asString().trim();
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            return annotations.getAnnotationValue(Annotations.NAME).asString().trim();
        }

        return classInfo.name().local() + postFix;
    }

    private static final String INPUT = "Input";
    private static final String EMPTY = "";
}
