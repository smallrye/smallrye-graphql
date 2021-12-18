package io.smallrye.graphql.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.impl.ResponseImpl;
import io.smallrye.graphql.client.impl.ResponseReader;

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

    private static final String EXAMPLE_RESPONSE_SCALARS = "{\n" +
            "  \"data\": {\n" +
            "    \"number\": 32,\n" +
            "    \"string\": \"hello\"\n" +
            "  }\n" +
            "}";

    private static final String EXAMPLE_RESPONSE_SCALARS_LIST = "{\n" +
            "  \"data\": {\n" +
            "    \"numbers\": [32, 33],\n" +
            "    \"strings\": [\"hello\", \"bye\"]\n" +
            "  }\n" +
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
    public void testGetObject() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_ONE_ITEM, null);
        Person person = response.getObject(Person.class, "people");
        assertEquals("jane", person.getName());
        assertEquals(Gender.FEMALE, person.getGender());
    }

    @Test
    public void testScalars() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_SCALARS, null);
        assertEquals("hello", response.getObject(String.class, "string"));
        assertEquals(32, response.getObject(Long.class, "number"));
    }

    @Test
    public void testScalarsList() {
        ResponseImpl response = ResponseReader.readFrom(EXAMPLE_RESPONSE_SCALARS_LIST, null);
        assertEquals("hello", response.getList(String.class, "strings").get(0));
        assertEquals("bye", response.getList(String.class, "strings").get(1));
        assertEquals(32, response.getList(Long.class, "numbers").get(0));
        assertEquals(33, response.getList(Long.class, "numbers").get(1));
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

}
