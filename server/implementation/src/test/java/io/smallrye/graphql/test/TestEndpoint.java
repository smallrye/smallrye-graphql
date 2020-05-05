package io.smallrye.graphql.test;

import java.util.UUID;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

/**
 * Basic test endpoint
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
public class TestEndpoint {

    @Query
    public TestObject getTestObject(String yourname) {
        String id = UUID.randomUUID().toString();
        TestObject testObject = new TestObject();
        testObject.setId(id);
        testObject.setName(yourname);
        return testObject;
    }

}
