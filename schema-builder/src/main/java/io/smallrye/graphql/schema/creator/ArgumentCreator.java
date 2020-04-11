package io.smallrye.graphql.schema.creator;

import java.util.Optional;

import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Reference;

/**
 * Creates a Argument object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ArgumentCreator {

    private ArgumentCreator() {
    }

    /**
     * Create an argument model. Arguments exist on Operations as input parameters
     * 
     * @param type The argument type
     * @param methodInfo the operation method
     * @param position the argument position
     * @return an Argument
     */
    public static Optional<Argument> createArgument(Type type, MethodInfo methodInfo, short position) {
        Annotations annotationsForThisArgument = Annotations.getAnnotationsForArgument(methodInfo, position);

        if (!IgnoreHelper.shouldIgnore(annotationsForThisArgument)) {

            // Name
            String defaultName = methodInfo.parameterName(position);
            String name = annotationsForThisArgument.getOneOfTheseAnnotationsValue(Annotations.NAME)
                    .orElse(defaultName);

            // Description    
            Optional<String> maybeDescription = DescriptionHelper.getDescriptionForField(annotationsForThisArgument, type);

            // Argument Type
            Type argumentType = type;
            if (methodInfo.parameters() != null && !methodInfo.parameters().isEmpty()) {
                argumentType = methodInfo.parameters().get(position);
            }
            Reference reference = ReferenceCreator.createReferenceForOperationArgument(argumentType,
                    annotationsForThisArgument);

            Argument argument = new Argument(argumentType.name().toString(),
                    methodInfo.name(),
                    name,
                    maybeDescription.orElse(null),
                    reference);

            // NotNull
            if (NonNullHelper.markAsNonNull(argumentType, annotationsForThisArgument)) {
                argument.markNotNull();
            }

            // Array
            argument.setArray(ArrayCreator.createArray(argumentType));

            // Format
            argument.setFormat(FormatHelper.getFormat(argumentType, annotationsForThisArgument));

            // Default Value
            Optional maybeDefaultValue = DefaultValueHelper.getDefaultValue(annotationsForThisArgument);
            argument.setDefaultValue(maybeDefaultValue);

            return Optional.of(argument);
        }
        return Optional.empty();
    }
}
