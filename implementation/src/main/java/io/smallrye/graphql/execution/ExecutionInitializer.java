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

package io.smallrye.graphql.execution;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.execution.error.ExceptionHandler;

/**
 * Initialize GraphQL
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class ExecutionInitializer {
    private static final Logger LOG = Logger.getLogger(ExecutionInitializer.class.getName());

    @Inject
    private GraphQLSchema graphQLSchema;

    @Inject
    private ExceptionHandler exceptionHandler;

    @Produces
    private GraphQL graphQL;

    @PostConstruct
    void init() {
        if (graphQLSchema != null) {
            this.graphQL = GraphQL
                    .newGraphQL(graphQLSchema)
                    .queryExecutionStrategy(new QueryExecutionStrategy(exceptionHandler))
                    .mutationExecutionStrategy(new MutationExecutionStrategy(exceptionHandler))
                    .build();
        } else {
            LOG.warn("No GraphQL methods found. Try annotating your methods with @Query");
        }
    }

}
