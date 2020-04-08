package io.smallrye.graphql.schema.type;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.model.Enum;

/**
 * Create an enum.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EnumCreator implements Creator<Enum> {
    private static final Logger LOG = Logger.getLogger(EnumCreator.class.getName());

    @Override
    public Enum create(ClassInfo classInfo) {
        LOG.debug("Creating enum from " + classInfo.name().toString());

        Enum enumType = new Enum(classInfo.name().toString());

        Annotations annotations = AnnotationsHelper.getAnnotationsForClass(classInfo);

        // Name
        String name = NameHelper.getEnumName(classInfo, annotations);
        enumType.setName(name);

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotations);
        enumType.setDescription(maybeDescription.orElse(null));

        // Values
        List<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {
            if (!field.type().kind().equals(Type.Kind.ARRAY)) {
                enumType.addValues(field.name());
            }
        }

        return enumType;
    }

}
