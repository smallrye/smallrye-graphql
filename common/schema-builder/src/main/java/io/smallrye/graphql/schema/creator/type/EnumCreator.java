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
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.EnumType;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.TypeAutoNameStrategy;

/**
 * This create an Enum Type.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EnumCreator implements Creator<EnumType> {
    private static final Logger LOG = Logger.getLogger(EnumCreator.class.getName());

    private final TypeAutoNameStrategy autoNameStrategy;

    public EnumCreator(TypeAutoNameStrategy autoNameStrategy) {
        this.autoNameStrategy = autoNameStrategy;
    }

    @Override
    public EnumType create(ClassInfo classInfo, Reference reference) {
        LOG.debug("Creating enum from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        // Name
        String name = TypeNameHelper.getAnyTypeName(reference, ReferenceType.ENUM, classInfo, annotations, autoNameStrategy);

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotations);

        EnumType enumType = new EnumType(classInfo.name().toString(), name, maybeDescription.orElse(null));

        // Values
        List<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {
            if (classInfo.name().equals(field.type().name())) { // Only include the enum fields
                Annotations annotationsForField = Annotations.getAnnotationsForPojo(Direction.OUT, field);
                if (!field.type().kind().equals(Type.Kind.ARRAY) && !IgnoreHelper.shouldIgnore(annotationsForField, field)) {
                    enumType.addValue(field.name());
                }
            }
        }

        return enumType;
    }

}
