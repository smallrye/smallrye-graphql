package io.smallrye.graphql.schema.test_generics;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.index.SchemaBuilderTest;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Schema;

public class GenericsTest {

    private static final Logger LOG = Logger.getLogger(GenericsTest.class.getName());

    @Test
    public void testSchemaString() throws Exception {

        Indexer indexer = new Indexer();
        SchemaBuilderTest.indexDirectory(indexer, "io/smallrye/graphql/schema/test_generics");
        Index index = indexer.complete();

        Schema schema = SchemaBuilder.build(index);
        assertNotNull(schema);

        String schemaString = SchemaBuilderTest.toString(schema);
        LOG.info(schemaString);

        // check types consistency in the scheme
        boolean correct = checkTypesInOperations(schema, schema.getQueries());
        correct = checkTypesInOperations(schema, schema.getMutations()) && correct;
        assertTrue(correct, "References in schema are invalid, see errors in log");

        // check type names in type definitions
        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithoutGenerics\""));
        assertTrue(schemaString.contains("\"ClassWithoutGenerics\": {"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithOneGenericsParam\""));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParam_String\": {"));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParam_Integer\": {"));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParam_StringInput\": {"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithTwoGenericsParams\""));
        assertTrue(schemaString.contains("\"ClassWithTwoGenericsParams_String_ClassWithOneGenericsParam_Integer\": {"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithOneGenericsParamToString2\""));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParamToString2_String\": {"));

        // appendix from generics is added even to classes with @Name and other annotations
        assertTrue(schemaString
                .contains("io.smallrye.graphql.schema.test_generics.ClassWithOneGenericsParamWithNameAnnotation\""));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParamWithNameAnnotationChanged_Integer\": {"));

        // check type names in interface definitions
        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.InterfaceWithOneGenericsParam\""));
        assertTrue(schemaString.contains("\"InterfaceWithOneGenericsParam_Integer\": {"));
        assertTrue(schemaString.contains("\"InterfaceWithOneGenericsParam_String\": {"));
    }

    protected boolean checkTypesInOperations(Schema schema, Collection<Operation> ops) {
        boolean correct = true;
        for (Operation op : ops) {
            correct = checkType(schema, op.getReference()) && correct;
            for (Argument arg : op.getArguments()) {
                correct = checkType(schema, arg.getReference()) && correct;
            }
        }
        return correct;
    }

    protected boolean checkType(Schema schema, Reference reference) {
        ReferenceType referenceType = reference.getType();
        String referenceName = reference.getName();
        if (null != referenceType)
            switch (referenceType) {
                case INTERFACE:
                    if (!schema.getInterfaces().containsKey(referenceName)) {
                        LOG.errorv("Missing interface: {0}", referenceName);
                        return false;
                    }
                    break;
                case TYPE:
                    if (!schema.getTypes().containsKey(referenceName)) {
                        LOG.errorv("Missing type: {0}", referenceName);
                        return false;
                    }
                    break;
                case INPUT:
                    if (!schema.getInputs().containsKey(referenceName)) {
                        LOG.errorv("Missing input: {0}", referenceName);
                        return false;
                    }
                    break;
                case ENUM:
                    if (!schema.getEnums().containsKey(referenceName)) {
                        LOG.errorv("Missing enum: {0}", referenceName);
                        return false;
                    }
                    break;
                default:
                    break;
            }
        return true;
    }
}
