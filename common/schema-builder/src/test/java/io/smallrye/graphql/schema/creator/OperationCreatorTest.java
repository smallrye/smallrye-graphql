package io.smallrye.graphql.schema.creator;

import static org.junit.Assert.*;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.junit.Assert;
import org.junit.Test;

import io.smallrye.graphql.schema.IndexCreator;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.OperationType;

public class OperationCreatorTest {

    private OperationCreator operationCreator() {
        ReferenceCreator referenceCreator = new ReferenceCreator();
        ArgumentCreator argumentCreator = new ArgumentCreator(referenceCreator);
        return new OperationCreator(referenceCreator, argumentCreator);
    }

    @Test
    public void testFailOnNonPublicOperation() throws Exception {
        Index complete = IndexCreator.index(TestApi.class);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(TestApi.class.getName()));
        MethodInfo method = classByName.method("nonPublicQuery");

        try {
            operationCreator().createOperation(method, OperationType.Query, null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testPublicOperation() throws Exception {
        Index complete = IndexCreator.index(TestApi.class);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(TestApi.class.getName()));
        MethodInfo method = classByName.method("publicQuery");

        final Operation operation = operationCreator().createOperation(method, OperationType.Query, null);

        Assert.assertEquals("publicQuery", operation.getName());
    }

}
