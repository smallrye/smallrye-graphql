package io.smallrye.graphql.client.core.utils.validation;

import java.util.regex.Pattern;

/**
 * This class provides utility methods for validating names according to the GraphQL specification.
 *
 * @see <a href="https://spec.graphql.org/draft/#Name">https://spec.graphql.org/draft/#Name</a>
 */
public class NameValidation {

    /**
     * The regular expression patterns for a valid GraphQL names.
     */
    private static final String _NAME_REGEX = "[a-zA-Z_][a-zA-Z0-9_]*";
    private static final String _FIELD_NAME_REGEX = "^" + _NAME_REGEX + "(:" + _NAME_REGEX + ")?$";
    private static final Pattern NAME_PATTERN = Pattern.compile(_NAME_REGEX);
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile(_FIELD_NAME_REGEX);

    /**
     * Validates a GraphQL name and returns it. Throws an IllegalArgumentException if the name is null or invalid.
     * Allows empty string "" as a valid input.
     *
     * @param name the name to validate
     * @return the validated name
     * @throws IllegalArgumentException if the name is null or invalid
     */
    public static String validateNameAllowEmpty(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        } else if (!nameMatchesPattern(name, NAME_PATTERN)) {
            throw new IllegalArgumentException("Invalid name: " + name);
        }
        return name;
    }

    /**
     * Validates a GraphQL fragment name and returns it. Throws an IllegalArgumentException if the name is null,
     * invalid or is equal to the reserved word "on".
     *
     * @param name the name to validate
     * @return the validated name
     * @throws IllegalArgumentException if the name is null, invalid or is equal to the reserved word "on"
     * @see <a href="https://spec.graphql.org/draft/#sec-Language.Fragments">
     *      https://spec.graphql.org/draft/#sec-Language.Fragments
     *      </a>
     */
    public static String validateFragmentName(String name) {
        if (name == null || !nameMatchesPattern(name, NAME_PATTERN)) {
            throw new IllegalArgumentException("Invalid fragment name: " + name);
        } else if (name.equals("on")) {
            throw new IllegalArgumentException("Fragment name cannot be 'on'");
        }
        return name;
    }

    /**
     * Validates a GraphQL name and returns it. Throws an IllegalArgumentException if the name is null or invalid.
     * Does not allow empty string "" as a valid input.
     *
     * @param name the name to validate
     * @return the validated name
     * @throws IllegalArgumentException if the name is null, invalid or empty
     */
    public static String validateName(String name) {
        if (name == null || !nameMatchesPattern(name, NAME_PATTERN)) {
            throw new IllegalArgumentException("Invalid name: " + name);
        }
        return name;
    }

    /**
     * Validates the given field name, allowing at most one colon that is not at the beginning or end
     * for alias.
     *
     * @param fieldName the field name to validate
     * @return the validated field name
     * @throws IllegalArgumentException if the field name is null or invalid
     * @see <a href="https://spec.graphql.org/draft/#sec-Language.Fields">
     *      https://spec.graphql.org/draft/#sec-Language.Fields
     *      </a>
     */
    public static String validateFieldName(String fieldName) {
        if (fieldName == null || !nameMatchesPattern(fieldName, FIELD_NAME_PATTERN)) {
            throw new IllegalArgumentException("Invalid field name: " + fieldName);
        }
        return fieldName;
    }

    private static boolean nameMatchesPattern(String name, Pattern pattern) {
        return pattern.matcher(name).matches();
    }

}
