package io.smallrye.graphql.client.model.helper;

import static io.smallrye.graphql.client.model.Annotations.ID;
import static io.smallrye.graphql.client.model.Annotations.INPUT;
import static io.smallrye.graphql.client.model.Annotations.NAME;
import static io.smallrye.graphql.client.model.Annotations.NESTED_PARAMETER;
import static io.smallrye.graphql.client.model.ScanningContext.getIndex;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.jboss.jandex.MethodParameterInfo;

import io.smallrye.graphql.client.model.Annotations;
import io.smallrye.graphql.client.model.Scalars;
import io.smallrye.graphql.client.typesafe.api.Header;

/**
 * Represents a model for a method parameter in GraphQL, providing information about the parameter's name,
 * type, and associated directives.
 *
 * @author mskacelik
 */
public class ParameterModel implements NamedElement {
    private MethodParameterInfo parameter;
    private TypeModel type;
    private List<DirectiveInstance> directives;

    private final static String PARAMETER_NAME_PLACEHOLDER = "arg";

    /**
     * Constructs a new {@code ParameterModel} instance based on the provided Jandex {@link MethodParameterInfo}.
     *
     * @param parameter The Jandex {@link MethodParameterInfo} representing the GraphQL method parameter.
     */
    ParameterModel(MethodParameterInfo parameter) {
        this.parameter = parameter;
        this.type = TypeModel.of(parameter.type());
        this.directives = DirectiveHelper.resolveDirectives(parameter.annotations().stream(), getDirectiveLocation())
                .map(DirectiveInstance::of)
                .collect(toList());
    }

    /**
     * Creates and returns a new {@code ParameterModel} instance based on the provided Jandex {@link MethodParameterInfo}.
     *
     * @param parameter The Jandex {@link MethodParameterInfo} representing the GraphQL method parameter.
     * @return A new {@code ParameterModel} instance.
     */
    public static ParameterModel of(MethodParameterInfo parameter) {
        return new ParameterModel(parameter);
    }

    /**
     * Checks if the parameter is a value parameter, either a root or nested parameter.
     *
     * @return {@code true} if the parameter is a value parameter, otherwise {@code false}.
     */
    public boolean isValueParameter() {
        return isRootParameter() || isNestedParameter();
    }

    /**
     * Checks if the parameter is a root parameter (neither header nor nested parameter).
     *
     * @return {@code true} if the parameter is a root parameter, otherwise {@code false}.
     */
    public boolean isRootParameter() {
        return !isHeaderParameter() && !isNestedParameter();
    }

    /**
     * Checks if the parameter is a nested parameter.
     *
     * @return {@code true} if the parameter is a nested parameter, otherwise {@code false}.
     */
    public boolean isNestedParameter() {
        return parameter.hasAnnotation(NESTED_PARAMETER);
    }

    /**
     * Gets the names of nested parameters if the parameter is a nested parameter.
     *
     * @return A stream of nested parameter names.
     */
    public Stream<String> getNestedParameterNames() {
        return Stream.of(parameter.annotation(NESTED_PARAMETER).value().asStringArray());
    }

    public String getName() {
        if (parameter.hasAnnotation(NAME))
            return parameter.annotation(NAME).value().asString();
        if (parameter.name() == null)
            throw new RuntimeException("Missing name information for " + this + ".\n" +
                    "You can either annotate all parameters with @Name, " +
                    "or compile your source code with the -parameters options, " +
                    "so the parameter names are compiled into the class file and available at runtime.");
        return getRawName();
    }

    public String getRawName() {
        String rawName = parameter.name();
        return (rawName != null) ? rawName : PARAMETER_NAME_PLACEHOLDER + parameter.position();
    }

    @Override
    public String getDirectiveLocation() {
        return "VARIABLE_DEFINITION";
    }

    /**
     * Gets the GraphQL input type name for the parameter.
     *
     * @return The GraphQL input type name.
     */
    public String graphQlInputTypeName() {
        return graphQlInputTypeNameRecursion(type);
    }

    @Override
    public boolean hasDirectives() {
        return !directives.isEmpty();
    }

    @Override
    public List<DirectiveInstance> getDirectives() {
        return directives;
    }

    /**
     * Base type name without container wrappers (no [] for collections/maps) and
     * without trailing nullability marker.
     */
    private String baseTypeName(TypeModel type) {
        if (type.isSimpleClassType() && !type.isScalar()) {
            if (type.hasClassAnnotation(INPUT)) {
                String value = type.getClassAnnotation(INPUT).orElseThrow().valueWithDefault(getIndex()).asString();
                if (!value.isEmpty()) {
                    return value;
                }
            }
            if (type.hasClassAnnotation(NAME)) {
                return type.getClassAnnotation(NAME).orElseThrow().value().asString();
            }
        }
        if (Scalars.isScalar(type.getName())) {
            return Scalars.getScalar(type.getName());
        }
        return type.getSimpleName() + (type.isEnum() ? "" : "Input");
    }

    /**
     * Adds an optional exclamation mark to the GraphQL input type name based on nullability.
     *
     * @return An optional exclamation mark.
     */
    private String optionalExclamationMark(TypeModel type) {
        // for some reason KOTLIN_NOT_NULL is not applied on type, but on parameter
        return (parameter.hasAnnotation(Annotations.KOTLIN_NOT_NULL) || type.isNonNull()) ? "!" : "";
    }

    /**
     * Checks if the parameter is associated with a {@link Header} annotation,
     * indicating it is a header parameter.
     *
     * @return {@code true} if the parameter is a header parameter, otherwise
     *         {@code false}.
     */
    private boolean isHeaderParameter() {
        return parameter.hasAnnotation(Header.class);
    }

    /**
     * Gets the GraphQL input type name for the specified {@code TypeModel}.
     *
     * @param type The {@link TypeModel} for which to get the GraphQL input type
     *        name.
     * @return The GraphQL input type name.
     */
    private String graphQlInputTypeNameRecursion(TypeModel type) {
        if (type.isCollectionOrArray()) {
            TypeModel elementType = type.isArray() ? type.getArrayElementType() : type.getCollectionElementType();
            return "[" + graphQlInputTypeNameRecursion(elementType) + "]" + optionalExclamationMark(type);
        }

        if (type.isMap()) {
            var keyType = type.getMapKeyType();
            var valueType = type.getMapValueType();
            String key = baseTypeName(keyType) + optionalExclamationMark(keyType);
            String value = baseTypeName(valueType) + optionalExclamationMark(valueType);
            return "[Entry_" + key + "_" + value + "Input]" + optionalExclamationMark(type);
        }

        if (parameter.hasAnnotation(ID)) {
            return "ID" + optionalExclamationMark(type);
        }

        if (type.isSimpleClassType() && !type.isScalar()) {
            if (type.hasClassAnnotation(INPUT)) {
                String value = type.getClassAnnotation(INPUT).orElseThrow().valueWithDefault(getIndex()).asString();
                if (!value.isEmpty()) {
                    return value + optionalExclamationMark(type);
                }
            }
            if (type.hasClassAnnotation(NAME)) {
                return type.getClassAnnotation(NAME).orElseThrow().value().asString() + optionalExclamationMark(type);
            }
        }

        if (Scalars.isScalar(type.getName())) {
            return Scalars.getScalar(type.getName()) + optionalExclamationMark(type);
        }
        return type.getSimpleName() + (type.isEnum() ? "" : "Input") + optionalExclamationMark(type);
    }
}
