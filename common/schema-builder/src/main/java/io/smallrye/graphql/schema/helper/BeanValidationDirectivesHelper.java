package io.smallrye.graphql.schema.helper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.model.DirectiveArgument;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

public class BeanValidationDirectivesHelper {

    private static final DotName VALIDATION_ANNOTATION_EMAIL = DotName.createSimple("javax.validation.constraints.Email");
    private static final DotName VALIDATION_ANNOTATION_MAX = DotName.createSimple("javax.validation.constraints.Max");
    private static final DotName VALIDATION_ANNOTATION_MIN = DotName.createSimple("javax.validation.constraints.Min");
    private static final DotName VALIDATION_ANNOTATION_PATTERN = DotName.createSimple("javax.validation.constraints.Pattern");
    private static final DotName VALIDATION_ANNOTATION_SIZE = DotName.createSimple("javax.validation.constraints.Size");

    public final static DirectiveType CONSTRAINT_DIRECTIVE_TYPE;

    static {
        CONSTRAINT_DIRECTIVE_TYPE = new DirectiveType();
        CONSTRAINT_DIRECTIVE_TYPE.setName("constraint");
        Set<String> locations = new LinkedHashSet<>();
        locations.add("INPUT_FIELD_DEFINITION");
        locations.add("ARGUMENT_DEFINITION");
        CONSTRAINT_DIRECTIVE_TYPE.setLocations(locations);
        CONSTRAINT_DIRECTIVE_TYPE.setDescription("Indicates a Bean Validation constraint");
        CONSTRAINT_DIRECTIVE_TYPE.setRepeatable(true);

        Reference INTEGER = new Reference();
        INTEGER.setType(ReferenceType.SCALAR);
        INTEGER.setClassName("java.lang.Integer");
        INTEGER.setGraphQlClassName("Int");
        INTEGER.setName("Int");

        Reference LONG = new Reference();
        LONG.setType(ReferenceType.SCALAR);
        LONG.setClassName("java.lang.Long");
        LONG.setGraphQlClassName("BigInteger");
        LONG.setName("BigInteger");

        Reference STRING = new Reference();
        STRING.setType(ReferenceType.SCALAR);
        STRING.setClassName("java.lang.String");
        STRING.setGraphQlClassName("String");
        STRING.setName("String");

        addArgument("minLength", INTEGER);
        addArgument("maxLength", INTEGER);
        addArgument("format", STRING);
        addArgument("min", LONG);
        addArgument("max", LONG);
        addArgument("pattern", STRING);
    }

    private static void addArgument(String name, Reference reference) {
        DirectiveArgument arg = new DirectiveArgument();
        arg.setName(name);
        arg.setReference(reference);
        CONSTRAINT_DIRECTIVE_TYPE.addArgumentType(arg);
    }

    /**
     * Finds supported bean validation annotations within the `annotations` list and for each of them, generates
     * a `DirectiveInstance` containing a corresponding `@constraint` GraphQL directive.
     */
    public List<DirectiveInstance> transformBeanValidationConstraintsToDirectives(Annotations annotations) {
        List<DirectiveInstance> result = new ArrayList<>();
        Set<DotName> annotationNames = annotations.getAnnotationNames();
        for (DotName annotationName : annotationNames) {
            if (annotationName.equals(VALIDATION_ANNOTATION_SIZE)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);

                Integer min = getIntValue(annotations, annotationName, "min");
                if (min != null) {
                    directive.setValue("minLength", min);
                }

                Integer max = getIntValue(annotations, annotationName, "max");
                if (max != null) {
                    directive.setValue("maxLength", max);
                }
                result.add(directive);
            }
            if (annotationName.equals(VALIDATION_ANNOTATION_EMAIL)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                directive.setValue("format", "email");
                result.add(directive);
            }
            if (annotationName.equals(VALIDATION_ANNOTATION_MAX)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                directive.setValue("max", getLongValue(annotations, annotationName, "value"));
                result.add(directive);
            }
            if (annotationName.equals(VALIDATION_ANNOTATION_MIN)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                directive.setValue("min", getLongValue(annotations, annotationName, "value"));
                result.add(directive);
            }
            if (annotationName.equals(VALIDATION_ANNOTATION_PATTERN)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                directive.setValue("pattern", getStringValue(annotations, annotationName, "regexp"));
                result.add(directive);
            }
        }
        return result;
    }

    private String getStringValue(Annotations annotations, DotName annotationName, String parameterName) {
        AnnotationValue aValue = annotations.getAnnotationValue(annotationName, parameterName);
        return aValue != null ? aValue.asString() : null;
    }

    private Integer getIntValue(Annotations annotations, DotName annotationName, String parameterName) {
        AnnotationValue aValue = annotations.getAnnotationValue(annotationName, parameterName);
        return aValue != null ? aValue.asInt() : null;
    }

    private Long getLongValue(Annotations annotations, DotName annotationName, String parameterName) {
        AnnotationValue aValue = annotations.getAnnotationValue(annotationName, parameterName);
        return aValue != null ? aValue.asLong() : null;
    }

}
