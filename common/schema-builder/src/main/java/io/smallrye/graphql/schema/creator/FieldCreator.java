package io.smallrye.graphql.schema.creator;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.BeanValidationDirectivesHelper;
import io.smallrye.graphql.schema.helper.DeprecatedDirectivesHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Creates a Field object
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FieldCreator extends ModelCreator {

    private final Logger logger = Logger.getLogger(FieldCreator.class.getName());

    private final BeanValidationDirectivesHelper validationHelper;
    private final DeprecatedDirectivesHelper deprecatedHelper;

    public FieldCreator(ReferenceCreator referenceCreator) {
        super(referenceCreator);
        validationHelper = new BeanValidationDirectivesHelper();
        deprecatedHelper = new DeprecatedDirectivesHelper();
    }

    /**
     * Creates a field from a method only.This is used in the case of an interface
     *
     * @param methodInfo the java method
     * @param parentObjectReference
     * @return a Field model object
     */
    public Optional<Field> createFieldForInterface(MethodInfo methodInfo, Reference parentObjectReference) {
        Annotations annotationsForMethod = Annotations.getAnnotationsForInterfaceField(methodInfo);

        if (!isGraphQlField(Direction.OUT, null, methodInfo)) {
            return Optional.empty();
        }

        Type returnType = getReturnType(methodInfo);

        // Name
        String name = getFieldName(Direction.OUT, annotationsForMethod, methodInfo.name());

        // Field Type
        validateFieldType(Direction.OUT, methodInfo);
        Reference reference = referenceCreator.createReferenceForInterfaceField(returnType, annotationsForMethod,
                parentObjectReference);

        Field field = new Field(methodInfo.name(), MethodHelper.getPropertyName(Direction.OUT, methodInfo.name()), name,
                reference);
        populateField(Direction.OUT, field, returnType, annotationsForMethod);

        return Optional.of(field);
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
    public Optional<Field> createFieldForPojo(Direction direction, FieldInfo fieldInfo, MethodInfo methodInfo,
            Reference parentObjectReference) {
        Annotations annotationsForPojo = Annotations.getAnnotationsForPojo(direction, fieldInfo, methodInfo);

        if (isGraphQlField(direction, fieldInfo, methodInfo)) {
            Type methodType = getMethodType(methodInfo, direction);

            // Name
            String name = getFieldName(direction, annotationsForPojo, methodInfo.name());

            // Field Type
            validateFieldType(direction, methodInfo);
            Type fieldType = getFieldType(fieldInfo, methodType);

            Reference reference = referenceCreator.createReferenceForPojoField(fieldType, methodType,
                    annotationsForPojo, direction, parentObjectReference);

            Field field = new Field(methodInfo.name(), MethodHelper.getPropertyName(direction, methodInfo.name()), name,
                    reference);
            if (direction == Direction.IN) {
                addDirectivesForBeanValidationConstraints(annotationsForPojo, field, parentObjectReference);
                addDirectivesForDeprecated(annotationsForPojo, field, parentObjectReference);
            }

            populateField(direction, field, fieldType, methodType, annotationsForPojo);

            return Optional.of(field);
        }
        return Optional.empty();
    }

    public Optional<Field> createFieldForParameter(MethodInfo method, short position, FieldInfo fieldInfo,
            Reference parentObjectReference) {
        Annotations annotationsForPojo = Annotations.getAnnotationsForInputCreator(method, position, fieldInfo);

        // Name
        String name = getFieldName(Direction.IN, annotationsForPojo, method.parameterName(position));

        // Field Type
        Type fieldType = getFieldType(fieldInfo, method.parameterType(position));

        Reference reference = referenceCreator.createReferenceForPojoField(fieldType,
                method.parameterType(position), annotationsForPojo, Direction.IN, parentObjectReference);

        String fieldName = fieldInfo != null ? fieldInfo.name() : null;
        Field field = new Field(null, fieldName, name, reference);
        addDirectivesForBeanValidationConstraints(annotationsForPojo, field, parentObjectReference);
        addDirectivesForDeprecated(annotationsForPojo, field, parentObjectReference);
        populateField(Direction.IN, field, fieldType, method.parameterType(position), annotationsForPojo);

        return Optional.of(field);

    }

    /**
     * Creates a field from a public field.
     * Used by Type and Input
     *
     * @param direction the direction (in/out)
     * @param fieldInfo the java property
     * @return a Field model object
     */
    public Optional<Field> createFieldForPojo(Direction direction, FieldInfo fieldInfo, Reference parentObjectReference) {
        if (isGraphQlField(direction, fieldInfo, null)) {
            Annotations annotationsForPojo = Annotations.getAnnotationsForPojo(direction, fieldInfo);

            // Name
            String name = getFieldName(direction, annotationsForPojo, fieldInfo.name());

            // Field Type
            Type fieldType = getFieldType(fieldInfo);

            Reference reference = referenceCreator.createReferenceForPojoField(fieldType, fieldType,
                    annotationsForPojo, direction, parentObjectReference);

            Field field = new Field(null,
                    fieldInfo.name(),
                    name,
                    reference);
            if (direction == Direction.IN) {
                addDirectivesForBeanValidationConstraints(annotationsForPojo, field, parentObjectReference);
                addDirectivesForDeprecated(annotationsForPojo, field, parentObjectReference);
            }
            populateField(direction, field, fieldType, annotationsForPojo);

            return Optional.of(field);
        }
        return Optional.empty();
    }

    private void addDirectivesForBeanValidationConstraints(Annotations annotationsForPojo, Field field,
            Reference parentObjectReference) {
        if (validationHelper != null) {
            List<DirectiveInstance> constraintDirectives = validationHelper
                    .transformBeanValidationConstraintsToDirectives(annotationsForPojo);
            if (!constraintDirectives.isEmpty()) {
                logger.debug("Adding constraint directives " + constraintDirectives + " to field '" + field.getName()
                        + "' of parent type '" + parentObjectReference.getName() + "'");
                field.addDirectiveInstances(constraintDirectives);
            }
        }
    }

    private void addDirectivesForDeprecated(Annotations annotationsForPojo, Field field,
            Reference parentObjectReference) {
        if (deprecatedHelper != null && directives != null) {
            List<DirectiveInstance> deprecatedDirectives = deprecatedHelper
                    .transformDeprecatedToDirectives(annotationsForPojo,
                            directives.getDirectiveTypes().get(DotName.createSimple("io.smallrye.graphql.api.Deprecated")));
            if (!deprecatedDirectives.isEmpty()) {
                logger.debug("Adding deprecated directives " + deprecatedDirectives + " to field '" + field.getName()
                        + "' of parent type '" + parentObjectReference.getName() + "'");
                field.addDirectiveInstances(deprecatedDirectives);
            }
        }
    }

    /**
     * Checks if method and/or field are use-able as a GraphQL-Field.
     *
     * @param direction the direction, IN if the field should be used on an input type, OUT otherwise
     * @param fieldInfo the field. If null, methodInfo must be provided
     * @param methodInfo the method. If null, fieldInfo must be provided
     * @return if it is an GraphQL field
     */
    protected static boolean isGraphQlField(Direction direction, FieldInfo fieldInfo, MethodInfo methodInfo) {
        boolean methodAccessible = isPossibleField(methodInfo);
        boolean fieldAccessible = isPossibleField(direction, fieldInfo);

        if (!methodAccessible && !fieldAccessible) {
            return false;
        }

        Annotations annotationsForPojo = Annotations.getAnnotationsForPojo(direction, fieldInfo, methodInfo);
        return !IgnoreHelper.shouldIgnore(annotationsForPojo, fieldInfo);
    }

    /**
     * Checks if the method is a possible GraphQl field (by method access).
     * This means that the method:
     * <ul>
     * </ul>
     * <li>exists</li>
     * <li>is public</li>
     * <li>and is not static</li>
     * </ul>
     *
     * @param methodInfo the method
     * @return if the method is a possible GraphQl field
     */
    private static boolean isPossibleField(MethodInfo methodInfo) {
        return methodInfo != null
                && Modifier.isPublic(methodInfo.flags())
                && !Modifier.isStatic(methodInfo.flags());
    }

    /**
     * Checks if the field is a possible GraphQl field (by field access).
     *
     * This means that the field:
     * <ul>
     * </ul>
     * <li>exists</li>
     * <li>is public</li>
     * <li>is not static</li>
     * <li>and is not final, if it's an input field</li>
     * </ul>
     *
     * @param direction the direction
     * @param fieldInfo the method
     * @return if the field is a possible GraphQl field
     */
    private static boolean isPossibleField(Direction direction, FieldInfo fieldInfo) {
        return fieldInfo != null
                && !(direction == Direction.IN && Modifier.isFinal(fieldInfo.flags()))
                && Modifier.isPublic(fieldInfo.flags())
                && !Modifier.isStatic(fieldInfo.flags());
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
            return method.parameterType(0);
        }
        return getReturnType(method);
    }

    private static Type getFieldType(FieldInfo fieldInfo) {
        return getReturnType(fieldInfo);
    }

    private static Type getFieldType(FieldInfo fieldInfo, Type defaultType) {

        if ((fieldInfo == null || fieldInfo.type().name().equals(Classes.SERIALIZABLE)
                || fieldInfo.type().name().equals(Classes.OBJECT))) {
            return defaultType;
        }
        return getReturnType(fieldInfo);
    }

    /**
     * Get the field name. Depending on the direction, we either also look at getter/setters
     *
     * @param direction the direction
     * @param annotationsForThisField annotations on this field
     * @param defaultFieldName the default field name
     * @return the field name
     */
    static String getFieldName(Direction direction, Annotations annotationsForThisField,
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
        return annotationsForThisField.getOneOfTheseMethodAnnotationsValue(
                Annotations.NAME,
                Annotations.QUERY,
                Annotations.JAKARTA_JSONB_PROPERTY,
                Annotations.JAVAX_JSONB_PROPERTY,
                Annotations.JACKSON_PROPERTY)
                .orElse(annotationsForThisField.getOneOfTheseAnnotationsValue(
                        Annotations.NAME,
                        Annotations.QUERY,
                        Annotations.JAKARTA_JSONB_PROPERTY,
                        Annotations.JAVAX_JSONB_PROPERTY,
                        Annotations.JACKSON_PROPERTY)
                        .orElse(MethodHelper.getPropertyName(Direction.OUT, fieldName)));
    }

    private static String getInputNameForField(Annotations annotationsForThisField, String fieldName) {
        return annotationsForThisField.getOneOfTheseMethodAnnotationsValue(
                Annotations.NAME,
                Annotations.JAKARTA_JSONB_PROPERTY,
                Annotations.JAVAX_JSONB_PROPERTY,
                Annotations.JACKSON_PROPERTY)
                .orElse(annotationsForThisField.getOneOfTheseAnnotationsValue(
                        Annotations.NAME,
                        Annotations.JAKARTA_JSONB_PROPERTY,
                        Annotations.JAVAX_JSONB_PROPERTY,
                        Annotations.JACKSON_PROPERTY)
                        .orElse(MethodHelper.getPropertyName(Direction.IN, fieldName)));
    }
}
