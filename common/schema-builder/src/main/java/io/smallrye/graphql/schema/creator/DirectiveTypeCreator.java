package io.smallrye.graphql.schema.creator;

import static io.smallrye.graphql.schema.Annotations.DIRECTIVE;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.TypeAutoNameStrategy;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.DirectiveArgument;
import io.smallrye.graphql.schema.model.DirectiveType;

public class DirectiveTypeCreator extends ModelCreator {
    private static final Logger LOG = Logger.getLogger(DirectiveTypeCreator.class.getName());

    private final ReferenceCreator referenceCreator;
    private final TypeAutoNameStrategy autoNameStrategy;

    public DirectiveTypeCreator(ReferenceCreator referenceCreator, TypeAutoNameStrategy autoNameStrategy) {
        this.referenceCreator = referenceCreator;
        this.autoNameStrategy = autoNameStrategy;
    }

    public DirectiveType create(ClassInfo classInfo) {
        LOG.debug("Creating directive from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        DirectiveType directiveType = new DirectiveType();
        directiveType.setClassName(classInfo.name().toString());
        directiveType.setName(toDirectiveName(classInfo, annotations));
        directiveType.setDescription(DescriptionHelper.getDescriptionForType(annotations).orElse(null));
        directiveType.setLocations(getLocations(classInfo.classAnnotation(DIRECTIVE)));

        for (MethodInfo method : classInfo.methods()) {
            DirectiveArgument argument = new DirectiveArgument();
            argument.setReference(referenceCreator.createReferenceForOperationArgument(method.returnType(), null));
            argument.setName(method.name());
            Annotations annotationsForMethod = Annotations.getAnnotationsForInterfaceField(method);
            populateField(argument, method.returnType(), annotationsForMethod);
            directiveType.addArgumentType(argument);
        }

        return directiveType;
    }

    private String toDirectiveName(ClassInfo classInfo, Annotations annotations) {
        String name = TypeNameHelper.getAnyTypeName((String) null, null, classInfo, annotations, autoNameStrategy);
        if (Character.isUpperCase(name.charAt(0)))
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return name;
    }

    private Set<String> getLocations(AnnotationInstance directiveAnnotation) {
        return Stream.of(directiveAnnotation.value("on").asEnumArray())
                .collect(toSet());
    }
}
