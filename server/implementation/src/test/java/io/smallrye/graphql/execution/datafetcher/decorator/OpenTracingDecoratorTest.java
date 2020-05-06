package io.smallrye.graphql.execution.datafetcher.decorator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import graphql.GraphQLContext;
import graphql.execution.ExecutionId;
import graphql.execution.ExecutionPath;
import graphql.execution.ExecutionStepInfo;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLNamedType;
import io.opentracing.Span;
import io.opentracing.mock.MockSpan;
import io.smallrye.graphql.opentracing.MockTracerOpenTracingService;

public class OpenTracingDecoratorTest {

    private DataFetchingEnvironment dfe;

    @Before
    public void reset() {
        MockTracerOpenTracingService.MOCK_TRACER.reset();
        this.dfe = dataFetchingEnvironment();
    }

    private DataFetchingEnvironment dataFetchingEnvironment() {
        Field field = mock(Field.class);
        when(field.getName()).thenReturn("myFastQuery");
        GraphQLNamedType query = mock(GraphQLNamedType.class);
        when(query.getName()).thenReturn("Query");
        OperationDefinition operationDefinition = mock(OperationDefinition.class);
        when(operationDefinition.getName()).thenReturn("someOperation");
        ExecutionPath executionPath = mock(ExecutionPath.class);
        when(executionPath.toString()).thenReturn("/Query/myFastQuery");
        ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
        when(executionStepInfo.getPath()).thenReturn(executionPath);

        DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
        when(dfe.getParentType()).thenReturn(query);
        when(dfe.getField()).thenReturn(field);
        when(dfe.getExecutionId()).thenReturn(ExecutionId.from("1"));
        when(dfe.getOperationDefinition()).thenReturn(operationDefinition);
        when(dfe.getExecutionStepInfo()).thenReturn(executionStepInfo);
        return dfe;
    }

    @Test
    public void testFetchTracing() {
        OpenTracingDecorator openTracingDecorator = new OpenTracingDecorator();

        openTracingDecorator.before(dfe);
        openTracingDecorator.after(dfe, GraphQLContext.newContext().build());

        Assert.assertEquals("One span should be finished", 1, MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size());
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        Assert.assertEquals("GraphQL:Query.myFastQuery", span.operationName());
        Assert.assertEquals("ExecutionId should be present in span", "1", span.tags().get("graphql.executionId"));
        Assert.assertEquals("field name should be present in span", "myFastQuery", span.tags().get("graphql.field"));
        Assert.assertEquals("parent name should be present in span", "Query", span.tags().get("graphql.parent"));
        Assert.assertEquals("path should be present in span", "/Query/myFastQuery", span.tags().get("graphql.path"));
        Assert.assertEquals("operation name should be present in span", "someOperation",
                span.tags().get("graphql.operationName"));
    }

    @Test
    public void shouldAddSpanToContext() {
        final GraphQLContext context = GraphQLContext.newContext().build();

        OpenTracingDecorator openTracingDecorator = new OpenTracingDecorator();

        openTracingDecorator.before(dfe);
        openTracingDecorator.after(dfe, context);

        Assert.assertEquals("One span should be finished", 1, MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size());
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        Span spanFromContext = context.get(Span.class);

        Assert.assertEquals("Span should be added to context", spanFromContext, span);
    }
}
