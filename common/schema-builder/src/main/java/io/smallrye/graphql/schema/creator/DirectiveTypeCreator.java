package io.smallrye.graphql.schema.creator;

import static io.smallrye.graphql.schema.Annotations.DIRECTIVE;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.DirectiveArgument;
import io.smallrye.graphql.schema.model.DirectiveType;

public class DirectiveTypeCreator extends ModelCreator {
    private static final Logger LOG = Logger.getLogger(DirectiveTypeCreator.class.getName());

    public DirectiveTypeCreator(ReferenceCreator referenceCreator) {
        super(referenceCreator);
    }

    private static DotName NON_NULL = DotName.createSimple("org.eclipse.microprofile.graphql.NonNull");

    public DirectiveType create(ClassInfo classInfo) {
        LOG.debug("Creating directive from " + classInfo.name().toString());

        Annotations annotations = Annotations.getAnnotationsForClass(classInfo);

        DirectiveType directiveType = new DirectiveType();
        directiveType.setClassName(classInfo.name().toString());
        directiveType.setName(toDirectiveName(classInfo, annotations));
        directiveType.setDescription(DescriptionHelper.getDescriptionForType(annotations).orElse(null));
        directiveType.setLocations(getLocations(classInfo.declaredAnnotation(DIRECTIVE)));
        directiveType.setRepeatable(classInfo.hasAnnotation(Annotations.REPEATABLE));

        for (MethodInfo method : classInfo.methods()) {
            DirectiveArgument argument = new DirectiveArgument();
            argument.setReference(referenceCreator.createReferenceForOperationArgument(method.returnType(), null));
            argument.setName(method.name());
            Annotations annotationsForMethod = Annotations.getAnnotationsForInterfaceField(method);
            populateField(Direction.IN, argument, method.returnType(), annotationsForMethod);
            if (annotationsForMethod.containsOneOfTheseAnnotations(NON_NULL)) {
                argument.setNotNull(true);
            }
            directiveType.addArgumentType(argument);
        }

        return directiveType;
    }

    private String toDirectiveName(ClassInfo classInfo, Annotations annotations) {
        String name = TypeNameHelper.getAnyTypeName(classInfo, annotations, getTypeAutoNameStrategy());
        if (Character.isUpperCase(name.charAt(0)))
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return name;
    }

    private Set<String> getLocations(AnnotationInstance directiveAnnotation) {
        return Stream.of(directiveAnnotation.value("on").asEnumArray())
                .collect(toSet());
    }
}
