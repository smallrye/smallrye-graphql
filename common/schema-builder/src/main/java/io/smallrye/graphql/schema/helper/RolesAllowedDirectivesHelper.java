package io.smallrye.graphql.schema.helper;

import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.model.DirectiveArgument;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Scalars;

public class RolesAllowedDirectivesHelper {

    public final static DirectiveType ROLES_ALLOWED_DIRECTIVE_TYPE;

    static {
        ROLES_ALLOWED_DIRECTIVE_TYPE = new DirectiveType();
        ROLES_ALLOWED_DIRECTIVE_TYPE.setName("rolesAllowed");
        ROLES_ALLOWED_DIRECTIVE_TYPE.setLocations(Set.of("FIELD_DEFINITION"));
        ROLES_ALLOWED_DIRECTIVE_TYPE.setDescription("Used to specify the role required to execute a given field or operation.");
        ROLES_ALLOWED_DIRECTIVE_TYPE.setRepeatable(false);

        ROLES_ALLOWED_DIRECTIVE_TYPE.addArgumentType(createArgument("value", Scalars.getStringScalar()));
    }

    private static DirectiveArgument createArgument(String name, Reference reference) {
        DirectiveArgument arg = new DirectiveArgument();
        arg.setName(name);
        arg.setReference(reference);
        return arg;
    }

    public DirectiveInstance transformRolesAllowedToDirectives(Annotations methodAnnotations,
            Annotations classAnnotations) {

        Set<DotName> annotationNames = new HashSet<>(methodAnnotations.getAnnotationNames());

        if (classAnnotations != null) {
            annotationNames.addAll(classAnnotations.getAnnotationNames());
        }

        for (DotName annotationName : annotationNames) {
            if (annotationName.equals(DotName.createSimple("jakarta.annotation.security.RolesAllowed"))) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(ROLES_ALLOWED_DIRECTIVE_TYPE);
                String value = getStringValue(methodAnnotations, annotationName, "value");
                if (value == null) {
                    value = getStringValue(classAnnotations, annotationName, "value");
                }
                directive.setValue("value", value);
                return directive;
            }
        }
        return null;
    }

    private String getStringValue(Annotations annotations, DotName annotationName, String parameterName) {
        if (!annotations.containsKeyAndValidValue(annotationName)) {
            return null;
        }

        AnnotationValue aValue = annotations.getAnnotationValue(annotationName, parameterName);
        return (aValue != null && aValue.asStringArray().length != 0) ? aValue.asStringArray()[0] : null;

    }
}
