package io.smallrye.graphql.client.model.helper;

import static io.smallrye.graphql.client.model.Annotations.MULTIPLE;
import static io.smallrye.graphql.client.model.Annotations.MUTATION;
import static io.smallrye.graphql.client.model.Annotations.NAME;
import static io.smallrye.graphql.client.model.Annotations.QUERY;
import static io.smallrye.graphql.client.model.Annotations.SUBCRIPTION;
import static io.smallrye.graphql.client.model.ScanningContext.getIndex;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.JandexReflection;
import org.jboss.jandex.MethodInfo;

import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.model.MethodKey;

/**
 * Represents a model for a GraphQL operation method, providing methods to generate GraphQL query fields,
 * handle parameter bindings, and extract operation-related information.
 *
 * @author mskacelik
 */
public class OperationModel implements NamedElement {
    private final MethodInfo method;
    private final List<ParameterModel> parameters;
    private final Stack<String> typeStack = new Stack<>();
    private final Stack<String> expressionStack = new Stack<>();
    private Stack<TypeModel> rawParametrizedTypes = new Stack<>();
    private final List<DirectiveInstance> directives;
    private final String groupName;

    /**
     * Creates a new {@code OperationModel} instance based on the provided Jandex {@link MethodInfo}.
     *
     * @param method The Jandex {@link MethodInfo} representing the GraphQL operation method.
     */
    OperationModel(MethodInfo method) {
        this.method = method;
        this.parameters = method.parameters().stream().map(ParameterModel::of).collect(Collectors.toList());
        this.directives = DirectiveHelper.resolveDirectives(method.annotations().stream(),
                getDirectiveLocation(), AnnotationTarget.Kind.METHOD)
                .map(DirectiveInstance::of)
                .collect(toList());
        this.groupName = readGroupName(method);
    }

    /**
     * Creates and returns a new {@code OperationModel} instance based on the provided Jandex {@link MethodInfo}.
     *
     * @param method The Jandex {@link MethodInfo} representing the GraphQL operation method.
     * @return A new {@code OperationModel} instance.
     */
    public static OperationModel of(MethodInfo method) {
        return new OperationModel(method);
    }

    /**
     * Generates GraphQL query fields for the specified {@code TypeModel}.
     *
     * @param type The {@link TypeModel} for which to generate GraphQL query fields.
     * @return The generated GraphQL query fields.
     * @throws IllegalStateException If a field recursion is detected.
     */
    public String fields(TypeModel type) {
        if (typeStack.contains(type.getName()))
            throw new IllegalStateException("field recursion found");
        try {
            typeStack.push(type.getName());
            return recursionCheckedFields(type);
        } finally {
            typeStack.pop();
        }
    }

    /**
     * Generates GraphQL query fields for the specified {@code TypeModel} with recursion checking.
     *
     * @param type The {@link TypeModel} for which to generate GraphQL query fields.
     * @return The generated GraphQL query fields.
     */
    public String recursionCheckedFields(TypeModel type) {
        while (type.isOptional() || type.isErrorOr() || type.isTypesafeResponse()) {
            type = type.getFirstRawType(); // unwrapping
        }
        if (type.isScalar())
            return "";
        if (type.isCollectionOrArray() || type.isAsync())
            return fields(type.getItemTypeOrElementType());
        if (type.isMap()) {
            String keyFields = fields(type.getMapKeyType());
            String valueFields = fields(type.getMapValueType());
            return " {key" + keyFields + " value" + valueFields + "}";
        }
        if (isRawParametrizedType(type)) {
            rawParametrizedTypes.push(type.getFirstRawType());
        }
        String fieldsResult = type.fields()
                .map(this::field)
                .collect(joining(" ", " {", "}"));
        if (isRawParametrizedType(type)) {
            rawParametrizedTypes.pop();
        }
        return fieldsResult;

    }

    /**
     * Generates the GraphQL representation of a field based on the provided {@code FieldModel}.
     *
     * @param field The {@link FieldModel} representing the field.
     * @return The GraphQL representation of the field.
     */
    public String field(FieldModel field) {
        TypeModel type = field.getType();
        if (type.isTypeVariable()) {
            type = rawParametrizedTypes.peek();
        }
        StringBuilder expression = new StringBuilder();
        field.getAlias().ifPresent(alias -> expression.append(alias).append(":"));
        expression.append(field.getName());

        if (field.hasDirectives()) {
            expression.append(field.getDirectives().stream().map(DirectiveInstance::buildDirective).collect(joining()));
        }

        String path = nestedExpressionPrefix() + field.getRawName();
        List<ParameterModel> nestedParameters = nestedParameters(path);
        if (!nestedParameters.isEmpty()) {
            expression.append(nestedParameters.stream()
                    .map(this::bind)
                    .collect(joining(", ", "(", ")")));
        }

        expressionStack.push(path);
        expression.append(fields(type)); // appends the empty string, if the type is scalar, etc.
        expressionStack.pop();

        return expression.toString();
    }

    /**
     * Declares a GraphQL parameter for the specified {@code ParameterModel}.
     *
     * @param parameter The {@link ParameterModel} for which to declare the GraphQL parameter.
     * @return The GraphQL declaration of the parameter.
     */
    public String declare(ParameterModel parameter) {
        return "$" + parameter.getRawName() + ": " + parameter.graphQlInputTypeName() +
                ((parameter.hasDirectives()) ? parameter.getDirectives()
                        .stream()
                        .map(DirectiveInstance::buildDirective)
                        .collect(joining())
                        : "");
    }

    /**
     * Binds a GraphQL parameter for the specified {@code ParameterModel}.
     *
     * @param parameter The {@link ParameterModel} for which to bind the GraphQL parameter.
     * @return The GraphQL binding of the parameter.
     */
    public String bind(ParameterModel parameter) {
        return parameter.getName() + ": $" + parameter.getRawName();
    }

    /**
     * Retrieves the prefix for nested expressions in GraphQL queries.
     *
     * @return The prefix for nested expressions.
     */
    public String nestedExpressionPrefix() {
        return expressionStack.isEmpty() ? "" : expressionStack.peek() + ".";
    }

    /**
     * Gets the operation type of the GraphQL operation.
     *
     * @return The {@link OperationType} of the GraphQL operation.
     */
    public OperationType getOperationType() {
        if (method.hasAnnotation(MUTATION)) {
            return OperationType.MUTATION;
        }
        if (method.hasAnnotation(SUBCRIPTION)) {
            return OperationType.SUBSCRIPTION;
        }
        return OperationType.QUERY;
    }

    /**
     * Gets the name of the GraphQL operation, considering any {@link org.eclipse.microprofile.graphql.Query}
     * or {@link org.eclipse.microprofile.graphql.Name} annotations.
     *
     * @return An optional containing the operation name if specified, otherwise empty.
     */
    public Optional<String> queryName() {
        Optional<AnnotationInstance> queryAnnotation = getMethodAnnotation(QUERY);
        if (queryAnnotation.isPresent() && queryAnnotation.orElseThrow().value() != null)
            return Optional.of(queryAnnotation.orElseThrow().value().asString());
        Optional<AnnotationInstance> nameAnnotation = getMethodAnnotation(NAME);
        if (nameAnnotation.isPresent())
            return Optional.of(nameAnnotation.orElseThrow().value().asString());
        return Optional.empty();
    }

    /**
     * Gets the name of the GraphQL mutation, considering any {@link org.eclipse.microprofile.graphql.Mutation} annotation.
     *
     * @return An optional containing the mutation name if specified, otherwise empty.
     */
    public Optional<String> mutationName() {
        Optional<AnnotationInstance> mutationAnnotation = getMethodAnnotation(MUTATION);
        if (mutationAnnotation.isPresent() && mutationAnnotation.orElseThrow().value() != null)
            return Optional.of(mutationAnnotation.orElseThrow().value().asString());
        return Optional.empty();
    }

    /**
     * Gets the name of the GraphQL subscription, considering any io.smallrye.graphql.api.Subscription annotation.
     *
     * @return An optional containing the subscription name if specified, otherwise empty.
     */
    public Optional<String> subscriptionName() {
        Optional<AnnotationInstance> subscriptionAnnotation = getMethodAnnotation(SUBCRIPTION);
        if (subscriptionAnnotation.isPresent() && subscriptionAnnotation.orElseThrow().value() != null)
            return Optional.of(subscriptionAnnotation.orElseThrow().value().asString());
        return Optional.empty();
    }

    @Override
    public String getName() {
        return queryName()
                .orElseGet(() -> mutationName()
                        .orElseGet(() -> subscriptionName()
                                .orElseGet(this::getRawName)));
    }

    @Override
    public String getRawName() {
        String name = method.name();
        if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3)))
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        return name;
    }

    @Override
    public String getDirectiveLocation() {
        // currently the definition is that, if there is a method targeted directive, that directive
        // will be transformed to high-level field. So the current implementation does not work with
        // QUERY, MUTATION, SUBSCRIPTION locations...
        return "FIELD";
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
     * Retrieves the list of parameters representing the values for the GraphQL operation.
     *
     * @return The list of value parameters.
     */
    public List<ParameterModel> valueParameters() {
        return parameters.stream().filter(ParameterModel::isValueParameter).collect(Collectors.toList());
    }

    /**
     * Retrieves the list of parameters representing the root-level parameters for the GraphQL operation.
     *
     * @return The list of root parameters.
     */
    public List<ParameterModel> rootParameters() {
        return parameters.stream().filter(ParameterModel::isRootParameter).collect(Collectors.toList());
    }

    /**
     * Retrieves the list of parameters representing nested parameters for the GraphQL operation.
     *
     * @param path The path for which nested parameters are retrieved.
     * @return The list of nested parameters.
     */
    public List<ParameterModel> nestedParameters(String path) {
        return parameters.stream()
                .filter(ParameterModel::isNestedParameter)
                .filter(parameter -> parameter
                        .getNestedParameterNames()
                        .anyMatch(path::equals))
                .collect(toList());
    }

    /**
     * Gets the return type of the GraphQL operation.
     *
     * @return The {@link TypeModel} representing the return type.
     */
    public TypeModel getReturnType() {
        return TypeModel.of(method.returnType());
    }

    /**
     * Checks if the GraphQL operation has value parameters.
     *
     * @return {@code true} if the operation has value parameters, otherwise {@code false}.
     */
    public boolean hasValueParameters() {
        return !valueParameters().isEmpty();
    }

    /**
     * Checks if the GraphQL operation has root-level parameters.
     *
     * @return {@code true} if the operation has root parameters, otherwise {@code false}.
     */
    public boolean hasRootParameters() {
        return !rootParameters().isEmpty();
    }

    /**
     * Checks if the GraphQL operation returns a single result (one GraphQL operation).
     *
     * @return {@code true} if the operation returns a single result, otherwise {@code false}.
     */
    public boolean isSingle() {
        return getReturnType().isScalar()
                || getReturnType().isParametrized()
                || getReturnType().isArray()
                || !getIndex().getClassByName(method.returnType().name()).hasAnnotation(MULTIPLE);
    }

    /**
     * Gets the key for identifying the GraphQL operation method.
     *
     * @return The {@link MethodKey} representing the key for the operation method (name, parameters types).
     */
    public MethodKey getMethodKey() {
        return new MethodKey(method.name(), method.parameters().stream()
                .map(methodParameterInfo -> JandexReflection.loadRawType(methodParameterInfo.type())).toArray(Class<?>[]::new));
    }

    /**
     * Gets the string representation of the operation type (query, mutation, or subscription).
     *
     * @return The string representation of the operation type.
     */
    public String getOperationTypeAsString() {
        switch (getOperationType()) {
            case MUTATION:
                return "mutation";
            case SUBSCRIPTION:
                return "subscription";
            default:
                return "query";
        }
    }

    /**
     * Retrieves a non-repeatable annotation for the GraphQL operation method.
     *
     * @param annotation The {@link DotName} representing the annotation to retrieve.
     * @return An optional containing the annotation instance if present, otherwise empty.
     */
    private Optional<AnnotationInstance> getMethodAnnotation(DotName annotation) {
        return method.annotations().stream()
                .filter(annotationInstance -> annotationInstance.target().kind() == AnnotationTarget.Kind.METHOD)
                .filter(annotationInstance -> annotationInstance.name().equals(annotation)).findFirst();
    }

    /**
     * Checks if the given TypeModel represents a raw parametrized type, excluding type variables.
     *
     * @param type The TypeModel to check.
     * @return {@code true} if the TypeModel represents a raw parametrized type (excluding type variables), otherwise
     *         {@code false}.
     */
    private boolean isRawParametrizedType(TypeModel type) {
        return type.isCustomParametrizedType() && !type.getFirstRawType().isTypeVariable();
    }

    public String getGroupName() {
        return groupName;
    }

    private String readGroupName(MethodInfo method) {
        List<AnnotationInstance> annotationInstances = method.declaringClass().annotations(NAME);
        for (AnnotationInstance annotationInstance : annotationInstances) {
            if (annotationInstance.target().kind() == AnnotationTarget.Kind.CLASS) {
                if (annotationInstance.target().asClass().name().equals(method.declaringClass().name())) {
                    String groupName = annotationInstance.value().asString().trim();
                    if (!groupName.isEmpty()) {
                        return groupName;
                    }
                }
            }
        }
        return null;
    }
}
