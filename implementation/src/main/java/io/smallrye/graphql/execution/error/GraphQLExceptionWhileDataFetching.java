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

import graphql.ExceptionWhileDataFetching;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;

/**
 * Simple way to override the message to only use the original exception message
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLExceptionWhileDataFetching extends ExceptionWhileDataFetching {

    private final String message;

    public GraphQLExceptionWhileDataFetching(ExecutionPath path, Throwable exception, SourceLocation sourceLocation) {
        super(path, exception, sourceLocation);
        this.message = super.getException().getMessage();
    }

    public GraphQLExceptionWhileDataFetching(String message, ExecutionPath path, Throwable exception,
            SourceLocation sourceLocation) {
        super(path, exception, sourceLocation);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
