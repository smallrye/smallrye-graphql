package io.smallrye.graphql.test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.context.SmallRyeContext;

/**
 * Basic test endpoint
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
public class TestEndpoint {

    @Query
    public TestObject getTestObject(String yourname) {
        TestObject testObject = createTestObject(yourname);
        printContext("testObject");
        return testObject;
    }

    @Query
    public List<TestObject> getTestObjects() {
        TestObject p = createTestObject("Phillip");
        TestObject c = createTestObject("Charmaine");
        return Arrays.asList(new TestObject[] { p, c });
    }

    @Query
    public String[] arrayDefault(@DefaultValue("[\"creature\",\"comfort\"]") String[] values) {
        return values;
    }

    @Query
    public List<String> listDefault(@DefaultValue("[\"electric\",\"blue\"]") List<String> values) {
        return values;
    }

    @Name("timestamp")
    public TestSource getTestSource(@Source TestObject testObject, String indicator) {
        printContext("timestamp (source)");
        return new TestSource();
    }

    @Name("timestamps")
    public List<TestSource> getTestSources(@Source List<TestObject> testObjects) {
        return Arrays.asList(new TestSource[] { new TestSource() });
    }

    private void printContext(String from) {
        Context context = SmallRyeContext.getContext();
        System.err.println("================ " + from + " ================");
        System.err.println(">>>>>> executionId = " + context.getExecutionId());
        System.err.println(">>>>>> path = " + context.getPath());
        System.err.println(">>>>>> query = " + context.getQuery());
        System.err.println(">>>>>> arguments = " + context.getArguments());
        System.err.println(">>>>>> operationName = " + context.getOperationName().orElse(""));
        System.err.println(">>>>>> variables = " + context.getVariables().orElse(null));
        System.err.println(">>>>>> source = " + context.getSource());
        System.err.println(">>>>>> selectedFields = " + context.getSelectedFields());
    }

    private TestObject createTestObject(String name) {
        String id = UUID.randomUUID().toString();
        TestObject testObject = new TestObject();
        testObject.setId(id);
        testObject.setName(name);
        testObject.addTestListObject(new TestListObject());
        return testObject;
    }
}
