package io.smallrye.graphql.client.model.helper;

import static io.smallrye.graphql.client.model.Annotations.IGNORE;
import static io.smallrye.graphql.client.model.Annotations.JACKSON_IGNORE;
import static io.smallrye.graphql.client.model.Annotations.JAKARTA_JSONB_TRANSIENT;
import static io.smallrye.graphql.client.model.Classes.ERROR_OR;
import static io.smallrye.graphql.client.model.Classes.OBJECT;
import static io.smallrye.graphql.client.model.Classes.OPTIONAL;
import static io.smallrye.graphql.client.model.Classes.TYPESAFE_RESPONSE;
import static io.smallrye.graphql.client.model.ScanningContext.getIndex;
import static java.util.stream.Collectors.toList;

import io.smallrye.graphql.client.model.Annotations;
import io.smallrye.graphql.client.model.Classes;
import io.smallrye.graphql.client.model.Scalars;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

/**
 * Represents a model for handling GraphQL types, including information about the underlying Jandex Type.
 *
 * @author mskacelik
 */
public class TypeModel {
    private Type type;

    /**
     * Factory method to create a TypeModel from a Jandex Type.
     *
     * @param type The Jandex Type.
     * @return A TypeModel instance.
     */
    public static TypeModel of(Type type) {
        return new TypeModel(type);
    }

    /**
     * Creates a new TypeModel for the given Jandex Type.
     *
     * @param type The Jandex Type.
     */
    TypeModel(Type type) {
        this.type = type;
    }

    /**
     * Retrieves whether the type represents a primitive.
     *
     * @return {@code true} if the type is primitive, otherwise {@code false}.
     */
    public boolean isPrimitive() {
        return Classes.isPrimitive(type);
    }

    /**
     * Retrieves whether the type represents a map.
     *
     * @return {@code true} if the type is a map, otherwise {@code false}.
     */
    public boolean isMap() {
        return Classes.isMap(type);
    }

    /**
     * Retrieves whether the type is a collection or array.
     *
     * @return {@code true} if the type is a collection or array, otherwise {@code false}.
     */
    public boolean isCollectionOrArray() {
        return isArray() || isCollection();
    }

    /**
     * Checks if the type is a class type (simple class, parametrized, or {@code Object}).
     *
     * @return {@code true} if the type is a class type, otherwise {@code false}.
     */
    public boolean isClassType() {
        return isSimpleClassType() || isParametrized() || type.name().equals(OBJECT);
    }

    /**
     * Checks if the type is a simple class type (not the primitive type).
     *
     * @return {@code true} if the type is a simple class type, otherwise {@code false}.
     */
    public boolean isSimpleClassType() {
        return Classes.isClass(type) && !isPrimitive();
    }

    /**
     * Retrieves the key type of map type.
     *
     * @return A {@link TypeModel} representing the key type of the map.
     * @throws IllegalArgumentException If the type is not a map.
     */
    public TypeModel getMapKeyType() {
        if (!isMap()) {
            throw new IllegalArgumentException("Expected type to be a Map");
        }
        return of(type.asParameterizedType().arguments().get(0));
    }

    /**
     * Retrieves the value type of map type.
     *
     * @return A {@link TypeModel} representing the value type of the map.
     * @throws IllegalArgumentException If the type is not a map.
     */
    public TypeModel getMapValueType() {
        if (!isMap()) {
            throw new IllegalArgumentException("Expected type to be a Map");
        }
        return of(type.asParameterizedType().arguments().get(1));
    }

    /**
     * Checks if the type is non-null (primitive or annotated with {@link Annotations#NON_NULL}
     * or {@link Annotations#JAKARTA_NON_NULL}).
     *
     * @return {@code true} if the type is non-null, otherwise {@code false}.
     */
    public boolean isNonNull() {
        return isPrimitive() ||
                type.hasAnnotation(Annotations.NON_NULL) ||
                type.hasAnnotation(Annotations.JAKARTA_NON_NULL);
    }

    /**
     * Checks if the type is an array.
     *
     * @return {@code true} if the type is an array, otherwise {@code false}.
     */
    public boolean isArray() {
        return Classes.isArray(type);
    }

    /**
     * Checks if the type is a collection.
     *
     * @return {@code true} if the type is a collection, otherwise {@code false}.
     */
    public boolean isCollection() {
        return Classes.isCollection(type);
    }

    /**
     * Retrieves the simple name of the type.
     *
     * @return The simple name of the type.
     */
    public String getSimpleName() {
        return type.name().local();
    }

    /**
     * Retrieves the full name of the type, considering parameters for generics.
     *
     * @return The full name of the type.
     */
    public String getName() {
        StringBuilder name = new StringBuilder((isArray()) ? getArrayElementType().getName() : type.name().toString());
        if (isParametrized()) {
            name.append(getItemTypes().map(TypeModel::getName).collect(Collectors.joining(", ", "<", ">")));
        }
        if (isArray()) {
            name.append("[]");
        }
        return name.toString();
    }

    /**
     * Retrieves a stream of {@link TypeModel} instances representing the parameters of parametrized type. (if parametrized).
     *
     * @return A stream of {@link TypeModel} parameter instances.
     * @throws IllegalArgumentException If the type is not parametrized.
     */
    public Stream<TypeModel> getItemTypes() {
        if (!Classes.isParameterized(type)) {
            throw new IllegalArgumentException("Type " + getName() + " is not parametrized, cannot get its Item Types");
        }
        return type.asParameterizedType().arguments().stream().map(TypeModel::of);
    }

    /**
     * Retrieves the element type for an array type.
     *
     * @return A {@link TypeModel} representing the element type of the array.
     * @throws IllegalArgumentException If the type is not an array.
     */
    public TypeModel getArrayElementType() {
        if (!isArray()) {
            throw new IllegalArgumentException("Type " + getName() + " is not an array type, cannot get its Element Type");
        }
        return of(type.asArrayType().elementType());
    }

    /**
     * Retrieves the item type for collections or arrays.
     *
     * @return A {@link TypeModel} representing the item type.
     */
    public TypeModel getItemTypeOrElementType() {
        if (isArray()) {
            return getArrayElementType();
        }
        return getFirstRawType();
    }

    /**
     * Retrieves the element type for collections.
     *
     * @return A {@link TypeModel} representing the element type of the collection.
     */
    public TypeModel getCollectionElementType() {
        return getFirstRawType();
    }

    /**
     * Retrieves the first raw type.
     *
     * @return A {@link TypeModel} representing the first raw type.
     */
    public TypeModel getFirstRawType() {
        return getItemTypes().collect(toList()).get(0);

    }

    /**
     * Checks if the type is an enum.
     *
     * @return {@code true} if the type is an enum, otherwise {@code false}.
     */
    public boolean isEnum() {
        return Classes.isEnum(type);
    }

    /**
     * Checks if the type is a scalar (string, number, etc.).
     *
     * @return {@code true} if the type is a scalar, otherwise {@code false}.
     */
    public boolean isScalar() {
        return Scalars.isScalar(getName()) || hasScalarConstructor() || isEnum();
    }

    /**
     * Checks if the type has a scalar constructor.
     *
     * @return {@code true} if the type has a scalar constructor, otherwise {@code false}.
     */
    private boolean hasScalarConstructor() {
        return isSimpleClassType()
                && getIndex().getClassByName(type.name()).methods().stream().anyMatch(this::isStaticStringConstructor);
    }

    /**
     * Checks if a method is a static string constructor method.
     *
     * @param method The method to check.
     * @return {@code true} if the method is a static string constructor method, otherwise {@code false}.
     */
    private boolean isStaticStringConstructor(MethodInfo method) {
        return isStaticConstructorMethodNamed(method, "of")
                || isStaticConstructorMethodNamed(method, "valueOf")
                || isStaticConstructorMethodNamed(method, "parse");
    }

    /**
     * Checks if a method is a static constructor method with a specific name.
     *
     * @param method The method to check.
     * @param name The name of the constructor method.
     * @return {@code true} if the method is a static constructor method with the specified name, otherwise {@code false}.
     */
    private boolean isStaticConstructorMethodNamed(MethodInfo method, String name) {
        return method.name().equals(name)
                && Modifier.isStatic(method.flags())
                && method.returnType().equals(type)
                // TODO: for other custom scalar types (float, int)
                && hasOneStringParameter(method);
    }

    /**
     * Checks if a method has exactly one string parameter.
     *
     * @param methodInfo The method information.
     * @return {@code true} if the method has exactly one string parameter, otherwise {@code false}.
     */
    private boolean hasOneStringParameter(MethodInfo methodInfo) {
        return methodInfo.parametersCount() == 1 &&
                Scalars.isStringScalar(TypeModel.of(methodInfo.parameterType(0)).getName());
    }

    /**
     * Checks if the type is a parametrized TypesafeResponse.
     *
     * @return {@code true} if the type is a parametrized TypesafeResponse, otherwise {@code false}.
     */
    public boolean isTypesafeResponse() {
        return isParametrized() && type.name().equals(TYPESAFE_RESPONSE);
    }

    /**
     * Checks if the type is a parametrized ErrorOr.
     *
     * @return {@code true} if the type is a parametrized ErrorOr, otherwise {@code false}.
     */
    public boolean isErrorOr() {
        return isParametrized() && type.name().equals(ERROR_OR);
    }

    /**
     * Checks if the type is a parametrized Optional.
     * Optional* classes are considered as Scalars; this method checks if the type is an {@code Optional<T>}.
     *
     * @return {@code true} if the type is a parametrized Optional, otherwise {@code false}.
     */
    public boolean isOptional() { // Optional* classes are considered as Scalars, we check here if type is a `Optional<T>`
        return isParametrized() && type.name().equals(OPTIONAL);
    }

    /**
     * Checks if the type is an asynchronous type.
     *
     * @return {@code true} if the type is asynchronous, otherwise {@code false}.
     */
    public boolean isAsync() {
        return Classes.isAsync(type);
    }

    /**
     * Checks if the type is parametrized.
     *
     * @return {@code true} if the type is parametrized, otherwise {@code false}.
     */
    public boolean isParametrized() {
        return Classes.isParameterized(type);
    }

    /**
     * Checks if the type is a type variable.
     *
     * @return {@code true} if the type is a type variable, otherwise {@code false}.
     */
    public boolean isTypeVariable() {
        return Classes.isTypeVariable(type);
    }

    /**
     * Retrieves a stream of FieldModel instances representing the fields of the type.
     *
     * @return A stream of FieldModel instances.
     * @throws IllegalArgumentException If the type is not a class type.
     */
    public Stream<FieldModel> fields() {
        if (!isClassType()) {
            throw new IllegalArgumentException(
                    "Expected type " + type.name().toString() + " to be Class type, cannot get fields from non-class type");
        }
        return fields(getIndex().getClassByName(type.name()));
    }

    /**
     * Retrieves a stream of {@link FieldModel} instances representing the fields of a class.
     *
     * @param clazz The class information.
     * @return A stream of {@link FieldModel} instances.
     */
    private Stream<FieldModel> fields(ClassInfo clazz) {
        return (clazz == null || clazz.superClassType() == null) ? Stream.of()
                : Stream.concat(
                        fields(getIndex().getClassByName(clazz.superClassType().name())), // to superClass
                        fieldsHelper(clazz)
                                .map(FieldModel::of)
                                .filter(this::isGraphQlField));

    }

    /**
     * Retrieves a stream of {@link FieldInfo} instances representing the fields of a class.
     *
     * @param clazz The class information.
     * @return A stream of {@link FieldInfo} instances.
     */
    private Stream<FieldInfo> fieldsHelper(ClassInfo clazz) {
        if (System.getSecurityManager() == null) {
            return clazz.unsortedFields().stream();
        }
        return AccessController.doPrivileged((PrivilegedAction<Stream<FieldInfo>>) () -> clazz.unsortedFields().stream());

    }

    /**
     * Checks if a field is a GraphQL field.
     *
     * @param field The field to check.
     * @return {@code true} if the field is a GraphQL field, otherwise {@code false}.
     */
    private boolean isGraphQlField(FieldModel field) {
        return !field.isStatic() &&
                !field.isSynthetic() &&
                !field.isTransient() &&
                !isAnnotatedBy(field, IGNORE, JAKARTA_JSONB_TRANSIENT, JACKSON_IGNORE);
    }

    /**
     * Checks if a field is annotated by any of the specified annotations.
     *
     * @param field The field to check.
     * @param annotationClasses The annotations to check for.
     * @return {@code true} if the field is annotated by any of the specified annotations, otherwise {@code false}.
     */
    private boolean isAnnotatedBy(FieldModel field, DotName... annotationClasses) {
        return Arrays.stream(annotationClasses).anyMatch(field::hasAnnotation);
    }

    /**
     * Checks if the class represented by this TypeModel has a specific annotation.
     *
     * @param annotationName The name of the annotation to check.
     * @return {@code true} if the class has the specified annotation, otherwise {@code false}.
     */
    public boolean hasClassAnnotation(DotName annotationName) {
        ClassInfo clazz = getIndex().getClassByName(type.name());
        return clazz.annotations().stream()
                .anyMatch(annotation -> isClassAnnotation(annotation) && annotation.name().equals(annotationName));
    }

    /**
     * Retrieves the annotation instance for a specific annotation on the class.
     *
     * @param annotationName The name of the annotation.
     * @return An Optional containing the AnnotationInstance, or an empty Optional if the annotation is not present.
     */
    public Optional<AnnotationInstance> getClassAnnotation(DotName annotationName) {
        ClassInfo clazz = getIndex().getClassByName(type.name());
        return clazz.annotations().stream()
                .filter(annotation -> isClassAnnotation(annotation) && annotation.name().equals(annotationName)).findFirst();
    }

    /**
     * Checks if the annotation is associated with a class.
     *
     * @param annotationInstance The AnnotationInstance to check.
     * @return {@code true} if the annotation is a class-level annotation, otherwise {@code false}.
     */
    private boolean isClassAnnotation(AnnotationInstance annotationInstance) {
        return annotationInstance.target().kind().equals(AnnotationTarget.Kind.CLASS);
    }

    /**
     * Checks if the type is a custom user-made parametrized type, excluding certain standard types.
     *
     * @return {@code true} if the type is a custom user-made parametrized type, otherwise {@code false}.
     */
    public boolean isCustomParametrizedType() {
        return isParametrized()
                && !isAsync()
                && !isOptional()
                && !isTypesafeResponse()
                && !isErrorOr()
                && !isMap()
                && !isCollection();
    }
}
