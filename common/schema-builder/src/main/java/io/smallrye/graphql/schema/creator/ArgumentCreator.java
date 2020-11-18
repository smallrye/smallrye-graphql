package io.smallrye.graphql.schema.creator;

import java.util.Optional;

import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.SchemaBuilderException;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.Direction;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.MappingHelper;
import io.smallrye.graphql.schema.helper.MethodHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Creates a Argument object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ArgumentCreator {

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

        if (!IgnoreHelper.shouldIgnore(annotationsForThisArgument)) {
            // Argument Type
            Type argumentType = methodInfo.parameters().get(position);

            // Name
            String defaultName = methodInfo.parameterName(position);
            String name = annotationsForThisArgument.getOneOfTheseAnnotationsValue(Annotations.NAME)
                    .orElse(defaultName);

            // Description    
            Optional<String> maybeDescription = DescriptionHelper.getDescriptionForField(annotationsForThisArgument,
                    argumentType);

            Reference reference;
            if (isSourceAnnotationOnSourceOperation(annotationsForThisArgument, operation)) {
                reference = referenceCreator.createReferenceForSourceArgument(argumentType, annotationsForThisArgument);
            } else {
                reference = referenceCreator.createReferenceForOperationArgument(argumentType, annotationsForThisArgument);
            }

            Argument argument = new Argument(defaultName,
                    methodInfo.name(),
                    MethodHelper.getPropertyName(Direction.IN, methodInfo.name()),
                    name,
                    maybeDescription.orElse(null),
                    reference);

            // NotNull
            if (NonNullHelper.markAsNonNull(argumentType, annotationsForThisArgument)) {
                argument.setNotNull(true);
            }

            if (isSourceAnnotationOnSourceOperation(annotationsForThisArgument, operation)) {
                argument.setSourceArgument(true);
            }

            // Wrapper
            argument.setWrapper(WrapperCreator.createWrapper(argumentType).orElse(null));

            // TransformInfo
            argument.setTransformation(FormatHelper.getFormat(argumentType, annotationsForThisArgument).orElse(null));

            // MappingInfo
            argument.setMapping(MappingHelper.getMapping(argument, annotationsForThisArgument).orElse(null));

            // Default Value
            argument.setDefaultValue(DefaultValueHelper.getDefaultValue(annotationsForThisArgument).orElse(null));

            return Optional.of(argument);
        }
        return Optional.empty();
    }

    /**
     * Source operation on types should remove the Source argument
     *
     * @param annotationsForArgument
     * @param operationType
     * @return
     */
    private static boolean isSourceAnnotationOnSourceOperation(Annotations annotationsForArgument,
            Operation operation) {
        return operation.isSourceField() &&
                annotationsForArgument.containsOneOfTheseAnnotations(Annotations.SOURCE);
    }

}
