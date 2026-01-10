package io.smallrye.graphql.client.model.helper;

import static io.smallrye.graphql.client.model.Annotations.NAME;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.graphql.Name;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;

/**
 * Represents a model for a Java (jandex) class field, providing information about the field's characteristics
 * and associated directives.
 *
 * @author mskacelik
 */
public class FieldModel implements NamedElement {
    private FieldInfo field;
    private List<DirectiveInstance> directives;

    /**
     * Creates a new {@code FieldModel} instance.
     *
     * @param field The {@link FieldInfo} object representing the field.
     */
    FieldModel(FieldInfo field) {
        this.field = field;
        this.directives = DirectiveHelper.resolveDirectives(field.annotations().stream(), getDirectiveLocation())
                .map(DirectiveInstance::of)
                .collect(toList());
    }

    /**
     * Creates and returns a new {@code FieldModel} instance based on the provided field information and raw type.
     *
     * @param field The {@link FieldInfo} object representing the field.
     * @return A new {@code FieldModel} instance.
     */
    public static FieldModel of(FieldInfo field) {
        return new FieldModel(field);
    }

    /**
     * If the field is renamed with a {@link Name} annotation, the real field name is used as an alias.
     *
     * @return An {@link Optional} containing the alias if explicitly stated, otherwise empty.
     */
    public Optional<String> getAlias() {
        if (field.hasAnnotation(NAME)) {
            return Optional.of(getRawName());
        }
        return Optional.empty();
    }

    @Override
    public String getName() {
        if (field.hasAnnotation(NAME)) {
            return field.annotation(NAME).value().asString();
        }
        return getRawName();
    }

    @Override
    public String getRawName() {
        return field.name();
    }

    @Override
    public String getDirectiveLocation() {
        return "FIELD";
    }

    /**
     * Checks if the field has a specific annotation.
     *
     * @param annotation The annotation to check.
     * @return {@code true} if the field has the specified annotation, otherwise {@code false}.
     */
    public boolean hasAnnotation(DotName annotation) {
        return field.hasAnnotation(annotation);
    }

    /**
     * Checks if the field is static.
     *
     * @return {@code true} if the field is static, otherwise {@code false}.
     */
    public boolean isStatic() {
        return Modifier.isStatic(field.flags());
    }

    /**
     * Checks if the field is transient.
     *
     * @return {@code true} if the field is transient, otherwise {@code false}.
     */
    public boolean isTransient() {
        return Modifier.isTransient(field.flags());
    }

    /**
     * Checks if the field is synthetic.
     *
     * @return {@code true} if the field is synthetic, otherwise {@code false}.
     */
    public boolean isSynthetic() {
        return field.asField().isSynthetic();
    }

    /**
     * Gets the type model associated with the field.
     *
     * @return The {@link TypeModel} representing the type of the field.
     */
    public TypeModel getType() {
        return TypeModel.of(field.type());
    }

    /**
     * Checks if the field has any associated directives.
     *
     * @return {@code true} if the field has directives, otherwise {@code false}.
     */
    public boolean hasDirectives() {
        return !directives.isEmpty();
    }

    /**
     * Gets the list of directive instances associated with the field.
     *
     * @return The list of {@link DirectiveInstance} objects.
     */
    public List<DirectiveInstance> getDirectives() {
        return directives;
    }

    FieldInfo getFieldInfo() {
        return field;
    }
}
