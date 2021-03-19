package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.enterprise.inject.Stereotype;

import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

public class MethodInvocation {
    public static MethodInvocation of(Method method, Object... args) {
        return new MethodInvocation(new TypeInfo(null, method.getDeclaringClass()), method, args);
    }

    private final TypeInfo type;
    private final Method method;
    private final Object[] parameterValues;

    private MethodInvocation(TypeInfo type, Method method, Object[] parameterValues) {
        this.type = type;
        this.method = method;
        this.parameterValues = parameterValues;
    }

    @Override
    public String toString() {
        return type + "#" + method.getName();
    }

    public String getKey() {
        return method.toGenericString();
    }

    public boolean isQuery() {
        return !method.isAnnotationPresent(Mutation.class);
    }

    public String getName() {
        return queryName()
                .orElseGet(() -> mutationName()
                        .orElseGet(this::methodName));
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

    private String methodName() {
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

    public boolean hasNestedParameters() {
        return nestedParameters().findAny().isPresent();
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

    public Stream<ParameterInfo> nestedParameters() {
        return parameters().filter(ParameterInfo::isNestedParameter);
    }

    private Stream<ParameterInfo> parameters() {
        Parameter[] parameters = method.getParameters();
        return IntStream.range(0, parameters.length)
                .mapToObj(i -> new ParameterInfo(this,
                        parameters[i],
                        parameterValues[i]));
    }

    public TypeInfo getDeclaringType() {
        return type;
    }

    public <A extends Annotation> Stream<A> getResolvedAnnotations(Class<?> declaring, Class<A> type) {
        return Stream.concat(resolveAnnotations(method, type),
                resolveInheritedAnnotations(declaring, type))
                .filter(Objects::nonNull);
    }

    private <A extends Annotation> Stream<A> resolveInheritedAnnotations(Class<?> declaring, Class<A> type) {
        Stream<A> stream = resolveAnnotations(declaring, type);
        for (Class<?> i : declaring.getInterfaces()) {
            stream = Stream.concat(stream, resolveInheritedAnnotations(i, type));
        }
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
            throw new GraphQlClientException("can't invoke " + this, e);
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

    public boolean isDeclaredInObject() {
        return method.getDeclaringClass().equals(Object.class);
    }

    public boolean isDeclaredInCloseable() {
        return method.getDeclaringClass().equals(Closeable.class);
    }
}
