package io.smallrye.graphql.schema.creator.type;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.Directives;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.EnumType;
import io.smallrye.graphql.schema.model.EnumValue;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * This create an Enum Type.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EnumCreator implements Creator<EnumType> {
    private static final Logger LOG = Logger.getLogger(EnumCreator.class.getName());

    private final TypeAutoNameStrategy autoNameStrategy;
    private Directives directives;

    public EnumCreator(TypeAutoNameStrategy autoNameStrategy) {
        this.autoNameStrategy = autoNameStrategy;
    }

    public void setDirectives(Directives directives) {
        this.directives = directives;
    }

    @Override
    public EnumType create(ClassInfo classInfo, Reference reference) {
        LOG.debug("Creating enum from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        // Name
        String name = TypeNameHelper.getAnyTypeName(classInfo,
                annotations,
                autoNameStrategy,
                ReferenceType.ENUM,
                reference.getClassParametrizedTypes());

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotations);

        EnumType enumType = new EnumType(classInfo.name().toString(), name, maybeDescription.orElse(null));

        // Directives
        enumType.setDirectiveInstances(getDirectiveInstances(annotations));

        // Values
        List<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {
            if (classInfo.name().equals(field.type().name())) { // Only include the enum fields
                Annotations annotationsForField = Annotations.getAnnotationsForPojo(Direction.OUT, field);
                if (!field.type().kind().equals(Type.Kind.ARRAY) && !IgnoreHelper.shouldIgnore(annotationsForField, field)) {
                    String description = annotationsForField.getOneOfTheseAnnotationsValue(Annotations.DESCRIPTION)
                            .orElse(null);
                    enumType.addValue(new EnumValue(description, field.name(), getDirectiveInstances(annotationsForField)));
                }
            }
        }

        return enumType;
    }

    private List<DirectiveInstance> getDirectiveInstances(Annotations annotations) {
        return directives.buildDirectiveInstances(dotName -> annotations.getOneOfTheseAnnotations(dotName).orElse(null));
    }

}
