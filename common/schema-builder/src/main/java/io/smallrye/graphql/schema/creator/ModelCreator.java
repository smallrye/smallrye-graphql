package io.smallrye.graphql.schema.creator;

import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Directives;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.helper.MappingHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Adapter;
import io.smallrye.graphql.schema.model.Field;

/**
 * Abstract creator
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class ModelCreator {

    private Directives directives;

    public void setDirectives(Directives directives) {
        this.directives = directives;
    }

    /**
     * The the return type. This is usually the method return type, but can also be adapted to something else
     * 
     * @param adapter possible adapter
     * @param methodInfo method
     * @return the return type
     */
    protected static Type getReturnType(Optional<Adapter> adapter, MethodInfo methodInfo) {
        if (adapter.isPresent()) {
            ClassInfo classInfo = ScanningContext.getIndex().getClassByName(DotName.createSimple(adapter.get().getToClass()));
            return classInfo.asType().target();
        } else {
            return methodInfo.returnType();
        }
    }

    /**
     * The the return type.This is usually the method return type, but can also be adapted to something else
     * 
     * @param adapter possible adapter
     * @param fieldInfo
     * @return the return type
     */
    protected static Type getReturnType(Optional<Adapter> adapter, FieldInfo fieldInfo) {
        if (adapter.isPresent()) {
            return Type.create(DotName.createSimple(adapter.get().getToClass()), Type.Kind.CLASS);
        }
        return fieldInfo.type();

    }

    protected void populateField(Field field, Type type, Annotations annotations) {
        populateField(field, type, Optional.empty(), annotations);
    }

    protected void populateField(Field field, Type type, Optional<Adapter> adapter, Annotations annotations) {
        // Wrapper
        field.setWrapper(WrapperCreator.createWrapper(type).orElse(null));

        doPopulateField(field, type, adapter, annotations);
    }

    protected void populateField(Field field, Type fieldType, Type methodType, Optional<Adapter> adapter,
            Annotations annotations) {
        // Wrapper
        field.setWrapper(WrapperCreator.createWrapper(fieldType, methodType).orElse(null));

        doPopulateField(field, methodType, adapter, annotations);
    }

    private void doPopulateField(Field field, Type type, Optional<Adapter> adapter, Annotations annotations) {
        // Description
        DescriptionHelper.getDescriptionForField(annotations, type).ifPresent(field::setDescription);

        // NotNull
        if (NonNullHelper.markAsNonNull(type, annotations)) {
            field.setNotNull(true);
        }

        // Transformation
        field.setTransformation(FormatHelper.getFormat(type, annotations).orElse(null));

        // Mapping
        field.setMapping(MappingHelper.getMapping(field, annotations).orElse(null));

        // Adapting
        field.setAdapter(adapter.orElse(null));

        // Default Value
        field.setDefaultValue(DefaultValueHelper.getDefaultValue(annotations).orElse(null));

        // Directives
        if (directives != null) { // this happens while scanning for the directive types
            field.setDirectiveInstances(
                    directives.buildDirectiveInstances(name -> annotations.getOneOfTheseAnnotations(name).orElse(null)));
        }
    }
}
