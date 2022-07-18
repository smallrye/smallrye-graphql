package io.smallrye.graphql.schema.creator.type;

import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.UnionType;

public class UnionCreator implements Creator<UnionType> {

    private static final Logger LOG = Logger.getLogger(UnionCreator.class.getName());

    private final ReferenceCreator referenceCreator;

    public UnionCreator(ReferenceCreator referenceCreator) {
        this.referenceCreator = referenceCreator;
    }

    @Override
    public UnionType create(ClassInfo classInfo, Reference reference) {
        LOG.debug("Creating union from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        // Name
        String name = TypeNameHelper.getAnyTypeName(classInfo,
                annotations,
                referenceCreator.getTypeAutoNameStrategy(),
                ReferenceType.UNION,
                reference.getClassParametrizedTypes());

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotations);

        return new UnionType(classInfo.name().toString(), name, maybeDescription.orElse(null));
    }
}
