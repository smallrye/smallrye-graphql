/*
 * Copyright 2020 Red Hat, Inc.
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

import org.jboss.logging.Logger;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import io.smallrye.graphql.execution.GraphQLConfig;

/**
 * Here we have the ability to mask certain messages to the client (for security reasons)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExceptionHandler implements DataFetcherExceptionHandler {
    private static final Logger LOG = Logger.getLogger(ExceptionHandler.class.getName());

    private final GraphQLConfig config;
    private final ExceptionLists exceptionLists;

    public ExceptionHandler(GraphQLConfig config) {
        this.config = config;
        this.exceptionLists = new ExceptionLists(config.getBlackList(), config.getWhiteList());
    }

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable throwable = handlerParameters.getException();
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();
        ExceptionWhileDataFetching error = getExceptionWhileDataFetching(throwable, sourceLocation, path);

        if (config.isPrintDataFetcherException()) {
            LOG.log(Logger.Level.ERROR, "Data Fetching Error", throwable);
        }

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }

    private ExceptionWhileDataFetching getExceptionWhileDataFetching(Throwable throwable, SourceLocation sourceLocation,
            ExecutionPath path) {
        if (throwable instanceof RuntimeException) {
            // Check for whitelist
            if (exceptionLists.isWhitelisted(throwable)) {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(config.getDefaultErrorMessage(), path, throwable, sourceLocation);
            }
        } else {
            // Check for blacklist
            if (exceptionLists.isBlacklisted(throwable)) {
                return new GraphQLExceptionWhileDataFetching(config.getDefaultErrorMessage(), path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            }
        }
    }
}
