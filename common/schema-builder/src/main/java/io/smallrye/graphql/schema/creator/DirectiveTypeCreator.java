package io.smallrye.graphql.schema.creator;

import static io.smallrye.graphql.schema.Annotations.DIRECTIVE;
import static io.smallrye.graphql.schema.Annotations.NON_NULL;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.api.federation.policy.Policy;
import io.smallrye.graphql.api.federation.policy.PolicyItem;
import io.smallrye.graphql.api.federation.requiresscopes.RequiresScopes;
import io.smallrye.graphql.api.federation.requiresscopes.ScopeItem;
import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.TypeNameHelper;
import io.smallrye.graphql.schema.model.DirectiveArgument;
import io.smallrye.graphql.schema.model.DirectiveType;

public class DirectiveTypeCreator extends ModelCreator {
    private static final DotName POLICY = DotName.createSimple(Policy.class.getName());
    private static final DotName POLICY_ITEM = DotName.createSimple(PolicyItem.class.getName());
    private static final DotName REQUIRES_SCOPES = DotName.createSimple(RequiresScopes.class.getName());
    private static final DotName SCOPE = DotName.createSimple(ScopeItem.class.getName());

    private static final Logger LOG = Logger.getLogger(DirectiveTypeCreator.class.getName());

    public DirectiveTypeCreator(ReferenceCreator referenceCreator) {
        super(referenceCreator);
    }

    @Override
    public String getDirectiveLocation() {
        throw new IllegalArgumentException(
                "This method should never be called since 'DirectiveType' cannot have another directives");
    }

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
            Type argumentType;
            if (classInfo.name().equals(POLICY) || classInfo.name().equals(REQUIRES_SCOPES)) {
                // For both of these directives, we need to override the argument type to be an array of nested arrays,
                // where none of the nested elements can be null
                DotName typeName;
                if (classInfo.name().equals(POLICY)) {
                    typeName = POLICY_ITEM;
                } else {
                    typeName = SCOPE;
                }
                AnnotationInstance nonNullAnnotation = AnnotationInstance.create(NON_NULL, null,
                        Collections.emptyList());
                Type type = ClassType.createWithAnnotations(typeName, Type.Kind.CLASS,
                        new AnnotationInstance[] { nonNullAnnotation });
                argumentType = buildArrayType(type, 2, nonNullAnnotation);
            } else {
                argumentType = method.returnType();
            }
            argument.setReference(referenceCreator.createReferenceForOperationArgument(argumentType, null));
            argument.setName(method.name());
            Annotations annotationsForMethod = Annotations.getAnnotationsForInterfaceField(method);
            populateField(Direction.IN, argument, argumentType, annotationsForMethod);
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

    private static Type buildArrayType(Type baseType, int dimensions, AnnotationInstance annotation) {
        Type currentType = baseType;
        for (int i = 0; i < dimensions; i++) {
            ArrayType.Builder builder = ArrayType.builder(currentType, 1);
            builder.addAnnotation(annotation);
            currentType = builder.build();
        }
        return currentType;
    }
}
