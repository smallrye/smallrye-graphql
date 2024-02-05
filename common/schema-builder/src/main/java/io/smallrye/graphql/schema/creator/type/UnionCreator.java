package io.smallrye.graphql.schema.creator.type;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Directives;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.UnionType;

public class UnionCreator implements Creator<UnionType> {

    private static final Logger LOG = Logger.getLogger(UnionCreator.class.getName());

    private final ReferenceCreator referenceCreator;
    private Directives directives;

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
                reference.getAllParametrizedTypes());

        // Description
        Optional<String> maybeDescription = DescriptionHelper.getDescriptionForType(annotations);

        UnionType unionType = new UnionType(classInfo.name().toString(), name, maybeDescription.orElse(null));

        // Directives
        unionType.setDirectiveInstances(getDirectiveInstances(annotations, unionType.getClassName()));

        return unionType;

    }

    @Override
    public String getDirectiveLocation() {
        return "UNION";
    }

    private List<DirectiveInstance> getDirectiveInstances(Annotations annotations, String referenceName) {
        return directives.buildDirectiveInstances(annotations, getDirectiveLocation(), referenceName);
    }

    public void setDirectives(Directives directives) {
        this.directives = directives;
    }
}
