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
package io.smallrye.graphql.execution;

import graphql.execution.AsyncSerialExecutionStrategy;
import io.smallrye.graphql.execution.error.ExceptionHandler;

/**
 * Execution strategy to use our own exception handler
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MutationExecutionStrategy extends AsyncSerialExecutionStrategy {

    public MutationExecutionStrategy(ExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }
}
