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

package io.smallrye.graphql.execution.datafetchers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jboss.logging.Logger;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLContext;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;

/**
 * a Custom GraphQL Error
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DataFetcherException extends Exception implements GraphQLError {
    private static final Logger LOG = Logger.getLogger(ReflectionDataFetcher.class.getName());
    private final DataFetchingEnvironment dfe;

    public DataFetcherException(DataFetchingEnvironment dfe, Throwable throwable) {
        super(throwable);
        this.dfe = dfe;
        GraphQLContext graphQLContext = (GraphQLContext) dfe.getContext();
        LOG.warn("========> " + throwable.getClass().getName());
        LOG.warn("========> " + throwable.getMessage());
        Stream<Map.Entry<Object, Object>> stream = graphQLContext.stream();
        Object[] toArray = stream.toArray();
        for (Object o : toArray) {
            LOG.warn("==> " + o);
        }
        //LOG.warn("========><=========");
        //((GraphQLContext) dfe.getLocalContext()).stream().forEach(System.out::println);
        LOG.warn("========> dfe. openration name() = " + dfe.getOperationDefinition().getOperation().name());
        LOG.warn("========> dfe.getExecutionStepInfo() = " + dfe.getExecutionStepInfo());
        LOG.warn("========> dfe.getOperationDefinition() = " + dfe.getOperationDefinition());
        LOG.warn("========> dfe.getSelectionSet() = " + dfe.getSelectionSet());
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> customAttributes = new LinkedHashMap<>();
        customAttributes.put("foo", "bar");
        customAttributes.put("fizz", "whizz");
        return customAttributes;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.DataFetchingException;
    }

}
