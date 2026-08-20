package io.smallrye.graphql.client.impl.typesafe.reflection;

import static java.util.stream.Collectors.toList;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Stereotype;

import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Namespace;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.model.MethodKey;
import io.smallrye.graphql.client.typesafe.api.Multiple;

public class MethodInvocation implements NamedElement {
    public static MethodInvocation of(Method method, Object... args) {
        return new MethodInvocation(new TypeInfo(null, method.getDeclaringClass()), method, args);
    }

    private final TypeInfo type;
    private final Method method;
    private final Object[] parameterValues;
    private final List<String> namespaces;
    private final String operationName;
    private List<ParameterInfo> parameters;

    private MethodInvocation(TypeInfo type, Method method, Object[] parameterValues) {
        this.type = type;
        this.method = method;
        this.parameterValues = parameterValues;
        this.namespaces = readNamespaces(method);
        this.operationName = readOperationName(this.namespaces);
    }

    @Override
    public String toString() {
        return type + "#" + method.getName();
    }

    public String getKey() {
        return method.toGenericString();
    }

    public MethodKey getMethodKey() {
        return new MethodKey(method.getName(), method.getParameterTypes());
    }

    public OperationType getOperationType() {
        if (method.isAnnotationPresent(Mutation.class)) {
            return OperationType.MUTATION;
        }
        if (method.isAnnotationPresent(org.eclipse.microprofile.graphql.Subscription.class)
                || method.isAnnotationPresent(io.smallrye.graphql.api.Subscription.class)) {
            return OperationType.SUBSCRIPTION;
        }
        return OperationType.QUERY;
    }

    @Override
    public String getName() {
        return queryName()
                .orElseGet(() -> mutationName()
                        .orElseGet(() -> subscriptionName()
                                .orElseGet(this::getRawName)));
    }

    private Optional<String> queryName() {
        Query query = method.getAnnotation(Query.class);
        if (query != null && !query.value().isEmpty())
            return Optional.of(query.value());
        Name name = method.getAnnotation(Name.class);
        if (name != null)
            return Optional.of(name.value());
        return Optional.empty();
    }

    private Optional<String> mutationName() {
        Mutation mutation = method.getAnnotation(Mutation.class);
        if (mutation != null && !mutation.value().isEmpty())
            return Optional.of(mutation.value());
        return Optional.empty();
    }

    private Optional<String> subscriptionName() {
        // Check MicroProfile annotation first (preferred)
        org.eclipse.microprofile.graphql.Subscription mpAnnotation = method
                .getAnnotation(org.eclipse.microprofile.graphql.Subscription.class);
        if (mpAnnotation != null && !mpAnnotation.value().isEmpty())
            return Optional.of(mpAnnotation.value());

        // Fall back to deprecated SmallRye annotation
        io.smallrye.graphql.api.Subscription srAnnotation = method
                .getAnnotation(io.smallrye.graphql.api.Subscription.class);
        if (srAnnotation != null && !srAnnotation.value().isEmpty())
            return Optional.of(srAnnotation.value());

        return Optional.empty();
    }

    @Override
    public String getRawName() {
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3)))
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        return name;
    }

    public TypeInfo getReturnType() {
        return new TypeInfo(type, method.getGenericReturnType(), method.getAnnotatedReturnType());
    }

    public boolean hasValueParameters() {
        return valueParameters().findAny().isPresent();
    }

    public boolean hasRootParameters() {
        return rootParameters().findAny().isPresent();
    }

    public Stream<ParameterInfo> headerParameters() {
        return parameters().filter(ParameterInfo::isHeaderParameter);
    }

    public Stream<ParameterInfo> valueParameters() {
        return parameters().filter(ParameterInfo::isValueParameter);
    }

    public Stream<ParameterInfo> rootParameters() {
        return parameters().filter(ParameterInfo::isRootParameter);
    }

    public List<ParameterInfo> nestedParameters(String path) {
        return parameters()
                .filter(ParameterInfo::isNestedParameter)
                .filter(parameterInfo -> parameterInfo
                        .getNestedParameterNames()
                        .anyMatch(path::equals))
                .collect(toList());
    }

    private Stream<ParameterInfo> parameters() {
        if (this.parameters == null)
            this.parameters = IntStream.range(0, method.getParameterCount())
                    .mapToObj(i -> new ParameterInfo(this,
                            method.getParameters()[i],
                            parameterValues[i],
                            method.getGenericParameterTypes()[i]))
                    .collect(toList());
        return parameters.stream();
    }

    public TypeInfo getDeclaringType() {
        return type;
    }

    public <A extends Annotation> Stream<A> getResolvedAnnotations(Class<?> declaring, Class<A> type) {
        return Stream.concat(
                resolveInheritedAnnotations(declaring, type),
                resolveAnnotations(method, type))
                .filter(Objects::nonNull);
    }

    private <A extends Annotation> Stream<A> resolveInheritedAnnotations(Class<?> declaring, Class<A> type) {
        // super first, sub second
        Stream<A> stream = Stream.empty();
        for (Class<?> i : declaring.getInterfaces()) {
            stream = Stream.concat(stream, resolveInheritedAnnotations(i, type));
        }
        stream = Stream.concat(stream, resolveAnnotations(declaring, type));
        return stream;
    }

    private static <A extends Annotation> Stream<A> resolveAnnotations(AnnotatedElement annotatedElement, Class<A> type) {
        return Stream.concat(Stream.of(annotatedElement.getAnnotationsByType(type)),
                resolveStereotypes(annotatedElement.getAnnotations(), type));
    }

    private static <A extends Annotation> Stream<A> resolveStereotypes(Annotation[] annotations, Class<A> type) {
        return Stream.of(annotations)
                .map(Annotation::annotationType)
                .filter(annotation -> annotation.isAnnotationPresent(Stereotype.class))
                .flatMap(a -> resolveAnnotations(a, type));
    }

    public Object invoke(Object instance) {
        try {
            if (System.getSecurityManager() == null) {
                method.setAccessible(true);
            } else {
                AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                    method.setAccessible(true);
                    return null;
                });
            }
            return method.invoke(instance, parameterValues);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException)
                throw (RuntimeException) e.getCause();
            if (e.getCause() instanceof Error)
                throw (Error) e.getCause();
            throw new RuntimeException("can't invoke " + this, e);
        } catch (IllegalAccessException e) {
            throw new AssertionError("expected to be unreachable", e);
        }
    }

    public boolean isStatic() {
        return is(Modifier::isStatic);
    }

    public boolean isPublic() {
        return is(Modifier::isPublic);
    }

    public boolean isPackagePrivate() {
        return !isPrivate() && !isProtected() && !isPublic();
    }

    public boolean isProtected() {
        return is(Modifier::isProtected);
    }

    public boolean isPrivate() {
        return is(Modifier::isPrivate);
    }

    private boolean is(Function<Integer, Boolean> f) {
        return f.apply(method.getModifiers());
    }

    public boolean isAccessibleFrom(TypeInfo caller) {
        return this.isPublic()
                || this.isPackagePrivate() && this.type.getPackage().equals(caller.getPackage())
                || (this.isPrivate() || this.isProtected()) && caller.isNestedIn(this.getDeclaringType());
    }

    public boolean isSingle() {
        return !method.getReturnType().isAnnotationPresent(Multiple.class);
    }

    public boolean isDeclaredInObject() {
        return method.getDeclaringClass().equals(Object.class);
    }

    public boolean isDeclaredInCloseable() {
        return method.getDeclaringClass().equals(Closeable.class) ||
                method.getDeclaringClass().equals(AutoCloseable.class);
    }

    public String getOperationTypeAsString() {
        switch (getOperationType()) {
            case MUTATION:
                return "mutation";
            case SUBSCRIPTION:
                return "subscription ";
            default:
                return "query";
        }
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    public String getOperationName() {
        return operationName;
    }

    private List<String> readNamespaces(Method method) {
        if (method.getDeclaringClass().isAnnotationPresent(Namespace.class)) {
            String[] names = method.getDeclaringClass().getAnnotation(Namespace.class).value();
            if (names.length > 0) {
                return List.of(names);
            }
        } else if (method.getDeclaringClass().isAnnotationPresent(Name.class)) {
            String name = method.getDeclaringClass().getAnnotation(Name.class).value();
            if (!name.isBlank()) {
                return List.of(name);
            }
        }
        return List.of();
    }

    private String readOperationName(List<String> names) {
        if (names.isEmpty()) {
            return getName();
        } else {
            String namespace = names.stream()
                    .map(this::makeFirstLetterUppercase)
                    .collect(Collectors.joining());
            return namespace + makeFirstLetterUppercase(getName());
        }
    }

    private String makeFirstLetterUppercase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
