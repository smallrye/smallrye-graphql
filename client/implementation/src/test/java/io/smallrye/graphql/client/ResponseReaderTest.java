package io.smallrye.graphql.client;

import io.smallrye.graphql.client.impl.ResponseImpl;
import io.smallrye.graphql.client.impl.ResponseReader;
import jakarta.json.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseReaderTest {
    private static final String EXAMPLE_RESPONSE_ONE_ITEM = "{\n" +
            "  \"data\": {\n" +
            "    \"people\": \n" +
            "      {\n" +
            "        \"name\": \"jane\",\n" +
            "        \"gender\": \"FEMALE\"\n" +
            "      }\n" +
            "  }\n" +
            "}";

    private static final String EXAMPLE_RESPONSE_ONE_ITEM_WITH_JSON_OBJECT = "{\n" +
            "  \"data\": {\n" +
            "    \"giraffe\": \n" +
            "      {\n" +
            "        \"name\": \"frank\",\n" +
            "        \"height\": 25.2,\n" +
            "        \"meta\": {\"spots\": \"many\"}\n" +
            "      }\n" +
            "  }\n" +
            "}";

    private static final String EXAMPLE_RESPONSE_ONE_ITEM_WITH_JSON_ARRAY = "{\n" +
            "  \"data\": {\n" +
            "    \"giraffe\": \n" +
            "      {\n" +
            "        \"name\": \"frank\",\n" +
            "        \"height\": 25.2,\n" +
            "        \"meta\": [\n" +
            "          {\"base_colour\": \"yellow\"},\n" +
            "          {\"spots\": \"many\"}\n" +
            "        ]\n" +
            "      }\n" +
            "  }\n" +
            "}";

    private static final String EXAMPLE_RESPONSE_TWO_ITEMS = "{\n" +
            "  \"data\": {\n" +
            "    \"people\": [\n" +
            "      {\n" +
            "        \"name\": \"david\",\n" +
            "        \"gender\": \"MALE\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"jane\",\n" +
            "        \"gender\": \"FEMALE\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    private static final String EXAMPLE_RESPONSE_NULL_DATA = "{\n" +
            "  \"data\": {\n" +
            "    \"people\": null" +
            "  }\n" +
            "}";

    private static final String EXAMPLE_RESPONSE_SCALARS = "{\n" +
            "  \"data\": {\n" +
            "    \"number\": 32,\n" +
            "    \"string\": \"hello\",\n" +
            "    \"json\": {\"key\": \"value\"}\n" +
            "  }\n" +
            "}";

    private static final String EXAMPLE_RESPONSE_SCALARS_LIST = "{\n" +
            "  \"data\": {\n" +
            "    \"numbers\": [32, 33],\n" +
            "    \"strings\": [\"hello\", \"bye\"],\n" +
            "    \"jsonArray\": [{\"key1\": \"value1\"}, {\"key2\": \"value2\"}]\n" +
            "  }\n" +
            "}";
    private static final String EXAMPLE_RESPONSE_WITH_UNEXPECTED_FIELD = "{\n" +
            "  \"data\": {\n" +
            "    \"number\": 32\n" +
            "  },\n" +
            "  \"bugs\": {\n" +
            "  }\n" +
            "}";

    private static final String EXAMPLE_RESPONSE_NULL_EXTENSIONS = "{\n" +
            "  \"data\": {\n" +
            "    \"greeting\":\"hello\"\n" +
            "  },\n" +
            " \"extensions\": null\n" +
            "}";

    enum Gender {
        MALE,
        FEMALE
    }

    static class Person {

        private String name;
        private Gender gender;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Gender getGender() {
            return gender;
        }

        public void setGender(Gender gender) {
            this.gender = gender;
        }
    }

    @Test
    public void testGetObjectWithJsonObject() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_ONE_ITEM_WITH_JSON_OBJECT, null);
        JsonNode jacksonObject = JsonNodeFactory.instance.objectNode().put("spots", "many");
        JacksonGiraffe jacksonFrank = response.getObject(JacksonGiraffe.class, "giraffe");
        assertEquals("frank", jacksonFrank.getName());
        assertEquals(25.2, jacksonFrank.getHeight());
        assertEquals(jacksonObject, jacksonFrank.getMeta());

        JakartaGiraffe jakartaFrank = response.getObject(JakartaGiraffe.class, "giraffe");
        JsonValue jakartaObject = Json.createObjectBuilder().add("spots", "many").build();
        assertEquals("frank", jakartaFrank.getName());
        assertEquals(25.2, jakartaFrank.getHeight());
        assertEquals(jakartaObject, jakartaFrank.getMeta());
    }

    @Test
    public void testRawJsonObject() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_ONE_ITEM, null);
        JsonNode jacksonResponse = JsonNodeFactory.instance.objectNode()
                .put("name", "jane")
                .put("gender", "FEMALE");
        JsonNode jacksonRawResponse = response.getObject(JsonNode.class, "people");
        assertEquals(jacksonResponse, jacksonRawResponse);

        JsonObject jakartaResponse = Json.createObjectBuilder().add("name", "jane").add("gender", "FEMALE").build();
        JsonValue jakartaRawResponse = response.getObject(JsonValue.class, "people");
        assertEquals(jakartaResponse, jakartaRawResponse);
    }

    @Test
    public void testRawJsonArray() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_TWO_ITEMS, null);
        JsonNode jacksonResponse = JsonNodeFactory.instance.arrayNode()
                .add(JsonNodeFactory.instance.objectNode().put("name", "david").put("gender", "MALE"))
                .add(JsonNodeFactory.instance.objectNode().put("name", "jane").put("gender", "FEMALE"));
        JsonNode jacksonRawResponse = response.getObject(ArrayNode.class, "people");
        assertEquals(jacksonResponse, jacksonRawResponse);

        JsonValue jakartaRawResponse = response.getObject(JsonValue.class, "people");
        JsonArray jakartaResponse = Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("name", "david").add("gender", "MALE").build())
                .add(Json.createObjectBuilder().add("name", "jane").add("gender", "FEMALE").build())
                .build();
        assertEquals(jakartaResponse, jakartaRawResponse);
    }

    @Test
    public void testGetObjectWithJsonArray() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_ONE_ITEM_WITH_JSON_ARRAY, null);
        JsonNode jacksonArray = JsonNodeFactory.instance.arrayNode()
                .add(JsonNodeFactory.instance.objectNode().put("base_colour", "yellow"))
                .add(JsonNodeFactory.instance.objectNode().put("spots", "many"));
        JacksonGiraffe jacksonFrank = response.getObject(JacksonGiraffe.class, "giraffe");
        assertEquals("frank", jacksonFrank.getName());
        assertEquals(25.2, jacksonFrank.getHeight());
        assertEquals(jacksonArray, jacksonFrank.getMeta());

        JakartaGiraffe jakartaFrank = response.getObject(JakartaGiraffe.class, "giraffe");
        JsonValue jakartaObject = Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("base_colour", "yellow").build())
                .add(Json.createObjectBuilder().add("spots", "many").build())
                .build();
        assertEquals("frank", jakartaFrank.getName());
        assertEquals(25.2, jakartaFrank.getHeight());
        assertEquals(jakartaObject, jakartaFrank.getMeta());
    }

    @Test
    public void testScalars() {
        JsonObject object = Json.createObjectBuilder().add("key", "value").build();
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_SCALARS, null);
        assertEquals("hello", response.getObject(String.class, "string"));
        assertEquals(32, response.getObject(Long.class, "number"));
        assertEquals(object, response.getObject(JsonObject.class, "json"));
    }

    @Test
    public void testGetObject() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_ONE_ITEM, null);
        Person person = response.getObject(Person.class, "people");
        assertEquals("jane", person.getName());
        assertEquals(Gender.FEMALE, person.getGender());
    }

    @Test
    public void testScalarsList() {
        JsonArray jakartaArray = Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("key1", "value1").build())
                .add(Json.createObjectBuilder().add("key2", "value2").build())
                .build();
        ArrayNode jacksonArray = JsonNodeFactory.instance
                .arrayNode()
                .add(JsonNodeFactory.instance.objectNode().put("key1", "value1"))
                .add(JsonNodeFactory.instance.objectNode().put("key2", "value2"));

        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_SCALARS_LIST, null);
        assertEquals("hello", response.getList(String.class, "strings").get(0));
        assertEquals("bye", response.getList(String.class, "strings").get(1));
        assertEquals(32, response.getList(Long.class, "numbers").get(0));
        assertEquals(33, response.getList(Long.class, "numbers").get(1));
        assertEquals(jakartaArray, response.getObject(JsonArray.class, "jsonArray"));
        assertEquals(jacksonArray, response.getObject(ArrayNode.class, "jsonArray"));
    }

    static class JacksonGiraffe {
        String name;
        Double height;
        JsonNode meta;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getHeight() {
            return height;
        }

        public void setHeight(Double height) {
            this.height = height;
        }

        public JsonNode getMeta() {
            return meta;
        }

        public void setMeta(JsonNode meta) {
            this.meta = meta;
        }
    }

    static class JakartaGiraffe {
        String name;
        Double height;
        JsonValue meta;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getHeight() {
            return height;
        }

        public void setHeight(Double height) {
            this.height = height;
        }

        public JsonValue getMeta() {
            return meta;
        }

        public void setMeta(JsonValue meta) {
            this.meta = meta;
        }
    }

    static class UnsupportedFieldType {
        JsonString string;

        public JsonString getString() {
            return string;
        }

        public void setString(JsonString string) {
            this.string = string;
        }
    }

    @Test
    public void testGetListWhenResponseContainsObject() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_ONE_ITEM, null);
        try {
            response.getList(Person.class, "people");
            fail("Exception expected");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("SRGQLDC035006"));
        }
    }

    @Test
    public void testGetList() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_TWO_ITEMS, null);
        List<Person> list = response.getList(Person.class, "people");
        assertEquals(2, list.size());
    }

    @Test
    public void testGetObjectWhenResponseContainsList() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_TWO_ITEMS, null);
        try {
            response.getObject(Person.class, "people");
            fail("Exception expected");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("SRGQLDC035007"));
        }
    }

    @Test
    public void testGetObjectWhenDataIsNull() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_NULL_DATA, null);
        assertNull(response.getObject(Person.class, "people"));
    }

    @Test
    public void testGetListWhenDataIsNull() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_NULL_DATA, null);
        assertNull(response.getList(Person.class, "people"));
    }

    @Test
    public void testGetObjectWrongField() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_ONE_ITEM, null);
        try {
            response.getObject(Person.class, "WRONG_FIELD");
            fail("Expected an exception");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains("people"), "Exception should tell what fields are available in the response");
        }
    }

    @Test
    public void testGetListWrongField() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_TWO_ITEMS, null);
        try {
            response.getList(Person.class, "WRONG_FIELD");
            fail("Expected an exception");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains("people"), "Exception should tell what fields are available in the response");
        }
    }

    @Test
    public void verifyErrors() {
        String responseString = "{\"errors\":[{\"message\":\"blabla\"," +
                "\"path\": [1, 2, 3, \"asd\"]," +
                "\"locations\": [{\"line\":1,\"column\":30}]," +
                "\"somethingExtra\": 123456," +
                "\"extensions\": {" +
                "\"exception\":\"EXCEPTION_EXT\"," +
                "\"classification\":\"CLASSIFICATION_EXT\"," +
                "\"code\":\"CODE_EXT\"," +
                "\"description\":\"DESCRIPTION_EXT\"," +
                "\"validationErrorType\":\"VALIDATION_ERROR_TYPE_EXT\"," +
                "\"queryPath\":\"QUERYPATH_EXT\"" +
                "}}]}";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Cookie", Collections.singletonList("myCookie"));
        ResponseImpl response = ResponseReader.readFrom(responseString, headers);

        GraphQLError theError = response.getErrors().get(0);
        assertEquals("blabla", theError.getMessage());
        assertEquals(123456L, theError.getOtherFields().get("somethingExtra"));
        assertEquals("EXCEPTION_EXT", theError.getException());
        assertEquals("CLASSIFICATION_EXT", theError.getClassification());
        assertEquals("CODE_EXT", theError.getCode());
        assertEquals("DESCRIPTION_EXT", theError.getDescription());
        assertEquals("VALIDATION_ERROR_TYPE_EXT", theError.getValidationErrorType());
        assertEquals("QUERYPATH_EXT", theError.getQueryPath());
        assertEquals(1, theError.getLocations().get(0).get("line"));
        assertEquals(30, theError.getLocations().get(0).get("column"));
        assertArrayEquals(new Object[] { 1, 2, 3, "asd" }, theError.getPath());
        assertEquals(response.getHeaders().get("Cookie").get(0), "myCookie");
    }

    @Test
    public void nullPathInError() {
        String responseString = "{\"errors\":[{\"message\":\"blabla\"," +
                "\"path\": null}]}";
        ResponseImpl response = ResponseReader.readFrom(responseString, Collections.emptyMap());
        assertNull(response.getErrors().get(0).getPath());
    }

    @Test
    public void nullResponse() {
        try {
            ResponseImpl response = ResponseReader.readFrom(null, Collections.emptyMap());
            Assertions.fail();
        } catch (InvalidResponseException ire) {
            Assertions.assertTrue(ire.getMessage().contains("Unexpected response"));
        }
    }

    @Test
    public void unexpectedResponseFieldThrowsException() {
        Assertions.assertThrows(InvalidResponseException.class, () -> ResponseReader.readFrom(
                EXAMPLE_RESPONSE_WITH_UNEXPECTED_FIELD,
                Collections.emptyMap()));
    }

    @Test
    public void ignoringUnexpectedResponseField() {
        ResponseImpl response = ResponseReader.readFrom(
                EXAMPLE_RESPONSE_WITH_UNEXPECTED_FIELD,
                Collections.emptyMap(), null, null, true);
        assertEquals(32, response.getObject(Long.class, "number"));
    }

    @Test
    public void nullExtensions() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_NULL_EXTENSIONS, Collections.emptyMap());
        assertEquals("hello", response.getObject(String.class, "greeting"));
        assertEquals(null, response.getExtensions());
    }
}
