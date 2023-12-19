package io.smallrye.graphql.schema.creator.type;

import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.FLOAT_TYPE;
import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.INT_TYPE;
import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.STRING_TYPE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.creator.ModelCreator;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Directives;
import io.smallrye.graphql.schema.model.CustomScalarType;
import io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType;
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

        Set<String> interfaces = classInfo.interfaceNames().stream().map(DotName::toString)
                .collect(Collectors.toSet());
        CustomScalarPrimitiveType customScalarPrimitiveType;
        if (interfaces.contains("io.smallrye.graphql.scalar.custom.CustomIntScalar")) {
            checkForOneArgConstructor(classInfo, BigInteger.class);
            customScalarPrimitiveType = INT_TYPE;
        } else if (interfaces.contains("io.smallrye.graphql.scalar.custom.CustomFloatScalar")) {
            checkForOneArgConstructor(classInfo, BigDecimal.class);
            customScalarPrimitiveType = FLOAT_TYPE;
        } else if (interfaces.contains("io.smallrye.graphql.scalar.custom.CustomStringScalar")) {
            checkForOneArgConstructor(classInfo, String.class);
            customScalarPrimitiveType = STRING_TYPE;
        } else {
            throw new RuntimeException(classInfo.name().toString() + " is required to implement a "
                    + "known CustomScalar primitive type. (CustomStringScalar, CustomFloatScalar, "
                    + "CustomIntScalar)");
        }

        return new CustomScalarType(
                classInfo.name().toString(),
                scalarName,
                DescriptionHelper.getDescriptionForType(annotations).orElse(null),
                customScalarPrimitiveType);

    }

    private static void checkForOneArgConstructor(final ClassInfo classInfo, Class<?> argType) {
        if (classInfo.constructors().stream().noneMatch(methodInfo -> methodInfo.parameters().size() == 1
                && methodInfo.parameterType(0).equals(ClassType.create(argType)))) {
            throw new RuntimeException(classInfo.name().toString() + " is required to implement a "
                    + "one arg constructor with a type of " + argType.getName());
        }
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
        this.directives = directives;
    }
}
