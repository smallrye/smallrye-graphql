package io.smallrye.graphql.test.apps.mapping.api;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;

/**
 * Map a email to and from json
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EmailAdapter implements JsonbAdapter<Email, JsonObject> {

    @Override
    public JsonObject adaptToJson(Email email) {
        return Json.createObjectBuilder()
                .add("value", email.getValue())
                .build();
    }

    @Override
    public Email adaptFromJson(JsonObject json) {
        return new Email(json.getString("value"));
    }
}