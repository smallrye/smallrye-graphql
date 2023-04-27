package io.smallrye.graphql.client.impl.typesafe.reflection;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import jakarta.json.bind.annotation.JsonbTransient;

import org.eclipse.microprofile.graphql.Ignore;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.TypesafeResponse;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class TypeInfo {
    private final TypeInfo container;
    private final Type type; // TODO only use annotatedType
    private final AnnotatedType annotatedType;
    private final Type genericType; // we need this for GraalVM native mode because the other Type
    // does not contain annotation metadata for some reason

    private TypeInfo itemType; // if `this` represents a collection, this field denotes the type of items included
    private TypeInfo keyType; // if `this` represents a map, this field denotes the type of the map's keys
    private TypeInfo valueType; // if `this` represents a map, this field denotes the type of the map's values
    private Class<?> rawType;

    public static TypeInfo of(Type type) {
        return new TypeInfo(null, type);
    }

    TypeInfo(TypeInfo container, Type type) {
        this(container, type, null, null);
    }

    TypeInfo(TypeInfo container, Type type, AnnotatedType annotatedType) {
        this(container, type, annotatedType, null);
    }

    TypeInfo(TypeInfo container, Type type, AnnotatedType annotatedType, Type genericType) {
        this.container = container;
        this.type = requireNonNull(type);
        this.annotatedType = annotatedType;
        this.genericType = genericType;
    }

    @Override
    public String toString() {
        return ((annotatedType == null) ? ((type instanceof Class) ? ((Class<?>) type).getName() : type) : annotatedType)
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
        Type actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof Class) {
            return (Class<?>) actualTypeArgument;
        } else if (actualTypeArgument instanceof ParameterizedType) {
            return Object.class;
        } else {
            throw new UnsupportedOperationException("can't resolve type variable of a " + actualTypeArgument.getTypeName());
        }
    }

    public String getPackage() {
        return ((Class<?>) type).getPackage().getName(); // TODO may throw Class Cast or NPE
    }

    public boolean isCollection() {
        return ifClass(Class::isArray)
                || Collection.class.isAssignableFrom(getRawType());
    }

    public boolean isAsync() {
        return isMulti() || isUni();
    }

    public boolean isMulti() {
        return Multi.class.isAssignableFrom(getRawType());
    }

    public boolean isUni() {
        return Uni.class.isAssignableFrom(getRawType());
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(getRawType());
    }

    public boolean isOptionalNumber() {
        Class<?> rawType = getRawType();
        return OptionalInt.class.isAssignableFrom(rawType) || OptionalLong.class.isAssignableFrom(rawType) ||
                OptionalDouble.class.isAssignableFrom(rawType);
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
                        Stream.of(getDeclaredFields(rawType))
                                .filter(this::isGraphQlField)
                                .map(field -> new FieldInfo(this, field)));
    }

    private Field[] getDeclaredFields(Class<?> type) {
        if (System.getSecurityManager() == null)
            return type.getDeclaredFields();
        return AccessController.doPrivileged((PrivilegedAction<Field[]>) type::getDeclaredFields);
    }

    private boolean isGraphQlField(Field field) {
        Class jsonIgnoreClass = null;
        try {
            jsonIgnoreClass = Class.forName("com.fasterxml.jackson.annotation.JsonIgnore", false,
                    Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            /* IN CASE THE CLASS IS NOT IMPORTED */ }
        return !isStatic(field.getModifiers()) && !isSynthetic(field.getModifiers()) && !isTransient(field.getModifiers())
                && !isAnnotatedBy(field, Ignore.class, JsonbTransient.class, jsonIgnoreClass);
    }

    private boolean isAnnotatedBy(Field field, Class<? extends Annotation>... annotationClasses) {
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            if (annotationClass != null
                    && field.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    /** Modifier.isSynthetic is package private */
    private static boolean isSynthetic(int mod) {
        return (mod & 0x00001000) != 0;
    }

    public boolean isOptional() {
        return Optional.class.equals(getRawType());
    }

    // this will be generalized to isUnionType or so
    public boolean isErrorOr() {
        return ErrorOr.class.equals(getRawType());
    }

    public boolean isTypesafeResponse() {
        return TypesafeResponse.class.equals(getRawType());
    }

    public boolean isRecord() {
        Class<?> superclass = rawType.getSuperclass();
        return superclass != null && superclass.getName().equals("java.lang.Record");
    }

    public boolean isScalar() {
        return isPrimitive()
                || Void.class.isAssignableFrom(getRawType())
                || Number.class.isAssignableFrom(getRawType())
                || Boolean.class.isAssignableFrom(getRawType())
                || isEnum()
                || CharSequence.class.isAssignableFrom(getRawType())
                || Character.class.equals(getRawType()) // has a valueOf(char), not valueOf(String)
                || java.util.Date.class.equals(getRawType())
                || java.util.UUID.class.equals(getRawType())
                || scalarConstructor().isPresent()
                || java.util.OptionalInt.class.equals(getRawType())
                || java.util.OptionalLong.class.equals(getRawType())
                || java.util.OptionalDouble.class.equals(getRawType());
    }

    public boolean isPrimitive() {
        return getRawType().isPrimitive();
    }

    public boolean isVoid() {
        return void.class.isAssignableFrom(getRawType())
                || Void.class.isAssignableFrom(getRawType());
    }

    public boolean isEnum() {
        if (type instanceof TypeVariable) {
            Class<?> resolved = resolveTypeVariable();
            return resolved.isEnum();
        } else {
            return ifClass(Class::isEnum);
        }
    }

    public Optional<ConstructionInfo> scalarConstructor() {
        return Stream.of(getRawType().getMethods())
                .filter(this::isStaticStringConstructor)
                .findFirst()
                .map(ConstructionInfo::new);
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

    private boolean hasOneStringParameter(Executable executable) {
        return executable.getParameterCount() == 1 && CharSequence.class.isAssignableFrom(executable.getParameterTypes()[0]);
    }

    public Object newInstance(Object[] args) {
        try {
            if (args.length == 0) {
                Constructor<?> noArgsConstructor = getDeclaredConstructor(getRawType());
                noArgsConstructor.setAccessible(true);
                return noArgsConstructor.newInstance();
            } else {
                Class<?> rawType = getRawType();
                Optional<Constructor<?>> constructor = Arrays.stream(rawType.getDeclaredConstructors())
                        .filter(c -> !c.getDeclaringClass().equals(Class.class))
                        .filter(c -> c.getParameterCount() == args.length)
                        .findAny();
                if (constructor.isPresent()) {
                    Constructor<?> c = constructor.get();
                    c.setAccessible(true);
                    return c.newInstance(args);
                } else {
                    throw new RuntimeException("Could not find a suitable constructor of type " + type);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't instantiate " + type, e);
        }
    }

    private Constructor<?> getDeclaredConstructor(Class<?> type) throws NoSuchMethodException {
        return getDeclaredConstructor(type, new Class[0]);
    }

    private Constructor<?> getDeclaredConstructor(Class<?> type, Class<?>[] parameters) throws NoSuchMethodException {
        if (System.getSecurityManager() == null) {
            return type.getDeclaredConstructor(parameters);
        }
        try {
            return AccessController
                    .doPrivileged((PrivilegedExceptionAction<Constructor<?>>) () -> type.getDeclaredConstructor(parameters));
        } catch (PrivilegedActionException pae) {
            if (pae.getCause() instanceof NoSuchMethodException) {
                throw (NoSuchMethodException) pae.getCause();
            }
            throw new RuntimeException(pae.getCause());
        }
    }

    public String getSimpleName() {
        if (type instanceof Class)
            return ((Class<?>) type).getSimpleName();
        return type.getTypeName();
    }

    public boolean isNonNull() {
        Class jakartaNotNullClass = null;
        try {
            jakartaNotNullClass = Class.forName("jakarta.validation.constraints.NotNull", false,
                    Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            /* IN CASE THE CLASS IS NOT IMPORTED */
        }

        Class finalJakartaNotNullClass = jakartaNotNullClass; // lambda
        if (ifClass(c -> c.isPrimitive() || c.isAnnotationPresent(NonNull.class) ||
                (finalJakartaNotNullClass != null && c.isAnnotationPresent(finalJakartaNotNullClass))))
            return true;
        if (annotatedType != null)
            return annotatedType.isAnnotationPresent(NonNull.class) ||
                    (jakartaNotNullClass != null && annotatedType.isAnnotationPresent(jakartaNotNullClass));
        if (container == null || !container.isCollection() || container.annotatedType == null)
            return false; // TODO test
        return container.annotatedType.isAnnotationPresent(NonNull.class) ||
                (jakartaNotNullClass != null && container.annotatedType.isAnnotationPresent(jakartaNotNullClass));

    }

    public Class<?> getRawType() {
        if (rawType == null)
            rawType = raw(type);
        return this.rawType;
    }

    public TypeInfo getItemType() {
        assert isCollection() || isOptional() || isErrorOr() || isAsync() || isTypesafeResponse();
        if (itemType == null)
            itemType = new TypeInfo(this, computeParameterType(0), computeAnnotatedItemType());
        return this.itemType;
    }

    /** Get the type of keys included in this Map. This only works when this type is a Map */
    public TypeInfo getKeyType() {
        if (keyType == null)
            keyType = new TypeInfo(this, computeParameterType(0), computeAnnotatedItemType());
        return this.keyType;
    }

    /** Get the type of values included in this Map. This only works when this type is a Map */
    public TypeInfo getValueType() {
        if (valueType == null)
            valueType = new TypeInfo(this, computeParameterType(1), computeAnnotatedItemType());
        return this.valueType;
    }

    private Type computeParameterType(Integer index) {
        if (annotatedType instanceof AnnotatedParameterizedType)
            return ((AnnotatedParameterizedType) annotatedType).getAnnotatedActualTypeArguments()[index].getType();
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getActualTypeArguments()[index];
        // this workaround might be needed in native mode because the other `annotatedType`,
        // which is retrieved by calling parameter.getAnnotatedType,
        // can't be casted to AnnotatedParameterizedType - at least with GraalVM 21.0 and 21.1
        if (genericType instanceof ParameterizedType)
            return ((ParameterizedType) genericType).getActualTypeArguments()[index];
        return ((Class<?>) type).getComponentType();
    }

    private AnnotatedType computeAnnotatedItemType() {
        // FIXME: if the item type contains annotations, they are not present in the
        // returned object if using native mode with GraalVM 21.0 and 21.1
        // But right now I have no idea how to work around that. This annotatedType
        // is retrieved using parameter.getAnnotatedType(). If I instead use an AnnotatedType
        // returned from calling method.getGenericParameterTypes()[i] on the enclosing method,
        // I get something that, after casting to AnnotatedParameterizedType and calling
        // getAnnotatedActualTypeArguments(), throws a NullPointerException. What do we do?
        // This effectively means that providing an argument of type
        // @NonNull String List<@NonNull String> will be treated as [String]! instead of [String!]!
        if (annotatedType instanceof AnnotatedParameterizedType)
            return ((AnnotatedParameterizedType) annotatedType).getAnnotatedActualTypeArguments()[0];
        return null;
    }

    private Class<?> raw(Type type) {
        if (type instanceof Class)
            return (Class<?>) type;
        if (type instanceof ParameterizedType)
            return raw(((ParameterizedType) type).getRawType());
        if (type instanceof TypeVariable)
            return resolveTypeVariable();
        throw new RuntimeException("unsupported reflection type " + type.getClass());
    }

    public Optional<MethodInvocation> getMethod(String name, Class<?>... args) {
        return getDeclaredMethod((Class<?>) this.type, name, args)
                .map(MethodInvocation::of);
    }

    private Optional<Method> getDeclaredMethod(Class<?> type, String name, Class<?>... args) {
        try {
            if (System.getSecurityManager() == null)
                return Optional.of(type.getDeclaredMethod(name, args));
            return Optional.of(AccessController
                    .doPrivileged((PrivilegedExceptionAction<Method>) () -> type.getDeclaredMethod(name, args)));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        } catch (PrivilegedActionException pae) {
            if (pae.getCause() instanceof NoSuchMethodException)
                return Optional.empty();
            throw new RuntimeException(pae.getCause());
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

    public boolean isAnnotated(Class<? extends Annotation> type) {
        return ifClass(c -> c.isAnnotationPresent(type)); // other type uses are not annotated in this sense
    }

    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return ((Class<?>) this.type).getAnnotation(type);
    }
}
