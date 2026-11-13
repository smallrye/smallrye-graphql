package io.smallrye.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.FloatValue;
import graphql.language.ObjectField;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.Value;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.execution.SchemaPrinter;

class NumericDefaultValueSchemaTest extends SchemaTestBase {

    @ParameterizedTest
    @CsvSource({ "doubleDefault,1.2", "floatDefault,0.3" })
    void scalarDefaultsKeepTheirFractionInThePrintedSchema(String field, double expected) {
        GraphQLSchema schema = createGraphQLSchema(DefaultValueApi.class, Dimensions.class);
        FloatValue value = assertInstanceOf(FloatValue.class, printedDefault(schema, field));
        assertEquals(expected, value.getValue().doubleValue());
    }

    @ParameterizedTest
    @CsvSource({ "doubleDefault,1.2", "floatDefault,0.3" })
    void scalarDefaultsKeepTheirFractionInIntrospection(String field, String expected) {
        GraphQLSchema schema = createGraphQLSchema(DefaultValueApi.class, Dimensions.class);
        assertEquals(expected, introspectionDefault(schema, field));
    }

    @Test
    void objectDefaultsKeepTheirFractionsInThePrintedSchema() {
        GraphQLSchema schema = createGraphQLSchema(DefaultValueApi.class, Dimensions.class);
        assertDimensions(printedDefault(schema, "objectDefault"));
    }

    @Test
    void objectDefaultsKeepTheirFractionsInIntrospection() {
        GraphQLSchema schema = createGraphQLSchema(DefaultValueApi.class, Dimensions.class);
        assertDimensions(Parser.parseValue(introspectionDefault(schema, "objectDefault")));
    }

    private Value<?> printedDefault(GraphQLSchema schema, String field) {
        ObjectTypeDefinition query = Parser.parse(new SchemaPrinter().print(schema))
                .getDefinitionsOfType(ObjectTypeDefinition.class).stream()
                .filter(type -> type.getName().equals("Query")).findFirst().orElseThrow();
        return query.getFieldDefinitions().stream().filter(definition -> definition.getName().equals(field))
                .findFirst().orElseThrow().getInputValueDefinitions().get(0).getDefaultValue();
    }

    @SuppressWarnings("unchecked")
    private String introspectionDefault(GraphQLSchema schema, String field) {
        ExecutionResult result = GraphQL.newGraphQL(schema).build()
                .execute("{ __type(name: \"Query\") { fields { name args { defaultValue } } } }");
        assertTrue(result.getErrors().isEmpty(), result.getErrors().toString());
        Map<String, Object> data = result.getData();
        Map<String, Object> query = (Map<String, Object>) data.get("__type");
        List<Map<String, Object>> fields = (List<Map<String, Object>>) query.get("fields");
        Map<String, Object> definition = fields.stream().filter(candidate -> candidate.get("name").equals(field))
                .findFirst().orElseThrow();
        List<Map<String, Object>> arguments = (List<Map<String, Object>>) definition.get("args");
        return (String) arguments.get(0).get("defaultValue");
    }

    private void assertDimensions(Value<?> value) {
        ObjectValue object = assertInstanceOf(ObjectValue.class, value);
        Map<String, Value> fields = object.getObjectFields().stream()
                .collect(Collectors.toMap(ObjectField::getName, ObjectField::getValue));
        assertEquals(2, fields.size());
        assertEquals(1.2d, assertInstanceOf(FloatValue.class, fields.get("height")).getValue().doubleValue());
        assertEquals(0.3d, assertInstanceOf(FloatValue.class, fields.get("weight")).getValue().doubleValue());
    }

    @GraphQLApi
    public static class DefaultValueApi {
        @Query
        public double doubleDefault(@Name("value") @DefaultValue("1.2") double value) {
            return value;
        }

        @Query
        public float floatDefault(@Name("value") @DefaultValue("0.3") float value) {
            return value;
        }

        @Query
        public double objectDefault(
                @Name("value") @DefaultValue("{\"height\":1.2,\"weight\":0.3}") Dimensions value) {
            return value.height;
        }
    }

    public static class Dimensions {
        public double height;
        public float weight;
    }
}
