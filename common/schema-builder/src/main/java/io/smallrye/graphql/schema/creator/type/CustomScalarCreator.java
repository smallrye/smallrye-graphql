package io.smallrye.graphql.schema.creator.type;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.creator.ModelCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Directives;
import io.smallrye.graphql.schema.model.CustomScalarType;
import io.smallrye.graphql.schema.model.DirectiveInstance;

public class CustomScalarCreator extends ModelCreator {

    private static final Logger LOG = Logger.getLogger(CustomScalarCreator.class.getName());

    private Directives directives;

    public CustomScalarCreator(ReferenceCreator referenceCreator) {
        super(referenceCreator);
    }

    public CustomScalarType create(
            ClassInfo classInfo,
            String scalarName) {
        LOG.debug("Creating custom scalar from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        return new CustomScalarType(
                classInfo.name().toString(),
                scalarName,
                DescriptionHelper.getDescriptionForType(annotations).orElse(null),
                classInfo.interfaceNames().stream().map(DotName::toString).collect(Collectors.toSet()));

    }

    @Override
    public String getDirectiveLocation() {
        return "SCALAR";
    }

    private List<DirectiveInstance> getDirectiveInstances(Annotations annotations,
            String referenceName) {
        return directives.buildDirectiveInstances(annotations, getDirectiveLocation(), referenceName);
    }

    public void setDirectives(Directives directives) {
        // TODO bdupras add support for directives on custom scalars
        this.directives = directives;
    }
}
