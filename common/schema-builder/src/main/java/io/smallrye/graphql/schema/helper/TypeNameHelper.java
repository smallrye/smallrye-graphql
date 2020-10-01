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
import io.smallrye.graphql.schema.model.TypeAutoNameStrategy;

/**
 * Helping with Name of types in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TypeNameHelper {
    private static final Logger LOG = Logger.getLogger(TypeNameHelper.class.getName());

    private TypeNameHelper() {
    }

    public static String getAnyTypeName(Reference reference, ReferenceType referenceType, ClassInfo classInfo,
            Annotations annotationsForThisClass, TypeAutoNameStrategy autoNameStrategy) {
        String parametrizedTypeNameExtension = createParametrizedTypeNameExtension(reference);
        return getAnyTypeName(parametrizedTypeNameExtension, referenceType, classInfo, annotationsForThisClass,
                autoNameStrategy);
    }

    public static String getAnyTypeName(String parametrizedTypeNameExtension, ReferenceType referenceType, ClassInfo classInfo,
            Annotations annotationsForThisClass, TypeAutoNameStrategy autoNameStrategy) {
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
        } else if (annotations.containsKeyAndValidValue(Annotations.NAME)) {
            sb.append(annotations.getAnnotationValue(Annotations.NAME).asString().trim());
        } else {
            sb.append(applyNamingStrategy(classInfo, autoNameStrategy));
        }

        if (parametrizedTypeNameExtension != null)
            sb.append(parametrizedTypeNameExtension);
        if (postFix != null)
            sb.append(postFix);
        return sb.toString();
    }

    public static String createParametrizedTypeNameExtension(Map<String, Reference> parametrizedTypeArgumentsReferences) {
        if (parametrizedTypeArgumentsReferences == null || parametrizedTypeArgumentsReferences.isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        for (Reference gp : parametrizedTypeArgumentsReferences.values()) {
            appendParametrizedArgumet(sb, gp);
        }
        return sb.toString();
    }

    private static final void appendParametrizedArgumet(StringBuilder sb, Reference gp) {
        sb.append(UNDERSCORE);
        sb.append(gp.getName());
    }

    public static String createParametrizedTypeNameExtension(Reference reference) {
        if (!reference.isAddParametrizedTypeNameExtension() || reference.getParametrizedTypeArguments() == null
                || reference.getParametrizedTypeArguments().isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        for (Reference gp : reference.getParametrizedTypeArguments().values()) {
            sb.append("_");
            sb.append(gp.getName());
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
