package io.smallrye.graphql.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.graphql.*;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;

/**
 * Basic test endpoint
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
public class TestEndpoint {

    private static final List<TestObject> persistedObjects = List.of(createTestObject("Alice"),createTestObject("Bob"));

    @Query
    public TestObject getTestObject(String yourname) {
        TestObject testObject = createTestObject(yourname);
        return testObject;
    }

    @Query
    public List<TestObject> getTestObjects() {
        TestObject p = createTestObject("Phillip");
        TestObject c = createTestObject("Charmaine");
        return Arrays.asList(new TestObject[]{p, c});
    }

    @Query("testObjectsPersisted")
    public List<TestObject> getTestObjectsPersisted() {
        return persistedObjects;
    }

    @Query
    public String[] arrayDefault(@DefaultValue("[\"creature\",\"comfort\"]") String[] values) {
        return values;
    }

    @Query
    public List<String> listDefault(@DefaultValue("[\"electric\",\"blue\"]") List<String> values) {
        return values;
    }

    // issues #429 and #472 reproducer part 1
    @Query
    public InterfaceWithOneGenericsParam<String> getGeneric1() {
        return new ClassWithOneGenericsParam<>("my param 1", "my name");
    }

    // issues #429 and #472 reproducer part 2
    @Query
    public InterfaceWithOneGenericsParam<Integer> getGeneric2() {
        return new ClassWithOneGenericsParam<>(22, "my name");
    }

    @Query
    public TestUnion basicUnion() {
        return new UnionMember("my name");
    }

    @Query
    public UnionOfInterfaces unionOfInterfacesDirectImplementor() {
        return new MemberOfManyUnions("im in many unions");
    }

    @Query
    public UnionOfInterfaces unionOfInterfacesNestedInterface1() {
        return new ObjectWithName("my name");
    }

    @Query
    public UnionOfInterfaces unionOfInterfacesNestedInterface2() {
        return new ObjectWithColor("purple");
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

    @Name("configuredSources")
    public List<TestSourceWithConfiguration> getTestSourcesWithConfiguration(@Source List<TestObject> testObjects, @NonNull TestSourceConfiguration configuration) {
        return testObjects.stream()
                .map(testObject -> new TestSourceWithConfiguration(configuration))
                .toList();
    }

    @Query
    public ContextInfo testContext() {
        Context context = SmallRyeContextManager.getCurrentSmallRyeContext();

        System.out.println(context.toString());

        ContextInfo contextInfo = new ContextInfo();
        contextInfo.executionId = context.getExecutionId();
        contextInfo.path = context.getPath();
        contextInfo.query = context.getQuery();
        return contextInfo;
    }

    private static TestObject createTestObject(String name) {
        String id = UUID.randomUUID().toString();
        TestObject testObject = new TestObject();
        testObject.setId(id);
        testObject.setName(name);
        testObject.addTestListObject(new TestListObject());
        return testObject;
    }
}
