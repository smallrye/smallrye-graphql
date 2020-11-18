package io.smallrye.graphql.schema.test_generics;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Set;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.index.SchemaBuilderTest;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.schema.model.InterfaceType;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.model.Type;

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
        correct = checkTypesInTypes(schema, schema.getTypes().values()) && correct;
        correct = checkTypesInInputTypes(schema, schema.getInputs().values()) && correct;
        correct = checkTypesInInterfaceTypes(schema, schema.getInterfaces().values()) && correct;

        assertTrue(correct, "References in schema are invalid, see errors in log");

        //check arrays are returned correctly
        checkOperationReturnsArray(schema.getQueries());
        checkOperationReturnsArray(schema.getMutations());

        // check type names in type definitions
        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithoutGenerics\""));
        assertTrue(schemaString.contains("\"ClassWithoutGenerics\": {"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithOneGenericsParam\""));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParam_String\": {"));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParam_Int\": {"));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParam_StringInput\": {"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithTwoGenericsParams\""));
        assertTrue(schemaString.contains("\"ClassWithTwoGenericsParams_String_ClassWithOneGenericsParam_Int\": {"));
        assertTrue(schemaString.contains("\"ClassWithTwoGenericsParams_String_Int\": {"));

        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.ClassWithOneGenericsParamToString2\""));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParamToString2\": {"));

        // error #423 tests
        assertTrue(schemaString
                .contains("io.smallrye.graphql.schema.test_generics.ClassWithGenericAttributeResolvedFromEnclosingClass\""));
        assertTrue(schemaString.contains("\"ClassWithGenericAttributeResolvedFromEnclosingClass_String\": {"));
        assertTrue(schemaString.contains(
                "io.smallrye.graphql.schema.test_generics.ClassWithGenericListAttributeResolvedFromEnclosingClass\""));
        assertTrue(schemaString.contains("\"ClassWithGenericListAttributeResolvedFromEnclosingClass_DateTime\": {"));
        assertTrue(schemaString.contains(
                "io.smallrye.graphql.schema.test_generics.ClassWithGenericArrayAttributeResolvedFromEnclosingClass\""));
        assertTrue(schemaString.contains("\"ClassWithGenericArrayAttributeResolvedFromEnclosingClass_Int\": {"));

        // appendix from generics is added even to classes with @Name and other annotations
        assertTrue(schemaString
                .contains("io.smallrye.graphql.schema.test_generics.ClassWithOneGenericsParamWithNameAnnotation\""));
        assertTrue(schemaString.contains("\"ClassWithOneGenericsParamWithNameAnnotationChanged_Int\": {"));

        // check type names in interface definitions
        assertTrue(schemaString.contains("io.smallrye.graphql.schema.test_generics.InterfaceWithOneGenericsParam\""));
        assertTrue(schemaString.contains("\"InterfaceWithOneGenericsParam_Int\": {"));
        assertTrue(schemaString.contains("\"InterfaceWithOneGenericsParam_String\": {"));
    }

    /**
     * Checks that operations with name starting by "getListOf" or "getArrayOf" return GraphQL array, and other no.
     * 
     * @param ops
     */
    protected void checkOperationReturnsArray(Collection<Operation> ops) {
        for (Operation o : ops) {
            String on = o.getMethodName();
            if (on.startsWith("getListOf") || on.startsWith("getArrayOf") || on.startsWith("setListOf")
                    || on.startsWith("setArrayOf")) {
                assertTrue(o.getWrapper() != null && o.getWrapper().isCollectionOrArray(),
                        "GraphQL array have to be returned for operation named " + o.getMethodName() + " - wrapper = "
                                + o.getWrapper());
            } else {
                assertTrue(o.getWrapper() == null || !o.getWrapper().isCollectionOrArray(),
                        "GraphQL array MUST NOT be returned for operation named " + o.getMethodName());
            }
        }

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

    protected boolean checkTypesInTypes(Schema schema, Collection<Type> ops) {
        boolean correct = true;

        for (Type op : ops) {
            correct = checkTypesInFields(schema, op.getFields().values()) && correct;
            correct = checkInterfacesExist(schema, op.getInterfaces()) && correct;
        }
        return correct;
    }

    protected boolean checkTypesInInputTypes(Schema schema, Collection<InputType> ops) {
        boolean correct = true;

        for (InputType op : ops) {
            correct = checkTypesInFields(schema, op.getFields().values()) && correct;
        }
        return correct;
    }

    protected boolean checkTypesInInterfaceTypes(Schema schema, Collection<InterfaceType> ops) {
        boolean correct = true;

        for (InterfaceType op : ops) {
            correct = checkTypesInFields(schema, op.getFields().values()) && correct;
            correct = checkInterfacesExist(schema, op.getInterfaces()) && correct;
        }
        return correct;
    }

    protected boolean checkTypesInFields(Schema schema, Collection<Field> ops) {
        boolean correct = true;

        for (Field f : ops) {
            correct = checkType(schema, f.getReference()) && correct;
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

    private boolean checkInterfacesExist(Schema schema, Set<Reference> interfaceReferences) {
        if (interfaceReferences != null) {
            for (Reference interfaceReference : interfaceReferences) {
                String referenceName = interfaceReference.getName();
                if (!schema.getInterfaces().containsKey(referenceName)) {
                    LOG.errorv("Missing interface: {0}", referenceName);
                    return false;
                }
                if (interfaceReference.getType() != ReferenceType.INTERFACE) {
                    LOG.errorv("InterfaceReference has bad type for interface: {0}", referenceName);
                    return false;
                }
            }
        }
        return true;
    }
}
