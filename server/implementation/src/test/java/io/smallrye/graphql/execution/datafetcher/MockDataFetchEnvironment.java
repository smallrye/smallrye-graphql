package io.smallrye.graphql.execution.datafetcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionId;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLNamedType;

public class MockDataFetchEnvironment {

    public static DataFetchingEnvironment myFastQueryDfe(String typeName, String fieldName, String operationName,
            String executionId) {
        GraphQLNamedType query = mock(GraphQLNamedType.class);
        when(query.getName()).thenReturn(typeName);

        Field field = mock(Field.class);
        when(field.getName()).thenReturn(fieldName);

        OperationDefinition operationDefinition = mock(OperationDefinition.class);
        when(operationDefinition.getName()).thenReturn(operationName);

        ResultPath executionPath = mock(ResultPath.class);
        when(executionPath.toString()).thenReturn("/" + typeName + "/" + fieldName);
        ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
        when(executionStepInfo.getPath()).thenReturn(executionPath);

        DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
        when(dfe.getParentType()).thenReturn(query);
        when(dfe.getField()).thenReturn(field);
        when(dfe.getOperationDefinition()).thenReturn(operationDefinition);
        when(dfe.getExecutionStepInfo()).thenReturn(executionStepInfo);
        when(dfe.getExecutionId()).thenReturn(ExecutionId.from(executionId));

        return dfe;
    }

}
