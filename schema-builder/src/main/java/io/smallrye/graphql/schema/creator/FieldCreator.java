package io.smallrye.graphql.schema.creator;

import java.util.Optional;

import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Creates a Field object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FieldCreator {

    private FieldCreator() {
    }

    /**
     * Creates a field from a method only.
     * This is used in the case of an interface
     * 
     * @param methodInfo the java method
     * @return a Field model object
     */
    public static Optional<Field> createFieldForInterface(MethodInfo methodInfo) {
        Annotations annotationsForMethod = Annotations.getAnnotationsForInterfaceField(methodInfo);

        if (!IgnoreHelper.shouldIgnore(annotationsForMethod)) {
            Type returnType = methodInfo.returnType();

            // Name
            String name = getFieldName(Direction.OUT, annotationsForMethod, methodInfo.name());

            // Description
            Optional<String> maybeDescription = DescriptionHelper.getDescriptionForField(annotationsForMethod, returnType);

            // Field Type
            validateFieldType(Direction.OUT, methodInfo);
            Reference reference = ReferenceCreator.createReferenceForInterfaceField(returnType, annotationsForMethod);

            Field field = new Field(methodInfo.name(),
                    name,
                    maybeDescription.orElse(null),
                    reference);

            // NotNull
            if (NonNullHelper.markAsNonNull(returnType, annotationsForMethod)) {
                field.markNotNull();
            }

            // Array
            field.setArray(ArrayCreator.createArray(returnType));

            // Format
            field.setFormat(FormatHelper.getFormat(returnType, annotationsForMethod));

            // Default Value
            Optional maybeDefaultValue = DefaultValueHelper.getDefaultValue(annotationsForMethod);
            field.setDefaultValue(maybeDefaultValue);

            return Optional.of(field);
        }
        return Optional.empty();
    }

    /**
     * Creates a field from a field and method.
     * Used by Type and Input
     * 
     * @param direction the direction (in/out)
     * @param fieldInfo the java property
     * @param methodInfo the java method
     * @return a Field model object
     */
    public static Optional<Field> createFieldForPojo(Direction direction, FieldInfo fieldInfo, MethodInfo methodInfo) {
        Annotations annotationsForPojo = Annotations.getAnnotationsForPojo(direction, fieldInfo, methodInfo);

        if (!IgnoreHelper.shouldIgnore(annotationsForPojo)) {
            Type methodType = getMethodType(methodInfo, direction);

            // Name
            String name = getFieldName(direction, annotationsForPojo, methodInfo.name());

            // Description
            Optional<String> maybeDescription = DescriptionHelper.getDescriptionForField(annotationsForPojo, methodType);

            // Field Type
            validateFieldType(direction, methodInfo);
            Type fieldType = getFieldType(fieldInfo, methodType);

            Reference reference = ReferenceCreator.createReferenceForPojoField(direction, fieldType, methodType,
                    annotationsForPojo);

            Field field = new Field(methodInfo.name(),
                    name,
                    maybeDescription.orElse(null),
                    reference);

            // NotNull
            if (NonNullHelper.markAsNonNull(methodType, annotationsForPojo)) {
                field.markNotNull();
            }

            // Array
            field.setArray(ArrayCreator.createArray(fieldType, methodType));

            // Format
            field.setFormat(FormatHelper.getFormat(methodType, annotationsForPojo));

            // Default Value
            Optional maybeDefaultValue = DefaultValueHelper.getDefaultValue(annotationsForPojo);
            field.setDefaultValue(maybeDefaultValue);

            return Optional.of(field);
        }
        return Optional.empty();
    }

    private static void validateFieldType(Direction direction, MethodInfo methodInfo) {
        Type returnType = methodInfo.returnType();
        if (direction.equals(Direction.OUT) && returnType.kind().equals(Type.Kind.VOID)) {
            throw new SchemaBuilderException(
                    "Can not have a void return method [" + methodInfo.name() + "] in class ["
                            + methodInfo.declaringClass().name() + "]");
        }
    }

    private static Type getMethodType(MethodInfo method, Direction direction) {
        if (direction.equals(Direction.IN)) {
            return method.parameters().get(0);
        }
        return method.returnType();
    }

    private static Type getFieldType(FieldInfo fieldInfo, Type defaultType) {
        if (fieldInfo == null) {
            return defaultType;
        }
        return fieldInfo.type();
    }

    /**
     * Get the field name. Depending on the direction, we either also look at getter/setters
     * 
     * @param direction the direction
     * @param annotationsForThisField annotations on this field
     * @param defaultFieldName the default field name
     * @return the field name
     */
    private static String getFieldName(Direction direction, Annotations annotationsForThisField,
            String defaultFieldName) {
        switch (direction) {
            case OUT:
                return getOutputNameForField(annotationsForThisField, defaultFieldName);
            case IN:
                return getInputNameForField(annotationsForThisField, defaultFieldName);
            default:
                return defaultFieldName;
        }
    }

    private static String getOutputNameForField(Annotations annotationsForThisField, String fieldName) {
        //Scan method-annotations first, if none exists use all
        return annotationsForThisField.getOneOfTheseMethodAnnotationsValue(
                Annotations.NAME,
                Annotations.JSONB_PROPERTY)
                .orElse(annotationsForThisField.getOneOfTheseAnnotationsValue(
                        Annotations.NAME,
                        Annotations.QUERY,
                        Annotations.JSONB_PROPERTY)
                        .orElse(MethodHelper.getFieldName(Direction.OUT, fieldName)));
    }

    private static String getInputNameForField(Annotations annotationsForThisField, String fieldName) {
        //Scan method-annotations first, if none exists use all
        return annotationsForThisField.getOneOfTheseMethodAnnotationsValue(
                Annotations.NAME,
                Annotations.JSONB_PROPERTY)
                .orElse(annotationsForThisField.getOneOfTheseAnnotationsValue(
                        Annotations.NAME,
                        Annotations.JSONB_PROPERTY)
                        .orElse(MethodHelper.getFieldName(Direction.IN, fieldName)));
    }
}
