package io.smallrye.graphql.schema.creator;

import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.AdaptToHelper;
import io.smallrye.graphql.schema.helper.AdaptWithHelper;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.Directives;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.model.Field;

/**
 * Abstract creator
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class ModelCreator {

    private Directives directives;
    protected final ReferenceCreator referenceCreator;

    public ModelCreator(ReferenceCreator referenceCreator) {
        this.referenceCreator = referenceCreator;
    }

    public void setDirectives(Directives directives) {
        this.directives = directives;
    }

    public ReferenceCreator getReferenceCreator() {
        return this.referenceCreator;
    }

    public TypeAutoNameStrategy getTypeAutoNameStrategy() {
        return this.referenceCreator.getTypeAutoNameStrategy();
    }

    /**
     * The the return type.This is usually the method return type, but can also be adapted to something else
     * 
     * @param methodInfo method
     * @return the return type
     */
    protected static Type getReturnType(MethodInfo methodInfo) {
        return methodInfo.returnType();
    }

    /**
     * The the return type.This is usually the method return type, but can also be adapted to something else
     * 
     * @param fieldInfo
     * @return the return type
     */
    protected static Type getReturnType(FieldInfo fieldInfo) {
        return fieldInfo.type();

    }

    protected void populateField(Direction direction, Field field, Type type, Annotations annotations) {
        // Wrapper
        field.setWrapper(WrapperCreator.createWrapper(type).orElse(null));

        doPopulateField(direction, field, type, annotations);
    }

    protected void populateField(Direction direction, Field field, Type fieldType, Type methodType, Annotations annotations) {
        // Wrapper
        field.setWrapper(WrapperCreator.createWrapper(fieldType, methodType).orElse(null));

        doPopulateField(direction, field, methodType, annotations);
    }

    private void doPopulateField(Direction direction, Field field, Type type, Annotations annotations) {
        // Description
        DescriptionHelper.getDescriptionForField(annotations, type).ifPresent(field::setDescription);

        // NotNull
        if (NonNullHelper.markAsNonNull(type, annotations)) {
            field.setNotNull(true);
        }

        // Transformation
        field.setTransformation(FormatHelper.getFormat(type, annotations).orElse(null));

        // Adapt to
        field.setAdaptTo(AdaptToHelper.getAdaptTo(field, annotations).orElse(null));

        // Adapt with
        field.setAdaptWith(AdaptWithHelper.getAdaptWith(direction, referenceCreator, field, annotations).orElse(null));

        // Default Value
        field.setDefaultValue(DefaultValueHelper.getDefaultValue(annotations).orElse(null));

        // Directives
        if (directives != null) { // this happens while scanning for the directive types
            field.addDirectiveInstances(
                    directives.buildDirectiveInstances(name -> annotations.getOneOfTheseAnnotations(name).orElse(null)));
        }
    }
}
