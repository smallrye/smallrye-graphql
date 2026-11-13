package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.test.numericmatrix.NumericMatrixApi;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NumericTypeMatrixTest {

    private static final List<Probe> PROBES = List.of(
            new Probe("primitiveByte", Kind.BYTE), new Probe("boxedByte", Kind.BYTE),
            new Probe("primitiveShort", Kind.SHORT), new Probe("boxedShort", Kind.SHORT),
            new Probe("primitiveInt", Kind.INT), new Probe("boxedInt", Kind.INT),
            new Probe("primitiveLong", Kind.LONG), new Probe("boxedLong", Kind.LONG),
            new Probe("primitiveFloat", Kind.FLOAT), new Probe("boxedFloat", Kind.FLOAT),
            new Probe("primitiveDouble", Kind.DOUBLE), new Probe("boxedDouble", Kind.DOUBLE),
            new Probe("atomicInteger", Kind.INT), new Probe("atomicLong", Kind.LONG),
            new Probe("optionalInt", Kind.INT), new Probe("optionalLong", Kind.LONG),
            new Probe("optionalDouble", Kind.DOUBLE),
            new Probe("optionalBoxedByte", Kind.BYTE), new Probe("optionalBoxedShort", Kind.SHORT),
            new Probe("optionalBoxedInteger", Kind.INT), new Probe("optionalBoxedLong", Kind.LONG),
            new Probe("optionalBoxedFloat", Kind.FLOAT), new Probe("optionalBoxedDouble", Kind.DOUBLE));

    private ExecutionService executionService;

    @BeforeAll
    void createSchemaOnce() {
        Schema schema = SchemaBuilder.build(Indexer.getTestIndex("io/smallrye/graphql/test/numericmatrix"));
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);
        executionService = new ExecutionService(graphQLSchema, schema);
    }

    @Test
    void numericOutputArraysRemainSerializable() {
        String selection = "{ matrixByteArray matrixShortArray matrixIntArray matrixLongArray "
                + "matrixFloatArray matrixBoxedFloatArray matrixDoubleArray }";
        JsonObject input = Json.createObjectBuilder().add("query", selection).build();
        JsonObjectResponseWriter writer = new JsonObjectResponseWriter(input);
        executionService.executeSync(input, writer);
        JsonObject result = writer.getOutput();
        assertNull(result.get("errors"), result::toString);
        JsonObject data = result.getJsonObject("data");
        assertEquals("-128", data.getJsonArray("matrixByteArray").get(0).toString());
        assertEquals("32767", data.getJsonArray("matrixShortArray").get(1).toString());
        assertEquals("-2147483648", data.getJsonArray("matrixIntArray").get(0).toString());
        assertEquals("9223372036854775807", data.getJsonArray("matrixLongArray").get(1).toString());
        // Compare the exact JSON decimal: casting it back to float would hide double widening noise in the output
        assertFloatJson(NumericMatrixApi.FLOAT_FRACTION, data.getJsonArray("matrixFloatArray").getJsonNumber(0));
        assertFloatJson(Float.MAX_VALUE, data.getJsonArray("matrixBoxedFloatArray").getJsonNumber(1));
        assertEquals(Double.MAX_VALUE, data.getJsonArray("matrixDoubleArray").getJsonNumber(1).doubleValue());
    }

    private static void assertFloatJson(float expected, JsonNumber actual) {
        assertEquals(0, new BigDecimal(Float.toString(expected)).compareTo(actual.bigDecimalValue()), actual::toString);
    }

    static Stream<Arguments> numericCases() {
        return Stream.of(Form.DIRECT, Form.POJO, Form.RECORD)
                .flatMap(form -> Stream.of(Transport.LITERAL, Transport.VARIABLE).flatMap(transport -> PROBES.stream().flatMap(
                        probe -> probe.kind.samples.stream().map(sample -> Arguments.of(form, transport, probe, sample)))));
    }

    @ParameterizedTest(name = "{0}, {1}, {2}, {3}")
    @MethodSource("numericCases")
    void numericInputsRespectTheirJavaType(Form form, Transport transport, Probe probe, Sample sample) {
        assertResult(execute(form, transport, probe, sample.value, false), sample.expected);
    }

    static Stream<Arguments> quotedLongCases() {
        return Stream.of(Form.DIRECT, Form.POJO, Form.RECORD)
                .flatMap(form -> PROBES.stream().filter(probe -> probe.kind == Kind.LONG)
                        .flatMap(probe -> probe.kind.samples.stream().map(sample -> Arguments.of(form, probe, sample))));
    }

    @ParameterizedTest(name = "{0}, quoted literal, {1}, {2}")
    @MethodSource("quotedLongCases")
    void quotedLongLiteralsRespectTheSameBounds(Form form, Probe probe, Sample sample) {
        assertResult(execute(form, Transport.LITERAL, probe, sample.value, true), sample.expected);
    }

    static Stream<Arguments> optionalCases() {
        return Stream.of(Form.DIRECT, Form.POJO, Form.RECORD).flatMap(form -> Stream.of(Transport.LITERAL, Transport.VARIABLE)
                .flatMap(transport -> PROBES.stream()
                        .filter(probe -> probe.field.startsWith("optional") || probe.field.startsWith("atomic")).flatMap(
                                probe -> Stream.of("missing", "null")
                                        .map(value -> Arguments.of(form, transport, probe, value)))));
    }

    @ParameterizedTest(name = "{0}, {1}, {2}, {3}")
    @MethodSource("optionalCases")
    void absentAndNullNumericWrappersRemainEmpty(Form form, Transport transport, Probe probe, String value) {
        assertResult(execute(form, transport, probe, value, false), "empty");
    }

    static Stream<Arguments> nestedCases() {
        return Stream.of(Form.NESTED_POJO, Form.NESTED_RECORD, Form.POJO_LIST, Form.RECORD_LIST)
                .flatMap(form -> Stream.of(Transport.LITERAL, Transport.VARIABLE)
                        .flatMap(transport -> PROBES.stream()
                                .filter(probe -> probe.field.equals("primitiveLong") || probe.field.equals("boxedLong"))
                                .flatMap(probe -> probe.kind.samples.stream()
                                        .map(sample -> Arguments.of(form, transport, probe, sample)))));
    }

    @ParameterizedTest(name = "{0}, {1}, {2}, {3}")
    @MethodSource("nestedCases")
    void nestedAndListInputsCannotBypassLongValidation(Form form, Transport transport, Probe probe, Sample sample) {
        assertResult(execute(form, transport, probe, sample.value, false), sample.expected);
    }

    private JsonObject execute(Form form, Transport transport, Probe probe, String value, boolean quoted) {
        boolean missing = value.equals("missing");
        boolean nullValue = value.equals("null");
        String queryName = form == Form.DIRECT ? probe.query() : form.query;
        String selection;
        JsonObjectBuilder request = Json.createObjectBuilder();
        if (transport == Transport.LITERAL) {
            String literal = quoted ? "\"" + value + "\"" : value;
            String input = form == Form.DIRECT ? literal : form.wrapLiteral(objectLiteral(probe, literal, missing));
            String arguments = form == Form.DIRECT && missing ? ""
                    : "(input: " + input + (form == Form.DIRECT ? "" : ", field: \"" + probe.field + "\"") + ")";
            selection = "{ probe: " + queryName + arguments + " }";
        } else {
            String inputType = form == Form.DIRECT ? probe.kind.scalar : form.inputType;
            if (form != Form.DIRECT || (!missing && !nullValue)) {
                inputType += "!";
            }
            selection = "query Probe($input: " + inputType + ") { probe: " + queryName + "(input: $input"
                    + (form == Form.DIRECT ? "" : ", field: \"" + probe.field + "\"") + ") }";
            JsonObjectBuilder variables = Json.createObjectBuilder();
            if (form != Form.DIRECT) {
                variables.add("input", form.wrapJson(objectVariable(probe, value, missing, nullValue)));
            } else if (nullValue) {
                variables.addNull("input");
            } else if (!missing) {
                variables.add("input", new BigDecimal(value));
            }
            request.add("variables", variables);
        }
        request.add("query", selection);
        JsonObject input = request.build();
        JsonObjectResponseWriter writer = new JsonObjectResponseWriter(input);
        executionService.executeSync(input, writer);
        return writer.getOutput();
    }

    private static String objectLiteral(Probe probe, String value, boolean missing) {
        StringBuilder input = new StringBuilder("{");
        for (String field : primitiveFields()) {
            input.append(field).append(": ").append(field.equals(probe.field) ? value : "1").append(", ");
        }
        if (!probe.field.startsWith("primitive") && !missing) {
            input.append(probe.field).append(": ").append(value);
        }
        return input.append("}").toString();
    }

    private static JsonObject objectVariable(Probe probe, String value, boolean missing, boolean nullValue) {
        JsonObjectBuilder input = Json.createObjectBuilder();
        for (String field : primitiveFields()) {
            input.add(field, new BigDecimal(field.equals(probe.field) ? value : "1"));
        }
        if (!probe.field.startsWith("primitive") && !missing) {
            if (nullValue) {
                input.addNull(probe.field);
            } else {
                input.add(probe.field, new BigDecimal(value));
            }
        }
        return input.build();
    }

    private static List<String> primitiveFields() {
        return List.of("primitiveByte", "primitiveShort", "primitiveInt", "primitiveLong", "primitiveFloat", "primitiveDouble");
    }

    private static void assertResult(JsonObject result, String expected) {
        if (expected == null) {
            assertNotNull(result.getJsonArray("errors"), result::toString);
            assertFalse(result.getJsonArray("errors").isEmpty(), result::toString);
            JsonObject data = result.get("data") instanceof JsonObject object ? object : null;
            assertTrue(data == null || !data.containsKey("probe") || data.isNull("probe"), result::toString);
        } else {
            assertNull(result.get("errors"), result::toString);
            assertNotNull(result.getJsonObject("data"), result::toString);
            assertEquals(expected, result.getJsonObject("data").getString("probe"), result::toString);
        }
    }

    private record Probe(String field, Kind kind) {
        String query() {
            return "matrix" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
        }

        @Override
        public String toString() {
            return field;
        }
    }

    private record Sample(String value, String expected) {
        @Override
        public String toString() {
            return value + (expected == null ? " rejected" : " -> " + expected);
        }
    }

    private enum Kind {
        BYTE("Int", List.of(new Sample("-128", "-128"), new Sample("127", "127"), new Sample("0", "0"),
                new Sample("-129", null), new Sample("128", null), new Sample("1.5", null))),
        SHORT("Int", List.of(new Sample("-32768", "-32768"), new Sample("32767", "32767"), new Sample("0", "0"),
                new Sample("-32769", null), new Sample("32768", null), new Sample("1.5", null))),
        INT("Int", List.of(new Sample("-2147483648", "-2147483648"), new Sample("2147483647", "2147483647"),
                new Sample("0", "0"), new Sample("-2147483649", null), new Sample("2147483648", null),
                new Sample("1.5", null))),
        LONG("BigInteger", List.of(new Sample("-9223372036854775808", "-9223372036854775808"),
                new Sample("9223372036854775807", "9223372036854775807"), new Sample("0", "0"),
                new Sample("-9223372036854775809", null), new Sample("9223372036854775808", null),
                new Sample("1.5", null))),
        FLOAT("Float", List.of(new Sample("-3.4028234663852886e38", "-3.4028235E38"),
                new Sample("3.4028234663852886e38", "3.4028235E38"),
                new Sample("1.401298464324817e-45", "1.4E-45"), new Sample("-1.401298464324817e-45", "-1.4E-45"),
                new Sample("1e-50", "0.0"), new Sample("0.1", "0.1"), new Sample("16777217", "1.6777216E7"),
                new Sample("1e39", null), new Sample("-1e39", null))),
        DOUBLE("Float", List.of(new Sample("-1.7976931348623157e308", "-1.7976931348623157E308"),
                new Sample("1.7976931348623157e308", "1.7976931348623157E308"),
                new Sample("4.9e-324", "4.9E-324"), new Sample("-4.9e-324", "-4.9E-324"),
                new Sample("1e-400", "0.0"), new Sample("0.1", "0.1"), new Sample("9007199254740993", "9.007199254740992E15"),
                new Sample("1e309", null), new Sample("-1e309", null)));

        private final String scalar;
        private final List<Sample> samples;

        Kind(String scalar, List<Sample> samples) {
            this.scalar = scalar;
            this.samples = samples;
        }
    }

    private enum Transport {
        LITERAL,
        VARIABLE
    }

    private enum Form {
        DIRECT(null, null),
        POJO("matrixPojo", "MatrixPojoInput"),
        RECORD("matrixRecord", "MatrixRecordInput"),
        NESTED_POJO("matrixNestedPojo", "MatrixPojoEnvelopeInput"),
        NESTED_RECORD("matrixNestedRecord", "MatrixRecordEnvelopeInput"),
        POJO_LIST("matrixPojoList", "[MatrixPojoInput!]"),
        RECORD_LIST("matrixRecordList", "[MatrixRecordInput!]");

        private final String query;
        private final String inputType;

        Form(String query, String inputType) {
            this.query = query;
            this.inputType = inputType;
        }

        String wrapLiteral(String input) {
            return switch (this) {
                case NESTED_POJO, NESTED_RECORD -> "{child: " + input + "}";
                case POJO_LIST, RECORD_LIST -> "[" + input + "]";
                default -> input;
            };
        }

        JsonValue wrapJson(JsonObject input) {
            return switch (this) {
                case NESTED_POJO, NESTED_RECORD -> Json.createObjectBuilder().add("child", input).build();
                case POJO_LIST, RECORD_LIST -> Json.createArrayBuilder().add(input).build();
                default -> input;
            };
        }
    }
}
