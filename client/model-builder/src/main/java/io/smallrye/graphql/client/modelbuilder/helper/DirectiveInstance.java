package io.smallrye.graphql.client.modelbuilder.helper;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

/**
 * Represents an instance of a GraphQL directive, providing methods to build the directive string
 * and convert the directive arguments to a string representation.
 *
 * @author mskacelik
 */
public class DirectiveInstance {
    private final String name;
    private final Map<String, Object> values;

    /**
     * Constructs a new {@code DirectiveInstance} based on the provided Jandex {@link AnnotationInstance}.
     *
     * @param annotationInstance The Jandex {@link AnnotationInstance} representing the GraphQL directive.
     */
    DirectiveInstance(AnnotationInstance annotationInstance) {
        this.name = toDirectiveName(annotationInstance.name().withoutPackagePrefix());
        this.values = annotationInstance.values().stream().collect(
                toUnmodifiableMap(AnnotationValue::name, AnnotationValue::value));
    }

    /**
     * Creates and returns a new {@code DirectiveInstance} based on the provided Jandex {@link AnnotationInstance}.
     *
     * @param annotationInstance The Jandex {@link AnnotationInstance} representing the GraphQL directive.
     * @return A new {@code DirectiveInstance} instance.
     */
    public static DirectiveInstance of(AnnotationInstance annotationInstance) {
        return new DirectiveInstance(annotationInstance);
    }

    /**
     * Builds the GraphQL directive string, including its name and arguments.
     *
     * @return The GraphQL directive string.
     */
    public String buildDirective() {
        StringBuilder builder = new StringBuilder();
        builder.append(" @") // space at the beginning for chaining multiple directives.
                .append(name);

        if (!values.isEmpty()) {
            builder
                    .append("(")
                    .append(buildDirectiveArgs())
                    .append(")");
        }

        return builder.toString();
    }

    /**
     * Converts the directive arguments to a string representation.
     *
     * @return The string representation of the directive arguments.
     */
    private String buildDirectiveArgs() {
        return values.entrySet().stream()
                .map(entry -> new StringBuilder()
                        .append(entry.getKey())
                        .append(": ")
                        .append(directiveArgumentValueToString(entry.getValue()))
                        .toString())
                .collect(Collectors.joining(", "));
    }

    /**
     * Converts the first character of the directive name to lowercase if it is uppercase.
     *
     * @param name The original directive name.
     * @return The modified directive name.
     */
    private String toDirectiveName(String name) {
        if (Character.isUpperCase(name.charAt(0)))
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return name;
    }

    /**
     * Converts the value of a directive argument to its string representation.
     *
     * @param dirArgVal The value of the directive argument.
     * @return The string representation of the directive argument value.
     */
    private String directiveArgumentValueToString(Object dirArgVal) {
        if (dirArgVal.getClass().isArray()) {
            return Arrays.stream((Object[]) dirArgVal).map(this::directiveArgumentValueToString)
                    .collect(Collectors.joining(", ", "[", "]"));
        }
        return ((dirArgVal instanceof String) ? "\"" + dirArgVal + "\"" : dirArgVal).toString();
    }

    @Override
    public String toString() {
        return "DirectiveInstance{" + "type=" + name + ", values=" + values + '}';
    }
}
