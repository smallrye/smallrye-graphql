package io.smallrye.graphql.schema.creator;

import java.util.Optional;

import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.AdapterHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.model.Adapter;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * Creates a Argument object
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ArgumentCreator extends ModelCreator {

    private final ReferenceCreator referenceCreator;

    public ArgumentCreator(ReferenceCreator referenceCreator) {
        this.referenceCreator = referenceCreator;
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
        if (position >= methodInfo.parameters().size()) {
            throw new SchemaBuilderException(
                    "Can not create argument for parameter [" + position + "] "
                            + "on method [" + methodInfo.declaringClass().name() + "#" + methodInfo.name() + "]: "
                            + "method has only " + methodInfo.parameters().size() + " parameters");
        }

        Annotations annotationsForThisArgument = Annotations.getAnnotationsForArgument(methodInfo, position);

        // Adapting
        Optional<Adapter> adapter = AdapterHelper.getAdapter(annotationsForThisArgument);

        if (IgnoreHelper.shouldIgnore(annotationsForThisArgument)) {
            return Optional.empty();
        }

        // Argument Type
        Type argumentType = methodInfo.parameters().get(position);

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

        populateField(argument, argumentType, adapter, annotationsForThisArgument);

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
    private static final Reference CONTEXT_REF = new Reference(CONTEXT.toString(), CONTEXT.toString(), ReferenceType.TYPE);
}
