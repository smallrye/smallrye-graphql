package io.smallrye.graphql.transformation;

import java.util.ArrayList;
import java.util.List;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherResult;
import graphql.execution.ResultPath;
import graphql.language.Argument;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;
import io.smallrye.graphql.scalar.GraphQLScalarTypes;
import io.smallrye.graphql.schema.model.Field;

/**
 * Exception thrown when the transformation failed on input parameters or return object.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TransformException extends AbstractDataFetcherException {
    private final Field field;
    private final String parameterValue;

    public TransformException(Throwable original, Field field, Object parameterValue) {
        super(original);
        this.field = field;
        if (parameterValue != null) {
            this.parameterValue = parameterValue.toString();
        } else {
            this.parameterValue = null;
        }
    }

    @Override
    public DataFetcherResult.Builder<Object> appendDataFetcherResult(DataFetcherResult.Builder<Object> builder,
            DataFetchingEnvironment dfe) {
        DataFetcherExceptionHandlerParameters handlerParameters = DataFetcherExceptionHandlerParameters
                .newExceptionParameters()
                .dataFetchingEnvironment(dfe)
                .exception(super.getCause())
                .build();

        SourceLocation sourceLocation = getSourceLocation(dfe, handlerParameters);

        List<String> paths = toPathList(handlerParameters.getPath());

        ValidationError error = new ValidationError(ValidationErrorType.WrongType,
                sourceLocation, "argument '" + field.getName() + "' with value 'StringValue{value='" + parameterValue
                        + "'}' is not a valid '" + getScalarTypeName() + "'",
                paths);

        return builder.error(error);
    }

    private String getScalarTypeName() {
        GraphQLScalarType graphQLScalarType = GraphQLScalarTypes.getScalarMap().get(field.getReference().getClassName());
        if (graphQLScalarType != null) {
            return graphQLScalarType.getName();
        }
        return "Unknown Scalar Type [" + field.getReference().getClassName() + "]";
    }

    private SourceLocation getSourceLocation(DataFetchingEnvironment dfe,
            DataFetcherExceptionHandlerParameters handlerParameters) {
        List<Argument> arguments = dfe.getField().getArguments();
        for (Argument a : arguments) {
            if (a.getName().equals(this.field.getName())) {
                return a.getSourceLocation();
            }
        }
        // Else fallback to more general
        return handlerParameters.getSourceLocation();
    }

    private List<String> toPathList(ResultPath path) {
        List<String> l = new ArrayList<>();
        for (Object o : path.toList()) {
            l.add(o.toString());
        }
        return l;
    }
}
