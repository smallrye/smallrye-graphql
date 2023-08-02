package io.smallrye.graphql.schema.creator;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.BeanValidationDirectivesHelper;
import io.smallrye.graphql.schema.helper.DeprecatedDirectivesHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * Creates a Argument object
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ArgumentCreator extends ModelCreator {

    private final BeanValidationDirectivesHelper validationHelper;
    private final DeprecatedDirectivesHelper deprecatedHelper;

    private final Logger logger = Logger.getLogger(ArgumentCreator.class.getName());

    public ArgumentCreator(ReferenceCreator referenceCreator) {
        super(referenceCreator);
        validationHelper = new BeanValidationDirectivesHelper();
        deprecatedHelper = new DeprecatedDirectivesHelper();
    }

    @Override
    public String getDirectiveLocation() {
        return "ARGUMENT_DEFINITION";
    }

    /**
     * Create an argument model. Arguments exist on Operations as input parameters
     *
     * @param operation The operation
     * @param methodInfo the operation method
     * @param position the argument position
     * @return an Argument
     */
    public Optional<Argument> createArgument(Operation operation, MethodInfo methodInfo, short position) {
        if (position >= methodInfo.parametersCount()) {
            throw new SchemaBuilderException(
                    "Can not create argument for parameter [" + position + "] "
                            + "on method [" + methodInfo.declaringClass().name() + "#" + methodInfo.name() + "]: "
                            + "method has only " + methodInfo.parametersCount() + " parameters");
        }

        Annotations annotationsForThisArgument = Annotations.getAnnotationsForArgument(methodInfo, position);

        if (IgnoreHelper.shouldIgnore(annotationsForThisArgument)) {
            return Optional.empty();
        }

        // Argument Type
        Type argumentType = methodInfo.parameterType(position);

        // Name
        String defaultName = methodInfo.parameterName(position);
        String name = annotationsForThisArgument.getOneOfTheseAnnotationsValue(Annotations.NAME)
                .orElse(defaultName);

        Reference reference;
        if (isSourceAnnotationOnSourceOperation(annotationsForThisArgument, operation)) {
            reference = referenceCreator.createReferenceForSourceArgument(argumentType, annotationsForThisArgument);
        } else if (!argumentType.name().equals(CONTEXT)) {
            reference = referenceCreator.createReferenceForOperationArgument(argumentType, annotationsForThisArgument);
        } else {
            reference = CONTEXT_REF;
        }

        Argument argument = new Argument(defaultName,
                methodInfo.name(),
                MethodHelper.getPropertyName(Direction.IN, methodInfo.name()),
                name,
                reference);

        if (isSourceAnnotationOnSourceOperation(annotationsForThisArgument, operation)) {
            argument.setSourceArgument(true);
        }

        if (validationHelper != null) {
            List<DirectiveInstance> constraintDirectives = validationHelper
                    .transformBeanValidationConstraintsToDirectives(annotationsForThisArgument);
            if (!constraintDirectives.isEmpty()) {
                logger.debug("Adding constraint directives " + constraintDirectives + " to argument '" + argument.getName()
                        + "' of method '" + argument.getMethodName() + "'");
                argument.addDirectiveInstances(constraintDirectives);
            }
        }
        if (deprecatedHelper != null && directives != null) {
            List<DirectiveInstance> deprecatedDirectives = deprecatedHelper
                    .transformDeprecatedToDirectives(annotationsForThisArgument,
                            directives.getDirectiveTypes().get(DotName.createSimple("io.smallrye.graphql.api.Deprecated")));
            if (!deprecatedDirectives.isEmpty()) {
                logger.debug("Adding deprecated directives " + deprecatedDirectives + " to field '" + argument.getName()
                        + "' of  of method '" + argument.getMethodName() + "'");
                argument.addDirectiveInstances(deprecatedDirectives);
            }
        }

        populateField(Direction.IN, argument, argumentType, annotationsForThisArgument);

        return Optional.of(argument);
    }

    /**
     * Source operation on types should remove the Source argument
     */
    private static boolean isSourceAnnotationOnSourceOperation(Annotations annotationsForArgument,
            Operation operation) {
        return operation.isSourceField() &&
                annotationsForArgument.containsOneOfTheseAnnotations(Annotations.SOURCE);
    }

    private static final DotName CONTEXT = DotName.createSimple("io.smallrye.graphql.api.Context");
    private static final Reference CONTEXT_REF = new Reference.Builder()
            .className(CONTEXT.toString())
            .name(CONTEXT.toString())
            .type(ReferenceType.TYPE)
            .build();

}
