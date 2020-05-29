package io.smallrye.graphql.client.typesafe.impl.reflection;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

public class TypeInfo {
    private final TypeInfo container;
    private final Type type;
    private final AnnotatedType[] annotatedArgs;

    private TypeInfo itemType;
    private Class<?> rawType;

    public static TypeInfo of(Type type) {
        return new TypeInfo(null, type);
    }

    TypeInfo(TypeInfo container, Type type) {
        this(container, type, new AnnotatedType[0]);
    }

    TypeInfo(TypeInfo container, Type type, AnnotatedType[] annotatedArgs) {
        this.container = container;
        this.type = requireNonNull(type);
        this.annotatedArgs = annotatedArgs;
    }

    @Override
    public String toString() {
        return ((type instanceof Class) ? ((Class<?>) type).getName() : type)
                + ((container == null) ? "" : " in " + container);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TypeInfo that = (TypeInfo) o;
        return this.type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    public String getTypeName() {
        if (type instanceof TypeVariable)
            return resolveTypeVariable().getTypeName();
        return type.getTypeName();
    }

    private Class<?> resolveTypeVariable() {
        // TODO this is not generally correct
        ParameterizedType parameterizedType = (ParameterizedType) container.type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<?>) actualTypeArguments[0];
    }

    public String getPackage() {
        return ((Class<?>) type).getPackage().getName(); // TODO may throw Class Cast or NPE
    }

    public boolean isCollection() {
        return ifClass(Class::isArray)
                || Collection.class.isAssignableFrom(getRawType());
    }

    private boolean ifClass(Predicate<Class<?>> predicate) {
        return (type instanceof Class) && predicate.test((Class<?>) type);
    }

    public Stream<FieldInfo> fields() {
        return fields(getRawType());
    }

    private Stream<FieldInfo> fields(Class<?> rawType) {
        return (rawType == null) ? Stream.of()
                : Stream.concat(
                        fields(rawType.getSuperclass()),
                        Stream.of(rawType.getDeclaredFields())
                                .filter(this::isGraphQlField)
                                .map(field -> new FieldInfo(this, field)));
    }

    private boolean isGraphQlField(Field field) {
        return !isStatic(field.getModifiers()) && !isTransient(field.getModifiers());
    }

    public boolean isOptional() {
        return Optional.class.equals(getRawType());
    }

    public boolean isScalar() {
        return isPrimitive()
                || Character.class.equals(getRawType()) // has a valueOf(char), not valueOf(String)
                || CharSequence.class.isAssignableFrom(getRawType())
                || isEnum()
                || scalarConstructor().isPresent();
    }

    public boolean isPrimitive() {
        return getRawType().isPrimitive();
    }

    public boolean isEnum() {
        return ifClass(Class::isEnum);
    }

    public Optional<ConstructionInfo> scalarConstructor() {
        return Stream.concat(
                Stream.of(getRawType().getMethods()).filter(this::isStaticStringConstructor),
                Stream.of(getRawType().getConstructors()).filter(this::hasOneStringParameter))
                .findFirst()
                .map(ConstructionInfo::new);
    }

    private boolean hasOneStringParameter(Executable executable) {
        return executable.getParameterCount() == 1 && CharSequence.class.isAssignableFrom(executable.getParameterTypes()[0]);
    }

    private boolean isStaticStringConstructor(Method method) {
        return isStaticConstructorMethodNamed(method, "of")
                || isStaticConstructorMethodNamed(method, "valueOf")
                || isStaticConstructorMethodNamed(method, "parse");
    }

    private boolean isStaticConstructorMethodNamed(Method method, String name) {
        return method.getName().equals(name)
                && Modifier.isStatic(method.getModifiers())
                && method.getReturnType().equals(type)
                && hasOneStringParameter(method);
    }

    public Object newInstance() {
        try {
            Constructor<?> noArgsConstructor = getRawType().getDeclaredConstructor();
            noArgsConstructor.setAccessible(true);
            return noArgsConstructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't instantiate " + type, e);
        }
    }

    public boolean isNonNull() {
        if (ifClass(c -> c.isAnnotationPresent(NonNull.class)))
            return true; // TODO test
        if (!container.isCollection())
            return false; // TODO test
        // TODO this is not generally correct
        AnnotatedType annotatedArg = container.annotatedArgs[0];
        return annotatedArg.isAnnotationPresent(NonNull.class);
    }

    public Class<?> getRawType() {
        if (rawType == null)
            rawType = raw(type);
        return this.rawType;
    }

    public TypeInfo getItemType() {
        assert isCollection() || isOptional();
        if (itemType == null)
            itemType = new TypeInfo(this, computeItemType());
        return this.itemType;
    }

    private Type computeItemType() {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        return ((Class<?>) type).getComponentType();
    }

    private Class<?> raw(Type type) {
        if (type instanceof Class)
            return (Class<?>) type;
        if (type instanceof ParameterizedType)
            return raw(((ParameterizedType) type).getRawType());
        if (type instanceof TypeVariable)
            return resolveTypeVariable();
        throw new GraphQlClientException("unsupported reflection type " + type.getClass());
    }

    public <A extends Annotation> Stream<A> getAnnotations(Class<A> type) {
        return Stream.of(((Class<?>) this.type).getAnnotationsByType(type));
    }

    public Optional<MethodInfo> getMethod(String name) {
        try {
            return Optional.of(MethodInfo.of(((Class<?>) this.type).getDeclaredMethod(name)));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public boolean isNestedIn(TypeInfo that) {
        return enclosingTypes().anyMatch(that::equals);
    }

    /** <code>this</code> and all enclosing types, i.e. the types this type is nested in. */
    public Stream<TypeInfo> enclosingTypes() {
        // requires JDK 9: return Stream.iterate(this, TypeInfo::hasEnclosingType, TypeInfo::enclosingType);
        Builder<TypeInfo> builder = Stream.builder();
        for (Class<?> enclosing = getRawType(); enclosing != null; enclosing = enclosing.getEnclosingClass())
            builder.accept(TypeInfo.of(enclosing));
        return builder.build();
    }
}
