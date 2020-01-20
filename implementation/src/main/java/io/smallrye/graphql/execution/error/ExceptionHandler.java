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
package io.smallrye.graphql.execution.error;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.graphql.ConfigKey;
import org.jboss.logging.Logger;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;

/**
 * Here we have the ability to mask certain messages to the client (for security reasons)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class ExceptionHandler implements DataFetcherExceptionHandler {
    private static final Logger LOG = Logger.getLogger(ExceptionHandler.class.getName());

    @Inject
    @ConfigProperty(name = ConfigKey.DEFAULT_ERROR_MESSAGE, defaultValue = "Server Error")
    private String defaultErrorMessage;

    @Inject
    @ConfigProperty(name = ConfigKey.EXCEPTION_BLACK_LIST, defaultValue = "[]")
    private List<String> blackList;

    @Inject
    @ConfigProperty(name = ConfigKey.EXCEPTION_WHITE_LIST, defaultValue = "[]")
    private List<String> whiteList;

    @Inject
    @ConfigProperty(name = "mp.graphql.printDataFetcherException", defaultValue = "false")
    private boolean printDataFetcherException;

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable throwable = handlerParameters.getException();
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();
        ExceptionWhileDataFetching error = getExceptionWhileDataFetching(throwable, sourceLocation, path);

        if (printDataFetcherException) {
            throwable.printStackTrace();
        }

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }

    private ExceptionWhileDataFetching getExceptionWhileDataFetching(Throwable throwable, SourceLocation sourceLocation,
            ExecutionPath path) {
        if (throwable instanceof RuntimeException) {
            // Check for whitelist
            if (isWhitelisted(throwable.getClass().getName())) {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(defaultErrorMessage, path, throwable, sourceLocation);
            }
        } else {
            // Check for blacklist
            if (isBlacklisted(throwable.getClass().getName())) {
                return new GraphQLExceptionWhileDataFetching(defaultErrorMessage, path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            }
        }
    }

    private boolean isBlacklisted(String exceptionClassName) {
        return (blackList != null && blackList.contains(exceptionClassName));
    }

    private boolean isWhitelisted(String exceptionClassName) {
        return (whiteList != null && whiteList.contains(exceptionClassName));
    }
}
