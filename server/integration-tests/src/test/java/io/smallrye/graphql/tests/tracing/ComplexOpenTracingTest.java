package io.smallrye.graphql.tests.tracing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.smallrye.graphql.tests.GraphQLAssured;

//@RunWith(Arquillian.class)
public class ComplexOpenTracingTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "tracing-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset("smallrye.graphql.tracing.enabled=true"),
                        "META-INF/microprofile-config.properties")
                .addClasses(TracerProducer.class, DummyGraphQLApi.class, Foo.class, Foo2.class, Foo3.class);
    }

    @ArquillianResource
    URL testingURL;

    @Inject
    MockTracer tracer;

    //@Before
    public void prepare() {
        tracer.reset();
    }

    //@Test
    public void testThreeTraces() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String query = "{"
                + " foo {"
                + "     foo2 {"
                + "         foo3 {"
                + "             value"
                + "         }"
                + "     }"
                + "     foo2uni {"
                + "         value"
                + "     }"
                + "}}";

        // execute three times to create three traces
        graphQLAssured.post(query);
        graphQLAssured.post(query);
        // log one of the responses to facilitate debugging
        System.out.println("Query response: " + graphQLAssured.post(query));
        System.out.println("Finished spans: " + tracer.finishedSpans());

        Map<Long, List<MockSpan>> groupedByTraceId = tracer.finishedSpans()
                .stream()
                .collect(Collectors.groupingBy(span -> span.context().traceId()));
        // we created 3 traces, so after grouping by trace ID, we should have 3 groups
        assertEquals(3, groupedByTraceId.size());

        // and each group should have the same structure because the query was the same
        for (Map.Entry<Long, List<MockSpan>> entry : groupedByTraceId.entrySet()) {
            List<MockSpan> spanList = entry.getValue();

            // find the root span of this trace
            List<MockSpan> spansWithoutParent = spanList.stream().filter(s -> s.parentId() == 0)
                    .collect(Collectors.toList());
            assertEquals("Each trace needs to have exactly one span without a parent. List of spans: " + spanList,
                    1, spansWithoutParent.size());
            MockSpan parentSpan = spansWithoutParent.get(0);

            assertTrue("Parent span needs to be named GraphQL",
                    parentSpan.operationName().startsWith("GraphQL"));

            MockSpan fooSpan = findSpan("GraphQL:Query.foo", spanList);
            assertEquals("/foo", fooSpan.tags().get("graphql.path"));

            MockSpan foo2Span = findSpan("GraphQL:Foo.foo2", spanList);
            assertEquals("/foo/foo2", foo2Span.tags().get("graphql.path"));

            MockSpan foo3Span = findSpan("GraphQL:Foo2.foo3", spanList);
            assertEquals("/foo/foo2/foo3", foo3Span.tags().get("graphql.path"));

            MockSpan foo2UniSpan = findSpan("GraphQL:Foo.foo2uni", spanList);
            assertEquals("/foo/foo2uni", foo2UniSpan.tags().get("graphql.path"));
        }

    }

    private MockSpan findSpan(String operationName, List<MockSpan> spans) {
        return spans.stream()
                .filter(span -> (span.operationName().equals(operationName)))
                .findFirst()
                .get();
    }

}
