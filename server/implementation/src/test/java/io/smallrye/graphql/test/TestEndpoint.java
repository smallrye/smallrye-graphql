package io.smallrye.graphql.test;

import java.util.ArrayList;
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

    // This method will be ignored, with a WARN in the log, due to below duplicate
    @Name("timestamp")
    public TestSource getTestSource(@Source TestObject testObject, String indicator) {
        return new TestSource();
    }

    @Name("timestamp")
    public List<TestSource> getTestSources(@Source List<TestObject> testObjects) {
        List<TestSource> batched = new ArrayList<>();
        for (TestObject testObject : testObjects) {
            batched.add(new TestSource());
        }

        return batched;
    }

    @Query
    public ContextInfo testContext() {
        Context context = SmallRyeContext.getContext();

        System.out.println(context.toString());

        ContextInfo contextInfo = new ContextInfo();
        contextInfo.executionId = context.getExecutionId();
        contextInfo.path = context.getPath();
        contextInfo.query = context.getQuery();
        return contextInfo;
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
