/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.graphql.schema.type.scalar;

import java.util.ArrayList;
import java.util.List;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;

/**
 * Exception thrown when the transformation failed on input parameters.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TransformException extends RuntimeException {
    private final String parameterName;
    private final String parameterValue;
    private final GraphQLScalarType scalar;

    public TransformException(Throwable original, GraphQLScalarType forScalar, String parameterName, String parameterValue) {
        super(original);
        this.scalar = forScalar;
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    public DataFetcherResult<Object> getDataFetcherResult(DataFetchingEnvironment dfe) {

        DataFetcherExceptionHandlerParameters handlerParameters = DataFetcherExceptionHandlerParameters
                .newExceptionParameters()
                .dataFetchingEnvironment(dfe)
                .exception(super.getCause())
                .build();

        SourceLocation sourceLocation = handlerParameters.getSourceLocation();

        List<String> paths = toPathList(handlerParameters.getPath());

        ValidationError error = new ValidationError(ValidationErrorType.WrongType,
                sourceLocation, "argument '" + parameterName + "' with value 'StringValue{value='" + parameterValue
                        + "'}' is not a valid '" + scalar.getName() + "'",
                paths);

        return DataFetcherResult.newResult()
                .error(error)
                .build();
    }

    private List<String> toPathList(ExecutionPath path) {
        List<String> l = new ArrayList<>();
        for (Object o : path.toList()) {
            l.add(o.toString());
        }
        return l;
    }
}
