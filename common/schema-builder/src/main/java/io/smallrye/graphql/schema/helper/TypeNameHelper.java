package io.smallrye.graphql.schema.helper;

import java.util.Map;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.model.Reference;
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

    public static String getAnyTypeName(ClassInfo classInfo,
            Annotations annotationsForThisClass,
            TypeAutoNameStrategy autoNameStrategy) {

        return getAnyTypeName(classInfo,
                annotationsForThisClass,
                autoNameStrategy,
                null, // referenceType
                null); //classParametrizedTypes
    }

    public static String getAnyTypeName(ClassInfo classInfo,
            Annotations annotationsForThisClass,
            TypeAutoNameStrategy autoNameStrategy,
            ReferenceType referenceType) {

        return getAnyTypeName(classInfo,
                annotationsForThisClass,
                autoNameStrategy,
                referenceType,
                null); //classParametrizedTypes
    }

    public static String getAnyTypeName(ClassInfo classInfo,
            Annotations annotationsForThisClass,
            TypeAutoNameStrategy autoNameStrategy,
            ReferenceType referenceType,
            Map<String, Reference> classParametrizedTypes) {

        String parametrizedTypeNameExtension = createParametrizedTypeNameExtension(classParametrizedTypes);

        if (Classes.isEnum(classInfo)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.ENUM, parametrizedTypeNameExtension,
                    autoNameStrategy);
        } else if (Classes.isInterface(classInfo)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.INTERFACE, parametrizedTypeNameExtension,
                    autoNameStrategy);
        } else if (referenceType.equals(ReferenceType.TYPE)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.TYPE, parametrizedTypeNameExtension,
                    autoNameStrategy);
        } else if (referenceType.equals(ReferenceType.INPUT)) {
            return getNameForClassType(classInfo, annotationsForThisClass, Annotations.INPUT, parametrizedTypeNameExtension,
                    INPUT, autoNameStrategy);
        } else if (referenceType.equals(ReferenceType.SCALAR)) {
            return classInfo.name().withoutPackagePrefix();
        } else {
            LOG.warn("Using default name for " + classInfo.simpleName() + " [" + referenceType.name() + "]");
            return classInfo.name().withoutPackagePrefix();
        }
    }

    public static String createParametrizedTypeNameExtension(Map<String, Reference> classParametrizedTypes) {
        if (classParametrizedTypes == null || classParametrizedTypes.isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        for (Reference gp : classParametrizedTypes.values()) {
            sb.append(UNDERSCORE);
            sb.append(gp.getName());
        }
        return sb.toString();
    }

    private static String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName,
            String parametrizedTypeNameExtension, TypeAutoNameStrategy autoNameStrategy) {
        return getNameForClassType(classInfo, annotations, typeName, parametrizedTypeNameExtension, null, autoNameStrategy);
    }

    private static String getNameForClassType(ClassInfo classInfo, Annotations annotations, DotName typeName,
            String parametrizedTypeNameExtension, String postFix, TypeAutoNameStrategy autoNameStrategy) {

        StringBuilder sb = new StringBuilder();

        if (annotations.containsKeyAndValidValue(typeName)) {
            AnnotationValue annotationValue = annotations.getAnnotationValue(typeName);
            sb.append(annotationValue.asString().trim());
            if (parametrizedTypeNameExtension != null) {
                sb.append(parametrizedTypeNameExtension);
            }
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            sb.append(annotations.getAnnotationValue(Annotations.NAME).asString().trim());
            if (parametrizedTypeNameExtension != null) {
                sb.append(parametrizedTypeNameExtension);
            }
        } else {
            sb.append(applyNamingStrategy(classInfo, autoNameStrategy));
            if (parametrizedTypeNameExtension != null) {
                sb.append(parametrizedTypeNameExtension);
            }
            if (postFix != null) {
                sb.append(postFix);
            }
        }

        return sb.toString();
    }

    private static String applyNamingStrategy(ClassInfo classInfo, TypeAutoNameStrategy autoNameStrategy) {
        if (autoNameStrategy.equals(TypeAutoNameStrategy.Full)) {
            return classInfo.name().toString().replaceAll("\\.", UNDERSCORE).replaceAll("\\$", "");
        } else if (autoNameStrategy.equals(TypeAutoNameStrategy.MergeInnerClass)) {
            DotName enclosingClass = classInfo.enclosingClass();
            if (enclosingClass != null) {
                return enclosingClass.local() + classInfo.name().local();
            }
        }
        return classInfo.name().local(); // Default
    }

    private static final String INPUT = "Input";
    private static final String UNDERSCORE = "_";
}