package io.smallrye.graphql.schema.creator.type;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.EnumType;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * This create an Enum Type.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EnumCreator implements Creator<EnumType> {
    private static final Logger LOG = Logger.getLogger(EnumCreator.class.getName());

    @Override
    public EnumType create(ClassInfo classInfo) {
        LOG.debug("Creating enum from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        // Name
        String name = TypeNameHelper.getAnyTypeName(ReferenceType.ENUM, classInfo, annotations);

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotations);

        EnumType enumType = new EnumType(classInfo.name().toString(), name, maybeDescription.orElse(null));

        // Values
        List<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {
            if (!field.type().kind().equals(Type.Kind.ARRAY)) {
                enumType.addValue(field.name());
            }
        }

        return enumType;
    }

}
