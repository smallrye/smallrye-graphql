package io.smallrye.graphql.schema.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.model.DirectiveArgument;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Scalars;

public class BeanValidationDirectivesHelper {

    public final static DirectiveType CONSTRAINT_DIRECTIVE_TYPE;

    private static Logger LOGGER = Logger.getLogger(BeanValidationDirectivesHelper.class);

    static {
        CONSTRAINT_DIRECTIVE_TYPE = new DirectiveType();
        CONSTRAINT_DIRECTIVE_TYPE.setName("constraint");
        Set<String> locations = new LinkedHashSet<>();
        locations.add("INPUT_FIELD_DEFINITION");
        locations.add("ARGUMENT_DEFINITION");
        CONSTRAINT_DIRECTIVE_TYPE.setLocations(locations);
        CONSTRAINT_DIRECTIVE_TYPE.setDescription("Indicates a Bean Validation constraint");
        CONSTRAINT_DIRECTIVE_TYPE.setRepeatable(true);

        addArgument("minLength", Scalars.getIntScalar());
        addArgument("maxLength", Scalars.getIntScalar());
        addArgument("format", Scalars.getStringScalar());
        addArgument("min", Scalars.getBigIntegerScalar());
        addArgument("minFloat", Scalars.getBigDecimalScalar());
        addArgument("max", Scalars.getBigIntegerScalar());
        addArgument("maxFloat", Scalars.getBigDecimalScalar());
        addArgument("pattern", Scalars.getStringScalar());
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
            if (annotationName.equals(Classes.JAKARTA_VALIDATION_ANNOTATION_SIZE)
                    || annotationName.equals(Classes.JAVAX_VALIDATION_ANNOTATION_SIZE)) {
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
            if (annotationName.equals(Classes.JAKARTA_VALIDATION_ANNOTATION_EMAIL)
                    || annotationName.equals(Classes.JAVAX_VALIDATION_ANNOTATION_EMAIL)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                directive.setValue("format", "email");
                result.add(directive);
            }
            if (annotationName.equals(Classes.JAKARTA_VALIDATION_ANNOTATION_MAX)
                    || annotationName.equals(Classes.JAVAX_VALIDATION_ANNOTATION_MAX)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                directive.setValue("max", getLongValue(annotations, annotationName, "value"));
                result.add(directive);
            }
            if (annotationName.equals(Classes.JAKARTA_VALIDATION_ANNOTATION_MIN)
                    || annotationName.equals(Classes.JAVAX_VALIDATION_ANNOTATION_MIN)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                directive.setValue("min", getLongValue(annotations, annotationName, "value"));
                result.add(directive);
            }
            if (annotationName.equals(Classes.JAKARTA_VALIDATION_ANNOTATION_DECIMAL_MAX)
                    || annotationName.equals(Classes.JAVAX_VALIDATION_ANNOTATION_DECIMAL_MAX)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                String value = getStringValue(annotations, annotationName, "value");
                try {
                    directive.setValue("maxFloat", new BigDecimal(value).doubleValue());
                    result.add(directive);
                } catch (NumberFormatException nfe) {
                    LOGGER.warn(
                            "Not generating a bean validation directive for " + annotations.getAnnotationValue(annotationName) +
                                    " because the value can't be parsed as a BigDecimal");
                }
            }
            if (annotationName.equals(Classes.JAKARTA_VALIDATION_ANNOTATION_DECIMAL_MIN)
                    || annotationName.equals(Classes.JAVAX_VALIDATION_ANNOTATION_DECIMAL_MIN)) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(CONSTRAINT_DIRECTIVE_TYPE);
                String value = getStringValue(annotations, annotationName, "value");
                try {
                    directive.setValue("minFloat", new BigDecimal(value).doubleValue());
                    result.add(directive);
                } catch (NumberFormatException nfe) {
                    LOGGER.warn(
                            "Not generating a bean validation directive for " + annotations.getAnnotationValue(annotationName) +
                                    " because the value can't be parsed as a BigDecimal");
                }
            }
            if (annotationName.equals(Classes.JAKARTA_VALIDATION_ANNOTATION_PATTERN)
                    || annotationName.equals(Classes.JAVAX_VALIDATION_ANNOTATION_PATTERN)) {
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
